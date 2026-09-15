package com.fpl.stats.repository;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.PlayerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

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
}
