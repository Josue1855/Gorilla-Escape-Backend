SELECT id, display_name, created_at FROM players ORDER BY created_at, id;
SELECT count(*) AS player_count FROM players;
SELECT version, success FROM flyway_schema_history ORDER BY installed_rank;
PRAGMA integrity_check;
PRAGMA foreign_key_check;
