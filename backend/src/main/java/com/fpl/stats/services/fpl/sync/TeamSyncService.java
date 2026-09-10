package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.Team;
import com.fpl.stats.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Synchronizes FPL team data into the local database.
 * Receives pre-fetched data from the caller -- does not call the FPL API directly.
 */
@Service
@Transactional
public class TeamSyncService {

    private static final Logger log = LoggerFactory.getLogger(TeamSyncService.class);

    private final TeamRepository teamRepository;

    /**
     * Constructs a {@code TeamSyncService} with its required repository dependency.
     *
     * @param teamRepository repository used to persist team entities
     */
    public TeamSyncService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    /**
     * Persists or updates teams from the provided bootstrap data.
     *
     * @param teamsData the raw team list from the FPL bootstrap-static response
     * @return a map of FPL team ID to persisted {@link Team} entity
     */
    public Map<Integer, Team> syncTeams(List<Map<String, Object>> teamsData) {
        log.info("Syncing {} teams", teamsData.size());
        Map<Integer, Team> teamMap = new HashMap<>();
        List<Team> toSave = new ArrayList<>();

        for (Map<String, Object> teamData : teamsData) {
            try {
                int fplId = ((Number) teamData.get("id")).intValue();
                Team team = teamRepository.findByFplId(fplId).orElseGet(Team::new);
                mapTeamFields(team, teamData);
                toSave.add(team);
                teamMap.put(fplId, team);
            } catch (Exception e) {
                log.error("Failed to process team: {}", teamData, e);
            }
        }

        teamRepository.saveAll(toSave);
        log.info("Successfully synced {} teams", toSave.size());
        return teamMap;
    }

    /**
     * Maps raw FPL bootstrap-static team data onto a {@link Team} entity.
     *
     * @param team     the entity to populate (new or existing)
     * @param teamData the raw map from the FPL bootstrap-static {@code teams} array
     */
    private void mapTeamFields(Team team, Map<String, Object> teamData) {
        team.setFplId(((Number) teamData.get("id")).intValue());
        team.setCode(((Number) teamData.get("code")).intValue());
        team.setName((String) teamData.get("name"));
        team.setShortName((String) teamData.get("short_name"));
        team.setStrength(toInt(teamData.get("strength")));
        team.setPosition(toInt(teamData.get("position")));
        team.setPlayed(toInt(teamData.get("played")));
        team.setWin(toInt(teamData.get("win")));
        team.setDraw(toInt(teamData.get("draw")));
        team.setLoss(toInt(teamData.get("loss")));
        team.setPoints(toInt(teamData.get("points")));
    }

    /**
     * Safely converts an {@link Object} value from the FPL API response to an {@code int}.
     * Returns {@code 0} if the value is {@code null} or not a {@link Number}.
     *
     * @param value the raw value from the API response map
     * @return the integer value, or {@code 0} if absent
     */
    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
