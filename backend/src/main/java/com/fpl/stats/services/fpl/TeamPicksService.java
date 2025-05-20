package com.fpl.stats.services.fpl;

import com.fpl.stats.exception.TeamNotFoundException;
import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TeamPicksService {
    private final FplApiClient fplApiClient;
    private static final Logger logger = LoggerFactory.getLogger(TeamPicksService.class);

    public TeamPicksService(FplApiClient fplApiClient) {
        this.fplApiClient = fplApiClient;
    }

    /**
     * Retrieves the team picks for a specific Fantasy Premier League (FPL) team and game week.
     * <p>
     * The method fetches the pick data from the FPL API and returns it as a list of maps,
     * where each map represents a player's pick details (e.g., element ID, captain status, etc.).
     * <p>
     * The result is cached using Spring's {@code @Cacheable} mechanism to improve performance
     * and reduce API calls. Caching is keyed by team ID and game week.
     *
     * @param teamId    the unique ID of the FPL team
     * @param gameWeek  the specific game week to retrieve picks for (1-38)
     * @return a list of maps containing pick data for the specified team and game week
     * @throws TeamNotFoundException if the team could not be found or the API request fails
     * @throws IllegalStateException if the response format does not include a valid "picks" list
     */
    @Cacheable(value = "teamPicks", key = "{#teamId, #gameWeek}", sync = true)
    public List<Map<String, Object>> getTeamPicks(int teamId, int gameWeek) {
        logger.info("Fetching team {} picks for gameWeek {}", teamId, gameWeek);
        try {
            Map<String, Object> response = fplApiClient.get("/entry/" + teamId + "/event/" + gameWeek + "/picks/");
            if (Objects.nonNull(response) && response.get("picks") instanceof List<?> picks) {
                return (List<Map<String, Object>>) picks;
            }
            throw new IllegalStateException("Expected 'picks' to be a List<Map<String, Object>>");

        }catch (Exception e) {
            throw new TeamNotFoundException(teamId);
        }
    }

}