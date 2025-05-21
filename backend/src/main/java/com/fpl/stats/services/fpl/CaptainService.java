package com.fpl.stats.services.fpl;

import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CaptainService {
    private final FplApiClient fplApiClient;
    private static final Logger logger = LoggerFactory.getLogger(CaptainService.class);

    public CaptainService(FplApiClient fplApiClient) {
        this.fplApiClient = fplApiClient;
    }

    /**
     * Retrieves the gameWeek in which the Triple Captain chip was used for a given team.
     *
     * @param teamId the FPL team ID
     * @return an {@link Optional} containing the gameWeek number if the Triple Captain chip was used, otherwise empty
     */
    @Cacheable(value = "tripleCaptainWeek", key = "#teamId", sync = true)
    public Optional<Integer> getTripleCaptainWeek(int teamId) {
        logger.info("Fetching for user {} triple captain chip", teamId);
        try {
            Map<String, Object> response = fplApiClient.get("/entry/" + teamId + "/history/");
            if (response == null || !response.containsKey("chips")) {
                return Optional.empty();
            }

            List<Map<String, Object>> chips = (List<Map<String, Object>>) response.get("chips");

            return chips.stream()
                    .filter(chip -> "3xc".equals(chip.get("name")))
                    .map(chip -> ((Number) chip.get("event")).intValue())
                    .findFirst();

        } catch (Exception ex) {
            logger.error("Failed to fetch triple captain chip for user {}: {}", teamId, ex.getMessage(), ex);
            return Optional.empty();
        }
    }
}
