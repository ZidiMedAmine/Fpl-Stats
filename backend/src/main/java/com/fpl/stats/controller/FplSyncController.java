package com.fpl.stats.controller;

import com.fpl.stats.services.fpl.sync.GlobalSyncService;
import com.fpl.stats.services.fpl.sync.PlayerHistorySyncService;
import com.fpl.stats.services.fpl.sync.UserSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for manually triggering FPL data synchronisation.
 *
 * <p>All endpoints require the {@code X-Sync-Key} header to match the configured
 * {@code sync.api.key} property. Requests with a missing or incorrect key are rejected
 * with 401 by the {@code SyncSecurityInterceptor}.</p>
 */
@RestController
@RequestMapping(FplSyncController.BASE_PATH)
public class FplSyncController {

    /** Base path for all sync trigger endpoints. */
    static final String BASE_PATH = "/api/fpl-sync";

    private final GlobalSyncService globalSyncService;
    private final PlayerHistorySyncService playerHistorySyncService;
    private final UserSyncService userSyncService;

    /**
     * Constructs a {@code FplSyncController} with its required sync service dependencies.
     *
     * @param globalSyncService        service for syncing global FPL data (players, teams, game weeks)
     * @param playerHistorySyncService service for syncing per-player season histories
     * @param userSyncService          service for syncing a single user's team and picks
     */
    public FplSyncController(GlobalSyncService globalSyncService,
                             PlayerHistorySyncService playerHistorySyncService,
                             UserSyncService userSyncService) {
        this.globalSyncService = globalSyncService;
        this.playerHistorySyncService = playerHistorySyncService;
        this.userSyncService = userSyncService;
    }

    /**
     * Triggers a full sync of global FPL data: players, teams, and game weeks.
     *
     * @return 200 with a confirmation message on success
     */
    @PostMapping("/global")
    public ResponseEntity<String> syncGlobalData() {
        globalSyncService.syncGlobalData();
        return ResponseEntity.ok("Global data synced successfully.");
    }

    /**
     * Triggers a sync of historical season stats for all players in the database.
     *
     * @return 200 with a confirmation message on success
     */
    @PostMapping("/player-histories")
    public ResponseEntity<String> syncPlayerHistories() {
        playerHistorySyncService.syncPlayerHistory();
        return ResponseEntity.ok("Player histories synced successfully.");
    }

    /**
     * Triggers a sync of team data and picks for a single FPL user.
     *
     * @param fplTeamId the FPL team ID of the user to sync
     * @return 200 with a confirmation message on success
     */
    @PostMapping("/user-teams/{fplTeamId}")
    public ResponseEntity<String> syncUserTeam(@PathVariable long fplTeamId) {
        userSyncService.syncUser(fplTeamId);
        return ResponseEntity.ok("User team synced successfully.");
    }
}
