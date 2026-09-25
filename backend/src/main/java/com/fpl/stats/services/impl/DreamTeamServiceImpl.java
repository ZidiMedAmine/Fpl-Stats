package com.fpl.stats.services.impl;

import com.fpl.stats.domain.Player;
import com.fpl.stats.repository.GameWeekRepository;
import com.fpl.stats.repository.PlayerHistoryRepository;
import com.fpl.stats.repository.PlayerRepository;
import com.fpl.stats.services.DreamTeamService;
import com.fpl.stats.services.dto.DreamTeamDto;
import com.fpl.stats.services.dto.DreamTeamPlayerDto;
import com.fpl.stats.services.dto.DreamTeamResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Computes FPL dream team lineups by selecting the starting XI that maximises
 * total points across all seven valid FPL formations.
 *
 * <p>The all-time lineup uses each player's season total points. The last-5-weeks
 * lineup sums each player's points over the five most recently finished gameweeks.</p>
 */
@Service
@Transactional(readOnly = true)
public class DreamTeamServiceImpl implements DreamTeamService {

    private static final Logger log = LoggerFactory.getLogger(DreamTeamServiceImpl.class);

    private static final int LAST_N_WEEKS = 5;
    private static final int MAX_GKP_SLOTS = 1;
    private static final int MAX_DEF_SLOTS = 5;
    private static final int MAX_MID_SLOTS = 5;
    private static final int MAX_FWD_SLOTS = 3;

    /** All valid FPL formations as [GKP, DEF, MID, FWD] slot counts. */
    private static final int[][] FORMATIONS = {
        {1, 3, 4, 3},
        {1, 3, 5, 2},
        {1, 4, 3, 3},
        {1, 4, 4, 2},
        {1, 4, 5, 1},
        {1, 5, 3, 2},
        {1, 5, 4, 1},
    };

    private static final String[] FORMATION_NAMES = {
        "3-4-3", "3-5-2", "4-3-3", "4-4-2", "4-5-1", "5-3-2", "5-4-1"
    };

    private final PlayerRepository playerRepository;
    private final PlayerHistoryRepository playerHistoryRepository;
    private final GameWeekRepository gameWeekRepository;

    /**
     * @param playerRepository        fetches top players by position for the all-time lineup
     * @param playerHistoryRepository aggregates per-player points for the last-N-weeks lineup
     * @param gameWeekRepository      locates the most recently finished gameweek
     */
    public DreamTeamServiceImpl(PlayerRepository playerRepository,
                                PlayerHistoryRepository playerHistoryRepository,
                                GameWeekRepository gameWeekRepository) {
        this.playerRepository = playerRepository;
        this.playerHistoryRepository = playerHistoryRepository;
        this.gameWeekRepository = gameWeekRepository;
    }

    /** {@inheritDoc} */
    @Override
    public DreamTeamDto getDreamTeam() {
        log.debug("Building dream team lineups");
        DreamTeamDto dreamTeamDto = new DreamTeamDto();
        dreamTeamDto.setAllTime(buildAllTimeDreamTeam());
        dreamTeamDto.setLast5Weeks(buildLast5WeeksDreamTeam());
        return dreamTeamDto;
    }

    /**
     * Builds the all-time dream team using each player's season total points.
     *
     * @return best XI from season totals
     */
    private DreamTeamResultDto buildAllTimeDreamTeam() {
        Map<String, List<DreamTeamPlayerDto>> candidatesByPosition = Map.of(
            "GKP", fetchTopPlayersByPosition("GKP", MAX_GKP_SLOTS),
            "DEF", fetchTopPlayersByPosition("DEF", MAX_DEF_SLOTS),
            "MID", fetchTopPlayersByPosition("MID", MAX_MID_SLOTS),
            "FWD", fetchTopPlayersByPosition("FWD", MAX_FWD_SLOTS)
        );
        return selectBestFormation(candidatesByPosition);
    }

    /**
     * Queries the top {@code limit} players for the given position by season total points,
     * with team data loaded.
     *
     * @param position one of GKP, DEF, MID, FWD
     * @param limit    maximum number of candidates to return
     * @return sorted candidate list, length at most {@code limit}
     */
    private List<DreamTeamPlayerDto> fetchTopPlayersByPosition(String position, int limit) {
        return playerRepository.findByPositionWithTeam(position).stream()
            .limit(limit)
            .map(this::toAllTimePlayerDto)
            .toList();
    }

    /**
     * Maps a {@link Player} entity to a {@link DreamTeamPlayerDto} using the player's season total points.
     *
     * @param player the player entity with team loaded
     * @return dream team player DTO
     */
    private DreamTeamPlayerDto toAllTimePlayerDto(Player player) {
        DreamTeamPlayerDto dto = new DreamTeamPlayerDto();
        dto.setFplId(player.getFplId());
        dto.setCode(player.getCode());
        dto.setWebName(player.getWebName());
        dto.setPosition(player.getPosition());
        dto.setTeamName(player.getTeam() != null ? player.getTeam().getShortName() : "");
        dto.setPoints(player.getTotalPoints());
        dto.setNowCost(player.getNowCost());
        return dto;
    }

    /**
     * Builds the last-5-weeks dream team by summing each player's points
     * across the five most recently finished gameweeks.
     *
     * @return best XI from the last 5 finished gameweeks, or an empty result if none are available
     */
    private DreamTeamResultDto buildLast5WeeksDreamTeam() {
        List<Integer> gwNumbers = findLast5FinishedGwNumbers();
        if (gwNumbers.isEmpty()) {
            log.warn("No finished gameweeks found — returning empty last-5-weeks dream team");
            return emptyResult();
        }
        log.debug("Last-5-weeks dream team using GWs: {}", gwNumbers);
        List<Object[]> rows = playerHistoryRepository.findPlayerPointsSumForGameWeeks(gwNumbers);
        Map<String, List<DreamTeamPlayerDto>> candidatesByPosition = groupByPositionAndLimit(rows);
        return selectBestFormation(candidatesByPosition);
    }

    /**
     * Resolves the gameweek numbers for the last {@value #LAST_N_WEEKS} finished gameweeks.
     * Uses the gameweek flagged as {@code isPrevious} as the anchor (last finished GW).
     *
     * @return list of gameweek numbers, empty if no finished gameweeks exist
     */
    private List<Integer> findLast5FinishedGwNumbers() {
        return gameWeekRepository.findByIsPreviousTrue()
            .map(previousGw -> {
                int lastFinished = previousGw.getGameWeekNumber();
                int firstInWindow = Math.max(1, lastFinished - LAST_N_WEEKS + 1);
                return IntStream.rangeClosed(firstInWindow, lastFinished).boxed().toList();
            })
            .orElse(List.of());
    }

    /**
     * Groups projection rows by position and limits each bucket to the maximum
     * number of slots that position can occupy across all formations.
     * Rows are already ordered by sumPoints descending from the repository query.
     *
     * @param rows raw projection rows: [fplId, webName, position, teamShortName, nowCost, sumPoints]
     * @return map of position code to candidate list
     */
    private Map<String, List<DreamTeamPlayerDto>> groupByPositionAndLimit(List<Object[]> rows) {
        Map<String, Integer> maxByPosition = Map.of(
            "GKP", MAX_GKP_SLOTS,
            "DEF", MAX_DEF_SLOTS,
            "MID", MAX_MID_SLOTS,
            "FWD", MAX_FWD_SLOTS
        );

        Map<String, List<DreamTeamPlayerDto>> byPosition = new HashMap<>();
        for (Object[] row : rows) {
            DreamTeamPlayerDto dto = fromProjectionRow(row);
            byPosition.computeIfAbsent(dto.getPosition(), k -> new ArrayList<>()).add(dto);
        }

        byPosition.replaceAll((pos, players) ->
            players.stream().limit(maxByPosition.getOrDefault(pos, 0)).toList()
        );
        return byPosition;
    }

    /**
     * Maps a raw projection row to a {@link DreamTeamPlayerDto}.
     * Row columns: [fplId, code, webName, position, teamShortName, nowCost, sumPoints].
     *
     * @param row the raw JPQL projection row
     * @return populated dream team player DTO
     */
    private DreamTeamPlayerDto fromProjectionRow(Object[] row) {
        DreamTeamPlayerDto dto = new DreamTeamPlayerDto();
        dto.setFplId(((Number) row[0]).intValue());
        dto.setCode(((Number) row[1]).intValue());
        dto.setWebName((String) row[2]);
        dto.setPosition((String) row[3]);
        dto.setTeamName((String) row[4]);
        dto.setNowCost(((Number) row[5]).doubleValue());
        dto.setPoints(((Number) row[6]).intValue());
        return dto;
    }

    /**
     * Iterates all seven valid FPL formations and returns the lineup that produces
     * the highest combined player points.
     *
     * @param candidatesByPosition map of position code to candidate list, pre-sorted by points descending
     * @return the best formation and its 11 players, or an empty result if no formation can be filled
     */
    private DreamTeamResultDto selectBestFormation(Map<String, List<DreamTeamPlayerDto>> candidatesByPosition) {
        DreamTeamResultDto bestResult = null;
        int bestPoints = Integer.MIN_VALUE;

        for (int i = 0; i < FORMATIONS.length; i++) {
            int[] formation = FORMATIONS[i];
            List<DreamTeamPlayerDto> gkp = topN(candidatesByPosition.get("GKP"), formation[0]);
            List<DreamTeamPlayerDto> def = topN(candidatesByPosition.get("DEF"), formation[1]);
            List<DreamTeamPlayerDto> mid = topN(candidatesByPosition.get("MID"), formation[2]);
            List<DreamTeamPlayerDto> fwd = topN(candidatesByPosition.get("FWD"), formation[3]);

            if (gkp.size() < formation[0] || def.size() < formation[1]
                    || mid.size() < formation[2] || fwd.size() < formation[3]) {
                continue;
            }

            int totalPoints = sumPoints(gkp) + sumPoints(def) + sumPoints(mid) + sumPoints(fwd);
            if (totalPoints > bestPoints) {
                bestPoints = totalPoints;
                bestResult = buildResult(FORMATION_NAMES[i], totalPoints, gkp, def, mid, fwd);
            }
        }

        return bestResult != null ? bestResult : emptyResult();
    }

    /**
     * Assembles a {@link DreamTeamResultDto} from the four positional lists.
     *
     * @param formationName the formation label, e.g. "4-3-3"
     * @param totalPoints   combined points of all 11 players
     * @param gkp           selected goalkeeper(s)
     * @param def           selected defenders
     * @param mid           selected midfielders
     * @param fwd           selected forwards
     * @return the assembled result DTO
     */
    private DreamTeamResultDto buildResult(String formationName, int totalPoints,
                                           List<DreamTeamPlayerDto> gkp,
                                           List<DreamTeamPlayerDto> def,
                                           List<DreamTeamPlayerDto> mid,
                                           List<DreamTeamPlayerDto> fwd) {
        List<DreamTeamPlayerDto> players = new ArrayList<>(11);
        players.addAll(gkp);
        players.addAll(def);
        players.addAll(mid);
        players.addAll(fwd);

        DreamTeamResultDto result = new DreamTeamResultDto();
        result.setFormation(formationName);
        result.setTotalPoints(totalPoints);
        result.setPlayers(players);
        return result;
    }

    /**
     * Returns the first {@code n} elements from the list, or fewer if the list is shorter.
     *
     * @param players the candidate list, may be null
     * @param n       desired count
     * @return sub-list of at most {@code n} elements
     */
    private List<DreamTeamPlayerDto> topN(List<DreamTeamPlayerDto> players, int n) {
        if (players == null) return List.of();
        return players.stream().limit(n).toList();
    }

    /**
     * Sums the points of all players in the list.
     *
     * @param players the list to sum
     * @return total points
     */
    private int sumPoints(List<DreamTeamPlayerDto> players) {
        return players.stream().mapToInt(DreamTeamPlayerDto::getPoints).sum();
    }

    /**
     * Returns an empty result with formation "N/A" and zero points.
     * Used when insufficient data is available to build a lineup.
     *
     * @return empty dream team result
     */
    private DreamTeamResultDto emptyResult() {
        DreamTeamResultDto result = new DreamTeamResultDto();
        result.setFormation("N/A");
        result.setTotalPoints(0);
        result.setPlayers(List.of());
        return result;
    }
}
