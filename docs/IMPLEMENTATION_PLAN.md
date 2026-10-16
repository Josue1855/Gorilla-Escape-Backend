# Plan incremental y gates de implementación

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

El encargo actual termina con evaluación, documentación, PR e issues. **No se implementa ahora**: ni driver, DataSource, schema, migraciones, supervisor, instalador ni tests de runtime nuevos.

| Incremento | Resultado futuro / dependencia | Gate y evidencia |
|---|---|---|
| I0 — Decisión | Revisar ADR y rúbrica; elegir versiones/driver/migrador, métricas y objetivos | ADR aceptado y decisión de stack registrada, sin deducir versiones por nombre |
| I1 — Prueba aislada SQLite/JDBC | Después de I0: transacciones/FK/UNIQUE/concurrencia, motor corregido, datos locales | Test real/commit, core sqlite_version, rollback/replay y busy acotado; no tocar transporte |
| I2 — Persistencia y migraciones | I1: modelo propuesto aprobado, rutas/lock, backups/recovery | Crear/reabrir/upgrade/restore/downgrade incompatible y fallos; sin reset silencioso |
| I3 — API y contratos | I1/I2 + revisión Unity/UI: DTO/roles/publicador/resultados/recibos | GET/POST/BD y rechazo de resultado no autorizado, idempotencia concurrente |
| I4 — Lifecycle auxiliar | Auditoría previa + I2/I3; componente nuevo con ownership/contención | READY separado, cierre total <=budget existente y fallos de árbol; revisión antes de modificar supervisor |
| I5 — Paquete único | I4 y stack compatible: Windows/Linux, JRE/nativos/recursos/avisos | Instalación limpia offline sin dependencias manuales, medición de tamaño/RSS; setup firewall una vez |
| I6 — Integración y academia | I3/I5 y clientes autorizados: LAN+persistencia, escenarios A/B | AC-01..12 + LAB auténtico y regresión WebRTC/IPC; no sustituir físico por mocks |
| I7 — Aceptación de entrega | Evidencia I6, riesgos críticos cerrados y autorización PO | PR develop→main sólo con entrega aprobada; no auto-merge |

Trabajo desde develop en ramas de tarea y PR a develop; main no recibe commits de desarrollo. Las issues viven en [seguimiento Backend](PROJECT_TRACKING.md), con dependencias/criterios. Sin fechas/Sprint/assignees inventados. Si PG se selecciona en revisión, sustituir I1/I2/I4/I5 por sus gates específicos antes de programar, no introducir dos motores por si acaso.

No modificar bootstrap previo a cerrar la auditoría contractual y revisar el diseño entre Unity/Java/Backend. La disponibilidad de una API futura no convierte la actual PWA UI/UX demo en cliente integrado ni autoriza cierre de T019/T020.
