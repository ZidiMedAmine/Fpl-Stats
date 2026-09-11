package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.domain.UserTeamRankHistory;
import com.fpl.stats.repository.UserTeamRankHistoryRepository;
import com.fpl.stats.repository.UserTeamRepository;
import com.fpl.stats.services.fpl.FixtureDataService;
import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Synchronizes a single user's team metadata and per-gameweek rank history
 * from the FPL entry and entry history endpoints.
 * Fetches its own per-user data directly from the API.
 */
@Service
@Transactional
public class UserTeamSyncService {

    private static final Logger log = LoggerFactory.getLogger(UserTeamSyncService.class);

    private final UserTeamRankHistoryRepository userTeamRankHistoryRepository;
    private final UserTeamRepository userTeamRepository;
    private final FixtureDataService fixtureDataService;
    private final FplApiClient fplApiClient;

    /**
     * @param fplApiClient                    used to fetch entry and history data per user
     * @param userTeamRepository              persists user team metadata
     * @param userTeamRankHistoryRepository   persists per-GW rank history entries
     * @param fixtureDataService              used to derive the last truly completed gameweek
     */
    public UserTeamSyncService(UserTeamRankHistoryRepository userTeamRankHistoryRepository,
                               UserTeamRepository userTeamRepository,
                               FixtureDataService fixtureDataService,
                               FplApiClient fplApiClient) {
        this.userTeamRankHistoryRepository = userTeamRankHistoryRepository;
        this.userTeamRepository = userTeamRepository;
        this.fixtureDataService = fixtureDataService;
        this.fplApiClient = fplApiClient;
    }

    /**
     * Fetches the FPL entry and entry history for the given team ID,
     * persists or updates the local record, and syncs per-GW rank history.
     *
     * @param fplTeamId the FPL team/entry ID
     * @return the persisted {@link UserTeam}
     * @throws com.fpl.stats.exception.SyncException if the FPL API call or persistence fails
     */
    public UserTeam syncUserTeam(long fplTeamId) {
        log.info("Syncing user team {}", fplTeamId);
        Map<String, Object> entryData = fplApiClient.get("/entry/" + fplTeamId + "/");
        UserTeam userTeam = userTeamRepository.findByFplTeamId(fplTeamId)
                .orElseGet(UserTeam::new);
        mapUserTeamFields(userTeam, entryData, fplTeamId);
        UserTeam savedTeam = userTeamRepository.save(userTeam);

        syncRankHistory(savedTeam);

        return savedTeam;
    }

    /**
     * Maps raw FPL entry API data onto a {@link UserTeam} entity.
     *
     * @param userTeam  the entity to populate (new or existing)
     * @param entryData the raw map from the FPL {@code /entry/{id}/} response
     * @param fplTeamId the FPL team/entry ID
     */
    private void mapUserTeamFields(UserTeam userTeam, Map<String, Object> entryData,
                                   long fplTeamId) {
        userTeam.setFplTeamId(fplTeamId);
        userTeam.setPlayerFirstName((String) entryData.get("player_first_name"));
        userTeam.setPlayerLastName((String) entryData.get("player_last_name"));
        userTeam.setTeamName((String) entryData.get("name"));
        userTeam.setRegion((String) entryData.get("player_region_name"));

        Object rank = entryData.get("summary_overall_rank");
        userTeam.setOverallRank(rank != null ? ((Number) rank).intValue() : null);

        Object points = entryData.get("summary_overall_points");
        userTeam.setTotalPoints(points != null ? ((Number) points).intValue() : null);

        Object startedEvent = entryData.get("started_event");
        userTeam.setStartedEvent(startedEvent != null ? ((Number) startedEvent).intValue() : null);

        Object teamValue = entryData.get("last_deadline_value");
        userTeam.setTeamValue(teamValue != null ? ((Number) teamValue).doubleValue() / 10.0 : null);

        Object bank = entryData.get("last_deadline_bank");
        userTeam.setBank(bank != null ? ((Number) bank).intValue() : null);

        Object totalTransfers = entryData.get("last_deadline_total_transfers");
        userTeam.setTotalTransfers(totalTransfers != null ? ((Number) totalTransfers).intValue() : null);
    }

    /**
     * Fetches and persists per-gameweek rank history for the given team.
     * Only syncs GWs up to the last truly completed one, derived from fixture data.
     * Skips GWs already recorded, except the latest which is always refreshed.
     *
     * @param userTeam the team whose rank history should be synced
     */
    @SuppressWarnings("unchecked")
    private void syncRankHistory(UserTeam userTeam) {
        try {
            Map<String, Object> historyData = fplApiClient.get("/entry/" + userTeam.getFplTeamId() + "/history/");
            List<Map<String, Object>> currentGws = (List<Map<String, Object>>) historyData.get("current");
            if (currentGws == null || currentGws.isEmpty()) return;

            Map<Integer, String> chipsByGw = buildChipsByGwMap(
                    (List<Map<String, Object>>) historyData.getOrDefault("chips", List.of()));

            int lastFinishedGw = fixtureDataService.getLastCompletedGameWeek();
            List<Map<String, Object>> finishedGws = currentGws.stream()
                    .filter(gwData -> ((Number) gwData.get("event")).intValue() <= lastFinishedGw)
                    .toList();

            if (finishedGws.isEmpty()) return;

            Set<Integer> existingGws = resolveExistingGwNumbers(userTeam.getFplTeamId());
            List<UserTeamRankHistory> toSave = buildRankHistoryEntries(finishedGws, userTeam, existingGws, chipsByGw);

            userTeamRankHistoryRepository.saveAll(toSave);
            log.info("Synced {} rank history entries for team {}", toSave.size(), userTeam.getFplTeamId());
        } catch (Exception e) {
            log.error("Failed to sync rank history for team {}", userTeam.getFplTeamId(), e);
        }
    }

    /**
     * Returns the set of gameWeek numbers already recorded in the database for the given team.
     *
     * @param fplTeamId the FPL team ID
     * @return set of already-persisted gameWeek numbers
     */
    private Set<Integer> resolveExistingGwNumbers(long fplTeamId) {
        return userTeamRankHistoryRepository
                .findAllByUserTeam_FplTeamIdOrderByGameWeekAsc(fplTeamId)
                .stream()
                .map(UserTeamRankHistory::getGameWeek)
                .collect(Collectors.toSet());
    }

    /**
     * Builds the list of {@link UserTeamRankHistory} entries to persist.
     * New GWs are inserted; the latest GW is always refreshed; older existing GWs are skipped.
     *
     * @param finishedGws raw per-GW data maps filtered to completed gameWeeks
     * @param userTeam    the owning team entity
     * @param existingGws set of gameWeek numbers already in the database
     * @param chipsByGw   map of gameWeek number to chip name activated that week
     * @return list of entries ready for {@code saveAll}
     */
    private List<UserTeamRankHistory> buildRankHistoryEntries(List<Map<String, Object>> finishedGws,
                                                               UserTeam userTeam,
                                                               Set<Integer> existingGws,
                                                               Map<Integer, String> chipsByGw) {
        int latestGw = ((Number) finishedGws.get(finishedGws.size() - 1).get("event")).intValue();
        return finishedGws.stream()
                .filter(gwData -> {
                    int gw = ((Number) gwData.get("event")).intValue();
                    return !existingGws.contains(gw) || gw == latestGw;
                })
                .map(gwData -> {
                    int gw = ((Number) gwData.get("event")).intValue();
                    UserTeamRankHistory entry = existingGws.contains(gw)
                            ? userTeamRankHistoryRepository
                                    .findByUserTeam_FplTeamIdAndGameWeek(userTeam.getFplTeamId(), gw)
                                    .orElseGet(UserTeamRankHistory::new)
                            : new UserTeamRankHistory();
                    upsertRankHistory(gwData, userTeam, entry, chipsByGw.get(gw));
                    return entry;
                })
                .toList();
    }

    /**
     * Builds a map of gameWeek number to chip name from the chips array in the FPL history response.
     *
     * @param chips the raw chips array from the FPL {@code /entry/{id}/history/} response
     * @return map of gameWeek → chip name (e.g. "wc", "fh", "bboost", "3xc")
     */
    private Map<Integer, String> buildChipsByGwMap(List<Map<String, Object>> chips) {
        return chips.stream()
                .filter(chip -> chip.get("event") != null && chip.get("name") != null)
                .collect(Collectors.toMap(
                        chip -> ((Number) chip.get("event")).intValue(),
                        chip -> (String) chip.get("name"),
                        (a, b) -> a
                ));
    }

    /**
     * Populates a {@link UserTeamRankHistory} entry from raw FPL history data.
     *
     * @param gwData   raw per-GW map from the FPL {@code /entry/{id}/history/} {@code current} array
     * @param userTeam the owning team entity
     * @param entry    the entity to populate (new or existing)
     * @param chipUsed the chip name activated in this gameweek, or {@code null} if none
     */
    private void upsertRankHistory(Map<String, Object> gwData,
                                   UserTeam userTeam,
                                   UserTeamRankHistory entry,
                                   String chipUsed) {
        entry.setUserTeam(userTeam);
        entry.setGameWeek(((Number) gwData.get("event")).intValue());
        entry.setOverallRank(((Number) gwData.get("overall_rank")).intValue());
        entry.setGwPoints(((Number) gwData.get("points")).intValue());
        entry.setGwRank(((Number) gwData.getOrDefault("rank", 0)).intValue());
        entry.setTotalPoints(((Number) gwData.get("total_points")).intValue());
        entry.setBank(((Number) gwData.getOrDefault("bank", 0)).intValue());
        Object teamValue = gwData.get("value");
        entry.setTeamValue(teamValue != null ? ((Number) teamValue).doubleValue() / 10.0 : null);
        entry.setEventTransfers(((Number) gwData.getOrDefault("event_transfers", 0)).intValue());
        entry.setPointsOnBench(((Number) gwData.getOrDefault("points_on_bench", 0)).intValue());
        entry.setChipUsed(chipUsed);
    }
}
