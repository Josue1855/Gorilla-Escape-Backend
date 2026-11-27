package com.gorillaescape.backend.players;

import com.gorillaescape.backend.players.api.CreatePlayer;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CreatePlayerValidationTest {
    @Test void trimsAndNormalizesUnicodeBeforeValidation() {
        var request = new CreatePlayer("  Jose\u0301  ");
        assertEquals("José", request.displayName());
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request).isEmpty());
        }
    }

    @Test void rejectsMissingBlankOversizeAndControlCharacters() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            for (String name : new String[]{null, "", "   ", "A".repeat(41), "Persona\nOtra", "Persona\u200BOtra"}) {
                assertFalse(factory.getValidator().validate(new CreatePlayer(name)).isEmpty());
            }
        }
    }
}
