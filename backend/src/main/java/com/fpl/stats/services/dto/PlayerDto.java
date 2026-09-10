package com.fpl.stats.services.dto;

import java.io.Serializable;
import java.util.List;

public class PlayerDto implements Serializable {

    private int fplId;
    private String name;
    private String position;
    private String teamName;
    private int code;
    private double nowCost;
    private String status;
    private double avgPoints;
    private int totalPointsForTeam;
    private List<GameWeekPerformance> performances;

    public int getFplId() { return fplId; }
    public void setFplId(int fplId) { this.fplId = fplId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public double getNowCost() { return nowCost; }
    public void setNowCost(double nowCost) { this.nowCost = nowCost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getAvgPoints() { return avgPoints; }
    public void setAvgPoints(double avgPoints) { this.avgPoints = avgPoints; }
    public int getTotalPointsForTeam() { return totalPointsForTeam; }
    public void setTotalPointsForTeam(int totalPointsForTeam) { this.totalPointsForTeam = totalPointsForTeam; }
    public List<GameWeekPerformance> getPerformances() { return performances; }
    public void setPerformances(List<GameWeekPerformance> performances) { this.performances = performances; }
}
