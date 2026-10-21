# Contrato de jugadores para Hiram / React

Estado: primer incremento implementado y consumido por la PWA del [PR de laboratorio #1](https://github.com/Josue1855/Dise-o_pwa/pull/1). GET, POST, renderizado y offline del navegador cuentan con [evidencia real](laboratorio/README.md); la incorporación al upstream de Hiram sigue pendiente. [OpenAPI JSON](../src/main/resources/static/openapi.json), también servido por GET `/openapi.json` con la app iniciada.

Origen de desarrollo por defecto `http://127.0.0.1:8090`. El móvil no debe usar 127.0.0.1 como dirección de la PC: en el teléfono apunta al teléfono. Separar siempre HTTP de negocio del transporte de controles Unity ↔ Java ↔ PWA existente.

| Endpoint | Entrada | Respuesta |
|---|---|---|
| GET /api/players | limit 1..100 (50 por defecto), offset 0..100000 (0 por defecto) | 200 `{items:[{id,displayName,createdAt}],limit,offset}`; orden createdAt,id |
| POST /api/players | application/json, sólo `displayName` string | 201 `{id,displayName,createdAt}` después de COMMIT; id UUID y fecha UTC generados por servidor |

Ejemplo POST:

```json
{"displayName":"Jugador Uno"}
```

Forma de respuesta ilustrativa, **no evidencia ejecutada**:

```json
{"id":"11111111-1111-4111-8111-111111111111","displayName":"Jugador Uno","createdAt":"2026-10-16T18:00:00Z"}
```

El nombre se recorta en extremos y normaliza NFC; 1–40 unidades UTF-16, letras/marcas/números Unicode, espacio, guion, punto, apóstrofe y guion bajo. No se aceptan nulos, números/booleanos convertidos a string, controles, campos extra ni claves JSON duplicadas. Body máximo 4096 bytes, nesting máximo 8; `maxLength` OpenAPI es una aproximación por caracteres, la validación Java usa UTF-16. Nombres iguales sí se permiten: la identidad es UUID, no el nombre visible.

Errores del controlador usan application/problem+json: status, type, title, detail, code, requestId y violations opcional sin valores rechazados. 400 JSON/nombre/paginación inválidos; 409 constraint; 413 body grande; 415 tipo de contenido; 405 método no permitido con Allow; 404 ruta no existente; 503 almacenamiento no disponible; 500 error interno sanitizado. CORS rechazado puede responder 403 del framework, fuera del formato de errores de negocio. No mostrar stack/SQL ni tomar `detail` como regla de dominio.

`X-Request-Id` correlaciona HTTP/log; `Server-Timing: app;dur=...` mide el backend hasta generar respuesta, no red ni latencia de juego. GET y POST envían Cache-Control no-store. No hay Location ni GET individual en esta entrega. POST no es idempotente: no hacer reintentos automáticos ante timeout; una respuesta perdida no demuestra que no se haya guardado. La deduplicación futura requiere otra decisión/contrato, no inventar Idempotency-Key soportado.

## Integración de desarrollo

El fork de laboratorio ya usa un proxy Vite en desarrollo y `tools/serve-lab.mjs` para la compilación, ambos hacia backend 127.0.0.1:8090. La PWA compilada se abre en http://127.0.0.1:4173/lab.html y llama a `/api/players` por el mismo origen. El upstream Quintana-117/Dise-o_pwa sigue intacto; necesita revisión del mantenedor y una rama develop para recibir el trabajo. Para llamadas entre orígenes, configurar `GORILLA_ALLOWED_ORIGINS=http://localhost:5173` en backend; lista separada por comas de orígenes exactos, sin *, paths ni credenciales. GET/POST/OPTIONS y Content-Type; expone X-Request-Id y Server-Timing, sin cookies/credentials.

Ejemplo mínimo de contrato; el cliente integrado del fork añade límites, validación de respuesta y protección ante envío incierto:

```javascript
const response = await fetch(`${apiBase}/api/players`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ displayName })
});
const payload = await response.json();
if (!response.ok) throw new Error(payload.code ?? `HTTP_${response.status}`);
// Sólo aquí reflejar el jugador confirmado; recargar GET si se necesita la lista.
```

## LAN para laboratorio y destino de producto

Por defecto el backend escucha sólo loopback y no incluye autenticación, TLS ni autorización de operaciones. Para un laboratorio aislado deliberado se puede iniciar con `--server.address=<IPv4-LAN-de-la-PC>` y origen exacto del frontend. Eso expone la API HTTP de prueba a esa interfaz; usar datos ficticios/red confiable, cerrar al terminar y no abrir puertos automáticamente. CORS no es autenticación y curl u otros clientes no están restringidos por CORS. Prueba física LAN: PENDIENTE.

Para producto, conservar backend loopback y añadir una fachada HTTPS autorizada en el Java existente mediante otra tarea revisada. Una PWA HTTPS puede bloquear fetch a HTTP privado por mixed content; configurar CORS no arregla TLS. No modificar DataChannels/IPC ni exponer SQL a LAN. [Mixed content, MDN](https://developer.mozilla.org/en-US/docs/Web/Security/Defenses/Mixed_content).

Hiram necesita: base URL/puerto acordado, origen permitido o proxy, schema del contrato, manejo de carga/vacío/error y respuesta 201, y ejecutar su validación Network/Console/Application con este backend real. La integración React y la recarga offline se verificaron en Chrome de escritorio en el fork; no se certifican integración al upstream, móvil ni LAN física. El borrador IndexedDB no se reenvía automáticamente y no sustituye idempotencia backend.
