# Fuentes y trazabilidad de la evaluación

Fecha de consulta: 2026-10-16. Se usaron fuentes primarias, sin incorporar código/artefactos de prototipos descartados. Los links de documentación /current o /stable pueden evolucionar; se consultaron como documentación, no como selección automática de versión. No se agregaron paquetes al repositorio.

## Requisitos y código oficial del equipo

- P1: instrucción PO «Base de datos incluida en Gorilla Escape» en este chat: doce requisitos obligatorios, comparación A/B, ocho entregables y evaluación sin implementación.
- P2: respuesta PO «No está especificado» a si la rúbrica exige PostgreSQL. La rúbrica formal no fue proporcionada; aceptación académica sigue pendiente.
- B0: Backend develop `e76371097f4bcd881fa081211ec8d0d78e308c0b`; sólo README y dos documentos iniciales. [Repositorio](https://github.com/Josue1855/Gorilla-Escape-Backend). [PR #1 de flujo](https://github.com/Josue1855/Gorilla-Escape-Backend/pull/1) abierto, no se presume integrado ni se copia como runtime.
- U0: Unity/Java develop `7a750826c5565cbafdec983c5988c4cd5657d72a`. [JavaProbeSupervisor](https://github.com/Josue1855/Gorilla-Escape/blob/7a750826c5565cbafdec983c5988c4cd5657d72a/Unity/Assets/_Project/Scripts/Network/JavaProbeSupervisor.cs), [IpcLifecycle](https://github.com/Josue1855/Gorilla-Escape/blob/7a750826c5565cbafdec983c5988c4cd5657d72a/Unity/Assets/_Project/Scripts/Network/IpcLifecycle.cs), [GorillaEscapeApplication](https://github.com/Josue1855/Gorilla-Escape/blob/7a750826c5565cbafdec983c5988c4cd5657d72a/Server/src/main/java/com/gorillaescape/server/GorillaEscapeApplication.java), [ParentLifetime](https://github.com/Josue1855/Gorilla-Escape/blob/7a750826c5565cbafdec983c5988c4cd5657d72a/Server/src/main/java/com/gorillaescape/server/ipc/ParentLifetime.java), [ManagedProcessGuard](https://github.com/Josue1855/Gorilla-Escape/blob/7a750826c5565cbafdec983c5988c4cd5657d72a/Server/src/main/java/com/gorillaescape/server/ipc/ManagedProcessGuard.java).
- U1: [pom técnico](https://github.com/Josue1855/Gorilla-Escape/blob/7a750826c5565cbafdec983c5988c4cd5657d72a/Server/pom.xml): Java21/Boot4.1.1 sin JDBC/SQL. [package.py](https://github.com/Josue1855/Gorilla-Escape/blob/7a750826c5565cbafdec983c5988c4cd5657d72a/tools/t020/package.py): empaquetado Linux de referencia, JRE relativo y avisos; no prueba Windows/Backend. FirewallPreflight y helper UFW: límites de inspección protegida conservados.

## Documentación oficial consultada

| Tema | Fuente | Qué aporta / límite |
|---|---|---|
| Adecuación local | [SQLite whentouse](https://sqlite.org/whentouse.html) | Local/embebido y aplicación intermedia; no benchmark del juego |
| Lectores/escritores | [SQLite isolation](https://sqlite.org/isolation.html) | Concurrencia; no justifica un pool ilimitado |
| WAL y revisión crítica | [WAL](https://sqlite.org/wal.html) | Restricciones locales/durabilidad y bug WAL-reset; core corregido obligatorio si se usa |
| Constraints/configuración | [PRAGMA](https://sqlite.org/pragma.html) | Ajustes por conexión y comprobaciones; aún no aplicados |
| Backup | [Backup API](https://sqlite.org/backup.html) | Copia consistente; adaptación JDBC pendiente de spike |
| Crash/integridad | [Atomic commit](https://sqlite.org/atomiccommit.html), [howtocorrupt](https://sqlite.org/howtocorrupt.html) | Límites de almacenamiento/uso incorrecto; no promesa universal |
| Driver Java | [Xerial SQLite JDBC](https://github.com/xerial/sqlite-jdbc), [guía de uso](https://raw.githubusercontent.com/xerial/sqlite-jdbc/master/USAGE.md) | Nativos/ubicación de extracción, backup y cifrado no incluido por defecto; release exacta no seleccionada |
| Spring SQL | [Spring Boot](https://docs.spring.io/spring-boot/reference/data/sql.html) | DataSource/JDBC; SQLite requiere diseño explícito |
| ORM | [Hibernate dialects](https://docs.hibernate.org/stable/orm/dialect/) | SQLite comunitario, no core soportado; no elección ORM automática |
| Migrador | [Flyway SQLite](https://documentation.red-gate.com/flyway/reference/database-driver-reference/sqlite) | Compatibilidad y límites; herramienta/versionado/licencia pendientes |
| PG procesos/concurrencia | [Arquitectura](https://www.postgresql.org/docs/current/tutorial-arch.html), [MVCC](https://www.postgresql.org/docs/current/mvcc-intro.html) | Servidor y procesos por conexión; concurrencia del motor, no benchmark del producto |
| PG bootstrap | [initdb](https://www.postgresql.org/docs/current/app-initdb.html), [pg_ctl](https://www.postgresql.org/docs/current/app-pg-ctl.html) | Usuario propietario y lifecycle; no implementados |
| PG aislamiento local | [conexiones](https://www.postgresql.org/docs/current/runtime-config-connection.html), [HBA](https://www.postgresql.org/docs/current/auth-pg-hba-conf.html) | Listeners/auth; loopback no basta sin autenticación |
| PG salud/backup/upgrade | [pg_isready](https://www.postgresql.org/docs/current/app-pg-isready.html), [pg_basebackup](https://www.postgresql.org/docs/current/app-pgbasebackup.html), [upgrading](https://www.postgresql.org/docs/current/upgrading.html) | Check de conexión distinto de integridad; plan de ciclo de motor |
| Licencias | [SQLite](https://sqlite.org/copyright.html), [PostgreSQL](https://www.postgresql.org/about/licence/) | Revisar terceros del bundle; no certificación legal global |
| Empaquetado Java | [JDK21 jpackage](https://docs.oracle.com/en/java/javase/21/jpackage/packaging-overview.html) | Runtimes distribuidos; no sustituye instalador integral Unity |

## Estado de validación

Se revisaron fuentes y sintaxis/enlaces de documentos; no se ejecutó spike SQL, migración, benchmark, instalador ni prueba física. Tamaño, memoria, latencias y RTO reales: NO MEDIDO. Los criterios/diagramas son diseño. Configuración y issues de Project se verifican aparte mediante consulta posterior en [recibo de gestión](PROJECT_TRACKING.md).
