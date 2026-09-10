package com.fpl.stats.services.fpl.sync;

/**
 * Orchestrates the full global FPL data sync: teams, game weeks, and players.
 */
public interface GlobalSyncService {

    /**
     * Syncs all global FPL reference data — teams, game weeks, and players —
     * from the FPL bootstrap API into the database.
     */
    void syncGlobalData();
}
