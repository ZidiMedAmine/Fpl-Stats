package com.fpl.stats.services.dto;

import java.util.List;

/**
 * Represents a dream team lineup for a specific time window, including the optimal
 * formation and the 11 selected players.
 */
public class DreamTeamResultDto {

    private String formation;
    private int totalPoints;
    private List<DreamTeamPlayerDto> players;

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public List<DreamTeamPlayerDto> getPlayers() { return players; }
    public void setPlayers(List<DreamTeamPlayerDto> players) { this.players = players; }
}
