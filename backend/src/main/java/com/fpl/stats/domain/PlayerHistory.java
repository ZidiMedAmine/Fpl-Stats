package com.fpl.stats.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "player_history", uniqueConstraints =
    @UniqueConstraint(columnNames = {"player_id", "gameweek_id"}))
public class PlayerHistory extends BaseEntity {

    private int points;
    private int minutesPlayed;
    private int goalsScored;
    private int assists;
    private int cleanSheets;
    private int yellowCards;
    private int redCards;
    private int bonus;
    private int bps;
    private int saves;
    private int ownGoals;
    private int penaltiesSaved;
    private int penaltiesMissed;
    private int starts;
    private String expectedGoals;
    private String expectedAssists;
    private String expectedGoalInvolvements;
    private int goalsConceded;
    private String expectedGoalsConceded;
    private int clearancesBlocksInterceptions;
    private int recoveries;
    private int tackles;
    private int defensiveContribution;
    private String influence;
    private String creativity;
    private String threat;
    private String ictIndex;
    private boolean wasHome;
    private int value;
    private int transfersIn;
    private int transfersOut;
    private int transfersBalance;
    private int selected;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private GameWeek gameWeek;

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public int getMinutesPlayed() { return minutesPlayed; }
    public void setMinutesPlayed(int minutesPlayed) { this.minutesPlayed = minutesPlayed; }
    public int getGoalsScored() { return goalsScored; }
    public void setGoalsScored(int goalsScored) { this.goalsScored = goalsScored; }
    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }
    public int getCleanSheets() { return cleanSheets; }
    public void setCleanSheets(int cleanSheets) { this.cleanSheets = cleanSheets; }
    public int getYellowCards() { return yellowCards; }
    public void setYellowCards(int yellowCards) { this.yellowCards = yellowCards; }
    public int getRedCards() { return redCards; }
    public void setRedCards(int redCards) { this.redCards = redCards; }
    public int getBonus() { return bonus; }
    public void setBonus(int bonus) { this.bonus = bonus; }
    public int getBps() { return bps; }
    public void setBps(int bps) { this.bps = bps; }
    public int getSaves() { return saves; }
    public void setSaves(int saves) { this.saves = saves; }
    public int getOwnGoals() { return ownGoals; }
    public void setOwnGoals(int ownGoals) { this.ownGoals = ownGoals; }
    public int getPenaltiesSaved() { return penaltiesSaved; }
    public void setPenaltiesSaved(int penaltiesSaved) { this.penaltiesSaved = penaltiesSaved; }
    public int getPenaltiesMissed() { return penaltiesMissed; }
    public void setPenaltiesMissed(int penaltiesMissed) { this.penaltiesMissed = penaltiesMissed; }
    public int getStarts() { return starts; }
    public void setStarts(int starts) { this.starts = starts; }
    public String getExpectedGoals() { return expectedGoals; }
    public void setExpectedGoals(String expectedGoals) { this.expectedGoals = expectedGoals; }
    public String getExpectedAssists() { return expectedAssists; }
    public void setExpectedAssists(String expectedAssists) { this.expectedAssists = expectedAssists; }
    public String getExpectedGoalInvolvements() { return expectedGoalInvolvements; }
    public void setExpectedGoalInvolvements(String expectedGoalInvolvements) { this.expectedGoalInvolvements = expectedGoalInvolvements; }
    public int getGoalsConceded() { return goalsConceded; }
    public void setGoalsConceded(int goalsConceded) { this.goalsConceded = goalsConceded; }
    public String getExpectedGoalsConceded() { return expectedGoalsConceded; }
    public void setExpectedGoalsConceded(String expectedGoalsConceded) { this.expectedGoalsConceded = expectedGoalsConceded; }
    public int getClearancesBlocksInterceptions() { return clearancesBlocksInterceptions; }
    public void setClearancesBlocksInterceptions(int clearancesBlocksInterceptions) { this.clearancesBlocksInterceptions = clearancesBlocksInterceptions; }
    public int getRecoveries() { return recoveries; }
    public void setRecoveries(int recoveries) { this.recoveries = recoveries; }
    public int getTackles() { return tackles; }
    public void setTackles(int tackles) { this.tackles = tackles; }
    public int getDefensiveContribution() { return defensiveContribution; }
    public void setDefensiveContribution(int defensiveContribution) { this.defensiveContribution = defensiveContribution; }
    public String getInfluence() { return influence; }
    public void setInfluence(String influence) { this.influence = influence; }
    public String getCreativity() { return creativity; }
    public void setCreativity(String creativity) { this.creativity = creativity; }
    public String getThreat() { return threat; }
    public void setThreat(String threat) { this.threat = threat; }
    public String getIctIndex() { return ictIndex; }
    public void setIctIndex(String ictIndex) { this.ictIndex = ictIndex; }
    public boolean isWasHome() { return wasHome; }
    public void setWasHome(boolean wasHome) { this.wasHome = wasHome; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public int getTransfersIn() { return transfersIn; }
    public void setTransfersIn(int transfersIn) { this.transfersIn = transfersIn; }
    public int getTransfersOut() { return transfersOut; }
    public void setTransfersOut(int transfersOut) { this.transfersOut = transfersOut; }
    public int getTransfersBalance() { return transfersBalance; }
    public void setTransfersBalance(int transfersBalance) { this.transfersBalance = transfersBalance; }
    public int getSelected() { return selected; }
    public void setSelected(int selected) { this.selected = selected; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public GameWeek getGameWeek() { return gameWeek; }
    public void setGameWeek(GameWeek gameWeek) { this.gameWeek = gameWeek; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerHistory other)) return false;
        if (player == null || other.player == null) return false;
        if (gameWeek == null || other.gameWeek == null) return false;
        return player.getFplId() == other.player.getFplId()
            && gameWeek.getGameWeekNumber() == other.gameWeek.getGameWeekNumber();
    }

    @Override
    public int hashCode() {
        int playerFplId = player != null ? player.getFplId() : 0;
        int gwNumber = gameWeek != null ? gameWeek.getGameWeekNumber() : 0;
        return 31 * playerFplId + gwNumber;
    }

    @Override
    public String toString() {
        return "PlayerHistory{points=" + points + ", gameWeek=" + gameWeek + "}";
    }
}
