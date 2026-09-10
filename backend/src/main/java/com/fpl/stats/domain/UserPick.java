package com.fpl.stats.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_pick", uniqueConstraints =
    @UniqueConstraint(columnNames = {"user_team_id", "gameweek_id", "player_id"}))
public class UserPick extends BaseEntity {

    private boolean isCaptain;
    private boolean isViceCaptain;
    private boolean isTripleCaptain;
    private boolean isBenched;
    private int multiplier;
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_team_id", nullable = false)
    private UserTeam userTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gameweek_id", nullable = false)
    private GameWeek gameWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    public boolean isCaptain() { return isCaptain; }
    public void setCaptain(boolean captain) { isCaptain = captain; }
    public boolean isViceCaptain() { return isViceCaptain; }
    public void setViceCaptain(boolean viceCaptain) { isViceCaptain = viceCaptain; }
    public boolean isTripleCaptain() { return isTripleCaptain; }
    public void setTripleCaptain(boolean tripleCaptain) { isTripleCaptain = tripleCaptain; }
    public boolean isBenched() { return isBenched; }
    public void setBenched(boolean benched) { isBenched = benched; }
    public int getMultiplier() { return multiplier; }
    public void setMultiplier(int multiplier) { this.multiplier = multiplier; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public UserTeam getUserTeam() { return userTeam; }
    public void setUserTeam(UserTeam userTeam) { this.userTeam = userTeam; }
    public GameWeek getGameWeek() { return gameWeek; }
    public void setGameWeek(GameWeek gameWeek) { this.gameWeek = gameWeek; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPick other)) return false;
        if (userTeam == null || other.userTeam == null) return false;
        if (gameWeek == null || other.gameWeek == null) return false;
        if (player == null || other.player == null) return false;
        return userTeam.getFplTeamId() == other.userTeam.getFplTeamId()
            && gameWeek.getGameWeekNumber() == other.gameWeek.getGameWeekNumber()
            && player.getFplId() == other.player.getFplId();
    }

    @Override
    public int hashCode() {
        long teamId = userTeam != null ? userTeam.getFplTeamId() : 0;
        int gwNum = gameWeek != null ? gameWeek.getGameWeekNumber() : 0;
        int playerId = player != null ? player.getFplId() : 0;
        return Long.hashCode(teamId) * 31 * 31 + gwNum * 31 + playerId;
    }

    @Override
    public String toString() {
        return "UserPick{position=" + position + ", multiplier=" + multiplier + "}";
    }
}
