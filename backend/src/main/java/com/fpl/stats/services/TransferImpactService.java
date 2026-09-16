package com.fpl.stats.services;

import com.fpl.stats.exception.TeamNotFoundException;
import com.fpl.stats.services.dto.TransferImpactDto;

/**
 * Service for computing transfer impact (points lost/gained from player swaps and hit penalties)
 * for a user's FPL team across the season.
 */
public interface TransferImpactService {

    /**
     * Computes the full transfer impact for the given FPL team.
     *
     * <p>Transfer swaps are inferred by comparing picks between consecutive gameweeks.
     * Wildcard and free-hit gameweeks are excluded from the swap calculation.
     * Hit penalties are read from the stored {@code eventTransfersCost} field.</p>
     *
     * @param fplTeamId the FPL team/entry ID
     * @return a {@link TransferImpactDto} containing per-GW breakdown and season totals
     * @throws TeamNotFoundException if no team with the given ID exists in the database
     */
    TransferImpactDto getTransferImpact(long fplTeamId);
}
