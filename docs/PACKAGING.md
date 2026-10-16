# Empaquetado autocontenido e instalación única

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

## Contrato de distribución

Un instalador por SO/arquitectura que prepara el juego y todos sus componentes. Ningún paso pide al jugador instalar Docker, PostgreSQL/SQLite CLI, pgAdmin, Java, Spring Boot, Python u otro SDK por separado. Los comandos de build/JDK/migrador sólo pertenecen al equipo. En ejecución no se descargan binarios, esquemas ni assets desde Internet.

Manifest futuro: versión de producto y schema mínimo/máximo, OS/arquitectura/ABI, ruta/hash/tamaño de Unity, JRE, Java técnico, API/JDBC/nativos, PWA, Vision opcional, licencias/SBOM y firma/procedencia del paquete. JRE común sólo si ambas JVM son compatibles con la versión y módulos probados; no asumir que un jlink actual ya sirve para la API futura. Backend se distribuye como librería/runtime del juego, no una instalación Spring Boot administrada por el jugador.

Estructura conceptual, no carpetas creadas ni binarios existentes:

```text
GorillaEscape/<version>/
  Unity/
  Runtime/Java/
  Services/connectivity.jar
  Services/backend.jar
  Web/
  Vision/                 si la distribución lo requiere
  Native/<os-arch>/        cuando el empaquetado lo necesite
  Licenses/
  runtime-manifest.json
```

Los datos NO van en esa carpeta. [Persistencia](PERSISTENCE.md) define directorio estable por usuario. El instalador/actualizador conserva datos/backups por defecto y solicita acción explícita para eliminarlos; no copiar bases reales en un artefacto de release.

## Plataformas

| Objetivo inicial propuesto | Riesgos a resolver y gate |
|---|---|
| Windows x86_64, usuario estándar | Firma/SmartScreen, paths Unicode, extracción JNI/antivirus, ACL, librerías de runtime, locks y contención de descendientes |
| Linux x86_64, usuario normal | glibc/ABI mínima, gráficos/cámara, permisos de datos, librerías JNI y temp noexec, UFW/nftables y supervisor |
| ARM64 u otras distribuciones | Futuro no confirmado: no publicar paquete sin binarios y tests de toda la cadena |

No se promete compatibilidad Windows/Linux porque SQLite tenga formato portable: Unity, JRE, Vision, nativos e instalador también deben pasar. jpackage puede empaquetar aplicaciones Java con runtime, pero no resuelve por sí solo un instalador de Unity; evaluar launcher/instalador del producto en cada SO. [JDK packaging](https://docs.oracle.com/en/java/javase/21/jpackage/packaging-overview.html).

## Privilegios y firewall

Instalación por usuario cuando sea viable. Si el SO requiere elevación para preparar firewall/ubicación global, pedirla una vez durante el instalador único, nunca ejecutar Unity/JVM como administrador. La base SQLite y la API loopback no necesitan reglas LAN nuevas. Conservar los puertos y restricciones actuales del transporte.

Brecha comprobada: el helper actual puede requerir autorización para primera inspección UFW protegida o cambio de red/estado. La evaluación no certifica que hoy cumpla ejecución sin elevación. El instalador debe preparar/verificar reglas mínimas y recibos con identidad estable. Cambios posteriores de red/política usan reparación explícita fuera del arranque normal; no insertar prompts por comprobación rutinaria. Si la política del SO obliga a elevar cada ejecución, el paquete no supera requisito 4. Sin cambios al helper en este encargo.

## Actualizar sin perder datos

Preparar versión nueva en directorio paralelo; verificar firmas/hash y espacio para backup/migración. Detener instancias propias, preservar base/backups, migrar con exclusión y activar sólo tras validación. Fallo mantiene versión previa y snapshot consistente; no arrancar binario viejo contra schema nuevo incompatible. Sin Internet, actualizaciones mediante instalador completo local/USB; aplicar el mismo control de procedencia.

Licencias: SQLite core dominio público; Xerial y dependencias/nativos conservan sus propias licencias/avisos. JRE, Unity, Vision/modelos y terceros requieren inventario propio; la elección SQLite no certifica toda redistribución. No ofrecer pgAdmin, Docker ni instaladores de bases de datos como requisito del usuario. [SQLite copyright](https://sqlite.org/copyright.html), [Xerial](https://github.com/xerial/sqlite-jdbc).

Medición obligatoria futura: [tamaño final y protocolo](DATABASE_COMPARISON.md#tamaño-no-inventar-una-medición). No hay instalador nuevo, tamaño final ni tests de PC limpia generados por este PR.
