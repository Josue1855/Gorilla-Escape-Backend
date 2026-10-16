# Alternativa A — PostgreSQL privado empaquetado

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

Alternativa evaluada y no seleccionada como recomendación principal. Si el PO elige PostgreSQL por rúbrica o carga medida, cumplir este diseño y actualizar ADR; no exigir Docker ni instalar una BD global. No se añadieron binarios ni configuración.

| Aspecto | Diseño necesario, PROPUESTO |
|---|---|
| Binarios | Un paquete verificado por Windows/Linux y arquitectura soportada: postgres, initdb, pg_ctl, herramientas backup/restore y lib/share requeridos. Registrar distribución/proveedor/hash/ABI y dependencias, sin descargar en runtime |
| Inicialización | Datadir estable privado por usuario, temporal y rename sólo tras initdb exitoso; no reinit de cluster existente. Ejecutar con usuario normal propietario, no root ni servicio global |
| Identidad | Lock de producto y cluster, PID+generación/ruta verificados; no adoptar instalación del sistema ni borrar postmaster.pid por conveniencia |
| Transporte local | Linux: socket en directorio privado y TCP deshabilitado si driver lo soporta; requiere adaptador socket no asumido en JDBC. Opción portable: bind explícito 127.0.0.1 y opcional ::1 deliberado, puerto privado no 5432 global. Nunca '*' ni IP LAN |
| Autenticación | HBA limitado a BD/rol del juego y loopback/socket; SCRAM para TCP, evitar trust. Rol de aplicación sin superusuario; rol migrador separado. No reglas 0.0.0.0/0 o ::/0. No depender sólo de firewall |
| Credenciales | Aleatorias por instalación, permisos/ACL y almacenamiento protegido de usuario; no CLI/logs/repo, no contraseña universal. Proteger material temporal de init y rotar con migración coordinada |
| Supervisión | Nuevo componente Java o launcher mínimo auditado posee PG; readiness independiente del IPC. Postmaster puede crear hijos; contención de árbol y muerte de padre deben probarse por SO |
| Salud | pg_isready más consulta autenticada a la BD correcta, versión/schema y escritura controlada. Puerto abierto o PID vivo no acreditan integridad/migraciones |
| Migración | Un migrador exclusivo, backup previo y resultado verificable; distinguir schema upgrade de major upgrade del motor |
| Apagado | Dejar de aceptar writes, cerrar pool, pg_ctl stop con modo fast/deadline y exit observado. Fast aborta transacciones abiertas de forma controlada; immediate queda para último recurso y posterior recuperación, no cierre limpio |
| Backup/restore | pg_dump/restore del catálogo de juego o backup físico consistente con WAL y herramienta adecuada. Restaurar en cluster temporal compatible y verificar; no copiar datadir activo como backup válido |
| Actualización | Minor: binarios compatibles con servidor detenido y backups; major: pg_upgrade --check y migración ensayada o dump/restore. No lanzar nueva major sobre datadir antiguo; conservar binarios viejos durante upgrade y espacio para rollback |
| Redistribución | PostgreSQL License y avisos obligatorios; inventario de OpenSSL/ICU/runtime/terceros del proveedor. Validar términos del bundle elegido; licencia del motor no certifica el instalador completo |

Fuentes: [initdb](https://www.postgresql.org/docs/current/app-initdb.html), [conexiones](https://www.postgresql.org/docs/current/runtime-config-connection.html), [HBA](https://www.postgresql.org/docs/current/auth-pg-hba-conf.html), [pg_isready](https://www.postgresql.org/docs/current/app-pg-isready.html), [pg_ctl](https://www.postgresql.org/docs/current/app-pg-ctl.html), [upgrades](https://www.postgresql.org/docs/current/upgrading.html), [pg_basebackup](https://www.postgresql.org/docs/current/app-pgbasebackup.html), [licencia](https://www.postgresql.org/about/licence/).

No elegir puerto "libre" y confiar para siempre: detectar colisiones/cambios de ownership, fallar sin conectar a otra instancia y comunicar el endpoint autenticado por canal privado. Las credenciales no aíslan de un administrador del SO o del mismo usuario con acceso a sus procesos; documentar amenaza local.

Pruebas extras frente a SQLite: initdb interrumpido, locales/rutas Unicode, puerto ocupado, rol/HBA negativos, listener IPv4/IPv6 comprobado desde otro host LAN, cierre de árbol PG, upgrade minor/major fallido, restauración sin Internet y ejecución normal sin admin. Health y deadlines deben caber en el presupuesto del juego; un timeout de inicialización PG no autoriza ampliar el handshake Unity actual.
