package com.fpl.stats.repository;

import com.fpl.stats.domain.GameWeek;
import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.PlayerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerHistoryRepository extends JpaRepository<PlayerHistory, UUID> {
    boolean existsByPlayerAndGameWeek(Player player, GameWeek gameWeek);
}
