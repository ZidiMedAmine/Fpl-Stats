package com.fpl.stats.services;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.services.dto.GwFormationLossDto;

import java.util.List;
import java.util.Map;

/**
 * Computes the theoretical maximum points achievable per gameweek by trying all
 * valid FPL formations and every possible starting-11 combination from the 15-player squad.
 *
 * <p>The captain designation is kept fixed — if the chosen captain ends up outside the
 * optimal 11, the vice-captain inherits the captaincy multiplier, mirroring FPL rules.</p>
 */
public interface FormationOptimizationService {

    /**
     * Computes per-gameweek formation loss for a team.
     *
     * @param allPicks       all {@link UserPick} records for the team across all gameweeks,
     *                       with player and gameweek associations eagerly loaded
     * @param playersByFplId map of player FPL ID to {@link Player} entity with histories loaded
     * @return list of per-GW formation loss records ordered by gameweek ascending
     */
    List<GwFormationLossDto> computeFormationLoss(List<UserPick> allPicks, Map<Integer, Player> playersByFplId);
}
