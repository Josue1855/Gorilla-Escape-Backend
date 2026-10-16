# Persistencia, integridad, migraciones y recuperación

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

## Propiedad y ubicación

SQLite es fichero local del backend, no recurso compartido SMB/NFS/OneDrive. Ruta lógica de datos: Windows `%LOCALAPPDATA%/GorillaEscape/data/`; Linux `${XDG_DATA_HOME:-~/.local/share}/gorilla-escape/data/`. Resolver mediante API del SO y política de distribución; no depender del cwd, carpeta de instalación ni ruta del desarrollador. Backups/versiones/logs tendrán subdirectorios privados hermanos. Múltiples cuentas del SO tienen datos separados salvo diseño futuro explícito.

Un propietario API por directorio, lock de proceso y transacciones SQLite. Permisos de directorio 0700/ficheros 0600 como objetivo POSIX y ACL del usuario correspondiente en Windows; probar creación, herencia y upgrade. Rechazar rutas remotas/enlaces no permitidos y no crear otra base vacía silenciosamente cuando se pierde acceso a la original. Preservar datos al desinstalar/actualizar por defecto; eliminación sólo explícita. No prometer cifrado en reposo nativo sin diseñarlo.

## Escrituras y configuración candidata

Transacciones cortas de resultados, pertenencia a sesión, FKs y claves únicas en la misma unidad. UUID durable no es PlayerId RTC. `transactionId` estable + hash canónico: replay idéntico devuelve mismo recibo; clave con payload distinto genera conflicto sin escritura parcial. Guardar resultado, intentos útiles y recibo atómicamente; conservar claves mientras exista posibilidad de replay. No guardar gyro/fotogramas por frame.

Como candidato concreto de backup, Xerial documenta una extensión de backup/restore accesible desde su driver; confirmar esa API en la versión fijada y ensayar consistencia antes de usarla, sin añadir una herramienta SQL requerida por el jugador. [Guía Xerial](https://raw.githubusercontent.com/xerial/sqlite-jdbc/master/USAGE.md).

Propuesta: escritor serializado con admisión/cola acotada y backpressure; pool pequeño revisado, no asumir que más conexiones mejoran escritura. `foreign_keys=ON` verificado por conexión; tipos/constraints explícitos, fechas UTC, magnitudes con escala/unidad versionadas. Retries de SQLITE_BUSY limitados por deadline, nunca spin infinito ni duplicación del resultado; outbox del productor autorizada/durable si se necesita tolerar caída de API. [PRAGMA](https://sqlite.org/pragma.html), [aislamiento](https://sqlite.org/isolation.html).

Evaluar WAL para lectores concurrentes en filesystem local, con `synchronous=FULL` como punto de partida para durabilidad; confirmar retorno efectivo de cada ajuste y versión segura. No cambiar a NORMAL/OFF por rendimiento sin aceptar pérdida de COMMIT ante corte. Los ficheros WAL/SHM forman parte del estado operacional: no borrarlos al iniciar o al actualizar. Un lector largo puede retrasar checkpoints; limitar transacciones y monitorizar espacio. [WAL](https://sqlite.org/wal.html).

## Migraciones

Elegir migrador mantenido compatible (Flyway es candidato, no instalado) con scripts SQLite versionados/checksum y un sólo migrador propietario. Backup consistente + comprobación de espacio antes de migrar; abrir servicio sólo tras verificar schema/foreign keys. No auto-DDL de Hibernate como estrategia de producción. Flyway documenta límites de migración concurrente y transacciones anidadas en SQLite; probar reconstrucciones de tablas y rollback de cada cambio que se utilice. [Flyway](https://documentation.red-gate.com/flyway/reference/database-driver-reference/sqlite).

Si schema está por delante del binario, mostrar incompatibilidad y conservar datos. No downgrade automático ni restauración silenciosa que descarte partidas posteriores. Instalar una versión correctiva compatible o permitir restauración explícita y auditable con alcance de pérdida conocido. La matriz de compatibilidad app/schema/motor es parte del manifest.

## Respaldo y restauración

Copia consistente mediante API de backup soportada y verificada desde el driver, o herramienta incluida/alternativa aprobada en tarea de implementación; no se asume que JdbcTemplate expone la API nativa directamente. Parar escritores o usar snapshot transaccional, producir archivo temporal, verificar integridad/FKs y marca de aplicación/schema, y renombrar/publicar el backup completado. No copiar sólo `.db` con WAL activo. [Backup API](https://sqlite.org/backup.html), [riesgos de corrupción](https://sqlite.org/howtocorrupt.html).

Backups propuestos: antes de migración y rotación tras sesiones confirmadas; límite de número/bytes y retención se aprueban con producto. Sin borrado automático de históricos de jugadores para liberar espacio. Backup local protege frente a una migración mala, no frente a pérdida total del disco; exportación a soporte externo ofrecida desde la app sin herramienta SQL manual. Nunca subir respaldos automáticamente a Internet.

Restauración: cerrar API, preservar archivos dañados y backups, seleccionar copia verificada compatible, comprobar espacio, restaurar a ruta temporal y validar, activar por reemplazo seguro con API detenida y registrar IDs/fecha de copia. Mostrar partidas que pueden faltar desde ese punto. Si no existe copia válida, error recuperable/exportación de diagnóstico; no crear BD nueva ni afirmar reparación de datos corruptos.

## Fallos y durabilidad

Corte de energía/kill puede dejar journal/WAL: SQLite intenta recuperar al abrir; eso no garantiza recuperar corrupción física o fsync mentiroso. Una confirmación de éxito HTTP sólo se emite tras COMMIT; si se pierde respuesta, el reintento por clave estable resuelve la incertidumbre. RPO objetivo: no perder transacciones confirmadas bajo las garantías de almacenamiento ensayadas; no es promesa frente a hardware que incumple flush. RTO de reapertura/restauración: NO MEDIDO, fijar tras ensayos.

Disco lleno, ACL denegada, base en uso, migración fallida y corrupción tienen errores diferentes y no modifican el transporte RTC. Readiness no valida toda la base en cada heartbeat: usar checks ligeros y pruebas de integridad completas fuera del frame y con mantenimiento acotado. [Commit atómico](https://sqlite.org/atomiccommit.html).
