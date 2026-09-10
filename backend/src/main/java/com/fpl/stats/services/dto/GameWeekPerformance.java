package com.fpl.stats.services.dto;

import java.io.Serializable;

public class GameWeekPerformance implements Serializable {

    private int gameWeek;
    private int points;
    private int minutesPlayed;
    private int goalsScored;
    private int assists;
    private boolean cleanSheet;
    private int yellowCards;
    private int redCards;
    private int bonusPoints;
    private int bps;
    private int saves;
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
    private boolean wasInMyTeam;
    private boolean wasCaptain;
    private boolean wasViceCaptain;
    private boolean wasTripleCaptain;
    private boolean wasBenched;
    private int multiplier;

    private GameWeekPerformance(Builder builder) {
        this.gameWeek = builder.gameWeek;
        this.points = builder.points;
        this.minutesPlayed = builder.minutesPlayed;
        this.goalsScored = builder.goalsScored;
        this.assists = builder.assists;
        this.cleanSheet = builder.cleanSheet;
        this.yellowCards = builder.yellowCards;
        this.redCards = builder.redCards;
        this.bonusPoints = builder.bonusPoints;
        this.bps = builder.bps;
        this.saves = builder.saves;
        this.expectedGoals = builder.expectedGoals;
        this.expectedAssists = builder.expectedAssists;
        this.expectedGoalInvolvements = builder.expectedGoalInvolvements;
        this.goalsConceded = builder.goalsConceded;
        this.expectedGoalsConceded = builder.expectedGoalsConceded;
        this.clearancesBlocksInterceptions = builder.clearancesBlocksInterceptions;
        this.recoveries = builder.recoveries;
        this.tackles = builder.tackles;
        this.defensiveContribution = builder.defensiveContribution;
        this.influence = builder.influence;
        this.creativity = builder.creativity;
        this.threat = builder.threat;
        this.ictIndex = builder.ictIndex;
        this.wasHome = builder.wasHome;
        this.value = builder.value;
        this.transfersIn = builder.transfersIn;
        this.transfersOut = builder.transfersOut;
        this.transfersBalance = builder.transfersBalance;
        this.selected = builder.selected;
        this.wasInMyTeam = builder.wasInMyTeam;
        this.wasCaptain = builder.wasCaptain;
        this.wasViceCaptain = builder.wasViceCaptain;
        this.wasTripleCaptain = builder.wasTripleCaptain;
        this.wasBenched = builder.wasBenched;
        this.multiplier = builder.multiplier;
    }

    public static class Builder {
        private int gameWeek;
        private int points;
        private int minutesPlayed;
        private int goalsScored;
        private int assists;
        private boolean cleanSheet;
        private int yellowCards;
        private int redCards;
        private int bonusPoints;
        private int bps;
        private int saves;
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
        private boolean wasInMyTeam;
        private boolean wasCaptain;
        private boolean wasViceCaptain;
        private boolean wasTripleCaptain;
        private boolean wasBenched;
        private int multiplier = 1;

        public Builder gameWeek(int gameWeek) { this.gameWeek = gameWeek; return this; }
        public Builder points(int points) { this.points = points; return this; }
        public Builder minutesPlayed(int minutesPlayed) { this.minutesPlayed = minutesPlayed; return this; }
        public Builder goalsScored(int goalsScored) { this.goalsScored = goalsScored; return this; }
        public Builder assists(int assists) { this.assists = assists; return this; }
        public Builder cleanSheet(boolean cleanSheet) { this.cleanSheet = cleanSheet; return this; }
        public Builder yellowCards(int yellowCards) { this.yellowCards = yellowCards; return this; }
        public Builder redCards(int redCards) { this.redCards = redCards; return this; }
        public Builder bonusPoints(int bonusPoints) { this.bonusPoints = bonusPoints; return this; }
        public Builder bps(int bps) { this.bps = bps; return this; }
        public Builder saves(int saves) { this.saves = saves; return this; }
        public Builder expectedGoals(String expectedGoals) { this.expectedGoals = expectedGoals; return this; }
        public Builder expectedAssists(String expectedAssists) { this.expectedAssists = expectedAssists; return this; }
        public Builder expectedGoalInvolvements(String expectedGoalInvolvements) { this.expectedGoalInvolvements = expectedGoalInvolvements; return this; }
        public Builder goalsConceded(int goalsConceded) { this.goalsConceded = goalsConceded; return this; }
        public Builder expectedGoalsConceded(String expectedGoalsConceded) { this.expectedGoalsConceded = expectedGoalsConceded; return this; }
        public Builder clearancesBlocksInterceptions(int clearancesBlocksInterceptions) { this.clearancesBlocksInterceptions = clearancesBlocksInterceptions; return this; }
        public Builder recoveries(int recoveries) { this.recoveries = recoveries; return this; }
        public Builder tackles(int tackles) { this.tackles = tackles; return this; }
        public Builder defensiveContribution(int defensiveContribution) { this.defensiveContribution = defensiveContribution; return this; }
        public Builder influence(String influence) { this.influence = influence; return this; }
        public Builder creativity(String creativity) { this.creativity = creativity; return this; }
        public Builder threat(String threat) { this.threat = threat; return this; }
        public Builder ictIndex(String ictIndex) { this.ictIndex = ictIndex; return this; }
        public Builder wasHome(boolean wasHome) { this.wasHome = wasHome; return this; }
        public Builder value(int value) { this.value = value; return this; }
        public Builder transfersIn(int transfersIn) { this.transfersIn = transfersIn; return this; }
        public Builder transfersOut(int transfersOut) { this.transfersOut = transfersOut; return this; }
        public Builder transfersBalance(int transfersBalance) { this.transfersBalance = transfersBalance; return this; }
        public Builder selected(int selected) { this.selected = selected; return this; }
        public Builder wasInMyTeam(boolean wasInMyTeam) { this.wasInMyTeam = wasInMyTeam; return this; }
        public Builder wasCaptain(boolean wasCaptain) { this.wasCaptain = wasCaptain; return this; }
        public Builder wasViceCaptain(boolean wasViceCaptain) { this.wasViceCaptain = wasViceCaptain; return this; }
        public Builder wasTripleCaptain(boolean wasTripleCaptain) { this.wasTripleCaptain = wasTripleCaptain; return this; }
        public Builder wasBenched(boolean wasBenched) { this.wasBenched = wasBenched; return this; }
        public Builder multiplier(int multiplier) { this.multiplier = multiplier; return this; }

        public GameWeekPerformance build() { return new GameWeekPerformance(this); }
    }

    public int getGameWeek() { return gameWeek; }
    public int getPoints() { return points; }
    public int getMinutesPlayed() { return minutesPlayed; }
    public int getGoalsScored() { return goalsScored; }
    public int getAssists() { return assists; }
    public boolean isCleanSheet() { return cleanSheet; }
    public int getYellowCards() { return yellowCards; }
    public int getRedCards() { return redCards; }
    public int getBonusPoints() { return bonusPoints; }
    public int getBps() { return bps; }
    public int getSaves() { return saves; }
    public String getExpectedGoals() { return expectedGoals; }
    public String getExpectedAssists() { return expectedAssists; }
    public String getExpectedGoalInvolvements() { return expectedGoalInvolvements; }
    public int getGoalsConceded() { return goalsConceded; }
    public String getExpectedGoalsConceded() { return expectedGoalsConceded; }
    public int getClearancesBlocksInterceptions() { return clearancesBlocksInterceptions; }
    public int getRecoveries() { return recoveries; }
    public int getTackles() { return tackles; }
    public int getDefensiveContribution() { return defensiveContribution; }
    public String getInfluence() { return influence; }
    public String getCreativity() { return creativity; }
    public String getThreat() { return threat; }
    public String getIctIndex() { return ictIndex; }
    public boolean isWasHome() { return wasHome; }
    public int getValue() { return value; }
    public int getTransfersIn() { return transfersIn; }
    public int getTransfersOut() { return transfersOut; }
    public int getTransfersBalance() { return transfersBalance; }
    public int getSelected() { return selected; }
    public boolean isWasInMyTeam() { return wasInMyTeam; }
    public boolean isWasCaptain() { return wasCaptain; }
    public boolean isWasViceCaptain() { return wasViceCaptain; }
    public boolean isWasTripleCaptain() { return wasTripleCaptain; }
    public boolean isWasBenched() { return wasBenched; }
    public int getMultiplier() { return multiplier; }
}
