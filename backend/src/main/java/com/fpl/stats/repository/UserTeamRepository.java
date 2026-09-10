package com.fpl.stats.repository;

import com.fpl.stats.domain.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, UUID> {
    Optional<UserTeam> findByFplTeamId(long fplTeamId);
}
