# Comparación técnica obligatoria: PostgreSQL privado frente a SQLite

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

## Evaluación por criterio

| Criterio | A. PostgreSQL empaquetado como proceso privado | B. SQLite integrado en Spring Boot | Implicación para Gorilla Escape |
|---|---|---|---|
| Distribución final | Backend/JRE comunes + binarios PG/lib/share/herramientas + inicialización de cluster | Backend/JRE comunes + driver/JNI; no servidor de BD | B reduce piezas y evita distribuir dos motores; bytes finales NO MEDIDOS |
| Instalación | Debe automatizar binarios compatibles, initdb, puerto/local auth y actualización | Crear directorio privado y abrir fichero por JDBC | Ninguna opción requiere que el jugador configure un gestor; B es más simple |
| Procesos | Postmaster y procesos auxiliares, además de API; no es un único daemon trivial | BD vive dentro de la API; cero proceso BD | B reduce dependencias de orden y residuos; API aún requiere supervisión |
| Migraciones de esquema | SQL específico y migrador, roles, control exclusivo | SQL específico/posible reconstrucción de tablas; migración exclusiva, no concurrencia de migradores Flyway | Ambos requieren versión/checksum/backup/rollback del despliegue |
| Integridad | Transacciones, constraints y control de concurrencia de servidor | Transacciones y FK habilitadas/verificadas por conexión; idempotencia en BD | No extrapolar mocks ni auto-DDL como migración |
| Concurrencia | Mayor capacidad de escritores concurrentes y transacciones complejas | Un escritor; lecturas concurrentes según modo; serializar escrituras cortas | Cuatro teléfonos no equivalen a cuatro conexiones directas; se guardan resultados de baja frecuencia |
| Persistencia/recuperación | Cluster+WAL/backup; control de versión mayor del motor | Fichero/journal/WAL, backup consistente y restauración validada | Ninguno garantiza recuperar un disco averiado sin respaldo externo |
| Windows/Linux | Binarios y bibliotecas verificados por sistema/arquitectura; locales/ABI/imports | Driver con JNI por plataforma; temp/noexec/antivirus/ABI deben probarse | Soporte futuro es PLANIFICADO, no compatibilidad universal verificada |
| Seguridad | Listener loopback/socket, HBA restrictivo, roles/secretos locales | Sin listener BD; ACL del directorio y autenticación API | SQLite elimina exposición SQL LAN, no necesidad de proteger API/datos |
| Pruebas | initdb, colisión de puerto, auth, procesos huérfanos, crash y upgrades mayores | Locks/SQLITE_BUSY, crash, FK, migración y carga JNI | Ambos: PC limpia offline, reinicio, actualizaciones y LAN simultánea |
| Rúbrica | Adecuado si exige PostgreSQL explícito; empaquetarlo sigue siendo obligación | Permite SQL real, GET/POST persistidos, scripts y logs | PO: motor no especificado; validación docente aún necesaria |
| Mantenimiento | Parches de servidor/deps, datos por major y plan pg_upgrade/dump | Actualizar driver/motor, schema y prueba de reapertura | B prioriza mantenimiento local; reevaluar si cambia el modelo de despliegue |

Hechos técnicos: [SQLite uso local](https://sqlite.org/whentouse.html), [aislamiento](https://sqlite.org/isolation.html), [Flyway SQLite](https://documentation.red-gate.com/flyway/reference/database-driver-reference/sqlite), [arquitectura PG](https://www.postgresql.org/docs/current/tutorial-arch.html), [MVCC](https://www.postgresql.org/docs/current/mvcc-intro.html), [initdb](https://www.postgresql.org/docs/current/app-initdb.html), [conexiones PG](https://www.postgresql.org/docs/current/runtime-config-connection.html), [actualizaciones PG](https://www.postgresql.org/docs/current/upgrading.html). Las valoraciones de complejidad son inferencias de arquitectura para este producto, no comparativas de rendimiento publicadas.

## Tamaño: no inventar una medición

Definir B = Unity + recursos propios + PWA + Java técnico/JRE + Vision y sus bibliotecas si se distribuye + launcher/avisos. La API añade JAR/dependencias; si requiere otra versión de JRE, ésta también debe contarse y reevaluar compatibilidad.

- Paquete A = B + API + JDBC PG + distribución privada PG y herramientas/licencias seleccionadas.
- Paquete B = B + API + SQLite JDBC/JNI y sus avisos.
- Disco A = archivos instalados + cluster inicial + datos + WAL + temporales + logs + backups + espacio durante upgrade.
- Disco B = archivos instalados + datos + journal/WAL/SHM según modo + temporales + logs + backups + espacio de migración.

Ninguna fórmula produce MB reales sin artefactos concretos. El motor embebido no hace desaparecer el tamaño de JVM/Unity/Vision. No usar tamaños de un instalador PG que incluya pgAdmin/StackBuilder como tamaño del motor privado, ni tamaño del C amalgamation como tamaño del driver Java completo. Xerial puede incluir nativos de varias plataformas en un JAR; cualquier recorte requiere pruebas y respeto de avisos. [Xerial](https://github.com/xerial/sqlite-jdbc).

Protocolo de medición futuro: build reproducible del mismo commit para cada opción y SO; SHA/versiones; bytes comprimidos del instalador, bytes descomprimidos, datos vacíos, datos tras fixture equivalente, WAL pico y espacio libre necesario para backup/upgrade. Registrar RSS de cada proceso y suma sin confundir memoria compartida con privada. Resultado actual de todas estas medidas: **NO MEDIDO**.

## Decisión razonada

Para un único host y resultados de baja frecuencia, se recomienda B por experiencia de instalación y menor superficie operativa. A conserva ventaja para múltiples escritores/servicios intensivos o requisitos PG específicos. No se afirma que B sea más rápido ni más durable en todo escenario; el [ADR](adr/ADR-001-LOCAL-DATABASE.md) condiciona adopción a evidencia de fiabilidad.
