package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.Team;
import com.fpl.stats.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Synchronizes FPL player data into the local database.
 * Receives pre-fetched data from the caller -- does not call the FPL API directly.
 */
@Service
@Transactional
public class PlayerSyncService {

    private static final Logger log = LoggerFactory.getLogger(PlayerSyncService.class);

    private final PlayerRepository playerRepository;

    /**
     * Constructs a {@code PlayerSyncService} with its required repository dependency.
     *
     * @param playerRepository repository used to persist player entities
     */
    public PlayerSyncService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Persists or updates players from the provided bootstrap data.
     *
     * @param playersData    the raw elements list from the FPL bootstrap-static response
     * @param teamsByFplId   lookup map of FPL team ID to persisted {@link Team} entity
     * @param positionMap    lookup map of element_type ID to position abbreviation (GKP, DEF, MID, FWD)
     */
    public void syncPlayers(List<Map<String, Object>> playersData,
                            Map<Integer, Team> teamsByFplId,
                            Map<Integer, String> positionMap) {
        log.info("Syncing {} players", playersData.size());
        List<Player> toSave = new ArrayList<>();

        for (Map<String, Object> playerData : playersData) {
            try {
                int fplId = ((Number) playerData.get("id")).intValue();
                Player player = playerRepository.findByFplId(fplId).orElseGet(Player::new);
                mapPlayerFields(player, playerData, teamsByFplId, positionMap);
                toSave.add(player);
            } catch (Exception e) {
                log.error("Failed to process player: {}", playerData.get("web_name"), e);
            }
        }

        playerRepository.saveAll(toSave);
        log.info("Successfully synced {} players", toSave.size());
    }

    /**
     * Maps raw FPL bootstrap-static player data onto a {@link Player} entity.
     *
     * @param player        the entity to populate (new or existing)
     * @param playerData    the raw map from the FPL bootstrap-static {@code elements} array
     * @param teamsByFplId  lookup map of FPL team ID to persisted {@link Team} entity
     * @param positionMap   lookup map of element_type ID to position abbreviation (GKP, DEF, MID, FWD)
     */
    private void mapPlayerFields(Player player, Map<String, Object> playerData,
                                 Map<Integer, Team> teamsByFplId,
                                 Map<Integer, String> positionMap) {
        player.setFplId(((Number) playerData.get("id")).intValue());
        player.setFirstName((String) playerData.get("first_name"));
        player.setSecondName((String) playerData.get("second_name"));
        player.setWebName((String) playerData.get("web_name"));
        player.setCode(((Number) playerData.get("code")).intValue());
        player.setNowCost(((Number) playerData.get("now_cost")).doubleValue() / 10.0);
        player.setTotalPoints(((Number) playerData.get("total_points")).intValue());
        player.setForm(String.valueOf(playerData.get("form")));
        player.setStatus((String) playerData.get("status"));

        Object selectedBy = playerData.get("selected_by_percent");
        if (selectedBy != null) {
            player.setSelectedByPercent(Double.parseDouble(String.valueOf(selectedBy)));
        }

        int elementType = ((Number) playerData.get("element_type")).intValue();
        player.setPosition(positionMap.getOrDefault(elementType, "UNK"));

        int teamFplId = ((Number) playerData.get("team")).intValue();
        Team team = teamsByFplId.get(teamFplId);
        player.setTeam(team);
    }
}
