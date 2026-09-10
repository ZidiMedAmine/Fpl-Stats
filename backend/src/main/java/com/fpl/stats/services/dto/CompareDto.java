package com.fpl.stats.services.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CompareDto implements Serializable {

    private UserTeamDto team1;
    private UserTeamDto team2;
    private List<PlayerDto> sharedPlayers;
    private List<PlayerDto> team1Differentials;
    private List<PlayerDto> team2Differentials;
    private Map<Integer, int[]> pointsByGameWeek;

    public UserTeamDto getTeam1() { return team1; }
    public void setTeam1(UserTeamDto team1) { this.team1 = team1; }
    public UserTeamDto getTeam2() { return team2; }
    public void setTeam2(UserTeamDto team2) { this.team2 = team2; }
    public List<PlayerDto> getSharedPlayers() { return sharedPlayers; }
    public void setSharedPlayers(List<PlayerDto> sharedPlayers) { this.sharedPlayers = sharedPlayers; }
    public List<PlayerDto> getTeam1Differentials() { return team1Differentials; }
    public void setTeam1Differentials(List<PlayerDto> team1Differentials) { this.team1Differentials = team1Differentials; }
    public List<PlayerDto> getTeam2Differentials() { return team2Differentials; }
    public void setTeam2Differentials(List<PlayerDto> team2Differentials) { this.team2Differentials = team2Differentials; }
    public Map<Integer, int[]> getPointsByGameWeek() { return pointsByGameWeek; }
    public void setPointsByGameWeek(Map<Integer, int[]> pointsByGameWeek) { this.pointsByGameWeek = pointsByGameWeek; }
}
