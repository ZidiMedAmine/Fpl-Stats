package com.fpl.stats.repository;

import com.fpl.stats.domain.TrackedTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackedTeamRepository extends JpaRepository<TrackedTeam, UUID> {
    List<TrackedTeam> findAllByActiveTrue();
    boolean existsByFplTeamId(long fplTeamId);
}
