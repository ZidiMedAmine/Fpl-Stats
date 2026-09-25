package com.fpl.stats.services;

import com.fpl.stats.services.dto.DreamTeamDto;

/**
 * Service for computing FPL dream team lineups.
 *
 * <p>Returns the best possible starting XI across all valid FPL formations,
 * optimised by maximising total points. Two windows are supported: all-time
 * season points and the last 5 finished gameweeks.</p>
 */
public interface DreamTeamService {

    /**
     * Returns the all-time and last-5-weeks dream teams, each using the formation
     * that yields the highest combined player points.
     *
     * @return dream team DTO containing both lineups
     */
    DreamTeamDto getDreamTeam();
}
