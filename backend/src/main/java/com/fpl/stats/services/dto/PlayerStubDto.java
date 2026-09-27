package com.fpl.stats.services.dto;

/**
 * Lightweight player DTO containing only the fields available without loading player history.
 * Used by the team summary endpoint to allow fast page rendering.
 */
public class PlayerStubDto {

    private int fplId;
    private String name;
    private String position;
    private String teamName;
    private int code;
    private double nowCost;
    private String status;
    private int totalPoints;

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

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
}
