package com.fpl.stats.services.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class UserTeamDto implements Serializable {

    private long fplTeamId;
    private String name;
    private String teamName;
    private String region;
    private Integer overallRank;
    private Integer totalPoints;
    private int currentGameWeek;
    private List<PlayerDto> players;
    private Map<Integer, Integer> gameWeekAverages;
    private Double teamValue;
    private Integer bank;
    private Integer totalTransfers;
    private Integer rankChange;
    private List<RankHistoryDto> rankHistory = new ArrayList<>();

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
    public List<PlayerDto> getPlayers() { return players; }
    public void setPlayers(List<PlayerDto> players) { this.players = players; }
    public Map<Integer, Integer> getGameWeekAverages() { return gameWeekAverages; }
    public void setGameWeekAverages(Map<Integer, Integer> gameWeekAverages) { this.gameWeekAverages = gameWeekAverages; }
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
}
