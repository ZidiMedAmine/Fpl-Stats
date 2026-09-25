package com.fpl.stats.repository;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.PlayerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for {@link PlayerHistory} entities.
 */
@Repository
public interface PlayerHistoryRepository extends JpaRepository<PlayerHistory, UUID> {

    /**
     * Returns the set of gameweek numbers for which a history record already exists for the given player.
     * Used to skip already-synced entries without issuing one EXISTS query per record.
     *
     * @param player the player to check
     * @return set of already-persisted gameweek numbers
     */
    @Query("SELECT ph.gameWeek.gameWeekNumber FROM PlayerHistory ph WHERE ph.player = :player")
    Set<Integer> findExistingGameWeekNumbersByPlayer(@Param("player") Player player);

    /**
     * Returns all history records for the given set of FPL player IDs, with player and gameweek eagerly loaded.
     * Used for bulk transfer-impact computation to avoid N+1 queries.
     *
     * @param playerFplIds the set of FPL player IDs to fetch histories for
     * @return list of history records with player and gameWeek associations loaded
     */
    @Query("SELECT ph FROM PlayerHistory ph JOIN FETCH ph.player JOIN FETCH ph.gameWeek WHERE ph.player.fplId IN :playerFplIds")
    List<PlayerHistory> findAllByPlayerFplIds(@Param("playerFplIds") Set<Integer> playerFplIds);

    /**
     * Aggregates player points across the given gameweeks.
     *
     * <p>Returns one row per player as {@code [fplId, code, webName, position, teamShortName, nowCost, sumPoints]},
     * ordered by sumPoints descending. Used to build the last-N-weeks dream team candidates.</p>
     *
     * @param gameWeekNumbers the gameweek numbers to aggregate over
     * @return projection rows ordered by total points descending
     */
    @Query("SELECT ph.player.fplId, ph.player.code, ph.player.webName, ph.player.position, " +
           "ph.player.team.shortName, ph.player.nowCost, SUM(ph.points) " +
           "FROM PlayerHistory ph " +
           "WHERE ph.gameWeek.gameWeekNumber IN :gameWeekNumbers " +
           "GROUP BY ph.player.fplId, ph.player.code, ph.player.webName, ph.player.position, " +
           "ph.player.team.shortName, ph.player.nowCost " +
           "ORDER BY SUM(ph.points) DESC")
    List<Object[]> findPlayerPointsSumForGameWeeks(@Param("gameWeekNumbers") List<Integer> gameWeekNumbers);
}
