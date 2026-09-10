package com.fpl.stats.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player", uniqueConstraints = @UniqueConstraint(columnNames = "fplId"))
public class Player extends BaseEntity {

    @Column(nullable = false, unique = true)
    private int fplId;

    private String firstName;
    private String secondName;
    private String webName;
    private String position;
    private int code;
    private double nowCost;
    private int totalPoints;
    private String form;
    private String status;
    private double selectedByPercent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private List<PlayerHistory> playerHistories = new ArrayList<>();

    public int getFplId() { return fplId; }
    public void setFplId(int fplId) { this.fplId = fplId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    public String getWebName() { return webName; }
    public void setWebName(String webName) { this.webName = webName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public double getNowCost() { return nowCost; }
    public void setNowCost(double nowCost) { this.nowCost = nowCost; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getSelectedByPercent() { return selectedByPercent; }
    public void setSelectedByPercent(double selectedByPercent) { this.selectedByPercent = selectedByPercent; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public List<PlayerHistory> getPlayerHistories() { return playerHistories; }
    public void setPlayerHistories(List<PlayerHistory> playerHistories) { this.playerHistories = playerHistories; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player other)) return false;
        return fplId == other.fplId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(fplId);
    }

    @Override
    public String toString() {
        return "Player{fplId=" + fplId + ", webName='" + webName + "'}";
    }
}
