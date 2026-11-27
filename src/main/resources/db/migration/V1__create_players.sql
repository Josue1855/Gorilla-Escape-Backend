CREATE TABLE players (
    id TEXT PRIMARY KEY NOT NULL CHECK(length(id) = 36),
    display_name TEXT NOT NULL CHECK(length(trim(display_name)) BETWEEN 1 AND 40),
    created_at TEXT NOT NULL
) STRICT;
CREATE INDEX players_order ON players(created_at, id);
