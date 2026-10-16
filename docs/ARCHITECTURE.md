# Frontera de arquitectura del backend

Última revisión: 2026-10-16. Fuente: encargo PO de auditoría y posterior instrucción explícita de crear el repositorio y vincularlo al Project. Estado del runtime: PLANIFICADO, sin implementación en esta base.

El flujo de juego existente es teléfono/PWA técnica → WebRTC LAN → Java local → IPC loopback autenticado → Unity. La persistencia futura es un flujo distinto: UI/adaptador autorizado → REST Spring Boot → BD local SQLite recomendada por ADR-001, aún sin implementar. Sin WAN con LAN activa no equivale a backend local inaccesible.

Unity emite los resultados oficiales. El backend deberá validar publicador, pertenencia del participante, reglas/unidades, transacción e idempotencia. La PWA no puede fabricar puntos oficiales. No se persisten muestras de gyro/touch/fotogramas por defecto.

El [contrato REST propuesto](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/01-architecture/API_CONTRACTS.md), el [modelo de BD propuesto](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/01-architecture/DATABASE_DESIGN.md) y la [estrategia offline propuesta](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/01-architecture/OFFLINE_STRATEGY.md) son fuentes de diseño, no código adoptado ni endpoints disponibles.

Decisiones abiertas: aceptación del ADR SQLite autocontenido, versiones del stack, migrador/ORM, migraciones, IDs persistentes y mapeo RTC, credencial del adaptador Unity, outbox/cola, retención, CORS/orígenes y hosting. Confirmarlas con los consumidores antes de programar. Crear el repositorio resuelve su localización, pero no prueba ninguna de estas decisiones.

La nueva [evaluación de distribución autocontenida](adr/ADR-001-LOCAL-DATABASE.md) concreta la recomendación del motor y las restricciones de instalación; sustituye cualquier inferencia de PostgreSQL instalado por el usuario. [Auditoría previa del supervisor](adr/SUPERVISOR_AUDIT.md), sin cambios de código.
