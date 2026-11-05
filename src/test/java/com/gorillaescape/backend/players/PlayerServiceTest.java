package com.gorillaescape.backend.players;

import com.gorillaescape.backend.players.application.PlayerService;
import com.gorillaescape.backend.players.domain.Player;
import com.gorillaescape.backend.players.domain.PlayerRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService: findAll y create sin base de datos")
class PlayerServiceTest {
    @Mock
    private PlayerRepository repository;

    @InjectMocks
    private PlayerService service;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Jugador Uno", Instant.parse("2026-01-01T12:00:00Z"));
    }

    @Test
    @DisplayName("findAll devuelve los jugadores completos en el orden del repositorio")
    void findAllReturnsPlayersInRepositoryOrder() {
        // Given: el repositorio simula una página con dos jugadores.
        var second = new Player(UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Jugador Dos", Instant.parse("2026-01-02T12:00:00Z"));
        given(repository.list(10, 20)).willReturn(List.of(player, second));

        // When
        var result = service.findAll(10, 20);

        // Then: contenido, orden y paginación, no sólo el tamaño.
        assertThat(result).containsExactly(player, second);
        verify(repository, times(1)).list(10, 20);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("findAll devuelve una lista vacía cuando no hay jugadores")
    void findAllReturnsEmptyPage() {
        given(repository.list(50, 0)).willReturn(List.of());

        var result = service.findAll(50, 0);

        assertThat(result).isNotNull().isEmpty();
        verify(repository, times(1)).list(50, 0);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("findAll propaga el error de almacenamiento sin simular una lista vacía")
    void findAllPropagatesStorageFailure() {
        var failure = new DataAccessResourceFailureException("fixture private detail");
        given(repository.list(50, 0)).willThrow(failure);

        assertThatThrownBy(() -> service.findAll(50, 0)).isSameAs(failure);

        verify(repository, times(1)).list(50, 0);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("create genera identidad y fecha y devuelve exactamente el jugador persistido")
    void createPersistsPlayerWithServerIdentityAndTimestamp() {
        // Given: insert es void; Mockito simula correctamente la inserción por defecto.
        var start = Instant.now();

        // When
        var result = service.create("O'Brien");
        var end = Instant.now();

        // Then: capturamos el objeto enviado al repositorio.
        var captured = ArgumentCaptor.forClass(Player.class);
        verify(repository, times(1)).insert(captured.capture());
        assertThat(result).isSameAs(captured.getValue());
        assertThat(result.id()).isNotNull();
        assertThat(result.displayName()).isEqualTo("O'Brien");
        assertThat(result.createdAt()).isBetween(start, end);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("create asigna identidades distintas incluso con el mismo nombre")
    void createAssignsIndependentIdentities() {
        var first = service.create(player.displayName());
        var second = service.create(player.displayName());

        assertThat(first.id()).isNotNull().isNotEqualTo(second.id());
        verify(repository, times(1)).insert(first);
        verify(repository, times(1)).insert(second);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("create propaga el error de inserción y no devuelve un éxito aparente")
    void createPropagatesStorageFailure() {
        var failure = new DataAccessResourceFailureException("fixture private detail");
        doThrow(failure).when(repository).insert(any(Player.class));

        assertThatThrownBy(() -> service.create("Persona")).isSameAs(failure);

        verify(repository, times(1)).insert(any(Player.class));
        verifyNoMoreInteractions(repository);
    }
}
