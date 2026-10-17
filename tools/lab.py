#!/usr/bin/env python3
"""Developer lab: real JAR, HTTP, SQLite and restart in an exclusively owned directory."""
import argparse
from contextlib import closing
import hashlib
import json
from pathlib import Path
import queue
import re
import sqlite3
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
READY = re.compile(r"event=backend_ready port=(\d+) sqliteVersion=(\S+) schemaVersion=(\S+) javaVersion=(\S+)")

def snapshot(database):
    with closing(sqlite3.connect(database)) as connection:
        return {"rows": [dict(zip(("id", "displayName", "createdAt"), row)) for row in
                         connection.execute("SELECT id,display_name,created_at FROM players ORDER BY created_at,id")],
                "integrityCheck": connection.execute("PRAGMA integrity_check").fetchone()[0],
                "foreignKeyViolations": connection.execute("PRAGMA foreign_key_check").fetchall(),
                "appliedMigrations": connection.execute("SELECT version,success FROM flyway_schema_history").fetchall()}

def query_script(database):
    results = []
    with closing(sqlite3.connect(database)) as connection:
        for sql in (ROOT / "scripts/sql/query.sql").read_text().split(";"):
            if not sql.strip(): continue
            cursor = connection.execute(sql)
            results.append({"sql": sql.strip(), "columns": [column[0] for column in cursor.description], "rows": cursor.fetchall()})
    return results

def start(java, jar, directory, logfile):
    process = subprocess.Popen([java, "-jar", str(jar), "--server.port=0", "--server.address=127.0.0.1",
                                "--gorilla.storage.directory=" + str(directory)], stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL, text=True, encoding="utf-8", errors="replace")
    ready = queue.Queue()
    def drain():
        with logfile.open("w", encoding="utf-8") as output:
            for line in process.stdout:
                output.write(line)
                match = READY.search(line)
                if match:
                    ready.put({"port": int(match[1]), "sqliteVersion": match[2], "schemaVersion": match[3], "javaVersion": match[4]})
    worker = threading.Thread(target=drain, daemon=True)
    worker.start()
    try:
        info = ready.get(timeout=40)
        return process, worker, info
    except queue.Empty:
        process.terminate()
        try: process.wait(timeout=10)
        except subprocess.TimeoutExpired: process.kill(); process.wait(timeout=5)
        worker.join(timeout=3)
        raise RuntimeError("Backend did not become ready; inspect " + str(logfile))

def stop(process, worker):
    process.terminate()
    try: code = process.wait(timeout=10)
    except subprocess.TimeoutExpired:
        process.kill(); process.wait(timeout=5)
        raise RuntimeError("Backend required forced termination")
    worker.join(timeout=3)
    process.stdout.close()
    return {"method": "terminate (SIGTERM on POSIX; not a window-close certification)", "exitCode": code, "forced": False}

def request(port, method, payload=None):
    body = None if payload is None else json.dumps(payload, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request("http://127.0.0.1:" + str(port) + "/api/players", data=body,
                                 headers={"Content-Type": "application/json"}, method=method)
    begin = time.perf_counter()
    try: response = urllib.request.urlopen(req, timeout=10)
    except urllib.error.HTTPError as error: response = error
    with response:
        result = {"httpStatus": response.status, "body": json.loads(response.read()),
                  "requestId": response.headers.get("X-Request-Id"), "serverTiming": response.headers.get("Server-Timing"),
                  "clientElapsedMs": round((time.perf_counter() - begin) * 1000, 3)}
    return result

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, default=ROOT / "target/backend-0.1.0-SNAPSHOT.jar")
    parser.add_argument("--java", default="java")
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    jar = args.jar.resolve()
    if not jar.is_file(): parser.error("Build the JAR first")
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=False)  # Never reuse or clean someone else's database.
    directory = output / "data"
    database = directory / "players.sqlite"
    process = worker = None
    report = {"classification": "AUTOMATED BACKEND LAB; no PWA browser, physical LAN or production packaging",
              "jarSha256": hashlib.sha256(jar.read_bytes()).hexdigest(),
              "pythonSqliteVersion": sqlite3.sqlite_version, "startedAtEpochSeconds": time.time()}
    try:
        process, worker, report["runtime"] = start(args.java, jar, directory, output / "server-first.log")
        with closing(sqlite3.connect(database)) as connection:
            connection.executescript((ROOT / "scripts/sql/seed.sql").read_text())
        report["beforePost"] = snapshot(database)
        report["sqlBeforePost"] = query_script(database)
        assert len(report["beforePost"]["rows"]) == 2
        report["post"] = request(report["runtime"]["port"], "POST", {"displayName": "Jugador Laboratorio"})
        assert report["post"]["httpStatus"] == 201, report["post"]
        created = report["post"]["body"]
        report["afterPost"] = snapshot(database)
        report["sqlAfterPost"] = query_script(database)
        assert len(report["afterPost"]["rows"]) == 3
        assert created["id"] in [row["id"] for row in report["afterPost"]["rows"]]
        report["get"] = request(report["runtime"]["port"], "GET")
        assert report["get"]["httpStatus"] == 200 and created in report["get"]["body"]["items"]
        report["invalidPost"] = request(report["runtime"]["port"], "POST", {"displayName": " "})
        assert report["invalidPost"]["httpStatus"] == 400
        report["afterInvalidPost"] = snapshot(database)
        assert report["afterInvalidPost"]["rows"] == report["afterPost"]["rows"]
        report["firstTermination"] = stop(process, worker)
        process = worker = None
        process, worker, report["restartRuntime"] = start(args.java, jar, directory, output / "server-restart.log")
        report["getAfterRestart"] = request(report["restartRuntime"]["port"], "GET")
        assert created in report["getAfterRestart"]["body"]["items"]
        report["afterRestart"] = snapshot(database)
        assert report["afterRestart"]["rows"] == report["afterPost"]["rows"]
        assert len(report["afterRestart"]["appliedMigrations"]) == 1
        with closing(sqlite3.connect(database)) as connection:
            changed = connection.execute((ROOT / "scripts/sql/cleanup.sql").read_text(), {"created_player_id": created["id"]}).rowcount
            connection.commit()
        report["cleanup"] = {"affectedRows": changed, "snapshot": snapshot(database)}
        assert changed == 3 and report["cleanup"]["snapshot"]["rows"] == []
        report["secondTermination"] = stop(process, worker)
        process = worker = None
        all_logs = (output / "server-first.log").read_text() + (output / "server-restart.log").read_text()
        assert "Jugador Laboratorio" not in all_logs and "Persona Semilla" not in all_logs, "Payload leaked into logs"
        report["httpLogSamples"] = [line[line.index("event=http_request"):] for line in all_logs.splitlines() if "event=http_request" in line]
        assert report["httpLogSamples"] and all("jvmHeapUsedBytes=" in line and "durationMs=" in line for line in report["httpLogSamples"])
        report["status"] = "PASS"
        report["limitations"] = ["Heap samples are JVM heap, not RSS or per-request allocations", "Server-Timing excludes network transfer", "No idempotency/retry guarantee for POST", "No browser screenshots or frontend integration executed", "No physical phone, installer or clean-PC acceptance"]
    except BaseException:
        report["status"] = "FAIL"
        raise
    finally:
        if process is not None:
            report["cleanupOnFailure"] = stop(process, worker)
        report["finishedAtEpochSeconds"] = time.time()
        (output / "results.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n")
    print(json.dumps({"status": report["status"], "evidence": str(output / "results.json"), "sqlBefore": 2, "sqlAfter": 3, "httpPost": 201, "httpGet": 200, "httpInvalid": 400}))

if __name__ == "__main__": main()
