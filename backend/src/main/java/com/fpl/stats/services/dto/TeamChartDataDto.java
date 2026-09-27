package com.fpl.stats.services.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chart data DTO returned by the slow {@code /user-info/{id}/chart-data} endpoint.
 * Contains full player histories, gameweek averages/high scores, and formation loss data —
 * everything needed to render the performance charts once the fast summary is already shown.
 */
public class TeamChartDataDto {

    private long fplTeamId;
    private List<PlayerDto> players = new ArrayList<>();
    private Map<Integer, Integer> gameWeekAverages = new HashMap<>();
    private Map<Integer, Integer> gameWeekHighScores = new HashMap<>();
    private List<GwFormationLossDto> formationLoss = new ArrayList<>();

    public long getFplTeamId() { return fplTeamId; }
    public void setFplTeamId(long fplTeamId) { this.fplTeamId = fplTeamId; }

    public List<PlayerDto> getPlayers() { return players; }
    public void setPlayers(List<PlayerDto> players) { this.players = players; }

    public Map<Integer, Integer> getGameWeekAverages() { return gameWeekAverages; }
    public void setGameWeekAverages(Map<Integer, Integer> gameWeekAverages) { this.gameWeekAverages = gameWeekAverages; }

    public Map<Integer, Integer> getGameWeekHighScores() { return gameWeekHighScores; }
    public void setGameWeekHighScores(Map<Integer, Integer> gameWeekHighScores) { this.gameWeekHighScores = gameWeekHighScores; }

    public List<GwFormationLossDto> getFormationLoss() { return formationLoss; }
    public void setFormationLoss(List<GwFormationLossDto> formationLoss) { this.formationLoss = formationLoss; }
}
