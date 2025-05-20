package com.fpl.stats.services;

import com.fpl.stats.domain.GameWeekPerformance;
import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.UserInfo;
import com.fpl.stats.services.fpl.FplApiService;
import com.fpl.stats.services.fpl.TeamPicksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserInfoService {
    private final FplApiService fplApiService;
    private final TeamPicksService teamPicksService;
    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    public UserInfoService(FplApiService fplApiService, TeamPicksService teamPicksService) {
        this.fplApiService = fplApiService;
        this.teamPicksService = teamPicksService;
    }

    public UserInfo getUserInfo(int teamId) {
        logger.info("Fetching team players for team id {}", teamId);
        Map<Integer, List<Map<String, Object>>> teamPicksByGw = getAllTeamPicksByGameWeek(teamId);
        Set<Integer> playerIds = fplApiService.getPlayersUsedByTeam(teamPicksByGw);
        Map<Integer, Map<String, String>> playerInfo = fplApiService.getPlayerInfo();
        UserInfo user = fplApiService.getUserInfo(teamId);
        user.setPlayers(playerIds.stream()
                .map(id -> buildPlayerData(id, teamId, playerInfo, teamPicksByGw)).toList());
        return user;
    }

    private Player buildPlayerData(int playerId, int teamId, Map<Integer, Map<String, String>> playerInfo, Map<Integer, List<Map<String, Object>>> teamPicksByGw) {
        Player player = new Player();
        player.setId(playerId);
        player.setName(playerInfo.get(playerId).get("name"));
        player.setPosition(playerInfo.get(playerId).get("position"));
        player.setPhotoCode(playerInfo.get(playerId).get("photoCode"));
        List<GameWeekPerformance> performances = fplApiService.getPlayerHistory(playerId, teamId, teamPicksByGw);
            player.setPerformances(performances);

        calculatePlayerStats(player, performances);

        return player;
    }

    private void calculatePlayerStats(Player player, List<GameWeekPerformance> performances) {
        int totalPoints = performances.stream()
            .mapToInt(GameWeekPerformance::getPoints)
            .sum();
        
        long gamesStarted = performances.stream()
            .filter(p -> !p.isWasBenched())
            .count();
        
        player.setTotalPointsForTeam(totalPoints);
        player.setAvgPoints(gamesStarted > 0 ? (double) totalPoints / gamesStarted : 0);
    }

    private Map<Integer, List<Map<String, Object>>> getAllTeamPicksByGameWeek(int teamId) {
        Map<Integer, List<Map<String, Object>>> allPicks = new HashMap<>();
        for (int gw = 1; gw <= 38; gw++) {
            try {
                allPicks.put(gw, teamPicksService.getTeamPicks(teamId, gw));
            } catch (Exception e) {
                logger.info("{}, gameWeek {}",e.getMessage(), gw);
                if(gw == 38) {
                    break;
                }
            }
        }
        return allPicks;
    }
}