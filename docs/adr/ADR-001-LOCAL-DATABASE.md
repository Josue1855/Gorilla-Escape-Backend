# ADR-001 — Persistencia local incluida en la distribución

Fecha: 2026-10-16. Estado: **PROPUESTO PARA ACEPTACIÓN**; evaluación técnica completada, elección recomendada concreta: **SQLite integrado en la API Spring Boot mediante JDBC**, sin servidor de BD separado. El requisito autocontenido es decisión obligatoria del PO; esta elección de motor no constituye permiso de implementar. [Fuentes](../SOURCES.md), [comparativa completa](../DATABASE_COMPARISON.md).

## Contexto y alcance

Una PC autoritativa con 1–4 teléfonos LAN; guardar participantes, sesiones, partidas, resultados e intentos útiles, no muestras por frame. Unity calcula resultados; Java actual mantiene WebRTC/IPC. Backend nuevo aún sólo documental en develop `e76371097f4bcd881fa081211ec8d0d78e308c0b`. El PO respondió que la rúbrica no especifica motor, lo cual no garantiza aceptación académica: documentar SQL/GET/POST reales y pedir confirmación docente antes de evaluación final.

## Elección y razones

SQLite evita el proceso servidor y el puerto de BD. Se propone un único propietario del fichero: la API local; teléfonos y Unity acceden a operaciones autorizadas, nunca al fichero. Este uso coincide con el almacenamiento local detrás de un servidor de aplicación descrito por [SQLite](https://sqlite.org/whentouse.html). El orden de preferencia responde a instalación simple, fiabilidad offline y mantenimiento, no a un benchmark inexistente.

La integración candidata es Spring JDBC/JdbcTemplate con DataSource explícito y driver Xerial empaquetado. No asumir que Spring Boot autoconfigura SQLite como H2, ni que Spring Data JDBC/JPA soportan automáticamente su dialecto. Hibernate SQLite es dialecto comunitario; evitar ese acoplamiento inicial. Versiones exactas de Boot/JDK/driver, migrador y nativos se fijarán mediante la issue de compatibilidad, sin añadir dependencias en este PR. [Spring SQL](https://docs.spring.io/spring-boot/reference/data/sql.html), [Xerial](https://github.com/xerial/sqlite-jdbc), [Hibernate](https://docs.hibernate.org/stable/orm/dialect/).

Decisiones de diseño: directorio persistente por usuario fuera de la instalación; escritura transaccional corta/serializada; idempotencia UNIQUE; migración exclusiva antes de readiness; backup consistente previo a cambios de esquema; recuperación sin borrar datos. WAL es candidato condicionado a pruebas, motor corregido y lectura concurrente necesaria. No habilitar WAL sobre un SQLite vulnerable: la documentación identifica el arreglo WAL-reset en 3.51.3 y posteriores, y backports concretos. Registrar `sqlite_version()` del nativo realmente cargado, no sólo versión del JAR. [WAL oficial](https://sqlite.org/wal.html#walresetbug).

## Alternativa A: PostgreSQL privado

Viable sin Docker y sin instalación PostgreSQL del usuario si el producto lleva binarios/recursos correctos, initdb, autenticación local, supervisión, salud, backup y actualización de motor. Mayor coste de procesos, binarios por plataforma, cluster y upgrades. Se conserva [diseño de contingencia](../POSTGRES_PRIVATE_ALTERNATIVE.md), no se distribuyen ambos motores por defecto.

Reabrir ADR si la rúbrica exige PostgreSQL, aparecen múltiples servidores/escritores intensivos que incumplen objetivos medidos, se requieren capacidades específicas de PostgreSQL o el spike JDBC/SQLite no supera integridad/recuperación/compatibilidad. No cambiar automáticamente al otro motor ante un error: eso perdería continuidad de los datos.

## Consecuencias y límites

Ventajas esperadas: menos componentes que instalar/administrar, cero socket de BD y operación local sin WAN. Costes: un escritor, disciplina de migraciones/backup y validación de la biblioteca nativa por SO/arquitectura. El fichero no está cifrado por defecto ni protegido frente a un administrador o proceso del mismo usuario; permisos de SO y minimización de datos son obligatorios.

Se elige una API en JVM separada para aislar persistencia del transporte, usando el runtime Java incluido compatible; no un nuevo servicio del sistema. No se modifica el supervisor existente: primero [auditoría de integración](SUPERVISOR_AUDIT.md). El posible coste de memoria de una segunda JVM y tamaño final están **NO MEDIDOS**. Si ese coste resulta inaceptable, evaluar el módulo en la JVM existente mediante otro ADR, sin asumir una refactorización aprobada.

## Gates previos a implementar/publicar

1. PO revisa este ADR y la rúbrica; implementación futura debe autorizarse por tarea.
2. Compatibilidad exacta JDBC/Boot/JDK/nativo/migrador y política de licencias revisadas.
3. Contrato de lifecycle/puertos/credenciales aprobado tras auditoría sin cambiar framing, DTO ni límites del transporte por inercia.
4. Spike de integridad/concurrencia/backup y ensayo Windows/Linux en tareas futuras; resultados PENDIENTE DE EJECUCIÓN.
5. Medir instalación completa, uso de disco/memoria y latencias, no sólo motor; verificar doce requisitos y regresión LAN.

Relación: [requisitos y aceptación](../ACCEPTANCE_MATRIX.md), [empaquetado](../PACKAGING.md), [persistencia](../PERSISTENCE.md), [plan incremental](../IMPLEMENTATION_PLAN.md).
