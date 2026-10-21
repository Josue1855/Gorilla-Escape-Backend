package com.gorillaescape.backend.players.api;

import com.gorillaescape.backend.players.application.PlayerService;
import com.gorillaescape.backend.players.domain.Player;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    private final PlayerService service;
    public PlayerController(PlayerService service) { this.service = service; }

    public record PlayerPage(List<Player> items, int limit, int offset) {}

    @GetMapping
    public PlayerPage list(@RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
                           @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int offset) {
        return new PlayerPage(service.list(limit, offset), limit, offset);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Player create(@Valid @RequestBody CreatePlayer request) { return service.create(request.displayName()); }
}
