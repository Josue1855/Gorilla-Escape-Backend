# Matriz de los doce requisitos y pruebas futuras

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

Todos los casos siguientes: **PENDIENTE DE EJECUCIÓN**. El diseño los cubre por intención; no certifica un instalador funcional.

| Req. / caso | Diseño y procedimiento de aceptación | Evidencia que debe producirse |
|---|---|---|
| 1 — AC-01 Instalación única | En PC limpia, instalar un artefacto y lanzar Unity; API/BD incluidos inician sin pasos externos | Versión/hash/entorno, registro de instalación/primer arranque |
| 2 — AC-02 Sin instalaciones manuales | Sin Java/Spring/SQL/Python/SDK en PATH del usuario; paquete resuelve todo localmente | Inventario SO y manifiesto de runtimes/procedencia; sin descargas runtime |
| 3 — AC-03 Sin Docker | Host sin Docker/daemon; instalación y sesiones completas | Procesos/dependencias y sesión persistida, sin contenedor |
| 4 — AC-04 Sin admin normal | Instalar con permisos necesarios una vez, ejecutar >=20 arranques/cierres como usuario estándar | Cero elevaciones rutinarias, sin servicios globales requeridos; casos firewall separados |
| 5 — AC-05 LAN sin Internet | WAN desconectada, LAN activa: 1–4 teléfonos, control RTC y GET/POST local | Ruta LAN, ausencia de accesos WAN requeridos, IDs/filas antes/después |
| 6 — AC-06 Datos/actualización | Crear sesiones/resultados, cerrar/reabrir, actualizar N→N+1 y reinstalar conservando datos | Mismos IDs/recibos, schema esperado, backups y conteos; no BD recreada |
| 7 — AC-07 Cierre limpio | Normal/ventana/quit: drenar API, cerrar DB y todos los hijos propios dentro del budget | Exit code/tiempos/locks liberados y sin procesos residuales; forzado separado |
| 8 — AC-08 Recuperación | Kill de cada proceso en BEGIN/escritura/COMMIT/respuesta/migración; base dañada, disco lleno y padre congelado | Integridad/FK, sin duplicados, error explícito y restauración validada; nunca reset automático |
| 9 — AC-09 Windows/Linux | Repetir instalación, carga nativa, persistencia/cierre/upgrade en objetivos soportados reales | Artefacto y hardware por OS/arquitectura/ABI; no extrapolar soporte |
| 10 — AC-10 Sin PostgreSQL LAN | SQLite: ningún listener de BD; alternativa PG: socket privado/loopback probado desde otro host | Listeners IPv4/IPv6 + pruebas negativas LAN; no limitarse a archivo config |
| 11 — AC-11 DB/backend sólo local | Fichero local sólo API; o PG local autenticado; ninguna ruta de fichero/SQL al cliente | ACL/directorio/endpoint, rechazo acceso no autorizado y LAN |
| 12 — AC-12 Transporte preservado | Corpus/IPC/lifecycle/QR/firewall/RTC existentes con backend sano, lento y caído | Checks previos como baseline y nuevas regresiones; sin cambio de slot/epoch por persistencia |

Casos adicionales: replay concurrente de misma clave y clave con payload distinto; migración fallida/downgrade incompatible; recovery con WAL y core seguro; backup/restore sin herramientas externas; borrado de caché PWA no borra BD PC; secretos ausentes en logs/CLI; escaneo de rutas business no publicadas; shadow DB por ruta incorrecta rechazada.

Laboratorio: esquema y datos antes/después; scripts de setup/seed/cleanup aislados; GET renderizado, POST durable único; logs HTTP/duración/JVM y SQL diferenciados; Network/Application/SW/cache/IndexedDB sólo si existe; capturas auténticas. Repetir (a) WAN caída con LAN y backend vivos, (b) LAN viva con backend inaccesible. La BD local no implementa por sí misma IndexedDB/cola del teléfono.

Ficha por prueba: runId, UTC/zona, repo/branch/commit, instalador/hash, SO/hardware/navegador, librería SQLite real/driver/JRE, schema, datos iniciales, pasos, esperado, observado, N/fallos, artefactos sanitizados y limitaciones. Tamaño/RSS/latencias/tiempos de recovery: NO MEDIDO hasta ejecutar. Criterios de rendimiento numéricos se acuerdan antes del spike y conservan gates existentes; no inventar resultados.
