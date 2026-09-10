package com.fpl.stats.domain;

import jakarta.persistence.*;

/**
 * Stores the per-gameweek overall rank for a user's FPL team,
 * sourced from the FPL entry history endpoint.
 */
@Entity
@Table(name = "user_team_rank_history", uniqueConstraints =
        @UniqueConstraint(columnNames = {"user_team_id", "game_week"}))
public class UserTeamRankHistory extends BaseEntity {

    @Column(nullable = false)
    private int gameWeek;

    @Column(nullable = false)
    private int overallRank;

    @Column(nullable = false)
    private int gwPoints;

    @Column(nullable = false)
    private int gwRank;

    @Column(nullable = false)
    private int totalPoints;

    @Column(nullable = false)
    private int bank;

    @Column(nullable = false)
    private Double teamValue;

    @Column(nullable = false)
    private int eventTransfers;

    @Column(nullable = false)
    private int pointsOnBench;

    @Column(length = 10)
    private String chipUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_team_id", nullable = false)
    private UserTeam userTeam;

    public int getGameWeek() { return gameWeek; }
    public void setGameWeek(int gameWeek) { this.gameWeek = gameWeek; }
    public int getOverallRank() { return overallRank; }
    public void setOverallRank(int overallRank) { this.overallRank = overallRank; }
    public int getGwPoints() { return gwPoints; }
    public void setGwPoints(int gwPoints) { this.gwPoints = gwPoints; }
    public int getGwRank() { return gwRank; }
    public void setGwRank(int gwRank) { this.gwRank = gwRank; }
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
    public UserTeam getUserTeam() { return userTeam; }
    public void setUserTeam(UserTeam userTeam) { this.userTeam = userTeam; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTeamRankHistory other)) return false;
        if (userTeam == null || other.userTeam == null) return false;
        return userTeam.getFplTeamId() == other.userTeam.getFplTeamId()
                && gameWeek == other.gameWeek;
    }

    @Override
    public int hashCode() {
        long teamId = userTeam != null ? userTeam.getFplTeamId() : 0L;
        return 31 * Long.hashCode(teamId) + gameWeek;
    }

    @Override
    public String toString() {
        return "UserTeamRankHistory{gameWeek=" + gameWeek + ", overallRank=" + overallRank + "}";
    }
}
