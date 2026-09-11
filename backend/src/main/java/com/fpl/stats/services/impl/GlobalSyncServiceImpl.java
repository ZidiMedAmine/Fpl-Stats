package com.fpl.stats.services.impl;

import com.fpl.stats.domain.Team;
import com.fpl.stats.services.fpl.BootstrapDataService;
import com.fpl.stats.services.fpl.FixtureDataService;
import com.fpl.stats.services.fpl.sync.GameWeekSyncService;
import com.fpl.stats.services.fpl.sync.GlobalSyncService;
import com.fpl.stats.services.fpl.sync.PlayerSyncService;
import com.fpl.stats.services.fpl.sync.TeamSyncService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Orchestrates the full global FPL data sync: teams, game weeks, and players.
 */
@Service
public class GlobalSyncServiceImpl implements GlobalSyncService {

    private final BootstrapDataService bootstrapDataService;
    private final GameWeekSyncService gameWeekSyncService;
    private final FixtureDataService fixtureDataService;
    private final PlayerSyncService playerSyncService;
    private final TeamSyncService teamSyncService;

    /**
     * Constructs a {@code GlobalSyncServiceImpl} with its required service dependencies.
     *
     * @param bootstrapDataService fetches global FPL data from bootstrap-static
     * @param fixtureDataService   fetches fixture list used to derive current gameweek
     * @param teamSyncService      syncs team data to the database
     * @param gameWeekSyncService  syncs gameweek data to the database
     * @param playerSyncService    syncs player data to the database
     */
    public GlobalSyncServiceImpl(BootstrapDataService bootstrapDataService,
                                 GameWeekSyncService gameWeekSyncService,
                                 FixtureDataService fixtureDataService,
                                 PlayerSyncService playerSyncService,
                                 TeamSyncService teamSyncService) {
        this.bootstrapDataService = bootstrapDataService;
        this.gameWeekSyncService = gameWeekSyncService;
        this.fixtureDataService = fixtureDataService;
        this.playerSyncService = playerSyncService;
        this.teamSyncService = teamSyncService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncGlobalData() {
        List<Map<String, Object>> gameWeeksData = bootstrapDataService.getGameWeeks();
        List<Map<String, Object>> playersData = bootstrapDataService.getPlayers();
        List<Map<String, Object>> fixturesData = fixtureDataService.getFixtures();
        Map<Integer, String> positionMap = bootstrapDataService.getPositionMap();
        List<Map<String, Object>> teamsData = bootstrapDataService.getTeams();

        Map<Integer, Team> teamMap = teamSyncService.syncTeams(teamsData);
        gameWeekSyncService.syncGameWeeks(gameWeeksData, fixturesData);
        playerSyncService.syncPlayers(playersData, teamMap, positionMap);
    }
}
