-- Sólo datos ficticios en una base aislada de laboratorio, después de las migraciones.
INSERT OR IGNORE INTO players(id, display_name, created_at)
VALUES ('00000000-0000-4000-8000-000000000001', 'Persona Semilla Uno', '2026-10-16T00:00:00Z');
INSERT OR IGNORE INTO players(id, display_name, created_at)
VALUES ('00000000-0000-4000-8000-000000000002', 'Persona Semilla Dos', '2026-10-16T00:00:00Z');
