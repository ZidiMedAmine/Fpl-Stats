package com.fpl.stats.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "tracked_team", uniqueConstraints = @UniqueConstraint(columnNames = "fplTeamId"))
public class TrackedTeam extends BaseEntity {

    @Column(nullable = false, unique = true)
    private long fplTeamId;

    private boolean active = true;

    private Instant lastSyncedAt;

    public long getFplTeamId() { return fplTeamId; }
    public void setFplTeamId(long fplTeamId) { this.fplTeamId = fplTeamId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Instant lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackedTeam other)) return false;
        return fplTeamId == other.fplTeamId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(fplTeamId);
    }

    @Override
    public String toString() {
        return "TrackedTeam{fplTeamId=" + fplTeamId + ", active=" + active + "}";
    }
}
