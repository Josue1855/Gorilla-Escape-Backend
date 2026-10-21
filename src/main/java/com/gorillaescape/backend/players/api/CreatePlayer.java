package com.gorillaescape.backend.players.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.text.Normalizer;

public record CreatePlayer(
        @NotBlank @Size(max = 40)
        @Pattern(regexp = "[\\p{L}\\p{M}\\p{N} _.'-]+") String displayName) {
    public CreatePlayer {
        if (displayName != null) displayName = Normalizer.normalize(displayName.strip(), Normalizer.Form.NFC);
    }
}
