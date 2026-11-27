package com.gorillaescape.backend.players.infrastructure;

import com.gorillaescape.backend.players.domain.Player;
import com.gorillaescape.backend.players.domain.PlayerRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPlayerRepository implements PlayerRepository {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcPlayerRepository.class);
    private final JdbcTemplate jdbc;
    public JdbcPlayerRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public void insert(Player player) {
        int rows = jdbc.update("INSERT INTO players(id, display_name, created_at) VALUES (?, ?, ?)",
                player.id().toString(), player.displayName(), player.createdAt().toString());
        if (rows != 1) throw new IllegalStateException("INSERT_ROW_COUNT");
        LOG.info("event=sql_operation operation=players_insert affectedRows={}", rows);
    }

    @Override public List<Player> list(int limit, int offset) {
        return jdbc.query("SELECT id, display_name, created_at FROM players ORDER BY created_at, id LIMIT ? OFFSET ?",
                (rs, n) -> new Player(UUID.fromString(rs.getString("id")),
                        rs.getString("display_name"), Instant.parse(rs.getString("created_at"))), limit, offset);
    }
}
