package com.fpl.stats.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "team", uniqueConstraints = @UniqueConstraint(columnNames = "fplId"))
public class Team extends BaseEntity {

    @Column(nullable = false, unique = true)
    private int fplId;

    private int code;
    private String name;
    private String shortName;
    private int strength;
    private int position;
    private int played;
    private int win;
    private int draw;
    private int loss;
    private int points;

    public int getFplId() { return fplId; }
    public void setFplId(int fplId) { this.fplId = fplId; }
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public int getStrength() { return strength; }
    public void setStrength(int strength) { this.strength = strength; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }
    public int getWin() { return win; }
    public void setWin(int win) { this.win = win; }
    public int getDraw() { return draw; }
    public void setDraw(int draw) { this.draw = draw; }
    public int getLoss() { return loss; }
    public void setLoss(int loss) { this.loss = loss; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team other)) return false;
        return fplId == other.fplId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(fplId);
    }

    @Override
    public String toString() {
        return "Team{fplId=" + fplId + ", name='" + name + "'}";
    }
}
