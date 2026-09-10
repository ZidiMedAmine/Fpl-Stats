package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.GameWeek;
import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.repository.GameWeekRepository;
import com.fpl.stats.repository.PlayerRepository;
import com.fpl.stats.repository.UserPickRepository;
import com.fpl.stats.repository.UserTeamRepository;
import com.fpl.stats.services.fpl.FixtureDataService;
import com.fpl.stats.services.util.FplApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Synchronizes a user's gameweek picks from the FPL picks endpoint.
 * Fetches its own per-user data directly from the API.
 * Triple captain week is extracted once before iterating over picks.
 */
@Service
@Transactional
public class UserPickSyncService {

    private static final Logger log = LoggerFactory.getLogger(UserPickSyncService.class);

    private final FplApiClient fplApiClient;
    private final UserPickRepository userPickRepository;
    private final UserTeamRepository userTeamRepository;
    private final GameWeekRepository gameWeekRepository;
    private final PlayerRepository playerRepository;
    private final FixtureDataService fixtureDataService;

    /**
     * @param fplApiClient        used to fetch per-user picks and chip history
     * @param userPickRepository  persists user picks
     * @param userTeamRepository  persists last synced gameweek state
     * @param gameWeekRepository  resolves gameweek entities by number
     * @param playerRepository    resolves player entities by FPL ID
     * @param fixtureDataService  used to derive the last truly completed gameweek
     */
    public UserPickSyncService(FplApiClient fplApiClient,
                               UserPickRepository userPickRepository,
                               UserTeamRepository userTeamRepository,
                               GameWeekRepository gameWeekRepository,
                               PlayerRepository playerRepository,
                               FixtureDataService fixtureDataService) {
        this.fplApiClient = fplApiClient;
        this.userPickRepository = userPickRepository;
        this.userTeamRepository = userTeamRepository;
        this.gameWeekRepository = gameWeekRepository;
        this.playerRepository = playerRepository;
        this.fixtureDataService = fixtureDataService;
    }

    /**
     * Syncs all unsynchronized gameweek picks for the given user team.
     * Starts from the gameweek after the last synced one (or the user's started event)
     * and continues through the last completed gameweek.
     *
     * @param userTeam the user team whose picks should be synced
     */
    public void syncUserPicks(UserTeam userTeam) {
        int startGw = userTeam.getLastSyncedGameWeek() + 1;
        if (userTeam.getStartedEvent() != null) {
            startGw = Math.max(startGw, userTeam.getStartedEvent());
        }

        int lastCompleted = fixtureDataService.getLastCompletedGameWeek();
        if (startGw > lastCompleted) {
            log.info("No new gameweeks to sync for team {}", userTeam.getFplTeamId());
            if (lastCompleted > 0) {
                userTeam.setLastSyncedGameWeek(lastCompleted);
                userTeamRepository.save(userTeam);
            }
            return;
        }

        log.info("Syncing picks for team {} from GW{} to GW{}",
                userTeam.getFplTeamId(), startGw, lastCompleted);

        Optional<Integer> tripleCaptainWeek = getTripleCaptainWeek(userTeam.getFplTeamId());

        List<UserPick> allPicks = new ArrayList<>();

        for (int gw = startGw; gw <= lastCompleted; gw++) {
            try {
                List<Map<String, Object>> picksData = fetchTeamPicks(userTeam.getFplTeamId(), gw);
                if (picksData == null) continue;

                GameWeek gameWeek = gameWeekRepository.findByGameWeekNumber(gw).orElse(null);
                if (gameWeek == null) continue;

                boolean isTripleCaptainGw = tripleCaptainWeek.isPresent()
                        && tripleCaptainWeek.get() == gw;

                for (Map<String, Object> pickData : picksData) {
                    UserPick pick = createUserPick(userTeam, gameWeek, pickData, isTripleCaptainGw);
                    if (pick != null) {
                        allPicks.add(pick);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to sync picks for team {} GW{}",
                        userTeam.getFplTeamId(), gw, e);
            }
        }

        if (!allPicks.isEmpty()) {
            userPickRepository.saveAll(allPicks);
        }

        userTeam.setLastSyncedGameWeek(lastCompleted);
        userTeamRepository.save(userTeam);
        log.info("Synced {} picks for team {}", allPicks.size(), userTeam.getFplTeamId());
    }

    /**
     * Fetches the picks for a single gameweek from the FPL API.
     *
     * @param fplTeamId the FPL team/entry ID
     * @param gameWeek  the gameweek number
     * @return the list of pick maps, or {@code null} if the fetch failed
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchTeamPicks(long fplTeamId, int gameWeek) {
        try {
            Map<String, Object> response = fplApiClient.get(
                    "/entry/" + fplTeamId + "/event/" + gameWeek + "/picks/");
            return (List<Map<String, Object>>) response.get("picks");
        } catch (Exception e) {
            log.warn("Could not fetch picks for team {} GW{}", fplTeamId, gameWeek);
            return null;
        }
    }

    /**
     * Determines the gameweek in which the user played the triple captain chip.
     *
     * @param fplTeamId the FPL team/entry ID
     * @return the gameweek number of the triple captain chip, or empty if not used
     */
    @SuppressWarnings("unchecked")
    private Optional<Integer> getTripleCaptainWeek(long fplTeamId) {
        try {
            Map<String, Object> historyData = fplApiClient.get(
                    "/entry/" + fplTeamId + "/history/");
            List<Map<String, Object>> chips =
                    (List<Map<String, Object>>) historyData.get("chips");

            if (chips == null) return Optional.empty();

            return chips.stream()
                    .filter(chip -> "3xc".equals(chip.get("name")))
                    .map(chip -> ((Number) chip.get("event")).intValue())
                    .findFirst();
        } catch (Exception e) {
            log.warn("Could not fetch chips for team {}", fplTeamId);
            return Optional.empty();
        }
    }

    private UserPick createUserPick(UserTeam userTeam, GameWeek gameWeek,
                                    Map<String, Object> pickData,
                                    boolean isTripleCaptainGw) {
        int playerFplId = ((Number) pickData.get("element")).intValue();
        Player player = playerRepository.findByFplId(playerFplId).orElse(null);
        if (player == null) {
            log.warn("Player {} not found in database -- skipping pick", playerFplId);
            return null;
        }

        int squadPosition = ((Number) pickData.get("position")).intValue();
        int multiplier = ((Number) pickData.get("multiplier")).intValue();
        boolean captain = Boolean.TRUE.equals(pickData.get("is_captain"));
        boolean viceCaptain = Boolean.TRUE.equals(pickData.get("is_vice_captain"));
        boolean benched = squadPosition > 11;
        boolean tripleCaptain = captain && isTripleCaptainGw;

        UserPick pick = new UserPick();
        pick.setUserTeam(userTeam);
        pick.setGameWeek(gameWeek);
        pick.setPlayer(player);
        pick.setPosition(squadPosition);
        pick.setMultiplier(multiplier);
        pick.setCaptain(captain);
        pick.setViceCaptain(viceCaptain);
        pick.setBenched(benched);
        pick.setTripleCaptain(tripleCaptain);
        return pick;
    }
}
