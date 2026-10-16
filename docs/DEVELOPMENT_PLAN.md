# Primeras tareas propuestas

Última revisión: 2026-10-16. Estado: PROPUESTO; estas filas no son issues remotas ni tareas ejecutadas. No hay fechas/Sprint/responsables personales asignados.

| Prioridad | Tarea | Aceptación / evidencia requerida |
|---|---|---|
| P0 | Acordar stack, motor y entorno aislado | Decisión revisada, versiones en manifiestos y documentación; sin secretos |
| P0 | Contrato con Unity/UI: identidad, roles, DTO/errores | Contrato versionado, mapeo de IDs y autorización del publicador aprobado |
| P1 | Modelo y scripts de preparación/carga/limpieza | Motor confirmado; PK/FK/constraints, datos ficticios, reproducibilidad e integridad antes/después |
| P1 | GET de participantes y POST transaccional | Lista desde BD real, validación, pertenencia a sesión, rollback y error sanitizado |
| P1 | Idempotencia concurrente de resultados | Una escritura por transactionId, replay idéntico devuelve mismo ID; payload distinto genera conflicto |
| P1 | Logs, métricas y laboratorio | Método/HTTP/duración/requestId/JVM, consulta SQL e interfaz coincidentes; evidencias auténticas |
| P1 | Probar WAN desconectada con LAN activa y backend inaccesible | Comportamientos separados, sin éxito ficticio ni resultados duplicados al reconectar |

El [laboratorio del producto](https://github.com/Josue1855/Gorilla-Escape/blob/17b6945cd5ffef321a8eb58788529a7cb42719a0/docs/04-testing/PWA_TEST_LAB.md) detalla LAB-01..10. Todo resultado de ejecución sigue PENDIENTE DE EJECUCIÓN. La preparación de este repositorio no cierra T019/T020/F05/E0 ni autoriza implementar gameplay durante el encargo documental.

La evaluación de almacenamiento local autocontenido se organiza en el [plan incremental](IMPLEMENTATION_PLAN.md). Sus tareas futuras requieren aceptación y evidencia; esta documentación no implementa el backend. Trabajo en ramas desde develop y PR hacia develop; main sólo para entregas validadas.
