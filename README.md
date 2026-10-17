# Gorilla Escape — Backend: primer incremento de jugadores

Spring Boot 4.1.1, Java21, SQLite JDBC 3.53.2.1 y Flyway12.4.0 (versiones del BOM). Estado: incremento funcional en rama para revisión; no es distribución final autocontenida. La integración React/SQLite está probada en el fork de laboratorio y pendiente de revisión/incorporación al upstream UI/UX. [Aprobación SQLite del PO](docs/adr/ADR-001-PLAYERS-APPROVAL.md), acotada a jugadores; PR #12 de arquitectura permanece separado.

## Empezar en desarrollo

```bash
./mvnw -B -ntp verify
java -jar target/backend-0.1.0-SNAPSHOT.jar
```

Windows: usar `mvnw.cmd` para el build. Se requiere JDK21 para desarrollar; el empaquetado con JRE para usuario final es tarea futura. Primer build/wrapper/dependencias necesitan red, ejecución del JAR y migraciones no descargan componentes.

- GET http://127.0.0.1:8090/api/players — listado paginado.
- POST http://127.0.0.1:8090/api/players — application/json `{"displayName":"Jugador Uno"}`, responde201 después de persistir.
- GET http://127.0.0.1:8090/openapi.json — contrato OpenAPI3.1.

Datos en directorio por usuario fuera del JAR; override GORILLA_DATA_DIR o --gorilla.storage.directory=/ruta/propia. Ver [laboratorio](docs/LAB_PLAYERS.md) para rutas, SQL antes/después, logs/JVM, errores y reinicio. [Resultado real](docs/VALIDATION_PLAYERS.md). No se guardan payloads/nombres ni SQL con valores en logs de aplicación.

## Integración y límites

[Contrato para Hiram/React](docs/API_PLAYERS.md). Default loopback; LAN sólo opt-in para laboratorio aislado. No hay auth/TLS: no publicar esta API ni equiparar CORS con autorización. SQLite no expone puerto. Unity sigue autoridad de gameplay; no se alteró el Java técnico, WebRTC/IPC ni el repositorio UI/UX. No se implementaron partidas, resultados ni rankings. POST de jugadores no garantiza idempotencia de reintento.

Estructura modular: players/api → players/application → players/domain; infraestructura JDBC implementa el puerto del repositorio. persistence configura almacenamiento/migraciones; http controla contrato de errores, JSON, CORS y telemetría. Tests usan SQLite real en fichero y HTTP real, además de unidades aisladas.

## Flujo del equipo y enlaces

Ramas de tarea desde develop → PR a develop. main sólo recibe promoción de una entrega completa validada y aprobada; sin commits directos ni merge automático. [Project Backend](https://github.com/users/Josue1855/projects/2), [Unity](https://github.com/Josue1855/Gorilla-Escape), [PWA UI/UX](https://github.com/Quintana-117/Dise-o_pwa). Issues amplias #3/#4/#10 continúan parciales; [BDB-11](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/13) delimita esta entrega. No se presupone un merge del PR #1 ni #12.

## Laboratorio PWA de integración

[Dossier de entrega y evidencias reales](docs/laboratorio/README.md): React, POST/GET, SQLite, Service Worker e IndexedDB. Incluye límites y tres capturas literales de DevTools todavía pendientes. El frontend está en un fork de trabajo; no se modificó el upstream de Hiram.
