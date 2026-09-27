package com.fpl.stats.services.dto;

import java.util.List;

/**
 * Per-gameweek breakdown of points lost due to suboptimal formation and bench selection.
 *
 * <p>{@code pointsLost} is the difference between the theoretical maximum achievable
 * by starting the best possible 11 from the 15-player squad (across all valid FPL formations),
 * and the actual points scored that gameweek.</p>
 */
public class GwFormationLossDto {

    private int gameWeek;
    private int actualPoints;
    private int optimalPoints;
    private int pointsLost;
    private String actualFormation;
    private String optimalFormation;
    private List<String> playersToStart;
    private List<String> playersToBench;

    public int getGameWeek() { return gameWeek; }
    public void setGameWeek(int gameWeek) { this.gameWeek = gameWeek; }

    public int getActualPoints() { return actualPoints; }
    public void setActualPoints(int actualPoints) { this.actualPoints = actualPoints; }

    public int getOptimalPoints() { return optimalPoints; }
    public void setOptimalPoints(int optimalPoints) { this.optimalPoints = optimalPoints; }

    public int getPointsLost() { return pointsLost; }
    public void setPointsLost(int pointsLost) { this.pointsLost = pointsLost; }

    public String getActualFormation() { return actualFormation; }
    public void setActualFormation(String actualFormation) { this.actualFormation = actualFormation; }

    public String getOptimalFormation() { return optimalFormation; }
    public void setOptimalFormation(String optimalFormation) { this.optimalFormation = optimalFormation; }

    public List<String> getPlayersToStart() { return playersToStart; }
    public void setPlayersToStart(List<String> playersToStart) { this.playersToStart = playersToStart; }

    public List<String> getPlayersToBench() { return playersToBench; }
    public void setPlayersToBench(List<String> playersToBench) { this.playersToBench = playersToBench; }
}
