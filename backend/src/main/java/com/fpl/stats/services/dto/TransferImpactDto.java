package com.fpl.stats.services.dto;

import java.util.List;

/**
 * Season-level transfer impact summary for a user's FPL team,
 * including per-gameweek breakdown and season totals.
 */
public class TransferImpactDto {

    /** Sum of all per-GW swap impacts. Negative means points were lost from transfers. */
    private int totalSwapPointsLost;
    /** Sum of all hit penalties across the season. Always zero or negative. */
    private int totalHitPointsLost;
    private List<TransferImpactGwDto> perGameWeek;

    public int getTotalSwapPointsLost() { return totalSwapPointsLost; }
    public void setTotalSwapPointsLost(int totalSwapPointsLost) { this.totalSwapPointsLost = totalSwapPointsLost; }
    public int getTotalHitPointsLost() { return totalHitPointsLost; }
    public void setTotalHitPointsLost(int totalHitPointsLost) { this.totalHitPointsLost = totalHitPointsLost; }
    public List<TransferImpactGwDto> getPerGameWeek() { return perGameWeek; }
    public void setPerGameWeek(List<TransferImpactGwDto> perGameWeek) { this.perGameWeek = perGameWeek; }
}
