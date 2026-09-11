package com.fpl.stats.repository;

import com.fpl.stats.domain.GameWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameWeekRepository extends JpaRepository<GameWeek, UUID> {
    Optional<GameWeek> findByGameWeekNumber(int gameWeekNumber);
    Optional<GameWeek> findByIsCurrentTrue();

    @Query("SELECT g.gameWeekNumber, g.averageScore FROM GameWeek g WHERE g.averageScore IS NOT NULL")
    List<Object[]> findAllGameWeekAverages();
}
