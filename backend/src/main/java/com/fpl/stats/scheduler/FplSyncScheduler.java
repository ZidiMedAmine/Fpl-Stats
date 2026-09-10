package com.fpl.stats.scheduler;

import com.fpl.stats.services.fpl.sync.GlobalSyncService;
import com.fpl.stats.services.fpl.sync.PlayerHistorySyncService;
import com.fpl.stats.services.fpl.sync.UserSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that triggers periodic FPL data synchronisation jobs.
 *
 * <p>Cron expressions are configured via {@code fpl.sync.cron.*} properties.
 * Each job delegates entirely to the service layer — no business or persistence
 * logic lives here.</p>
 */
@Component
public class FplSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(FplSyncScheduler.class);

    private final GlobalSyncService globalSyncService;
    private final PlayerHistorySyncService playerHistorySyncService;
    private final UserSyncService userSyncService;

    /**
     * Constructs a {@code FplSyncScheduler} with its required sync service dependencies.
     *
     * @param globalSyncService        service for syncing global FPL data
     * @param playerHistorySyncService service for syncing per-player season histories
     * @param userSyncService          service for syncing all tracked user teams
     */
    public FplSyncScheduler(GlobalSyncService globalSyncService,
                            PlayerHistorySyncService playerHistorySyncService,
                            UserSyncService userSyncService) {
        this.globalSyncService = globalSyncService;
        this.playerHistorySyncService = playerHistorySyncService;
        this.userSyncService = userSyncService;
    }

    /**
     * Triggers a full sync of global FPL data (players, teams, game weeks) every 4 hours.
     */
    @Scheduled(cron = "${fpl.sync.cron.global}")
    public void syncGlobalData() {
        log.info("=== Starting global FPL data sync ===");
        try {
            globalSyncService.syncGlobalData();
            log.info("=== Global FPL data sync completed ===");
        } catch (Exception e) {
            log.error("Global data sync failed", e);
        }
    }

    /**
     * Triggers a sync of historical season stats for all players daily at 05:00.
     */
    @Scheduled(cron = "${fpl.sync.cron.player-histories}")
    public void syncPlayerHistories() {
        log.info("=== Starting player history sync ===");
        try {
            playerHistorySyncService.syncPlayerHistory();
            log.info("=== Player history sync completed ===");
        } catch (Exception e) {
            log.error("Player history sync failed", e);
        }
    }

    /**
     * Triggers a sync of all active tracked teams every 2 hours.
     */
    @Scheduled(cron = "${fpl.sync.cron.tracked-teams}")
    public void syncTrackedTeams() {
        log.info("=== Starting tracked teams sync ===");
        try {
            userSyncService.syncAllTrackedTeams();
            log.info("=== Tracked teams sync completed ===");
        } catch (Exception e) {
            log.error("Tracked teams sync failed", e);
        }
    }
}
