# Primeras tareas propuestas

Última revisión: 2026-10-16. Las filas originales siguientes son planificación de producto; el estado del incremento actual se detalla al final. No hay fechas/Sprint/responsables personales asignados.

| Prioridad | Tarea | Aceptación / evidencia requerida |
|---|---|---|
| P0 | Acordar stack, motor y entorno aislado | Decisión revisada, versiones en manifiestos y documentación; sin secretos |
| P0 | Contrato con Unity/UI: identidad, roles, DTO/errores | Contrato versionado, mapeo de IDs y autorización del publicador aprobado |
| P1 | Modelo y scripts de preparación/carga/limpieza | Motor confirmado; PK/FK/constraints, datos ficticios, reproducibilidad e integridad antes/después |
| P1 | GET de participantes y POST transaccional | Lista desde BD real, validación, pertenencia a sesión, rollback y error sanitizado |
| P1 | Idempotencia concurrente de resultados | Una escritura por transactionId, replay idéntico devuelve mismo ID; payload distinto genera conflicto |
| P1 | Logs, métricas y laboratorio | Método/HTTP/duración/requestId/JVM, consulta SQL e interfaz coincidentes; evidencias auténticas |
| P1 | Probar WAN desconectada con LAN activa y backend inaccesible | Comportamientos separados, sin éxito ficticio ni resultados duplicados al reconectar |

El [laboratorio del producto](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/04-testing/PWA_TEST_LAB.md) detalla LAB-01..10. La integración PWA/GET/POST/SQLite y el offline emulado ya fueron ejecutados en el fork; el [dossier](laboratorio/README.md) conserva la evidencia. Quedan tres capturas literales de DevTools, integración al upstream y validación física; no se declara cerrado todo el laboratorio. La preparación de este repositorio no cierra T019/T020/F05/E0 ni autoriza implementar gameplay durante el encargo documental.

## Incremento actual

La [issue #13](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/13) delimita jugadores: GET/POST, SQLite real, migración, validaciones, SQL, OpenAPI y pruebas. Ver [resultados](VALIDATION_PLAYERS.md) y [procedimiento reproducible](LAB_PLAYERS.md). Las issues #3, #4 y #10 avanzan parcialmente; no se consideran cerradas por esta entrega. No se implementan sesiones, resultados, rankings, gameplay ni el instalador. Se requiere revisión del PR hacia develop antes de integrar.
