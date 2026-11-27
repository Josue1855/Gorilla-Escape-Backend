package com.gorillaescape.backend.players.domain;

import java.time.Instant;
import java.util.UUID;

public record Player(UUID id, String displayName, Instant createdAt) {}
