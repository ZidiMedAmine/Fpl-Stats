package com.fpl.stats.services.dto;

import java.io.Serializable;

/**
 * Lightweight player DTO used for autocomplete search results on the comparison page.
 */
public class PlayerSummaryDto implements Serializable {

    private int fplId;
    private String webName;
    private String position;
    private String teamName;
    private int code;
    private double nowCost;
    private int totalPoints;
    private double avgPoints;
    private double selectedByPercent;

    public int getFplId() { return fplId; }
    public void setFplId(int fplId) { this.fplId = fplId; }
    public String getWebName() { return webName; }
    public void setWebName(String webName) { this.webName = webName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public double getNowCost() { return nowCost; }
    public void setNowCost(double nowCost) { this.nowCost = nowCost; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public double getAvgPoints() { return avgPoints; }
    public void setAvgPoints(double avgPoints) { this.avgPoints = avgPoints; }
    public double getSelectedByPercent() { return selectedByPercent; }
    public void setSelectedByPercent(double selectedByPercent) { this.selectedByPercent = selectedByPercent; }
}
