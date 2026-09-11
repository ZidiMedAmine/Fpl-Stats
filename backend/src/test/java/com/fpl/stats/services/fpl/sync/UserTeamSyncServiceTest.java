package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.domain.UserTeamRankHistory;
import com.fpl.stats.repository.UserTeamRankHistoryRepository;
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
 * Unit tests for {@link UserTeamSyncService}, focusing on rank history sync behaviour.
 */
@ExtendWith(MockitoExtension.class)
class UserTeamSyncServiceTest {

    private static final long FPL_TEAM_ID = 1L;

    @Mock private UserTeamRankHistoryRepository userTeamRankHistoryRepository;
    @Mock private UserTeamRepository userTeamRepository;
    @Mock private FixtureDataService fixtureDataService;
    @Mock private FplApiClient fplApiClient;

    private UserTeamSyncService userTeamSyncService;

    /**
     * Wires up the service with mock dependencies before each test.
     */
    @BeforeEach
    void setUp() {
        userTeamSyncService = new UserTeamSyncService(
                userTeamRankHistoryRepository,
                userTeamRepository,
                fixtureDataService,
                fplApiClient
        );
    }

    /**
     * When there are no existing rank history entries, all finished GWs should be saved.
     */
    @Test
    void shouldSaveAllFinishedGwsWhenNoHistoryExists() {
        stubEntryApi();
        stubHistoryApi(List.of(gwData(1), gwData(2), gwData(3)), List.of());
        stubUserTeamRepository();
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(3);
        when(userTeamRankHistoryRepository
                .findAllByUserTeam_FplTeamIdOrderByGameWeekAsc(FPL_TEAM_ID))
                .thenReturn(List.of());

        userTeamSyncService.syncUserTeam(FPL_TEAM_ID);

        ArgumentCaptor<List<UserTeamRankHistory>> captor = ArgumentCaptor.captor();
        verify(userTeamRankHistoryRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(3)
                .extracting(UserTeamRankHistory::getGameWeek)
                .containsExactly(1, 2, 3);
    }

    /**
     * When older GWs already exist, only the latest GW should be refreshed and older ones skipped.
     */
    @Test
    void shouldOnlyRefreshLatestGwWhenOlderGwsAlreadyExist() {
        stubEntryApi();
        stubHistoryApi(List.of(gwData(1), gwData(2), gwData(3)), List.of());
        stubUserTeamRepository();
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(3);

        UserTeamRankHistory gw1Entry = rankHistoryEntry(1);
        UserTeamRankHistory gw2Entry = rankHistoryEntry(2);
        UserTeamRankHistory gw3Entry = rankHistoryEntry(3);
        when(userTeamRankHistoryRepository
                .findAllByUserTeam_FplTeamIdOrderByGameWeekAsc(FPL_TEAM_ID))
                .thenReturn(List.of(gw1Entry, gw2Entry, gw3Entry));
        when(userTeamRankHistoryRepository
                .findByUserTeam_FplTeamIdAndGameWeek(FPL_TEAM_ID, 3))
                .thenReturn(Optional.of(gw3Entry));

        userTeamSyncService.syncUserTeam(FPL_TEAM_ID);

        ArgumentCaptor<List<UserTeamRankHistory>> captor = ArgumentCaptor.captor();
        verify(userTeamRankHistoryRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1)
                .extracting(UserTeamRankHistory::getGameWeek)
                .containsExactly(3);
    }

    /**
     * When all API GWs are above the last completed GW, nothing should be saved.
     */
    @Test
    void shouldNotSaveAnythingWhenNoGwsAreFinished() {
        stubEntryApi();
        stubHistoryApi(List.of(gwData(4)), List.of());
        stubUserTeamRepository();
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(3);

        userTeamSyncService.syncUserTeam(FPL_TEAM_ID);

        verify(userTeamRankHistoryRepository, never()).saveAll(any());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Stubs the FPL {@code /entry/{id}/} endpoint with minimal valid data.
     */
    private void stubEntryApi() {
        when(fplApiClient.get("/entry/" + FPL_TEAM_ID + "/")).thenReturn(Map.of(
                "player_first_name", "John",
                "player_last_name", "Doe",
                "name", "My Team",
                "player_region_name", "England"
        ));
    }

    /**
     * Stubs the FPL {@code /entry/{id}/history/} endpoint with the supplied GW and chip data.
     *
     * @param currentGws list of per-GW data maps
     * @param chips      list of chip activation maps
     */
    private void stubHistoryApi(List<Map<String, Object>> currentGws,
                                List<Map<String, Object>> chips) {
        when(fplApiClient.get("/entry/" + FPL_TEAM_ID + "/history/")).thenReturn(Map.of(
                "current", currentGws,
                "chips", chips
        ));
    }

    /**
     * Stubs {@link UserTeamRepository} to act as if the team is new and returns
     * the saved entity unchanged.
     */
    private void stubUserTeamRepository() {
        when(userTeamRepository.findByFplTeamId(FPL_TEAM_ID)).thenReturn(Optional.empty());
        when(userTeamRepository.save(any())).thenAnswer(inv -> {
            UserTeam team = inv.getArgument(0);
            team.setFplTeamId(FPL_TEAM_ID);
            return team;
        });
    }

    /**
     * Builds a minimal per-GW data map for the given gameweek number.
     *
     * @param gw the gameweek number
     * @return a map matching the structure of the FPL history {@code current} array entries
     */
    private Map<String, Object> gwData(int gw) {
        return Map.of(
                "event", gw,
                "overall_rank", 1000,
                "points", 60,
                "rank", 5000,
                "total_points", gw * 60,
                "bank", 5,
                "value", 1005,
                "event_transfers", 1,
                "points_on_bench", 4
        );
    }

    /**
     * Creates a {@link UserTeamRankHistory} stub for the given gameweek.
     *
     * @param gw the gameweek number
     * @return a rank history entity with only the gameweek set
     */
    private UserTeamRankHistory rankHistoryEntry(int gw) {
        UserTeamRankHistory entry = new UserTeamRankHistory();
        entry.setGameWeek(gw);
        return entry;
    }
}
