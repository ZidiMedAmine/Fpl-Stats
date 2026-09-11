package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.GameWeek;
import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.repository.GameWeekRepository;
import com.fpl.stats.repository.PlayerRepository;
import com.fpl.stats.repository.UserPickRepository;
import com.fpl.stats.repository.UserTeamRepository;
import com.fpl.stats.services.fpl.FixtureDataService;
import com.fpl.stats.services.util.FplApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserPickSyncService}, focusing on pick collection and sync-range behaviour.
 */
@ExtendWith(MockitoExtension.class)
class UserPickSyncServiceTest {

    private static final long FPL_TEAM_ID = 1L;
    private static final int PLAYER_FPL_ID = 100;

    @Mock private FplApiClient fplApiClient;
    @Mock private UserPickRepository userPickRepository;
    @Mock private UserTeamRepository userTeamRepository;
    @Mock private GameWeekRepository gameWeekRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private FixtureDataService fixtureDataService;

    private UserPickSyncService userPickSyncService;

    /**
     * Wires up the service with mock dependencies before each test.
     */
    @BeforeEach
    void setUp() {
        userPickSyncService = new UserPickSyncService(
                fplApiClient,
                userPickRepository,
                userTeamRepository,
                gameWeekRepository,
                playerRepository,
                fixtureDataService
        );
    }

    /**
     * When picks exist for all GWs in the sync range, all picks should be saved.
     */
    @Test
    void shouldSavePicksForAllGwsInSyncRange() {
        UserTeam userTeam = userTeamWithState(0, 1);
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(2);
        stubNoChips();
        stubPicksApi(1, List.of(pickData(PLAYER_FPL_ID, 1, 1, false, false)));
        stubPicksApi(2, List.of(pickData(PLAYER_FPL_ID, 2, 1, false, false)));
        when(gameWeekRepository.findByGameWeekNumber(1)).thenReturn(Optional.of(gameWeek(1)));
        when(gameWeekRepository.findByGameWeekNumber(2)).thenReturn(Optional.of(gameWeek(2)));
        when(playerRepository.findByFplId(PLAYER_FPL_ID)).thenReturn(Optional.of(player()));

        userPickSyncService.syncUserPicks(userTeam);

        ArgumentCaptor<List<UserPick>> captor = ArgumentCaptor.captor();
        verify(userPickRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    /**
     * When the team is already up to date, no picks should be saved and
     * the lastSyncedGameWeek should still be updated.
     */
    @Test
    void shouldReturnEarlyAndUpdateLastSyncedGwWhenAlreadyUpToDate() {
        UserTeam userTeam = userTeamWithState(3, 1);
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(3);

        userPickSyncService.syncUserPicks(userTeam);

        verify(userPickRepository, never()).saveAll(any());
        verify(userTeamRepository).save(userTeam);
        assertThat(userTeam.getLastSyncedGameWeek()).isEqualTo(3);
    }

    /**
     * When the triple captain chip is active in a GW, the captain pick in that GW
     * should have isTripleCaptain set to true.
     */
    @Test
    void shouldMarkCaptainAsTripleCaptainWhenChipActiveInThatGw() {
        UserTeam userTeam = userTeamWithState(0, 1);
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(1);
        stubChips(List.of(Map.of("name", "3xc", "event", 1)));
        stubPicksApi(1, List.of(pickData(PLAYER_FPL_ID, 1, 3, true, false)));
        when(gameWeekRepository.findByGameWeekNumber(1)).thenReturn(Optional.of(gameWeek(1)));
        when(playerRepository.findByFplId(PLAYER_FPL_ID)).thenReturn(Optional.of(player()));

        userPickSyncService.syncUserPicks(userTeam);

        ArgumentCaptor<List<UserPick>> captor = ArgumentCaptor.captor();
        verify(userPickRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).isTripleCaptain()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link UserTeam} with the given lastSyncedGameWeek and startedEvent.
     *
     * @param lastSyncedGw the last gameweek that was already synced
     * @param startedEvent the gameweek in which the team was created
     * @return a configured user team
     */
    private UserTeam userTeamWithState(int lastSyncedGw, int startedEvent) {
        UserTeam userTeam = new UserTeam();
        userTeam.setFplTeamId(FPL_TEAM_ID);
        userTeam.setLastSyncedGameWeek(lastSyncedGw);
        userTeam.setStartedEvent(startedEvent);
        return userTeam;
    }

    /**
     * Stubs the history endpoint to return no chips (triple captain not played).
     */
    private void stubNoChips() {
        stubChips(List.of());
    }

    /**
     * Stubs the history endpoint to return the given chip activations.
     *
     * @param chips list of chip activation maps
     */
    private void stubChips(List<Map<String, Object>> chips) {
        when(fplApiClient.get("/entry/" + FPL_TEAM_ID + "/history/"))
                .thenReturn(Map.of("chips", chips));
    }

    /**
     * Stubs the picks endpoint for a specific gameweek.
     *
     * @param gw       the gameweek number
     * @param picksData the list of pick data maps to return
     */
    private void stubPicksApi(int gw, List<Map<String, Object>> picksData) {
        when(fplApiClient.get("/entry/" + FPL_TEAM_ID + "/event/" + gw + "/picks/"))
                .thenReturn(Map.of("picks", picksData));
    }

    /**
     * Builds a minimal pick data map.
     *
     * @param playerFplId   the FPL player element ID
     * @param position      the squad position (1–15; &gt;11 means benched)
     * @param multiplier    the captain multiplier (1, 2, or 3)
     * @param captain       whether this pick is the captain
     * @param viceCaptain   whether this pick is the vice-captain
     * @return a map matching the FPL picks API structure
     */
    private Map<String, Object> pickData(int playerFplId, int position, int multiplier,
                                         boolean captain, boolean viceCaptain) {
        return Map.of(
                "element", playerFplId,
                "position", position,
                "multiplier", multiplier,
                "is_captain", captain,
                "is_vice_captain", viceCaptain
        );
    }

    /**
     * Creates a {@link GameWeek} entity for the given number.
     *
     * @param number the gameweek number
     * @return a gameweek entity
     */
    private GameWeek gameWeek(int number) {
        GameWeek gw = new GameWeek();
        gw.setGameWeekNumber(number);
        return gw;
    }

    /**
     * Creates a minimal {@link Player} entity with a known FPL ID.
     *
     * @return a player with {@code fplId = PLAYER_FPL_ID}
     */
    private Player player() {
        Player player = new Player();
        player.setFplId(PLAYER_FPL_ID);
        return player;
    }
}
