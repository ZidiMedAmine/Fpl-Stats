package com.fpl.stats.domain;

import java.io.Serializable;
import java.util.List;

public class Player implements Serializable {
    private int id;
    private String name;
    private String position;
    private double avgPoints;
    private String photoCode;
    private int totalPointsForTeam;
    private List<GameWeekPerformance> performances;

    // Getters and Setters
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<GameWeekPerformance> getPerformances() {
        return performances;
    }

    public void setPerformances(List<GameWeekPerformance> performances) {
        this.performances = performances;
    }

    public double getAvgPoints() {
        return avgPoints;
    }

    public String getPhotoCode() {
        return photoCode;
    }

    public void setPhotoCode(String photoCode) {
        this.photoCode = photoCode;
    }

    public void setAvgPoints(double avgPoints) {
        this.avgPoints = avgPoints;
    }

    public int getTotalPointsForTeam() {
        return totalPointsForTeam;
    }

    public void setTotalPointsForTeam(int totalPointsForTeam) {
        this.totalPointsForTeam = totalPointsForTeam;
    }
}