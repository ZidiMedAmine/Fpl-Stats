package com.fpl.stats.services.dto;

import java.io.Serializable;

public class RankHistoryDto implements Serializable {

    private int gameWeek;
    private int overallRank;
    private int gwRank;
    private int gwPoints;
    private int totalPoints;
    private int bank;
    private Double teamValue;
    private int eventTransfers;
    private int pointsOnBench;
    private String chipUsed;

    public int getGameWeek() { return gameWeek; }
    public void setGameWeek(int gameWeek) { this.gameWeek = gameWeek; }
    public int getOverallRank() { return overallRank; }
    public void setOverallRank(int overallRank) { this.overallRank = overallRank; }
    public int getGwRank() { return gwRank; }
    public void setGwRank(int gwRank) { this.gwRank = gwRank; }
    public int getGwPoints() { return gwPoints; }
    public void setGwPoints(int gwPoints) { this.gwPoints = gwPoints; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public int getBank() { return bank; }
    public void setBank(int bank) { this.bank = bank; }
    public Double getTeamValue() { return teamValue; }
    public void setTeamValue(Double teamValue) { this.teamValue = teamValue; }
    public int getEventTransfers() { return eventTransfers; }
    public void setEventTransfers(int eventTransfers) { this.eventTransfers = eventTransfers; }
    public int getPointsOnBench() { return pointsOnBench; }
    public void setPointsOnBench(int pointsOnBench) { this.pointsOnBench = pointsOnBench; }
    public String getChipUsed() { return chipUsed; }
    public void setChipUsed(String chipUsed) { this.chipUsed = chipUsed; }
}
