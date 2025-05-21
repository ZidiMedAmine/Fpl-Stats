package com.fpl.stats.services.fpl;

import com.fpl.stats.domain.GameWeekPerformance;
import com.fpl.stats.domain.UserInfo;
import com.fpl.stats.exception.PlayerDataNotFoundException;
import com.fpl.stats.services.util.FplApiClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FplApiService {
    private final FplApiClient fplApiClient;
    private final PlayerInfoService playerInfoService;
    private final PlayerHistoryService playerHistoryService;

    public FplApiService(FplApiClient fplApiClient, PlayerInfoService playerInfoService, PlayerHistoryService playerHistoryService) {
        this.fplApiClient = fplApiClient;
        this.playerInfoService = playerInfoService;
        this.playerHistoryService = playerHistoryService;
    }

    /**
     * Retrieves and caches basic user information for a given FPL team ID.
     *
     * @param teamId the FPL team ID
     * @return populated {@link UserInfo} object
     * @throws PlayerDataNotFoundException if the data is missing or the API call fails
     */
    @Cacheable(value = "userInfo", key = "#teamId")
    public UserInfo getUserInfo(int teamId) {
        try{
            Map<String, Object> response = fplApiClient.get("/entry/" + teamId + "/");

            if (response == null || response.isEmpty()) {
                throw new PlayerDataNotFoundException("No player data found for teamId: " + teamId);
            }
            UserInfo user = new UserInfo();
            user.setId(teamId);
            user.setName(response.get("player_first_name") + " " + response.get("player_last_name"));
            user.setTeamName((String) response.get("name"));
            user.setRegion((String) response.get("player_region_name"));
            user.setOverallRank((Integer) response.get("summary_overall_rank"));
            user.setTotalPoints((Integer) response.get("summary_overall_points"));
            user.setCurrentGameWeek((Integer) response.get("current_event"));
            return user;
        }catch (Exception e){
            throw new PlayerDataNotFoundException("No player data found or invalid format.");
        }
    }

    /**
     * Extracts the unique player IDs used by a team across all gameWeeks.
     *
     * @param teamPicksByGw a map where the key is the gameWeek number and the value is a list of player picks
     * @return a set of unique player IDs used by the team
     */
    public Set<Integer> getPlayersUsedByTeam(Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        Set<Integer> playerIds = new HashSet<>();
        for (int gw = 1; gw <= 38; gw++) {
            try {
                List<Map<String, Object>> picks = teamPicksByGw.get(gw);
                picks.forEach(pick -> playerIds.add((Integer) pick.get("element")));
            } catch (Exception e) {
                if(gw == 38){
                    break; // No more GameWeeks
                }
            }
        }
        return playerIds;
    }

    /**
     * Retrieves the gameWeek-by-gameWeek performance history for a specific player.
     *
     * @param playerId the player's ID
     * @param teamId the FPL team ID
     * @param teamPicksByGw map of team picks grouped by gameWeek
     * @return list of {@link GameWeekPerformance} representing the player's history
     */
    public List<GameWeekPerformance> getPlayerHistory(int playerId, int teamId, Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        return playerHistoryService.getPlayerHistory(playerId, teamId, teamPicksByGw);
    }

    /**
     * Retrieves detailed information for all players.
     *
     * @return a map where the key is the player ID and the value is a map of player attributes
     */
    public Map<Integer, Map<String, String>>  getPlayerInfo() {
        return playerInfoService.getPlayerInfo();
    }
}