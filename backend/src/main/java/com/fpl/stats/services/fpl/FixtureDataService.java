package com.fpl.stats.services.fpl;

import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fetches fixture data from the FPL {@code /fixtures/} endpoint.
 * Used to derive accurate gameweek status based on real match completion,
 * as an alternative to the {@code is_current} flag which can lag during transitions.
 */
@Service
public class FixtureDataService {

    private static final Logger log = LoggerFactory.getLogger(FixtureDataService.class);
    private static final String FIXTURES_ENDPOINT = "/fixtures/";

    private final FplApiClient fplApiClient;

    /**
     * @param fplApiClient the FPL API client used to perform HTTP requests
     */
    public FixtureDataService(FplApiClient fplApiClient) {
        this.fplApiClient = fplApiClient;
    }

    /**
     * Fetches all fixtures for the current season from the FPL API.
     *
     * @return a list of fixture maps, each containing fields such as {@code event},
     *         {@code started}, {@code finished}, {@code kickoff_time}, and scores
     */
    public List<Map<String, Object>> getFixtures() {
        log.info("Fetching fixtures from FPL API");
        return fplApiClient.getList(FIXTURES_ENDPOINT);
    }

    /**
     * Returns the set of gameweek numbers that are truly complete — every fixture in the
     * gameweek has {@code finished: true}. Operates on pre-fetched fixture data so no
     * additional API call is made.
     *
     * @param fixturesData the full fixture list already fetched from the FPL API
     * @return set of fully completed gameweek numbers
     */
    public Set<Integer> getCompletedGameWeekNumbers(List<Map<String, Object>> fixturesData) {
        Map<Integer, List<Map<String, Object>>> fixturesByGw = fixturesData.stream()
                .filter(f -> f.get("event") != null)
                .collect(Collectors.groupingBy(f -> ((Number) f.get("event")).intValue()));

        return fixturesByGw.entrySet().stream()
                .filter(e -> e.getValue().stream().allMatch(f -> Boolean.TRUE.equals(f.get("finished"))))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Derives the last truly completed gameweek from the current fixture data.
     * A gameweek is considered complete only when every one of its fixtures has
     * {@code finished: true}. This is more reliable than the bootstrap-static
     * {@code finished} flag, which can lag after a gameweek ends.
     *
     * @return the highest gameweek number where all fixtures are finished, or 0 if none
     */
    public int getLastCompletedGameWeek() {
        Map<Integer, List<Map<String, Object>>> fixturesByGw = getFixtures().stream()
                .filter(f -> f.get("event") != null)
                .collect(Collectors.groupingBy(f -> ((Number) f.get("event")).intValue()));

        return fixturesByGw.entrySet().stream()
                .filter(e -> e.getValue().stream().allMatch(f -> Boolean.TRUE.equals(f.get("finished"))))
                .mapToInt(Map.Entry::getKey)
                .max()
                .orElse(0);
    }
}
