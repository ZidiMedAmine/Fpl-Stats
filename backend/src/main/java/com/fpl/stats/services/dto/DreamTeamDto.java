package com.fpl.stats.services.dto;

/**
 * Top-level dream team DTO returned by the dream-team endpoint.
 * Contains both the all-time best XI and the best XI from the last 5 gameweeks.
 */
public class DreamTeamDto {

    private DreamTeamResultDto allTime;
    private DreamTeamResultDto last5Weeks;

    public DreamTeamResultDto getAllTime() { return allTime; }
    public void setAllTime(DreamTeamResultDto allTime) { this.allTime = allTime; }
    public DreamTeamResultDto getLast5Weeks() { return last5Weeks; }
    public void setLast5Weeks(DreamTeamResultDto last5Weeks) { this.last5Weeks = last5Weeks; }
}
