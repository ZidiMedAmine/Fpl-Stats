package com.fpl.stats.domain;

import java.io.Serializable;
import java.util.List;

public class UserInfo implements Serializable {

    private int id;
    private String name;
    private String teamName;
    private String region;
    private int overallRank;
    private int totalPoints;
    private int currentGameWeek;
    private int gameWeekPoints;
    private int gameWeekRank;
    private List<Player> players;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getOverallRank() {
        return overallRank;
    }

    public void setOverallRank(int overallRank) {
        this.overallRank = overallRank;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getCurrentGameWeek() {
        return currentGameWeek;
    }

    public void setCurrentGameWeek(int currentGameWeek) {
        this.currentGameWeek = currentGameWeek;
    }

    public int getGameWeekPoints() {
        return gameWeekPoints;
    }

    public void setGameWeekPoints(int gameWeekPoints) {
        this.gameWeekPoints = gameWeekPoints;
    }

    public int getGameWeekRank() {
        return gameWeekRank;
    }

    public void setGameWeekRank(int gameWeekRank) {
        this.gameWeekRank = gameWeekRank;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}
