package com.fpl.stats.repository;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.domain.UserTeam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link UserPick} entities.
 */
@Repository
public interface UserPickRepository extends JpaRepository<UserPick, UUID> {

    /**
     * Returns all picks for a user team with player, team, and gameweek eagerly loaded.
     * Use this when pick context (captain, multiplier, benched) and full player data are needed.
     *
     * @param userTeam the owning user team
     * @return list of picks with full graph loaded
     */
    @EntityGraph(attributePaths = {"player", "player.team", "gameWeek"})
    List<UserPick> findAllByUserTeam(UserTeam userTeam);

    /**
     * Returns all picks for a user team in a specific gameweek, with player, team, and gameweek eagerly loaded.
     *
     * @param userTeam       the owning user team
     * @param gameWeekNumber the gameweek number to filter by
     * @return list of picks for that gameweek with full graph loaded
     */
    @EntityGraph(attributePaths = {"player", "player.team", "gameWeek"})
    List<UserPick> findAllByUserTeamAndGameWeek_GameWeekNumber(UserTeam userTeam, int gameWeekNumber);

    /**
     * Returns the distinct set of {@link Player} entities ever picked by the given user team.
     * Does not load player histories or team associations — use this when only the player identity
     * (FPL ID) is needed, such as when queuing a history sync.
     *
     * @param userTeam the owning user team
     * @return list of distinct player entities without lazy associations loaded
     */
    @Query("SELECT DISTINCT up.player FROM UserPick up WHERE up.userTeam = :userTeam")
    List<Player> findDistinctPlayersByUserTeam(@Param("userTeam") UserTeam userTeam);
}
