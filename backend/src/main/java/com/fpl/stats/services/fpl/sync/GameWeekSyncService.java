package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.GameWeek;
import com.fpl.stats.repository.GameWeekRepository;
import com.fpl.stats.services.fpl.FixtureDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Synchronizes FPL gameweek data into the local database.
 * Receives pre-fetched data from the caller -- does not call the FPL API directly.
 */
@Service
@Transactional
public class GameWeekSyncService {

    private static final Logger log = LoggerFactory.getLogger(GameWeekSyncService.class);

    private final GameWeekRepository gameWeekRepository;
    private final FixtureDataService fixtureDataService;

    /**
     * Constructs a {@code GameWeekSyncService} with its required dependencies.
     *
     * @param gameWeekRepository repository used to persist gameweek entities
     * @param fixtureDataService used to derive completed gameweek numbers from fixture data
     */
    public GameWeekSyncService(GameWeekRepository gameWeekRepository,
                               FixtureDataService fixtureDataService) {
        this.gameWeekRepository = gameWeekRepository;
        this.fixtureDataService = fixtureDataService;
    }

    /**
     * Persists or updates gameweeks from the provided bootstrap data.
     * The current gameweek is derived from fixture completion status rather than
     * the {@code is_current} flag, which can lag during FPL API gameweek transitions.
     *
     * @param gameWeeksData the raw events list from the FPL bootstrap-static response
     * @param fixturesData  the full fixture list from the FPL {@code /fixtures/} endpoint
     */
    public void syncGameWeeks(List<Map<String, Object>> gameWeeksData, List<Map<String, Object>> fixturesData) {
        log.info("Syncing {} gameweeks", gameWeeksData.size());
        int currentGwNumber = deriveCurrentGameWeekNumber(fixturesData);
        Set<Integer> completedGwNumbers = fixtureDataService.getCompletedGameWeekNumbers(fixturesData);
        log.info("Derived current gameweek: GW{}, completed gameweeks: {}", currentGwNumber, completedGwNumbers.size());
        List<GameWeek> toSave = new ArrayList<>();

        for (Map<String, Object> gwData : gameWeeksData) {
            int gwNumber = ((Number) gwData.get("id")).intValue();
            GameWeek gameWeek = gameWeekRepository.findByGameWeekNumber(gwNumber)
                    .orElseGet(GameWeek::new);
            mapGameWeekFields(gameWeek, gwData, currentGwNumber, completedGwNumbers);
            toSave.add(gameWeek);
        }

        gameWeekRepository.saveAll(toSave);
        log.info("Successfully synced {} gameweeks", toSave.size());
    }

    /**
     * Derives the current gameweek number from fixture completion status.
     * <p>
     * Fixtures are grouped by gameweek. The current GW is the lowest-numbered GW
     * where at least one fixture is not yet finished — meaning all previous GWs
     * are fully complete. For example, if GW1, GW2, and GW3 are all finished,
     * GW4 is the current gameweek.
     * <p>
     * Returns 0 if all fixtures are finished (end of season) or none have started yet.
     *
     * @param fixturesData the full fixture list from the FPL {@code /fixtures/} endpoint
     * @return the derived current gameweek number, or 0 if indeterminate
     */
    private int deriveCurrentGameWeekNumber(List<Map<String, Object>> fixturesData) {
        return fixturesData.stream()
                .filter(f -> f.get("event") != null && !Boolean.TRUE.equals(f.get("finished")))
                .mapToInt(f -> ((Number) f.get("event")).intValue())
                .min()
                .orElse(0);
    }

    /**
     * Maps raw FPL API gameweek data onto a {@link GameWeek} entity.
     * The is_current, is_next, and is_previous flags are derived from {@code currentGwNumber}
     * rather than the API flags, which can lag during gameweek transitions.
     *
     * @param gameWeek            the entity to populate (new or existing)
     * @param gwData              raw gameweek map from the FPL bootstrap-static response
     * @param currentGwNumber     the derived current gameweek number
     * @param completedGwNumbers  set of gameweek numbers where all fixtures are finished
     */
    private void mapGameWeekFields(GameWeek gameWeek, Map<String, Object> gwData,
                                   int currentGwNumber, Set<Integer> completedGwNumbers) {
        int gwNumber = ((Number) gwData.get("id")).intValue();
        gameWeek.setGameWeekNumber(gwNumber);
        gameWeek.setName((String) gwData.get("name"));
        gameWeek.setIsCurrent(gwNumber == currentGwNumber);
        gameWeek.setNext(gwNumber == currentGwNumber + 1);
        gameWeek.setPrevious(gwNumber == currentGwNumber - 1);
        gameWeek.setFinished(completedGwNumbers.contains(gwNumber));

        Object deadlineTime = gwData.get("deadline_time");
        if (deadlineTime instanceof String deadlineStr) {
            gameWeek.setDeadlineTime(Instant.parse(deadlineStr));
        }

        Object avgScore = gwData.get("average_entry_score");
        gameWeek.setAverageScore(avgScore != null ? ((Number) avgScore).intValue() : null);

        Object highScore = gwData.get("highest_score");
        gameWeek.setHighestScore(highScore != null ? ((Number) highScore).intValue() : null);
    }
}
