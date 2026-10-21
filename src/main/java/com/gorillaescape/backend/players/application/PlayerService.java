package com.gorillaescape.backend.players.application;

import com.gorillaescape.backend.players.domain.Player;
import com.gorillaescape.backend.players.domain.PlayerRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {
    private final PlayerRepository repository;
    public PlayerService(PlayerRepository repository) { this.repository = repository; }

    @Transactional
    public Player create(String displayName) {
        var player = new Player(UUID.randomUUID(), displayName, Instant.now());
        repository.insert(player);
        return player;
    }

    @Transactional(readOnly = true)
    public List<Player> list(int limit, int offset) { return repository.list(limit, offset); }
}
