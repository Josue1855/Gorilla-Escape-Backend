# Auditoría previa del supervisor y frontera de integración

Revisión 2026-10-16, **DOCUMENTAL**. No se modificó el supervisor ni se ejecutó otro proceso de producto. Base de Unity/Java: develop `7a750826c5565cbafdec983c5988c4cd5657d72a`; [fuentes exactas](../SOURCES.md).

| Observación AS-IS | Fuente / consecuencia |
|---|---|
| Unity posee un hijo Java por intento; STARTING/CONNECTING/RUNNING y cleanup | JavaProbeSupervisor.cs, IpcLifecycle.cs; no adopta procesos ajenos |
| Presupuesto 15 s hasta primer PONG; framing/token y loopback | JavaProbeSupervisor.cs:136–189; no insertar migraciones largas antes de READY/PONG |
| EOF de stdin y contexto Spring cerrable | GorillaEscapeApplication.java:33–38, ParentLifetime.java; base reutilizable, no garantiza cierre de un hijo futuro congelado |
| Shutdown Java esperado <=10 s, luego Kill del hijo propio y observación 2 s | JavaProbeSupervisor.cs:282–300; Kill actual no certifica cierre de todos los descendientes |
| Tres lanzamientos por ejecución, recuperación manual | IpcLaunchBudget y JavaProbeSupervisor; no reinterpretar esto como watchdog automático |
| Lock de JVM técnica por usuario | ManagedProcessGuard.java; no es lock de schema/datos de un backend que no existe |
| Empaquetado actual prepara JRE relativo y recursos locales | tools/t020/package.py; referencia Linux, Windows/PC limpia y nuevo backend no certificados |

## Topología recomendada, aún no implementada

Conservar supervisor Unity → Java técnico. Un nuevo componente de lifecycle en Java poseería la API local en otra JVM con SQLite dentro, y mantendría vivos sus pipes, identidad de generación y presupuesto propios. Debe diseñarse/revisarse como extensión aislada, no incluir lógica SQL en los workers WebRTC/IPC. Compartir runtime Java compatible, no heap. No lanzar un proceso PostgreSQL cuando se selecciona SQLite.

La persistencia arranca después de readiness técnico, en worker propio; status de persistencia independiente del PONG. El estado READY de transporte no significa DB_READY. La API sólo publica persistencia lista tras lock, apertura, migraciones y comprobación de esquema/escritura. No aumentar el deadline IPC de 15 s para esconder migraciones.

## Contratos nuevos que necesitan revisión

Canal privado de bootstrap/credenciales del backend, descubrimiento del puerto loopback, autenticación/roles del publicador Unity y proxy REST de UI/UX. No están implementados. No exponer token IPC ni reutilizarlo como contraseña REST. El secreto del backend debe entregarse por canal privado del arranque, sin CLI/logs/URLs persistidas; el diseño definitivo de entrega a cada consumidor es gate de la issue de lifecycle/contratos.

La PWA puede acceder a REST de negocio mediante una futura fachada autorizada en Java HTTPS existente, con allowlist independiente. No abrir API backend a 0.0.0.0 por conveniencia ni añadir endpoints de negocio al túnel público automáticamente. El nuevo proxy no puede modificar señalización, DataChannels, Shared ni límites de frames de transporte.

## Presupuestos y fallos

Propuesta a medir: drenar/cerrar API dentro de 4 s y completar Java dentro del presupuesto total existente de 10 s, dejando margen; no sumar shutdowns secuenciales que excedan 10 s. Migraciones/backups largos ocurren antes de activar o durante mantenimiento, nunca en el hilo de cierre de Unity. Si no cabe, bloquear integración y revisar lifecycle, no aumentar timeouts sin evidencia.

Un fallo de API no mata RTC ni invalida sesiones/epoch. Marcar persistencia no disponible y detener aceptación de resultados que prometan guardarse si no existe outbox durable. Un posible reinicio automático del backend se limita a un intento con exit/cleanup/lock comprobados y nueva generación; no se cambia la recuperación manual del Java técnico. Fallo de migración/corrupción exige recuperación explícita, nunca bucle de reinicios.

Muerte abrupta o congelamiento: EOF ayuda sólo si el hijo responde. Diseñar y probar contención específica de descendientes (p. ej. Job Object Windows y mecanismo de muerte de padre/grupo controlado en Linux) mediante componente nativo/launcher mínimo revisado si es necesario. No asumir que Process.Kill, shutdown hook o PID guard garantizan esto. No matar por nombre/PID sin identidad ni borrar locks de un proceso activo.

Gate: no cambiar fuentes del supervisor hasta revisión del contrato, budgets, contención, pruebas de fallo y responsables productor/consumidor. [Plan](../IMPLEMENTATION_PLAN.md) y [riesgos](../RISKS.md).
