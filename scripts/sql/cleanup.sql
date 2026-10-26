-- :created_player_id es el UUID confirmado por el POST de ESTA ejecución.
-- Si es NULL, sólo se eliminan los dos registros semilla; nunca se usa DELETE global.
DELETE FROM players
WHERE id IN ('00000000-0000-4000-8000-000000000001', '00000000-0000-4000-8000-000000000002')
   OR id = :created_player_id;
