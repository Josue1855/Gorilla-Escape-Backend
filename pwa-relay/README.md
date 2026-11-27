# Backend local de la PWA del ejecutable

Extraído de `Unity/CameraTracker/camera_tracker.py` del [PR #135 de Gorilla Escape](https://github.com/Josue1855/Gorilla-Escape/pull/135). El servicio conserva las rutas HTTP de movimiento, salas y resultados del ejecutable. Los datos viven en memoria durante la sesión.

Desde la raíz de este repositorio, con Python 3:

```bash
python3 pwa-relay/server.py
```

Para servir también la PWA del ejecutable desde su checkout, usa el mismo origen para la página y las rutas:

```bash
python3 pwa-relay/server.py --pwa-dir /ruta/Gorilla-Escape/Unity/MotionRelay
```

El servicio escucha en `127.0.0.1:8788`. Usa `--port` para cambiar el puerto. El ejecutable de Unity levanta su propio servicio en ese puerto, por lo que deben ejecutarse por separado. Este servicio es local y no está preparado para exposición pública independiente.

| Ruta | Uso |
| --- | --- |
| `GET/POST /motion` | Lectura y recepción del control móvil |
| `GET/POST /party` | Consulta de sala y configuración local |
| `GET/POST /results` | Consulta por jugador y publicación local de resultados |
