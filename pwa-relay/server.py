import argparse
import math
import json
import os
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

pwa_directory = None

data_lock = threading.Lock()
rooms = {}
parties = {}
player_results = {}
last_players = {}
preview_frame = None


class PoseHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        request = urlparse(self.path)
        if request.path == "/results":
            query = parse_qs(request.query)
            key = (query.get("room", [""])[0].upper(), query.get("player", [""])[0])
            with data_lock:
                payload = json.dumps(player_results.get(key, [])).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)
        elif request.path == "/party":
            room = parse_qs(request.query).get("room", [""])[0].upper()
            with data_lock:
                party = parties.get(room, {"players": [], "active": "", "locked": False})
                payload = json.dumps(party).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(payload)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(payload)
        elif request.path == "/motion":
            room = parse_qs(request.query).get("room", [""])[0].upper()
            with data_lock:
                sample = rooms.get(room)
            if sample is None:
                self.send_response(204)
                self.end_headers()
                return
            _, payload = sample
            encoded = payload.encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(encoded)))
            self.end_headers()
            self.wfile.write(encoded)
        elif pwa_directory is None:
            self.send_error(404)
        elif request.path in ("/fonts/LilitaOne.ttf", "/fonts/NunitoSans.ttf"):
            font_path = os.path.join(pwa_directory, "Fonts", os.path.basename(request.path))
            try:
                with open(font_path, "rb") as font:
                    payload = font.read()
            except FileNotFoundError:
                self.send_error(404)
                return
            self.send_response(200)
            self.send_header("Content-Type", "font/ttf")
            self.send_header("Cache-Control", "public, max-age=86400")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)
        elif request.path in ("/art/jungle.png", "/art/leaves.png"):
            art_path = os.path.join(pwa_directory, "Art", os.path.basename(request.path))
            try:
                with open(art_path, "rb") as image:
                    payload = image.read()
            except FileNotFoundError:
                self.send_error(404)
                return
            self.send_response(200)
            self.send_header("Content-Type", "image/png")
            self.send_header("Cache-Control", "public, max-age=86400")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)
        elif request.path in ("/profile.js", "/sw.js", "/manifest.webmanifest", "/icon-192.png", "/icon-512.png"):
            types = {".js": "application/javascript; charset=utf-8", ".webmanifest": "application/manifest+json", ".png": "image/png"}
            resource = os.path.join(pwa_directory, request.path[1:])
            with open(resource, "rb") as asset:
                payload = asset.read()
            self.send_response(200)
            self.send_header("Content-Type", types[os.path.splitext(resource)[1]])
            self.send_header("Cache-Control", "no-cache")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)
        elif request.path == "/":
            page_path = os.path.join(pwa_directory, "index.html")
            with open(page_path, "rb") as page:
                payload = page.read()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        request = urlparse(self.path)
        if request.path == "/results":
            if self.headers.get("Cf-Ray") or self.client_address[0] != "127.0.0.1":
                self.send_error(403)
                return
            try:
                length = int(self.headers.get("Content-Length", "0"))
                if not 0 < length <= 1024:
                    raise ValueError("size")
                result = json.loads(self.rfile.read(length))
                room = parse_qs(request.query).get("room", [""])[0].upper()
                games = {"BananaBowling", "GorillaBoxing", "JungleBaseball", "JungleDrums", "CoconutSlice",
                         "WhackAMole", "CoconutThrow", "LogBalance", "JungleKeeper"}
                if (not isinstance(result.get("id"), str) or not 1 <= len(result["id"]) <= 64
                        or result.get("game") not in games or type(result.get("score")) not in (float, int)
                        or not math.isfinite(result["score"])):
                    raise ValueError("result")
                with data_lock:
                    player = result.get("player") or last_players.get(room)
                    if not isinstance(player, str) or not 1 <= len(player) <= 64:
                        raise ValueError("player")
                    key = (room, player)
                    if key not in player_results and len(player_results) >= 300:
                        player_results.pop(next(iter(player_results)))
                    history = player_results.setdefault(key, [])
                    if not any(item["id"] == result["id"] for item in history):
                        history.append({"id": result["id"], "game": result["game"], "score": result["score"],
                                        "time": int(time.time() * 1000)})
                        del history[:-50]
            except (ValueError, TypeError, KeyError, AttributeError):
                self.send_error(400, "Invalid result")
                return
            self.send_response(200)
            self.send_header("Content-Length", "0")
            self.end_headers()
            return
        if request.path == "/party":
            if self.headers.get("Cf-Ray") or self.client_address[0] != "127.0.0.1":
                self.send_error(403)
                return
            room = parse_qs(request.query).get("room", [""])[0].upper()
            command = json.loads(self.rfile.read(int(self.headers.get("Content-Length", "0"))))
            with data_lock:
                party = parties.setdefault(room, {"players": [], "active": "", "locked": False})
                if command.get("reset"):
                    party = parties[room] = {"players": [], "active": "", "locked": False}
                party["locked"] = bool(command.get("locked", False))
                party["active"] = command.get("active", "")
                rooms.pop(room, None)
                if command.get("disable"):
                    parties.pop(room, None)
            self.send_response(200)
            self.end_headers()
            return
        if request.path != "/motion":
            self.send_response(404)
            self.end_headers()
            return
        room = parse_qs(request.query).get("room", [""])[0].upper()
        length = int(self.headers.get("Content-Length", "0"))
        payload = self.rfile.read(length).decode("utf-8")
        try:
            packet = json.loads(payload)
            timestamp = packet["time"]
            if type(timestamp) is not int or timestamp <= 0:
                raise ValueError("Invalid motion timestamp")
        except (ValueError, TypeError, KeyError):
            self.send_error(400, "Invalid motion packet")
            return
        raw_name = packet.get("playerName", "")
        name = " ".join("".join(c for c in raw_name if c.isprintable() and c not in "<>").split())[:20] if isinstance(raw_name, str) else ""
        with data_lock:
            party = parties.get(room)
            player = parse_qs(request.query).get("player", [""])[0]
            if player:
                last_players[room] = player
            if party is not None:
                members = party["players"]
                member = next((item for item in members if item["id"] == player), None)
                if player and member is None and not party["locked"] and len(members) < 4:
                    member = {"id": player, "name": name or "Jugador " + str(len(members) + 1)}
                    members.append(member)
                if member is None:
                    self.send_error(409, "Sala completa o partida iniciada")
                    return
                if name:
                    member["name"] = name
                member["seen"] = int(time.time() * 1000)
                if not party["active"]:
                    party["active"] = player
                if player != party["active"]:
                    self.send_response(200)
                    self.end_headers()
                    self.wfile.write(b"wait")
                    return
            previous = rooms.get(room)
            if previous is None or timestamp > previous[0]:
                rooms[room] = (timestamp, payload)
        self.send_response(200)
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(b"ok")

    def log_message(self, *_):
        return


def main():
    global pwa_directory
    parser = argparse.ArgumentParser(description="Run the local Gorilla Escape PWA relay.")
    parser.add_argument("--port", type=int, default=8788)
    parser.add_argument("--pwa-dir", help="Path to Unity/MotionRelay in the game checkout")
    args = parser.parse_args()
    if args.pwa_dir:
        pwa_directory = os.path.abspath(args.pwa_dir)
        if not os.path.isfile(os.path.join(pwa_directory, "index.html")):
            parser.error("--pwa-dir must contain index.html")
    with ThreadingHTTPServer(("127.0.0.1", args.port), PoseHandler) as server:
        server.serve_forever()


if __name__ == "__main__":
    main()
