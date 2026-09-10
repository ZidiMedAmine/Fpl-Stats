package com.fpl.stats.repository;

import com.fpl.stats.domain.UserTeamRankHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for per-gameweek rank history of a user's FPL team.
 */
@Repository
public interface UserTeamRankHistoryRepository extends JpaRepository<UserTeamRankHistory, UUID> {

    /** Returns all rank history entries for the given team, ordered by gameweek ascending. */
    List<UserTeamRankHistory> findAllByUserTeam_FplTeamIdOrderByGameWeekAsc(long fplTeamId);

    Optional<UserTeamRankHistory> findByUserTeam_FplTeamIdAndGameWeek(long fplTeamId, int gameWeek);
}
