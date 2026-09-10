package com.fpl.stats.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_team", uniqueConstraints = @UniqueConstraint(columnNames = "fplTeamId"))
public class UserTeam extends BaseEntity {

    @Column(nullable = false, unique = true)
    private long fplTeamId;

    private String playerFirstName;
    private String playerLastName;
    private String teamName;
    private String region;
    private Integer overallRank;
    private Integer totalPoints;
    private Integer startedEvent;
    private int lastSyncedGameWeek;
    private Double teamValue;
    private Integer bank;
    private Integer totalTransfers;

    public long getFplTeamId() { return fplTeamId; }
    public void setFplTeamId(long fplTeamId) { this.fplTeamId = fplTeamId; }
    public String getPlayerFirstName() { return playerFirstName; }
    public void setPlayerFirstName(String playerFirstName) { this.playerFirstName = playerFirstName; }
    public String getPlayerLastName() { return playerLastName; }
    public void setPlayerLastName(String playerLastName) { this.playerLastName = playerLastName; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Integer getOverallRank() { return overallRank; }
    public void setOverallRank(Integer overallRank) { this.overallRank = overallRank; }
    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }
    public Integer getStartedEvent() { return startedEvent; }
    public void setStartedEvent(Integer startedEvent) { this.startedEvent = startedEvent; }
    public int getLastSyncedGameWeek() { return lastSyncedGameWeek; }
    public void setLastSyncedGameWeek(int lastSyncedGameWeek) { this.lastSyncedGameWeek = lastSyncedGameWeek; }
    public Double getTeamValue() { return teamValue; }
    public void setTeamValue(Double teamValue) { this.teamValue = teamValue; }
    public Integer getBank() { return bank; }
    public void setBank(Integer bank) { this.bank = bank; }
    public Integer getTotalTransfers() { return totalTransfers; }
    public void setTotalTransfers(Integer totalTransfers) { this.totalTransfers = totalTransfers; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTeam other)) return false;
        return fplTeamId == other.fplTeamId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(fplTeamId);
    }

    @Override
    public String toString() {
        return "UserTeam{fplTeamId=" + fplTeamId + ", teamName='" + teamName + "'}";
    }
}
