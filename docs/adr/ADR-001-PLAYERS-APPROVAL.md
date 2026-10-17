# ADR-001 — Aprobación acotada del primer incremento

Fecha: 2026-10-16, zona del PO America/Chihuahua. Fuente autorizada: el PO respondió en este chat «Sí, apruebo SQLite para este incremento» tras informarle que el PR #12 seguía abierto sin reviews.

**ACEPTADO para implementar únicamente jugadores con Spring Boot/JDBC/SQLite**, migraciones, GET/POST, validación, pruebas y preparación del laboratorio. La [evaluación original ADR-001](https://github.com/Josue1855/Gorilla-Escape-Backend/blob/24f9f7e94584ebb09c587275a2eff1b160713530/docs/adr/ADR-001-LOCAL-DATABASE.md) está documentada en PR #12; no se afirma que esté integrada ni revisada formalmente en GitHub. No se hizo merge de #12 para empezar; la rama funcional parte de develop `e76371097f4bcd881fa081211ec8d0d78e308c0b`.

Stack fijado para el incremento: Java 21, Spring Boot 4.1.1, SQLite JDBC 3.53.2.1 y Flyway 12.4.0, estos últimos gestionados por el BOM de Boot. Maven wrapper 3.9.11 con checksum; no JPA/Hibernate. Las versiones efectivas y SQLite nativo se verifican en build/log de readiness, no por el nombre de una carpeta.

Decisión operativa inicial: un pool de una conexión y transacciones cortas, `foreign_keys=ON`, journal DELETE y synchronous FULL; sin WAL ni proceso servidor SQL. El directorio persiste fuera del JAR y tiene lock de instancia. Cambiar journal/concurrencia requiere pruebas/decisión posteriores. El backend no sustituye ni modifica Unity/Java/WebRTC.

Fuera de alcance: partidas/resultados/rankings, idempotencia de resultados, publicador autoritativo, supervisión desde Unity, proxy HTTPS, instalador con JRE y aceptación física. Este incremento de desarrollo requiere JDK para build/arranque; el usuario final no deberá instalarlo por separado cuando se construya la distribución autocontenida.

Issues: [BDB-11](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/13), con trabajo parcial relacionado con #3/#4/#10. No cerrar esas issues completas por validar sólo jugadores. main no se modifica; PR funcional a develop para revisión, sin merge automático.
