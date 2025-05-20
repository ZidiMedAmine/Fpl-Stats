package com.fpl.stats.services.fpl;

import com.fpl.stats.domain.GameWeekPerformance;
import com.fpl.stats.exception.ExternalServiceException;
import com.fpl.stats.exception.PlayerDataNotFoundException;
import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlayerHistoryService {
    private final FplApiClient fplApiClient;
    private final CaptainService captainService;
    private static final String KEY_ELEMENT = "element";
    private static final Logger logger = LoggerFactory.getLogger(PlayerHistoryService.class);

    public PlayerHistoryService(FplApiClient fplApiClient, CaptainService captainService) {
        this.fplApiClient = fplApiClient;
        this.captainService = captainService;
    }

    /**
     * Retrieves the history of a player's performance by game week for a given team.
     * <p>
     * This method fetches player data from an external Fantasy Premier League API,
     * processes it along with the team's picks per game week, and returns a list of
     * {@link GameWeekPerformance} objects. Results are cached by playerId and teamId.
     * <p>
     * It handles scenarios including:
     * <ul>
     *   <li>Fetching triple captain week for the team.</li>
     *   <li>Mapping raw API data to the domain model.</li>
     *   <li>Identifying if the player was in the team, captain, vice-captain, benched, or triple captain.</li>
     *   <li>Gracefully handling external service failures with custom exceptions.</li>
     * </ul>
     *
     * @param playerId the ID of the player whose history is requested
     * @param teamId the ID of the team
     * @param teamPicksByGw a map of game week to team picks data for that game week
     * @return a list of {@link GameWeekPerformance} representing player's stats per game week
     * @throws ExternalServiceException if the external FPL service call fails
     * @throws PlayerDataNotFoundException if player history data is missing or malformed
     */
    @Cacheable(value = "playerHistory", key = "{#playerId, #teamId}", sync = true)
    public List<GameWeekPerformance> getPlayerHistory(int playerId, int teamId, Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        logger.info("Fetching history for player {} and team {}", playerId, teamId);

        Optional<Integer> tripleCaptainWeek = captainService.getTripleCaptainWeek(teamId);

        Map<String, Object> response;
        try {
            response = fplApiClient.get("/element-summary/" + playerId + "/");
        } catch (Exception e) {
            throw new ExternalServiceException("Failed to fetch player summary from external service for playerId: " + playerId, e);
        }

        if (response == null || !(response.get("history") instanceof List<?> historyList)) {
            throw new PlayerDataNotFoundException("No player history found or invalid format for playerId: " + playerId);
        }

        List<Map<String, Object>> history = historyList.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList();

        Set<Integer> gameWeeksInTeam = getGameWeeksWhenPlayerWasInTeam(playerId, teamPicksByGw);

        return history.stream()
                .map(h -> {
                    int gameWeek = toInt(h.get("round"));
                    boolean inTeam = gameWeeksInTeam.contains(gameWeek);
                    boolean captain = inTeam && wasPlayerCaptain(playerId, gameWeek, teamPicksByGw);
                    boolean tripleCaptain = inTeam && captain && tripleCaptainWeek.map(w -> w == gameWeek).orElse(false);

                    return new GameWeekPerformance.Builder()
                            .points(toInt(h.get("total_points")))
                            .gameWeek(gameWeek)
                            .wasBenched(inTeam && wasPlayerBenched(playerId, gameWeek, teamPicksByGw))
                            .wasInMyTeam(inTeam)
                            .wasCaptain(captain)
                            .wasViceCaptain(inTeam && wasPlayerViceCaptain(playerId, gameWeek, teamPicksByGw))
                            .wasTripleCaptain(tripleCaptain)
                            .minutesPlayed(toInt(h.get("minutes")))
                            .yellowCards(toInt(h.get("yellow_cards")))
                            .redCards(toInt(h.get("red_cards")))
                            .bonusPoints(toInt(h.get("bonus")))
                            .goalsScored(toInt(h.get("goals_scored")))
                            .assists(toInt(h.get("assists")))
                            .cleanSheet(toInt(h.get("clean_sheets")) == 1)
                            .build();
                })
                .toList();
    }

    /**
     * Converts an {@link Object} to an {@code int}.
     * <p>
     * If the object is an instance of {@link Number}, its integer value is returned.
     * If the object is a non-null {@link String} representation of a number, it is parsed.
     * Returns 0 if the object is {@code null}, not a number, or parsing fails.
     *
     * @param value the object to convert
     * @return the integer value, or 0 if conversion is not possible
     */
    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value != null ? Integer.parseInt(value.toString()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Determines the set of gameWeeks in which the specified player was part of the team.
     *
     * @param playerId         the ID of the player to check
     * @param teamPicksByGw    a map where each key is a gameWeek number and the value is the list of player picks for that gameWeek
     * @return a set of integers representing the gameWeeks in which the player was present in the team
     */
    private Set<Integer> getGameWeeksWhenPlayerWasInTeam(int playerId, Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        Set<Integer> gameWeeks = new HashSet<>();
        for (int gw = 1; gw <= 38; gw++) {
            try {
                List<Map<String, Object>> picks = teamPicksByGw.get(gw);
                boolean inTeam = picks.stream()
                        .anyMatch(p -> playerId == (Integer) p.get(KEY_ELEMENT));
                if (inTeam) {
                    gameWeeks.add(gw);
                }
            } catch (Exception e) {
                if(gw == 38){
                    break; // No more GameWeeks
                }
            }
        }
        return gameWeeks;
    }

    /**
     * Checks whether the specified player was benched in the given gameWeek.
     * A player is considered benched if their position is greater than 11.
     *
     * @param playerId         the ID of the player to check
     * @param gameWeek         the gameWeek number
     * @param teamPicksByGw    a map where the key is the gameWeek and the value is the list of picks for that gameWeek
     * @return {@code true} if the player was benched in the given gameWeek, {@code false} otherwise
     */
    private boolean wasPlayerBenched(int playerId,  int gameWeek, Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        List<Map<String, Object>> picks = teamPicksByGw.get(gameWeek);
        return picks.stream()
                .filter(p -> playerId == (Integer) p.get(KEY_ELEMENT))
                .findFirst()
                .map(p -> (Integer) p.get("position") > 11) // Position > 11 means benched
                .orElse(false);
    }

    /**
     * Determines if the specified player was the captain in the given gameWeek.
     *
     * @param playerId         the ID of the player to check
     * @param gameWeek         the gameWeek number
     * @param teamPicksByGw    a map where the key is the gameWeek and the value is the list of picks for that gameWeek
     * @return {@code true} if the player was captain in the specified gameWeek, {@code false} otherwise
     */
    private boolean wasPlayerCaptain(int playerId, int gameWeek, Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        List<Map<String, Object>> picks = teamPicksByGw.get(gameWeek);
        return picks.stream()
                .filter(p -> playerId == (Integer) p.get(KEY_ELEMENT))
                .findFirst()
                .map(p -> (Boolean) p.get("is_captain"))
                .orElse(false);
    }

    /**
     * Checks if a given player was assigned as the vice-captain during a specific gameWeek.
     *
     * @param playerId the ID of the player to check
     * @param gameWeek the gameWeek number
     * @param teamPicksByGw a map containing the team picks for each gameWeek
     * @return {@code true} if the player was vice-captain during the given gameWeek, {@code false} otherwise
     */
    private boolean wasPlayerViceCaptain(int playerId, int gameWeek, Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        List<Map<String, Object>> picks = teamPicksByGw.get(gameWeek);

        return picks.stream()
                .filter(p -> playerId == (Integer) p.get(KEY_ELEMENT))
                .findFirst()
                .map(p -> (Boolean) p.get("is_vice_captain"))
                .orElse(false);
    }


}
