# Matriz de riesgos de persistencia autocontenida

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

Probabilidad/impacto son valoración de ingeniería previa, no frecuencia observada. Todos los ensayos nuevos están PENDIENTE DE EJECUCIÓN.

| ID | Riesgo | Prob. / impacto | Mitigación y gate / rol propuesto |
|---|---|---|---|
| R01 | Sobrescribir datos al actualizar o cambiar cwd | Media / crítico | Directorio estable por usuario, nunca dentro del paquete; upgrade conserva IDs. Packaging/Backend |
| R02 | Pérdida/corrupción por backup .db con WAL activo | Media / crítico | Snapshot/API consistente y restauración verificada; preservar WAL. Backend/QA |
| R03 | Escritura concurrente larga, SQLITE_BUSY o cola sin límite | Media / alto | Escritor serializado, transacciones cortas/deadlines y backpressure; carga 1–4 clientes. Backend |
| R04 | Driver/JNI no carga o core vulnerable en WAL | Media / alto | Fijar versión real/hash/nativos; probar noexec/ACL/AV; rechazar core sin corrección WAL-reset. Packaging/QA |
| R05 | Backend descendiente queda vivo si padre muere/congela | Media / alto | Contención específica de árbol y fault injection; EOF no basta para congelados. Lifecycle |
| R06 | Migración bloquea startup o supera cierre 10 s actual | Media / crítico | Readiness separado; auditoría antes de tocar supervisor; budgets medidos. Unity/Java/Backend |
| R07 | Prompts administrativos en cada arranque por firewall | Media / alto | Preparación en instalador único y reparación explícita; probar arranques repetidos estándar. Packaging/Red |
| R08 | Exposición SQL/API o token de operador al teléfono | Baja / crítico | SQLite sin puerto; API sólo loopback y fachada autorizada; escaneo LAN y pruebas negativas. Seguridad/QA |
| R09 | Doble autoridad de puntos o duplicados tras respuesta perdida | Media / alto | Unity autoritativo, publicación autenticada, receipt/resultado atómicos y UNIQUE. Unity/Backend |
| R10 | Rúbrica requiere PG tras implementación | Desconocida / alto | PO indicó motor no especificado; confirmar docente y registrar LAB real antes de aceptar. PO/QA |
| R11 | Parches/avisos/licencias incompletos | Media / alto | SBOM y procedencia de todos los componentes; no tratar driver como sólo SQLite core. Packaging |
| R12 | Se anuncia Windows/Linux sin build/hardware real | Media / alto | Matriz instalador/ABI/nativos/gráficos/cámara/soak, releases separadas. QA |
| R13 | Disco lleno, FS remoto o corrupción física | Media / crítico | Espacio/errores visibles, copias verificadas, no reset silencioso, exportación externa. Backend |
| R14 | Segunda JVM consume memoria e impacta frame | Media / alto | Medición de paquete completo; presupuestos por host antes de fijar topología final. Performance |
| R15 | Auto-recovery borra datos o mata procesos ajenos | Baja / crítico | Identidad/lock/generación; no reinit; restablecimiento explícito; no kill por nombre. Lifecycle |

Riesgo residual: permisos de usuario no protegen frente a administrador/proceso malicioso del mismo usuario; backup mismo disco no cubre pérdida física. Si el modelo de amenaza exige cifrado/aislamiento mayor, nuevo diseño y medidas antes de distribuir. Referencias y aceptación: [fuentes](SOURCES.md), [matriz](ACCEPTANCE_MATRIX.md).
