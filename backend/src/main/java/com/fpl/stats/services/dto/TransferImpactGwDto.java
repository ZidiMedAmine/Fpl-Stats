package com.fpl.stats.services.dto;

import java.util.List;

/**
 * Aggregates the transfer impact for a single gameweek, including
 * the net swap impact and the hit penalty.
 */
public class TransferImpactGwDto {

    private int gameWeek;
    /** Sum of {@link TransferSwapDto#getSwapImpact()} for this GW. Negative = lost points. */
    private int swapImpact;
    /** Points deducted for extra transfers (0, -4, -8 …). Always zero or negative. */
    private int hitCost;
    private List<TransferSwapDto> transfers;

    public int getGameWeek() { return gameWeek; }
    public void setGameWeek(int gameWeek) { this.gameWeek = gameWeek; }
    public int getSwapImpact() { return swapImpact; }
    public void setSwapImpact(int swapImpact) { this.swapImpact = swapImpact; }
    public int getHitCost() { return hitCost; }
    public void setHitCost(int hitCost) { this.hitCost = hitCost; }
    public List<TransferSwapDto> getTransfers() { return transfers; }
    public void setTransfers(List<TransferSwapDto> transfers) { this.transfers = transfers; }
}
