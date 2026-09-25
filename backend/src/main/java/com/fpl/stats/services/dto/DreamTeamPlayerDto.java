package com.fpl.stats.services.dto;

/**
 * Represents a single player slot in a dream team lineup.
 */
public class DreamTeamPlayerDto {

    private int fplId;
    private int code;
    private String webName;
    private String position;
    private String teamName;
    private int points;
    private double nowCost;

    public int getFplId() { return fplId; }
    public void setFplId(int fplId) { this.fplId = fplId; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getWebName() { return webName; }
    public void setWebName(String webName) { this.webName = webName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public double getNowCost() { return nowCost; }
    public void setNowCost(double nowCost) { this.nowCost = nowCost; }
}
