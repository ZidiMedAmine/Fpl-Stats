package com.fpl.stats.services.dto;

/**
 * Represents a single player swap (one transfer) and its points impact
 * in a given gameweek.
 */
public class TransferSwapDto {

    private String playerOut;
    private String playerIn;
    private int pointsOut;
    private int pointsIn;
    /** Negative when the sold player outscored the bought player (points lost). */
    private int swapImpact;

    public String getPlayerOut() { return playerOut; }
    public void setPlayerOut(String playerOut) { this.playerOut = playerOut; }
    public String getPlayerIn() { return playerIn; }
    public void setPlayerIn(String playerIn) { this.playerIn = playerIn; }
    public int getPointsOut() { return pointsOut; }
    public void setPointsOut(int pointsOut) { this.pointsOut = pointsOut; }
    public int getPointsIn() { return pointsIn; }
    public void setPointsIn(int pointsIn) { this.pointsIn = pointsIn; }
    public int getSwapImpact() { return swapImpact; }
    public void setSwapImpact(int swapImpact) { this.swapImpact = swapImpact; }
}
