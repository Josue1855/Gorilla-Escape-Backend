# Seguimiento Backend — operaciones verificadas

2026-10-16. [Gorilla Escape — Backend](https://github.com/users/Josue1855/projects/2), Project V2 independiente, privado, owner Josue1855, vinculado a Josue1855/Gorilla-Escape-Backend. Antes de crear se consultaron Projects visibles y las issues del repositorio; no existía un Project Backend independiente ni issues equivalentes. El Project histórico #1 no fue reorganizado ni recibió mutaciones de esta evaluación.

Consulta posterior GraphQL y gh confirmó **10 issues abiertas enlazadas, 16 campos y tres vistas**. Estado de tareas: una In Review (decisión/documentos disponibles) y nueve Backlog (implementación/pruebas futuras). Ninguna Done, sin assignees ni fechas ficticias. Las dependencias son URLs reales y texto de apoyo, no una relación nativa de bloqueo que no se haya configurado.

## Issues y criterios

Los cuerpos remotos incluyen objetivo, alcance, dependencias, criterios de aceptación, evidencia requerida, prioridad y rol propuesto. Crear tareas no autoriza empezar implementación durante este encargo.

| ID | Issue real | Estado | Prioridad / tipo | Depende de |
|---|---|---|---|---|
| BDB-01 | [#2 — Revisar ADR-001 y aceptar motor para distribución autocontenida](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/2) | In Review | P0 / Decision | Revisión PO/arquitectura |
| BDB-02 | [#3 — Validar stack JDBC, SQLite nativo y migrador en Windows/Linux](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/3) | Backlog | P0 / Task | [#2](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/2) |
| BDB-03 | [#4 — Diseñar e implementar directorio durable, lock y esquema versionado](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/4) | Backlog | P0 / Task | [#3](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/3) |
| BDB-04 | [#5 — Añadir publicación autoritativa e idempotencia de resultados](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/5) | Backlog | P0 / Task | [#4](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/4) |
| BDB-05 | [#6 — Revisar e integrar lifecycle de API sin alterar WebRTC ni IPC](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/6) | Backlog | P0 / Task | [#2](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/2), [#4](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/4) |
| BDB-06 | [#7 — Implementar respaldos, restauración y actualizaciones sin pérdida silenciosa](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/7) | Backlog | P0 / Task | [#4](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/4) |
| BDB-07 | [#8 — Preparar instalador único offline con runtimes incluidos](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/8) | Backlog | P0 / Task | [#3](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/3), [#6](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/6), [#7](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/7) |
| BDB-08 | [#9 — Ejecutar concurrencia, fallos y regresión del transporte](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/9) | Backlog | P0 / Test | [#5](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/5), [#6](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/6), [#7](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/7) |
| BDB-09 | [#10 — Completar laboratorio PWA REST BD con evidencias auténticas](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/10) | Backlog | P1 / Test | [#5](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/5), [#8](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/8), [#9](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/9) |
| BDB-10 | [#11 — Aceptar distribución Windows/Linux y promoción controlada de entrega](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/11) | Backlog | P0 / Task | [#8](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/8), [#9](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/9), [#10](https://github.com/Josue1855/Gorilla-Escape-Backend/issues/10) |

## Configuración verificada

- Status: Backlog, Ready, In Progress, In Review, Blocked, Done.
- Priority: P0, P1, P2.
- Work Type: Decision, Task, Test, Documentation. El nombre Type fue rechazado por GitHub como reservado; se usó Work Type y se verificó el resultado.
- Depends on: texto con enlaces de dependencias reales; Assignees permanece como campo nativo sin asignaciones automáticas.
- Vistas configuradas y consultadas: Overview (tabla), Kanban (board), Revisión y gates (tabla filtrada In Review). No se presume una agrupación o automatización adicional no leída de la API.
- Sin fechas/iteraciones inventadas ni workflows de autoaceptación. Issue cerrada no sustituye evidencia ni gate de entrega.

Cada issue fue consultada después de crear/editar y se verificaron número, URL, estado OPEN, pertenencia a repo, ausencia de assignees y valores Status/Priority/Work Type del item en Project. Los contadores reflejan ese instante. La implementación seguirá el plan desde develop con ramas y PR hacia develop, sin merge automático a ninguna rama.

## Validación de la documentación

Se validan enlaces relativos/anchors, cercas Markdown, tres diagramas con parser Mermaid y git diff --check. Todos los cambios del repositorio son Markdown; no se añadieron driver/JDBC, schema, migraciones ejecutables, supervisor, instalador, pruebas de runtime ni dependencias. Los presupuestos y pruebas del diseño no están ejecutados; tamaño, memoria, RTO y latencias NO MEDIDOS. Fuente consultada y commit de base en [SOURCES.md](SOURCES.md).
