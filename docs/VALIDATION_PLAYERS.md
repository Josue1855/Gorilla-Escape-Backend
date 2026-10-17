# Validación del primer incremento de jugadores

Fecha local: 2026-10-16, America/Chihuahua. Fechas de filas/logs UTC pueden ser 2026-10-17. Aprobación PO: [ADR acotado](adr/ADR-001-PLAYERS-APPROVAL.md). Base develop e76371097f4bcd881fa081211ec8d0d78e308c0b; rama feature/players-sqlite-increment. No se presume merge del PR #12.

## Resultados locales realmente ejecutados

- `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw -o -B -ntp verify`: PASS, 17 tests (4 unitarios +13 de integración HTTP/SQLite), cero fallos/errores/omitidos. Modo offline después de preparar la caché; no prueba primera descarga sin Internet. [Resumen Surefire real](evidence/players-2026-10-16/tests.json).
- SQLite JDBC3.53.2.1 gestionado por Boot4.1.1, core cargado3.53.2, Flyway12.4.0, schema1 y Java21.0.12.1; readiness real en [reporte](evidence/players-2026-10-16/lab-results.json).
- Harness del JAR real: SQL antes2 filas, POST201, SQL después3, GET200 con mismo UUID/valores; POST inválido400 sin escritura. Reinicio conserva tres filas y una sola migración. Integridad ok, FK sin violaciones; limpieza parametrizada afectó sólo las tres filas ficticias de ese run y dejó cero. Se ejecutó también query.sql, con resultados incluidos.
- [Logs HTTP reales](evidence/players-2026-10-16/http-telemetry.log): requestId/status/durationMs y heap JVM. Cuatro muestras, no benchmark general, RSS ni asignación por solicitud. [Hash del código/build/tests/scripts](evidence/players-2026-10-16/source-sha256.json); SHA del JAR en lab-results. No se publica la BD ni variables de entorno.
- Dos cierres del harness mediante SIGTERM, exit143 observado, sin kill forzado; reapertura y datos comprobados. No se presenta como exit0 ni certificación del cierre de Unity/árbol de procesos.

Las pruebas de integración cubren errores de nombres/JSON/coerción/campos extra/duplicados, body limitado incluso chunked, query inválida/paginación, SQL con apóstrofe, concurrencia de ocho POST, reinicio/migración única, rechazo de schema futuro sin borrar datos, FK/integridad, almacenamiento inaccesible503, constraint/rollback409, lock de segunda instancia, base inválida preservada, CORS permitido/rechazado, método405/Allow, media415, recurso404 y OpenAPI servido por HTTP. Los números de solicitudes son fixtures del backend, no número de teléfonos físicos.

Se detectaron y corrigieron durante desarrollo: escape de regexp al compilar; coerción JSON de número a nombre y clasificación de constraint SQLite. Las validaciones finales mantuvieron sus aserciones y pasaron; no se suprimieron pruebas para ocultar fallos.

## CI y límites

Se añadió CI Ubuntu/Windows con Java21: Maven verify y evidencias de tests; el harness se ejecuta además en Ubuntu. Hasta consultar los jobs del PR, no se declara PASS Windows ni CI. El backend no tenía pipeline antes de este incremento. No hay tests de UI/UX ni despliegue final ejecutados.

Pendiente: consumo React y capturas DevTools, LAN físico/WAN desconectada real, instalador con JRE, cierre por Unity, TLS/auth/fachada de negocio, backup/restore general, actualizaciones más allá de schema inicial y POST idempotente. Los AC amplios de #3/#4/#10 siguen parciales. La issue #13 delimita el incremento y el PR a develop queda para revisión, sin merge automático ni cambios a main.
