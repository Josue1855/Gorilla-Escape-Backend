#!/usr/bin/env python3
"""Non-destructive, loopback-only HTTP/SQLite performance smoke for the packaged JAR."""
import argparse
import json
import math
import os
from pathlib import Path
import socket
import statistics
import subprocess
import tempfile
import time
from urllib.error import URLError
from urllib.request import urlopen


def free_port():
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0))
        return sock.getsockname()[1]


def request(url):
    start = time.perf_counter()
    with urlopen(url, timeout=5) as response:
        data = json.load(response)
        if response.status != 200 or not isinstance(data.get("items"), list):
            raise RuntimeError("Unexpected /api/players response")
    return round((time.perf_counter() - start) * 1000, 3)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", default="target/backend-0.1.0-SNAPSHOT.jar")
    parser.add_argument("--output", default="performance-results.json")
    parser.add_argument("--samples", type=int, default=40)
    args = parser.parse_args()
    if not (10 <= args.samples <= 1000):
        parser.error("--samples must be between 10 and 1000")
    jar = Path(args.jar).resolve()
    if not jar.is_file():
        parser.error("JAR missing; run ./mvnw verify first")

    with tempfile.TemporaryDirectory(prefix="gorilla-perf-") as temp:
        port = free_port()
        url = f"http://127.0.0.1:{port}/api/players?limit=10"
        command = [
            "java", "-jar", str(jar),
            "--server.address=127.0.0.1",
            f"--server.port={port}",
            f"--gorilla.storage.directory={temp}/data",
        ]
        with open(Path(temp) / "server.log", "w", encoding="utf-8") as logs:
            server = subprocess.Popen(command, stdout=logs, stderr=subprocess.STDOUT)
            try:
                deadline = time.monotonic() + 60
                while True:
                    if server.poll() is not None:
                        raise RuntimeError(f"Backend exited during startup ({server.returncode})")
                    try:
                        request(url)
                        break
                    except (URLError, TimeoutError, ConnectionError):
                        if time.monotonic() > deadline:
                            raise RuntimeError("Backend did not become ready within 60 seconds")
                        time.sleep(0.5)
                for _ in range(5):
                    request(url)
                samples = sorted(request(url) for _ in range(args.samples))
            finally:
                server.terminate()
                try:
                    server.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    server.kill()
                    server.wait(timeout=5)

    result = {
        "test": "GET /api/players over local HTTP with SQLite",
        "samples": len(samples),
        "p50_ms": round(statistics.median(samples), 3),
        "p95_ms": samples[math.ceil(len(samples) * 0.95) - 1],
        "max_ms": samples[-1],
        "minimum_ms": samples[0],
        "scope": "CI runner loopback, warm server; not LAN/WebSocket/mobile latency",
        "gate": "informational; no unapproved performance SLA",
    }
    path = Path(args.output)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    summary = os.getenv("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as out:
            out.write(
                f"### Backend HTTP performance smoke\n"
                f"P50: {result['p50_ms']} ms; P95: {result['p95_ms']} ms; "
                f"max: {result['max_ms']} ms ({result['samples']} requests).\n\n"
                "Loopback/CI only; results are informational, not a game-network SLA.\n"
            )
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
