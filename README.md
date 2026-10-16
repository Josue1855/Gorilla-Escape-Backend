# Gorilla Escape — PWA Backend

Repositorio oficial del equipo para el backend de servicios y persistencia de Gorilla Escape: Gorilimpiadas. Creado el 2026-10-16 por instrucción del Product Owner. Estado: **PLANIFICADO** para implementación; esta base contiene exclusivamente documentación.

## Responsabilidad

API REST con Spring Boot, validación de DTO, almacenamiento transaccional, idempotencia y observabilidad. Unity conserva la autoridad de física, reglas y resultados. Este backend no sustituye al servicio Java local de WebRTC/IPC que ya existe en el repositorio Unity.

No hay aplicación ejecutable, endpoints de negocio, esquema, scripts SQL, tests ni CI implementados en esta revisión. La versión de Java/Spring Boot, motor de BD y ORM deben confirmarse antes de crear sus manifiestos; no se presume PostgreSQL ni JPA.

## Repositorios y seguimiento

- [Unity y conectividad técnica](https://github.com/Josue1855/Gorilla-Escape), integración existente en develop.
- [PWA UI/UX](https://github.com/Quintana-117/Dise-o_pwa), demo React independiente aún pendiente de integración.
- [Backend, este repositorio](https://github.com/Josue1855/Gorilla-Escape-Backend).
- [Project del producto](https://github.com/users/Josue1855/projects/1). La vinculación del repositorio no implica crear issues, cambiar estados ni cerrar aceptación.
- [PR documental de arquitectura #122](https://github.com/Josue1855/Gorilla-Escape/pull/122), pendiente de revisión; contiene el índice y contratos candidatos del producto.

## Empezar

Leer [arquitectura y decisiones pendientes](docs/ARCHITECTURE.md) y [primeras tareas y aceptación](docs/DEVELOPMENT_PLAN.md). No hay comando de arranque hasta implementar la aplicación. La base de trabajo e integración es **develop**. Crear ramas de tarea desde develop y abrir sus PR hacia develop. **main se reserva exclusivamente para entregas validadas y aprobadas**, promovidas mediante PR desde develop; nunca trabajar ni hacer commits directamente allí. Consultar [CONTRIBUTING.md](CONTRIBUTING.md).

Los ejemplos de API/BD del producto son PROPUESTO y necesitan revisión backend/Unity/UI antes de implementarse. Los resultados académicos Spring/SQL/DevTools permanecen PENDIENTE DE EJECUCIÓN. Usar exclusivamente trabajo propio del equipo y dependencias aprobadas; no incorporar prototipos descartados ni credenciales.
