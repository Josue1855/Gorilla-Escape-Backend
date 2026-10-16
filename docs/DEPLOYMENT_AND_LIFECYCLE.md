# Despliegue, arranque y cierre

Última revisión: 2026-10-16. Alcance: evaluación y diseño; ninguna funcionalidad implementada. Fuentes primarias y revisiones locales en [SOURCES.md](SOURCES.md).

## Despliegue objetivo con SQLite

Flechas continuas: relación existente auditada; discontinuas: integración propuesta. Todas las cajas de API/SQLite son diseño, no artefactos disponibles. La instalación única incluye los runtimes necesarios; el jugador no instala Java, Spring Boot, Docker ni herramientas SQL.

```mermaid
flowchart LR
  subgraph mobile ["Teléfonos en LAN"]
    pwa["PWA / UI móvil"]
  end
  subgraph host ["PC del juego: usuario normal"]
    unity["Unity: aplicación principal"]
    java["Java técnico existente / JRE incluido"]
    api["API Spring Boot local propuesta / JVM propia"]
    sqlite["SQLite JDBC dentro de la API"]
    data["Directorio persistente privado del usuario"]
    vision["Vision incluido si se usa"]
  end
  pwa <-->|"HTTPS bootstrap y RTC LAN existentes"| java
  unity <-->|"IPC loopback existente"| java
  unity -->|"Supervisión existente"| vision
  java -.->|"Nuevo lifecycle auditado"| api
  java -.->|"Fachada REST futura autorizada"| api
  unity -.->|"Resultado autoritativo por adaptador autorizado"| api
  api -.-> sqlite
  sqlite -.-> data
```

La arista Unity→API expresa responsabilidad funcional, no un endpoint ya existente ni credencial resuelta. Puerto API sólo loopback; SQLite no crea puerto BD. La fachada HTTP del teléfono termina en Java y debe autorizar sólo operaciones de dominio aprobadas, sin compartir credenciales de operador. El tráfico táctil/sensor sigue por RTC/IPC y no entra al SQL por lectura.

## Arranque propuesto

```mermaid
sequenceDiagram
  participant U as Unity
  participant J as Java tecnico existente
  participant B as API local propuesta
  participant D as SQLite local
  U->>J: Arranque supervisado existente
  J-->>U: READY y PONG dentro de presupuesto existente
  J->>B: Arranque asincrono, identidad y canal privado
  B->>B: Lock de instancia, rutas/ACL y version
  B->>D: Abrir y recuperar transacciones si procede
  B->>D: Backup previo y migracion exclusiva
  B->>D: Verificar schema, FK y prueba de escritura
  alt Datos y version compatibles
    B-->>J: PERSISTENCE_READY autenticado
    J-->>U: Estado de persistencia por contrato futuro
  else Fallo, datos nuevos incompatibles o sin espacio
    B-->>J: Error sanitizado, no borrar ni recrear
    J-->>U: Persistencia no disponible, transporte conservado
  end
```

Nueva instalación: crear fichero vacío únicamente cuando no existen datos ni estado previo. Datos presentes y apertura fallida no equivalen a primer arranque. El chequeo de escritura usa una transacción de prueba controlada/rollback, no inserta un jugador fantasma. El lock de aplicación no sustituye locks SQLite.

## Cierre propuesto

```mermaid
sequenceDiagram
  participant U as Unity
  participant J as Java tecnico existente
  participant B as API local propuesta
  participant D as SQLite local
  U->>U: Detener nuevas acciones/resultados oficiales
  U->>J: Cierre por lifecycle existente
  J->>B: Rechazar nuevos writes y drenar trabajo acotado
  B->>D: COMMIT o ROLLBACK de cada transaccion
  B->>D: Checkpoint seguro si aplica y cerrar conexiones
  B-->>J: Exit observado y lock liberado
  J-->>U: Exit Java dentro del deadline existente
  U->>U: Cerrar UI y comprobar hijos propios
```

Checkpoint incompleto no autoriza borrar WAL. Si cierre excede presupuesto, registrar cierre forzado y conservar archivos recuperables; siguiente arranque valida estado. Forzado no se etiqueta cierre limpio. Kill abrupto en cualquier punto requiere los casos de [aceptación](ACCEPTANCE_MATRIX.md); no hay PASS de estas secuencias. [Auditoría del supervisor](adr/SUPERVISOR_AUDIT.md).
