package com.fpl.stats.domain;

import java.io.Serializable;

public class GameWeekPerformance implements Serializable {
    private int points;
    private int gameWeek;
    private int bonusPoints;
    private boolean wasBenched;
    private boolean wasCaptain;
    private boolean wasInMyTeam;
    private boolean wasViceCaptain;
    private boolean wasTripleCaptain;
    private boolean cleanSheet;
    private int minutesPlayed;
    private int yellowCards;
    private int goalsScored;
    private int  assists;
    private int redCards;

    private GameWeekPerformance(Builder builder) {
        this.points = builder.points;
        this.gameWeek = builder.gameWeek;
        this.bonusPoints = builder.bonusPoints;
        this.wasBenched = builder.wasBenched;
        this.wasCaptain = builder.wasCaptain;
        this.wasInMyTeam = builder.wasInMyTeam;
        this.wasViceCaptain = builder.wasViceCaptain;
        this.wasTripleCaptain = builder.wasTripleCaptain;
        this.minutesPlayed = builder.minutesPlayed;
        this.yellowCards = builder.yellowCards;
        this.redCards = builder.redCards;
        this.cleanSheet = builder.cleanSheet;
        this.goalsScored = builder.goalsScored;
        this.assists = builder.assists;
    }

    public static class Builder {
        private int points;
        private int gameWeek;
        private boolean wasBenched;
        private boolean wasInMyTeam;
        private boolean wasCaptain;
        private boolean wasViceCaptain;
        private boolean wasTripleCaptain;
        private int minutesPlayed;
        private int yellowCards;
        private int redCards;
        private int bonusPoints;
        private int goalsScored;
        private int assists;
        private boolean cleanSheet;

        public Builder gameWeek(int gameWeek) {
            this.gameWeek = gameWeek;
            return this;
        }

        public Builder points(int points) {
            this.points = points;
            return this;
        }

        public Builder wasBenched(boolean wasBenched) {
            this.wasBenched = wasBenched;
            return this;
        }

        public Builder wasInMyTeam(boolean wasInMyTeam) {
            this.wasInMyTeam = wasInMyTeam;
            return this;
        }

        public Builder wasCaptain(boolean wasCaptain) {
            this.wasCaptain = wasCaptain;
            return this;
        }

        public Builder wasViceCaptain(boolean wasViceCaptain) {
            this.wasViceCaptain = wasViceCaptain;
            return this;
        }

        public Builder wasTripleCaptain(boolean wasTripleCaptain) {
            this.wasTripleCaptain = wasTripleCaptain;
            return this;
        }

        public Builder minutesPlayed(int minutesPlayed) {
            this.minutesPlayed = minutesPlayed;
            return this;
        }

        public Builder yellowCards(int yellowCards) {
            this.yellowCards = yellowCards;
            return this;
        }

        public Builder redCards(int redCards) {
            this.redCards = redCards;
            return this;
        }

        public Builder bonusPoints(int bonusPoints) {
            this.bonusPoints = bonusPoints;
            return this;
        }

        public Builder goalsScored(int goalsScored) {
            this.goalsScored = goalsScored;
            return this;
        }

        public Builder assists(int assists) {
            this.assists = assists;
            return this;
        }

        public Builder cleanSheet(boolean cleanSheet) {
            this.cleanSheet = cleanSheet;
            return this;
        }


        public GameWeekPerformance build() {
            return new GameWeekPerformance(this);
        }
    }

    public int getGameWeek() {
        return gameWeek;
    }

    public void setGameWeek(int gameWeek) {
        this.gameWeek = gameWeek;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(int bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isWasBenched() {
        return wasBenched;
    }

    public void setWasBenched(boolean wasBenched) {
        this.wasBenched = wasBenched;
    }

    public boolean isWasInMyTeam() {
        return wasInMyTeam;
    }

    public void setWasInMyTeam(boolean wasInMyTeam) {
        this.wasInMyTeam = wasInMyTeam;
    }

    public boolean isWasCaptain() {
        return wasCaptain;
    }

    public void setWasCaptain(boolean wasCaptain) {
        this.wasCaptain = wasCaptain;
    }

    public boolean getWasViceCaptain() {
        return wasViceCaptain;
    }

    public void setWasViceCaptain(boolean wasViceCaptain) {
        this.wasViceCaptain = wasViceCaptain;
    }

    public boolean getWasTripleCaptain() {
        return wasTripleCaptain;
    }

    public void setWasTripleCaptain(boolean wasTripleCaptain) {
        this.wasTripleCaptain = wasTripleCaptain;
    }

    public int getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setMinutesPlayed(int minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }

    public int getYellowCards() {
        return yellowCards;
    }

    public void setYellowCards(int yellowCards) {
        this.yellowCards = yellowCards;
    }

    public int getRedCards() {
        return redCards;
    }

    public void setRedCards(int redCards) {
        this.redCards = redCards;
    }

    public boolean getCleanSheet() {
        return cleanSheet;
    }

    public void setCleanSheet(boolean cleanSheet) {
        this.cleanSheet = cleanSheet;
    }

    public int getGoalsScored() {
        return goalsScored;
    }

    public void setGoalsScored(int goalsScored) {
        this.goalsScored = goalsScored;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }
}