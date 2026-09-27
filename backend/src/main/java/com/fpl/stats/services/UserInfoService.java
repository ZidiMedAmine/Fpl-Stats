package com.fpl.stats.services;

import com.fpl.stats.exception.TeamNotFoundException;
import com.fpl.stats.services.dto.CompareDto;
import com.fpl.stats.services.dto.TeamChartDataDto;
import com.fpl.stats.services.dto.UserTeamSummaryDto;

/**
 * Service for querying FPL user team data from the local database.
 */
public interface UserInfoService {

    /**
     * Returns a lightweight team summary for the given FPL team ID.
     * Includes team metadata, rank history, and player stubs — no player history loaded.
     *
     * @param fplTeamId the FPL team/entry ID
     * @return a {@link UserTeamSummaryDto} with team metadata and player stubs
     * @throws TeamNotFoundException if no team with the given ID exists in the database
     */
    UserTeamSummaryDto getUserTeamSummary(long fplTeamId);

    /**
     * Returns the full chart data for the given FPL team ID.
     * Includes full player histories, gameweek averages, high scores, and formation loss.
     * This is the slow path — it loads all player histories from the database.
     *
     * @param fplTeamId the FPL team/entry ID
     * @return a {@link TeamChartDataDto} with all data needed to render performance charts
     * @throws TeamNotFoundException if no team with the given ID exists in the database
     */
    TeamChartDataDto getTeamChartData(long fplTeamId);

    /**
     * Compares two FPL teams side by side, including shared players, differentials,
     * and per-gameweek points for both teams.
     *
     * @param fplTeamId1 the FPL team/entry ID of the first team
     * @param fplTeamId2 the FPL team/entry ID of the second team
     * @return a {@link CompareDto} containing both team DTOs and the comparison data
     * @throws TeamNotFoundException if either team does not exist in the database
     */
    CompareDto compareTeams(long fplTeamId1, long fplTeamId2);
}
