package com.gorillaescape.backend.players.domain;

import java.util.List;

public interface PlayerRepository {
    void insert(Player player);
    List<Player> list(int limit, int offset);
}
