# Laboratorio reproducible de jugadores

Este procedimiento es para desarrollo/QA, no instalación del jugador final. Requiere JDK21 y Python3 para el harness; Python/CLI SQL no son dependencias del JAR. Descargar Maven/dependencias requiere red la primera preparación. El JAR inicia y migra localmente sin Maven/descargas; el instalador con JRE sigue pendiente.

## Compilar y probar

```bash
./mvnw -B -ntp verify
python3 tools/lab.py --java /ruta/al/java21 --output lab-output/ejecucion-nueva
```

Windows: `mvnw.cmd -B -ntp verify` y `python tools/lab.py --java C:\ruta\al\java.exe --output lab-output/ejecucion-nueva`. El directorio de salida debe ser nuevo; nunca se reutiliza ni limpia una BD ajena. El harness arranca dos procesos sucesivos del JAR con puerto aleatorio/loopback y datos propios, ejecuta SQL+HTTP y conserva results.json/logs. Ver [resultado real registrado](VALIDATION_PLAYERS.md).

## Arranque manual y almacenamiento

```bash
java -jar target/backend-0.1.0-SNAPSHOT.jar --gorilla.storage.directory=/ruta/absoluta/datos-laboratorio
```

GET http://127.0.0.1:8090/api/players. Configuración opcional: GORILLA_DATA_DIR o propiedad gorilla.storage.directory; puerto con --server.port. Sin override: Windows LOCALAPPDATA/GorillaEscape/backend; Linux XDG_DATA_HOME/gorilla-escape/backend o ~/.local/share/gorilla-escape/backend. El fichero se llama players.sqlite. No se almacena en el JAR ni se borra al cerrar/reiniciar; no usar carpeta de runtime como datadir.

Flyway aplica [V1](../src/main/resources/db/migration/V1__create_players.sql) y valida checksums al iniciar; sin auto-DDL ni migración destructiva/clean. Backend abre el mismo fichero en la siguiente ejecución. Un segundo dueño del directorio o una base corrupta falla explícitamente sin reset silencioso. Lock no se elimina al cerrar; se libera el recurso. Conservar archivos para investigar.

## Evidencias 1–10

| Requisito | Procedimiento y registro generado |
|---|---|
| BD antes de POST | Migrar app, cargar [seed.sql](../scripts/sql/seed.sql); snapshot beforePost con 2 filas ficticias |
| POST válido | Payload Jugador Laboratorio, HTTP201, body UUID/fecha y requestId reales en results.json |
| Persistencia real | SELECT independiente con SQLite de Python sobre archivo de la API; no mocks ni memoria |
| BD después | afterPost contiene 3 filas, UUID del HTTP presente; integridad ok |
| GET | GET HTTP200 con items/limit/offset y mismo objeto retornado por POST |
| Datos recuperados | Comparación de IDs/campos en GET, SQL y después de reiniciar proceso |
| Tiempo backend | Server-Timing y logs durationMs; no confundir con clientElapsedMs/red |
| Memoria JVM | jvmHeapUsedBytes desde MemoryMXBean en logs de cada solicitud; no RSS, pico ni bytes asignados por request |
| SQL frente a HTTP | Conteos/filas/integridad separados de httpStatus; sql_operation registra affectedRows sin payload |
| Validación fallida | POST nombre vacío HTTP400; conteos idénticos antes/después, error sin datos sensibles |

Reinicio verifica mismo UUID, migración única e integridad. Limpieza con [cleanup.sql](../scripts/sql/cleanup.sql) sólo elimina UUIDs semilla y `:created_player_id` de esa ejecución; el harness liga el parámetro desde el POST confirmado. No ejecutar DELETE global. [query.sql](../scripts/sql/query.sql) permite inspección manual del esquema/historial/datos. Un CLI SQLite es herramienta opcional del desarrollador, no del usuario final.

Para consulta mínima sin CLI SQL, desde Python3 (cambiar sólo la ruta de laboratorio):

```python
import sqlite3
from contextlib import closing
with closing(sqlite3.connect('/ruta/absoluta/datos-laboratorio/players.sqlite')) as db:
    print(db.execute('SELECT id, display_name, created_at FROM players').fetchall())
```

## Límites y siguientes evidencias

El harness usa datos ficticios, no captura pantallas ni toca UI/UX. No prueba WAN desconectada físicamente, teléfono/PC limpia, instalador, backup/restore general, auto-supervisión Unity ni POST idempotente. Su terminación usa terminate/SIGTERM en POSIX; un exit143 observado no se etiqueta exit0 ni cierre de ventana certificado. La recuperación del proceso se valida aquí por reinicio del JAR y cierre de contextos en tests; la contención de árbol del producto sigue pendiente.

El [laboratorio PWA posterior](laboratorio/README.md) ya demostró GET renderizado en React, POST desde UI, SW/Cache Storage/IndexedDB, recarga offline, reconexión con envío manual y API inaccesible simulada. El harness descrito arriba sigue siendo sólo backend: sus resultados no se mezclan con los del navegador. Faltan las tres capturas literales Network/Application/Console y los escenarios físicos WAN caída con LAN activa/backend inaccesible. No fabricar capturas a partir del JSON ni presentar heap como memoria total. Nunca publicar nombres reales, headers de autorización/cookies ni bases de datos reales.
