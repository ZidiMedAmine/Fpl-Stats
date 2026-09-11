package com.fpl.stats.services.impl;

import com.fpl.stats.domain.TrackedTeam;
import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.repository.TrackedTeamRepository;
import com.fpl.stats.repository.UserPickRepository;
import com.fpl.stats.repository.UserTeamRepository;
import com.fpl.stats.services.fpl.FixtureDataService;
import com.fpl.stats.services.fpl.sync.PlayerHistorySyncService;
import com.fpl.stats.services.fpl.sync.UserPickSyncService;
import com.fpl.stats.services.fpl.sync.UserTeamSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserSyncServiceImpl}, focusing on the sync-skip logic.
 */
@ExtendWith(MockitoExtension.class)
class UserSyncServiceImplTest {

    @Mock private PlayerHistorySyncService playerHistorySyncService;
    @Mock private TrackedTeamRepository trackedTeamRepository;
    @Mock private UserTeamRepository userTeamRepository;
    @Mock private FixtureDataService fixtureDataService;
    @Mock private UserTeamSyncService userTeamSyncService;
    @Mock private UserPickSyncService userPickSyncService;
    @Mock private UserPickRepository userPickRepository;

    private UserSyncServiceImpl userSyncService;

    /**
     * Sets up the service under test with mock dependencies before each test.
     */
    @BeforeEach
    void setUp() {
        userSyncService = new UserSyncServiceImpl(
                playerHistorySyncService,
                trackedTeamRepository,
                userTeamRepository,
                fixtureDataService,
                userTeamSyncService,
                userPickSyncService,
                userPickRepository
        );
    }

    /**
     * When the team's last synced gameweek equals the last completed gameweek,
     * no sync should be performed — GW4 is in progress but GW3 is already synced.
     */
    @Test
    void shouldSkipSyncWhenAlreadySyncedForLastCompletedGameWeek() {
        long fplTeamId = 1L;
        TrackedTeam trackedTeam = trackedTeamWithId(fplTeamId);
        UserTeam userTeam = userTeamWithLastSyncedGw(fplTeamId, 3);

        when(trackedTeamRepository.findByFplTeamId(fplTeamId)).thenReturn(Optional.of(trackedTeam));
        when(userTeamRepository.findByFplTeamId(fplTeamId)).thenReturn(Optional.of(userTeam));
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(3);

        userSyncService.syncUser(fplTeamId);

        verify(userTeamSyncService, never()).syncUserTeam(fplTeamId);
    }

    /**
     * When the team's last synced gameweek is behind the last completed gameweek,
     * a full sync should proceed.
     */
    @Test
    void shouldSyncWhenLastSyncedGameWeekIsBehindLastCompleted() {
        long fplTeamId = 1L;
        TrackedTeam trackedTeam = trackedTeamWithId(fplTeamId);
        UserTeam syncedTeam = userTeamWithLastSyncedGw(fplTeamId, 3);

        when(trackedTeamRepository.findByFplTeamId(fplTeamId)).thenReturn(Optional.of(trackedTeam));
        when(userTeamRepository.findByFplTeamId(fplTeamId)).thenReturn(Optional.of(userTeamWithLastSyncedGw(fplTeamId, 3)));
        when(fixtureDataService.getLastCompletedGameWeek()).thenReturn(4);
        when(userTeamSyncService.syncUserTeam(fplTeamId)).thenReturn(syncedTeam);
        when(userPickRepository.findDistinctPlayersByUserTeam(syncedTeam)).thenReturn(List.of());

        userSyncService.syncUser(fplTeamId);

        verify(userTeamSyncService).syncUserTeam(fplTeamId);
        verify(userPickSyncService).syncUserPicks(syncedTeam);
    }

    private TrackedTeam trackedTeamWithId(long fplTeamId) {
        TrackedTeam trackedTeam = new TrackedTeam();
        trackedTeam.setFplTeamId(fplTeamId);
        return trackedTeam;
    }

    private UserTeam userTeamWithLastSyncedGw(long fplTeamId, int lastSyncedGameWeek) {
        UserTeam userTeam = new UserTeam();
        userTeam.setFplTeamId(fplTeamId);
        userTeam.setLastSyncedGameWeek(lastSyncedGameWeek);
        return userTeam;
    }
}
