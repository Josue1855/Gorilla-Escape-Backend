package com.gorillaescape.backend.players;

import com.gorillaescape.backend.players.application.PlayerService;
import com.gorillaescape.backend.players.domain.PlayerRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerServiceTest {
    @Test void generatesServerIdentityAndPreservesTheNameWhenPersisting() {
        var repository = mock(PlayerRepository.class);
        var start = Instant.now();
        var result = new PlayerService(repository).create("O'Brien");
        assertNotNull(result.id());
        assertFalse(result.createdAt().isBefore(start));
        assertEquals("O'Brien", result.displayName());
        verify(repository).insert(result);
    }

    @Test void storageFailureCannotReturnAnApparentSuccess() {
        var repository = mock(PlayerRepository.class);
        doThrow(new DataAccessResourceFailureException("fixture private detail")).when(repository).insert(any());
        assertThrows(DataAccessResourceFailureException.class, () -> new PlayerService(repository).create("Persona"));
    }
}
