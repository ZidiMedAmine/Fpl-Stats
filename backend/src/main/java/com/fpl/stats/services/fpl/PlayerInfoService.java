package com.fpl.stats.services.fpl;

import com.fpl.stats.exception.PlayerDataNotFoundException;
import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlayerInfoService {
    private final FplApiClient fplApiClient;
    private static final Logger logger = LoggerFactory.getLogger(PlayerInfoService.class);

    public PlayerInfoService(FplApiClient fplApiClient) {
        this.fplApiClient = fplApiClient;
    }

    /**
     * Fetches and returns player information from the Fantasy Premier League API.
     * <p>
     * Each player's information includes:
     * <ul>
     *   <li>name – the web name of the player</li>
     *   <li>position – the player's role (GK, DEF, MID, FWD)</li>
     *   <li>photoCode – the OPTA code associated with the player's photo</li>
     * </ul>
     *
     * @return a map where the key is the player ID and the value is another map containing
     *         the player's name, position, and photo code
     * @throws PlayerDataNotFoundException if the API response is null or in an unexpected format
     */
    @Cacheable(value = "playerInfo", sync = true)
    public Map<Integer, Map<String, String>> getPlayerInfo() {
        logger.info("Fetching players information");

        Map<String, Object> response = fplApiClient.get("/bootstrap-static/");
        Map<Integer, Map<String, String>> playerInfo = new HashMap<>();
        if (response != null && response.get("elements") instanceof List) {
            List<Map<String, Object>> players = (List<Map<String, Object>>) response.get("elements");
            players.forEach(p -> {
                int id = (Integer) p.get("id");
                String name = (String) p.get("web_name");
                String photoCode = p.get("opta_code").toString();
                String position = switch ((Integer) p.get("element_type")) {
                    case 1 -> "GK"; case 2 -> "DEF"; case 3 -> "MID"; case 4 -> "FWD"; default -> "Manager";
                };
                playerInfo.put(id, Map.of("name", name, "position", position, "photoCode", photoCode));
            });
            return playerInfo;
        }
        throw new PlayerDataNotFoundException("No player data found or invalid format.");
    }
}
