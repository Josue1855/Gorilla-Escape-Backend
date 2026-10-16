# Gorilla Escape — PWA Backend

Repositorio oficial del equipo para el backend de servicios y persistencia de Gorilla Escape: Gorilimpiadas. Creado el 2026-10-16 por instrucción del Product Owner. Estado: **PLANIFICADO** para implementación; esta base contiene exclusivamente documentación.

## Responsabilidad

API REST con Spring Boot, validación de DTO, almacenamiento transaccional, idempotencia y observabilidad. Unity conserva la autoridad de física, reglas y resultados. Este backend no sustituye al servicio Java local de WebRTC/IPC que ya existe en el repositorio Unity.

No hay aplicación ejecutable, endpoints de negocio, esquema, scripts SQL, tests ni CI implementados en esta revisión. La distribución debe ser autocontenida: ninguna instalación manual adicional, sin Docker, sin admin en ejecuciones normales y sin dependencias WAN. La evaluación recomienda SQLite local por JDBC; el [ADR-001](docs/adr/ADR-001-LOCAL-DATABASE.md) está para aceptación y no autoriza implementar. Versiones y migrador/ORM se fijarán tras revisión.

## Repositorios y seguimiento

- [Unity y conectividad técnica](https://github.com/Josue1855/Gorilla-Escape), integración existente en develop.
- [PWA UI/UX](https://github.com/Quintana-117/Dise-o_pwa), demo React independiente aún pendiente de integración.
- [Backend, este repositorio](https://github.com/Josue1855/Gorilla-Escape-Backend).
- [Project del producto](https://github.com/users/Josue1855/projects/1). La vinculación del repositorio no implica crear issues, cambiar estados ni cerrar aceptación.
- [PR documental de arquitectura #122](https://github.com/Josue1855/Gorilla-Escape/pull/122), pendiente de revisión; contiene el índice y contratos candidatos del producto.

## Empezar

Leer [arquitectura y decisiones pendientes](docs/ARCHITECTURE.md) y [primeras tareas y aceptación](docs/DEVELOPMENT_PLAN.md). No hay comando de arranque hasta implementar la aplicación. La rama de integración es develop. Crear ramas de tarea desde develop y abrir sus PR hacia develop; main sólo recibe promoción de una entrega validada y aprobada. No trabajar ni hacer commits directamente en main/develop y no hacer merge automático.

Los ejemplos de API/BD del producto son PROPUESTO y necesitan revisión backend/Unity/UI antes de implementarse. Los resultados académicos Spring/SQL/DevTools permanecen PENDIENTE DE EJECUCIÓN. Usar exclusivamente trabajo propio del equipo y dependencias aprobadas; no incorporar prototipos descartados ni credenciales.

Evaluación autocontenida: [índice de entregables](docs/README.md), [Project Backend](https://github.com/users/Josue1855/projects/2) y [plan incremental](docs/IMPLEMENTATION_PLAN.md).
