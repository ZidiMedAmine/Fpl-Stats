package com.fpl.stats.services.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight team summary DTO returned by the fast {@code /user-info/{id}} endpoint.
 * Contains team metadata, rank history, and player stubs — all resolvable without loading
 * full player history, allowing the front-end to render the summary card immediately.
 */
public class UserTeamSummaryDto {

    private long fplTeamId;
    private String name;
    private String teamName;
    private String region;
    private Integer overallRank;
    private Integer totalPoints;
    private int currentGameWeek;
    private Double teamValue;
    private Integer bank;
    private Integer totalTransfers;
    private Integer rankChange;
    private List<RankHistoryDto> rankHistory = new ArrayList<>();
    private List<PlayerStubDto> players = new ArrayList<>();

    public long getFplTeamId() { return fplTeamId; }
    public void setFplTeamId(long fplTeamId) { this.fplTeamId = fplTeamId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Integer getOverallRank() { return overallRank; }
    public void setOverallRank(Integer overallRank) { this.overallRank = overallRank; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public int getCurrentGameWeek() { return currentGameWeek; }
    public void setCurrentGameWeek(int currentGameWeek) { this.currentGameWeek = currentGameWeek; }

    public Double getTeamValue() { return teamValue; }
    public void setTeamValue(Double teamValue) { this.teamValue = teamValue; }

    public Integer getBank() { return bank; }
    public void setBank(Integer bank) { this.bank = bank; }

    public Integer getTotalTransfers() { return totalTransfers; }
    public void setTotalTransfers(Integer totalTransfers) { this.totalTransfers = totalTransfers; }

    public Integer getRankChange() { return rankChange; }
    public void setRankChange(Integer rankChange) { this.rankChange = rankChange; }

    public List<RankHistoryDto> getRankHistory() { return rankHistory; }
    public void setRankHistory(List<RankHistoryDto> rankHistory) { this.rankHistory = rankHistory; }

    public List<PlayerStubDto> getPlayers() { return players; }
    public void setPlayers(List<PlayerStubDto> players) { this.players = players; }
}
