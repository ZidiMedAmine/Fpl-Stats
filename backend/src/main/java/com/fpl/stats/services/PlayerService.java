package com.fpl.stats.services;

import com.fpl.stats.services.dto.PlayerDetailDto;
import com.fpl.stats.services.dto.PlayerSummaryDto;

import java.util.List;

/**
 * Service for querying FPL player data for the player comparison feature.
 */
public interface PlayerService {

    /**
     * Returns all players of the given position, ordered by total points descending.
     *
     * @param position one of GKP, DEF, MID, FWD
     * @return list of lightweight player summaries
     */
    List<PlayerSummaryDto> getPlayersByPosition(String position);

    /**
     * Returns full player detail including per-gameweek history for the comparison page.
     *
     * @param fplId the FPL player ID
     * @return player detail DTO with performances
     */
    PlayerDetailDto getPlayerDetail(int fplId);
}
