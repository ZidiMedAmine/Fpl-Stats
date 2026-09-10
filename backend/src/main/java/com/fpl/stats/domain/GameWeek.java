package com.fpl.stats.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "gameweek", uniqueConstraints = @UniqueConstraint(columnNames = "gameWeekNumber"))
public class GameWeek extends BaseEntity {

    @Column(nullable = false, unique = true)
    private int gameWeekNumber;

    private String name;
    private Instant deadlineTime;
    private boolean isCurrent;
    private boolean isNext;
    private boolean isPrevious;
    private boolean isFinished;
    private Integer averageScore;
    private Integer highestScore;

    public int getGameWeekNumber() { return gameWeekNumber; }
    public void setGameWeekNumber(int gameWeekNumber) { this.gameWeekNumber = gameWeekNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getDeadlineTime() { return deadlineTime; }
    public void setDeadlineTime(Instant deadlineTime) { this.deadlineTime = deadlineTime; }
    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }
    public boolean isNext() { return isNext; }
    public void setNext(boolean next) { isNext = next; }
    public boolean isPrevious() { return isPrevious; }
    public void setPrevious(boolean previous) { isPrevious = previous; }
    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }
    public Integer getAverageScore() { return averageScore; }
    public void setAverageScore(Integer averageScore) { this.averageScore = averageScore; }
    public Integer getHighestScore() { return highestScore; }
    public void setHighestScore(Integer highestScore) { this.highestScore = highestScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameWeek other)) return false;
        return gameWeekNumber == other.gameWeekNumber;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(gameWeekNumber);
    }

    @Override
    public String toString() {
        return "GameWeek{number=" + gameWeekNumber + ", name='" + name + "', finished=" + isFinished + "}";
    }
}
