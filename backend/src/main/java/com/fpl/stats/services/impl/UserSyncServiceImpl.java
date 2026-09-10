package com.fpl.stats.services.impl;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.TrackedTeam;
import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.repository.TrackedTeamRepository;
import com.fpl.stats.repository.UserPickRepository;
import com.fpl.stats.services.fpl.sync.PlayerHistorySyncService;
import com.fpl.stats.services.fpl.sync.UserPickSyncService;
import com.fpl.stats.services.fpl.sync.UserSyncService;
import com.fpl.stats.services.fpl.sync.UserTeamSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Orchestrates a full per-user sync: registers the team for tracking,
 * syncs team metadata, picks, and player history.
 */
@Service
@Transactional
public class UserSyncServiceImpl implements UserSyncService {

    private static final Logger log = LoggerFactory.getLogger(UserSyncServiceImpl.class);

    private final PlayerHistorySyncService playerHistorySyncService;
    private final TrackedTeamRepository trackedTeamRepository;
    private final UserTeamSyncService userTeamSyncService;
    private final UserPickSyncService userPickSyncService;
    private final UserPickRepository userPickRepository;

    /**
     * Constructs a {@code UserSyncServiceImpl} with its required dependencies.
     *
     * @param playerHistorySyncService syncs per-player gameweek history
     * @param trackedTeamRepository   persists tracked team records
     * @param userTeamSyncService     syncs user team metadata and rank history
     * @param userPickSyncService     syncs gameweek picks for a user team
     * @param userPickRepository      used to retrieve players picked by a team
     */
    public UserSyncServiceImpl(PlayerHistorySyncService playerHistorySyncService,
                               TrackedTeamRepository trackedTeamRepository,
                               UserTeamSyncService userTeamSyncService,
                               UserPickSyncService userPickSyncService,
                               UserPickRepository userPickRepository) {
        this.playerHistorySyncService = playerHistorySyncService;
        this.trackedTeamRepository = trackedTeamRepository;
        this.userTeamSyncService = userTeamSyncService;
        this.userPickSyncService = userPickSyncService;
        this.userPickRepository = userPickRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncUser(long fplTeamId) {
        registerForTracking(fplTeamId);

        UserTeam syncedTeam = userTeamSyncService.syncUserTeam(fplTeamId);
        userPickSyncService.syncUserPicks(syncedTeam);

        List<Player> teamPlayers = userPickRepository.findDistinctPlayersByUserTeam(syncedTeam);
        playerHistorySyncService.syncPlayerHistoryForPlayers(teamPlayers);

        log.info("Full sync completed for team {}", fplTeamId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void syncAllTrackedTeams() {
        List<TrackedTeam> trackedTeams = trackedTeamRepository.findAllByActiveTrue();
        if (trackedTeams.isEmpty()) {
            log.info("No tracked teams to sync");
            return;
        }
        log.info("Syncing {} tracked teams", trackedTeams.size());
        int successCount = 0;
        for (TrackedTeam trackedTeam : trackedTeams) {
            try {
                syncUser(trackedTeam.getFplTeamId());
                trackedTeam.setLastSyncedAt(Instant.now());
                trackedTeamRepository.save(trackedTeam);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to sync tracked team {}", trackedTeam.getFplTeamId(), e);
            }
        }
        log.info("Tracked teams sync completed: {}/{} successful", successCount, trackedTeams.size());
    }

    /**
     * Registers the given FPL team for tracking if it has not been tracked before.
     *
     * @param fplTeamId the FPL team ID to register
     */
    private void registerForTracking(long fplTeamId) {
        if (!trackedTeamRepository.existsByFplTeamId(fplTeamId)) {
            TrackedTeam trackedTeam = new TrackedTeam();
            trackedTeam.setFplTeamId(fplTeamId);
            trackedTeam.setActive(true);
            trackedTeamRepository.save(trackedTeam);
            log.info("Registered team {} for tracking", fplTeamId);
        }
    }
}
