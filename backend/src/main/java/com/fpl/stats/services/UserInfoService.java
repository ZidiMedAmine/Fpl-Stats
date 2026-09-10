package com.fpl.stats.services;

import com.fpl.stats.exception.TeamNotFoundException;
import com.fpl.stats.services.dto.CompareDto;
import com.fpl.stats.services.dto.UserTeamDto;

/**
 * Service for querying FPL user team data from the local database.
 */
public interface UserInfoService {

    /**
     * Returns full team information for the given FPL team ID, including players,
     * gameweek performances, rank history, and overall rank change.
     *
     * @param fplTeamId the FPL team/entry ID
     * @return a fully populated {@link UserTeamDto}
     * @throws TeamNotFoundException if no team with the given ID exists in the database
     */
    UserTeamDto getUserTeamInfo(long fplTeamId);

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
