#!/usr/bin/env python3
"""Desktop-browser integration: React Lab -> Java HTTP -> real SQLite.

Does not exercise Unity, real iOS/Android sensors, LAN, TLS or WebRTC.
Only synthetic test data in a temporary directory.
"""
import argparse
from contextlib import closing
import json
import os
from pathlib import Path
import socket
import sqlite3
import subprocess
import tempfile
import time
from urllib.error import URLError
from urllib.request import urlopen

from playwright.sync_api import sync_playwright


def check_json(url):
    with urlopen(url, timeout=5) as response:
        assert response.status == 200
        return json.load(response)


def stop(proc):
    if proc.poll() is None:
        proc.terminate()
        try:
            proc.wait(timeout=12)
        except subprocess.TimeoutExpired:
            proc.kill()
            proc.wait(timeout=5)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--frontend", type=Path, required=True)
    parser.add_argument("--output", type=Path, default=Path("e2e-results"))
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    frontend = args.frontend.resolve()
    out = args.output.resolve()
    out.mkdir(parents=True, exist_ok=True)
    jar = root / "target/backend-0.1.0-SNAPSHOT.jar"
    assert jar.is_file(), "Maven JAR missing"
    assert (frontend / "dist/lab.html").is_file(), "Frontend Lab build missing"
    report = {
        "scope": "Chromium headless -> React lab -> HTTP Java backend -> real SQLite",
        "limitations": "Not Unity, not hardware phone, not WebRTC/HTTPS/LAN",
        "passed": False,
    }
    with tempfile.TemporaryDirectory(prefix="gorilla-browser-e2e-") as d:
        data = Path(d) / "data"
        with (out / "backend.log").open("w", encoding="utf-8") as backend_log, (out / "pwa.log").open("w", encoding="utf-8") as pwa_log:
            backend = subprocess.Popen(
                ["java", "-jar", str(jar), "--server.address=127.0.0.1", "--server.port=8090",
                 f"--gorilla.storage.directory={data}"],
                cwd=root, stdout=backend_log, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL,
            )
            ui = None
            try:
                deadline = time.monotonic() + 65
                while True:
                    if backend.poll() is not None:
                        raise RuntimeError("Backend terminated; inspect backend.log")
                    try:
                        assert isinstance(check_json("http://127.0.0.1:8090/api/players")["items"], list)
                        break
                    except (URLError, TimeoutError, ConnectionError):
                        if time.monotonic() >= deadline:
                            raise RuntimeError("Backend readiness timeout")
                        time.sleep(0.5)

                ui = subprocess.Popen(
                    ["node", "tools/serve-lab.mjs"], cwd=frontend, stdout=pwa_log,
                    stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL,
                    env={**os.environ, "LAB_PORT": "4173"},
                )
                deadline = time.monotonic() + 25
                while True:
                    if ui.poll() is not None:
                        raise RuntimeError("PWA server terminated; inspect pwa.log")
                    try:
                        with urlopen("http://127.0.0.1:4173/lab.html", timeout=4) as response:
                            assert response.status == 200
                        break
                    except (URLError, TimeoutError, ConnectionError):
                        if time.monotonic() >= deadline:
                            raise RuntimeError("PWA readiness timeout")
                        time.sleep(0.25)

                with sync_playwright() as playwright:
                    browser = playwright.chromium.launch(headless=True)
                    context = browser.new_context(service_workers="allow")
                    page = context.new_page()
                    page.goto("http://127.0.0.1:4173/lab.html", wait_until="domcontentloaded")
                    page.get_by_role("status").filter(has_text="Consulta confirmada: HTTP 200").wait_for(timeout=20000)
                    page.locator("#display-name").fill("Jugador E2E CI")
                    page.get_by_role("button", name="Registrar jugador").click()
                    page.get_by_role("status").filter(has_text="Registro confirmado: HTTP 201").wait_for(timeout=20000)
                    page.get_by_role("button", name="Actualizar listado").click()
                    page.get_by_role("status").filter(has_text="Consulta confirmada: HTTP 200").wait_for(timeout=20000)
                    page.locator("ul.players li").filter(has_text="Jugador E2E CI").wait_for(timeout=10000)
                    report["browser_title"] = page.title()
                    context.close()
                    browser.close()

                players = check_json("http://127.0.0.1:8090/api/players")["items"]
                created = [player for player in players if player["displayName"] == "Jugador E2E CI"]
                assert len(created) == 1, "HTTP GET did not expose exactly one inserted player"
                database = data / "players.sqlite"
                assert database.is_file(), "SQLite database missing"
                with closing(sqlite3.connect(database)) as db:
                    found = db.execute(
                        "SELECT id, display_name FROM players WHERE id=?",
                        (created[0]["id"],),
                    ).fetchone()
                    integrity = db.execute("PRAGMA integrity_check").fetchone()[0]
                assert found == (created[0]["id"], "Jugador E2E CI")
                assert integrity == "ok"
                report.update({
                    "passed": True,
                    "api_post_status": 201,
                    "api_get_status": 200,
                    "sqlite_integrity": integrity,
                    "same_player_in_browser_api_and_db": True,
                    "backend_commit": os.getenv("GITHUB_SHA", "local"),
                })
            finally:
                if ui is not None:
                    stop(ui)
                stop(backend)
    (out / "result.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    with open(os.getenv("GITHUB_STEP_SUMMARY", os.devnull), "a", encoding="utf-8") as summary:
        summary.write("### PWA / Backend / SQLite browser integration\n")
        summary.write("Chromium -> real React form -> HTTP 201/GET 200 -> real SQLite ID/integrity verified.\n")
        summary.write("Unity / physical devices / LAN / WebRTC are NOT tested here.\n")


if __name__ == "__main__":
    main()
