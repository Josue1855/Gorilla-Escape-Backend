# Frontera de arquitectura del backend

Última revisión: 2026-10-16. Fuente: encargo PO de auditoría y posterior instrucción explícita de crear el repositorio y vincularlo al Project. Estado actual: primer incremento de jugadores implementado en rama para revisión; el resto del runtime de negocio sigue planificado.

El flujo de juego existente es teléfono/PWA técnica → WebRTC LAN → Java local → IPC loopback autenticado → Unity. La persistencia futura es un flujo distinto: UI/adaptador autorizado → REST Spring Boot → SQLite local mediante JDBC. Sin WAN con LAN activa no equivale a backend local inaccesible.

Unity emite los resultados oficiales. El backend deberá validar publicador, pertenencia del participante, reglas/unidades, transacción e idempotencia. La PWA no puede fabricar puntos oficiales. No se persisten muestras de gyro/touch/fotogramas por defecto.

El [contrato REST propuesto](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/01-architecture/API_CONTRACTS.md), el [modelo de BD propuesto](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/01-architecture/DATABASE_DESIGN.md) y la [estrategia offline propuesta](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/01-architecture/OFFLINE_STRATEGY.md) son fuentes de diseño, no código adoptado ni endpoints disponibles.

Para jugadores se aprobaron SQLite/JDBC, Spring Boot, UUID del servidor, migraciones Flyway y almacenamiento por usuario fuera del JAR. La [aprobación acotada](adr/ADR-001-PLAYERS-APPROVAL.md), el [contrato implementado](API_PLAYERS.md) y la [validación real](VALIDATION_PLAYERS.md) delimitan ese alcance. No hay ORM, resultados oficiales ni relación jugador/sesión todavía.

Siguen abiertas: mapeo RTC, credencial del adaptador Unity, outbox/cola, retención, TLS y hosting de distribución final. La LAN HTTP es opt-in para laboratorio aislado; no constituye autenticación. El supervisor existente no se modificó.
