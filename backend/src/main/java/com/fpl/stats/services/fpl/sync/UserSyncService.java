package com.fpl.stats.services.fpl.sync;

/**
 * Orchestrates a full per-user sync: team metadata, picks, and player history.
 */
public interface UserSyncService {

    /**
     * Registers the team for tracking if not already tracked, then synchronizes
     * team metadata, gameweek picks, and player history from the FPL API.
     *
     * @param fplTeamId the FPL team/entry ID
     */
    void syncUser(long fplTeamId);

    /**
     * Syncs all currently active tracked teams.
     *
     * <p>Each team is synced independently — a failure on one team is logged and skipped,
     * so remaining teams are not affected.</p>
     */
    void syncAllTrackedTeams();
}
