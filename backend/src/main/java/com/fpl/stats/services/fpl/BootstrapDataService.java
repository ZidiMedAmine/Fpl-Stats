package com.fpl.stats.services.fpl;

import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Single cached source for the FPL /bootstrap-static/ endpoint.
 * All services that need player, team, or gameweek data from the API
 * must go through this service instead of calling FplApiClient directly.
 */
@Service
public class BootstrapDataService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapDataService.class);
    private static final String BOOTSTRAP_ENDPOINT = "/bootstrap-static/";

    private final FplApiClient fplApiClient;

    /**
     * Constructs a {@code BootstrapDataService} with its required API client dependency.
     *
     * @param fplApiClient the FPL API client used to fetch bootstrap-static data
     */
    public BootstrapDataService(FplApiClient fplApiClient) {
        this.fplApiClient = fplApiClient;
    }

    /**
     * Fetches and caches the full bootstrap-static response (~2MB).
     * Cached for 10 minutes (configured in application.properties).
     */
    @Cacheable(value = "bootstrap", sync = true)
    public Map<String, Object> getBootstrapData() {
        log.info("Fetching bootstrap-static data from FPL API");
        return fplApiClient.get(BOOTSTRAP_ENDPOINT);
    }

    /**
     * Returns the raw player (element) list from the cached bootstrap-static response.
     *
     * @return list of player data maps keyed by FPL field names
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPlayers() {
        return (List<Map<String, Object>>) getBootstrapData().get("elements");
    }

    /**
     * Returns the raw team list from the cached bootstrap-static response.
     *
     * @return list of team data maps keyed by FPL field names
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTeams() {
        return (List<Map<String, Object>>) getBootstrapData().get("teams");
    }

    /**
     * Returns the raw gameweek (events) list from the cached bootstrap-static response.
     *
     * @return list of gameweek data maps keyed by FPL field names
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getGameWeeks() {
        return (List<Map<String, Object>>) getBootstrapData().get("events");
    }

    /**
     * Returns the raw element-type list from the cached bootstrap-static response.
     * Element types map to player positions (GKP, DEF, MID, FWD).
     *
     * @return list of element-type data maps keyed by FPL field names
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getElementTypes() {
        return (List<Map<String, Object>>) getBootstrapData().get("element_types");
    }

    /**
     * Returns the current gameweek number derived from deadline_time.
     * The current GW is the highest-numbered GW whose deadline has already passed.
     * More reliable than is_current, which lags during FPL API gameweek transitions.
     * Falls back to is_current if no deadline has passed yet (pre-season).
     */
    public OptionalInt getCurrentGameWeek() {
        Instant now = Instant.now();
        OptionalInt byDeadline = getGameWeeks().stream()
                .filter(gw -> {
                    String deadline = (String) gw.get("deadline_time");
                    return deadline != null && Instant.parse(deadline).isBefore(now);
                })
                .mapToInt(gw -> ((Number) gw.get("id")).intValue())
                .max();

        if (byDeadline.isPresent()) {
            return byDeadline;
        }

        log.warn("No GW deadline has passed yet — falling back to is_current flag");
        return getGameWeeks().stream()
                .filter(gw -> Boolean.TRUE.equals(gw.get("is_current")))
                .mapToInt(gw -> ((Number) gw.get("id")).intValue())
                .findFirst();
    }

    /**
     * Returns the last completed (finished) gameweek number.
     * Falls back to 0 if no gameweeks are finished.
     */
    public int getLastCompletedGameWeek() {
        return getGameWeeks().stream()
                .filter(gw -> Boolean.TRUE.equals(gw.get("finished")))
                .mapToInt(gw -> ((Number) gw.get("id")).intValue())
                .max()
                .orElse(0);
    }

    /**
     * Builds and caches a map from element_type ID to position name (GKP, DEF, MID, FWD).
     * Cached alongside the bootstrap data to avoid rebuilding on every call.
     *
     * @return an unmodifiable map of element_type ID to position abbreviation
     */
    @Cacheable(value = "bootstrap", key = "'positionMap'")
    public Map<Integer, String> getPositionMap() {
        List<Map<String, Object>> elementTypes = getElementTypes();
        Map<Integer, String> positionMap = new HashMap<>();
        for (Map<String, Object> et : elementTypes) {
            int typeId = ((Number) et.get("id")).intValue();
            String singularName = (String) et.get("singular_name_short");
            positionMap.put(typeId, singularName);
        }
        return Collections.unmodifiableMap(positionMap);
    }
}
