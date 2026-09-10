package com.fpl.stats.repository;

import com.fpl.stats.domain.Player;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findByFplId(int fplId);

    @Query("SELECT p FROM Player p WHERE p.position = :position ORDER BY p.totalPoints DESC")
    List<Player> findByPosition(@Param("position") String position);

    @EntityGraph(attributePaths = {"playerHistories", "playerHistories.gameWeek", "team"})
    @Query("SELECT p FROM Player p WHERE p.fplId = :fplId")
    Optional<Player> findByFplIdWithHistory(@Param("fplId") int fplId);

    /**
     * Fetches multiple players by their FPL IDs in a single query, with histories,
     * gameweeks, and team eagerly loaded. Use this instead of calling
     * {@link #findByFplIdWithHistory} in a loop.
     *
     * @param fplIds the set of FPL player IDs to fetch
     * @return list of matching players with full history graph loaded
     */
    @EntityGraph(attributePaths = {"playerHistories", "playerHistories.gameWeek", "team"})
    @Query("SELECT p FROM Player p WHERE p.fplId IN :fplIds")
    List<Player> findByFplIdInWithHistory(@Param("fplIds") java.util.Collection<Integer> fplIds);
}
