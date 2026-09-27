package com.fpl.stats.services.impl;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.PlayerHistory;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.services.FormationOptimizationService;
import com.fpl.stats.services.dto.GwFormationLossDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes the theoretical optimal lineup per gameweek by exhaustively evaluating all
 * seven valid FPL formations against the user's 15-player squad.
 *
 * <p>For each gameweek the service resolves each player's actual points from
 * {@link PlayerHistory}, then selects the best starting 11 per formation while
 * preserving the user's captain and vice-captain designations.</p>
 */
@Service
public class FormationOptimizationServiceImpl implements FormationOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(FormationOptimizationServiceImpl.class);

    private static final int MIN_SQUAD_SIZE = 11;

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

    /**
     * Holds a single pick together with its actual points for a given gameweek.
     *
     * @param pick     the user pick entity
     * @param gwPoints the player's actual FPL points in that gameweek
     */
    private record PickWithPoints(UserPick pick, int gwPoints) {}

    /**
     * Holds the result of evaluating a single formation: its name, total points, and selected 11.
     *
     * @param formationName the formation label, e.g. "4-3-3"
     * @param totalPoints   combined effective points of the 11 selected players
     * @param starting11    the 11 selected picks
     */
    private record OptimalResult(String formationName, int totalPoints, List<PickWithPoints> starting11) {}

    /** {@inheritDoc} */
    @Override
    public List<GwFormationLossDto> computeFormationLoss(List<UserPick> allPicks, Map<Integer, Player> playersByFplId) {
        Map<Integer, List<UserPick>> picksByGw = allPicks.stream()
            .collect(Collectors.groupingBy(pick -> pick.getGameWeek().getGameWeekNumber()));

        return picksByGw.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= MIN_SQUAD_SIZE)
            .map(entry -> buildGwFormationLoss(entry.getKey(), entry.getValue(), playersByFplId))
            .sorted(Comparator.comparingInt(GwFormationLossDto::getGameWeek))
            .toList();
    }

    /**
     * Computes the formation loss DTO for a single gameweek.
     *
     * @param gwNumber        the gameweek number
     * @param gwPicks         all 15 picks for this gameweek
     * @param playersByFplId  player map used to resolve actual GW points
     * @return populated {@link GwFormationLossDto} for the gameweek
     */
    private GwFormationLossDto buildGwFormationLoss(int gwNumber, List<UserPick> gwPicks, Map<Integer, Player> playersByFplId) {
        log.debug("Computing formation loss for GW{}", gwNumber);
        List<PickWithPoints> picksWithPoints = resolvePicks(gwPicks, gwNumber, playersByFplId);

        int actualPoints = computeActualPoints(picksWithPoints);
        String actualFormation = deriveFormation(picksWithPoints.stream().filter(pwp -> !pwp.pick().isBenched()).toList());

        int captainFplId = findCaptainFplId(gwPicks);
        int vcFplId = findVcFplId(gwPicks);
        int captainMultiplier = gwPicks.stream().anyMatch(UserPick::isTripleCaptain) ? 3 : 2;

        OptimalResult optimalResult = findOptimalLineup(picksWithPoints, captainFplId, vcFplId, captainMultiplier);

        Set<Integer> optimalStarterIds = optimalResult.starting11().stream()
            .map(pwp -> pwp.pick().getPlayer().getFplId())
            .collect(Collectors.toSet());

        return buildDto(gwNumber, actualPoints, actualFormation, optimalResult, picksWithPoints, optimalStarterIds);
    }

    /**
     * Maps each pick to a {@link PickWithPoints} by resolving actual GW points from player history.
     *
     * @param gwPicks         the picks for this gameweek
     * @param gwNumber        the gameweek number used to look up history
     * @param playersByFplId  player map with histories loaded
     * @return list of picks paired with their actual points
     */
    private List<PickWithPoints> resolvePicks(List<UserPick> gwPicks, int gwNumber, Map<Integer, Player> playersByFplId) {
        return gwPicks.stream()
            .map(pick -> new PickWithPoints(pick, resolveGwPoints(pick, gwNumber, playersByFplId)))
            .toList();
    }

    /**
     * Resolves the actual FPL points for a pick in a given gameweek from the player's history.
     *
     * @param pick            the pick whose player's points are needed
     * @param gwNumber        the target gameweek number
     * @param playersByFplId  player map with histories loaded
     * @return the player's points for that gameweek, or {@code 0} if history is unavailable
     */
    private int resolveGwPoints(UserPick pick, int gwNumber, Map<Integer, Player> playersByFplId) {
        Player player = playersByFplId.get(pick.getPlayer().getFplId());
        if (player == null) return 0;
        return player.getPlayerHistories().stream()
            .filter(h -> h.getGameWeek().getGameWeekNumber() == gwNumber)
            .mapToInt(PlayerHistory::getPoints)
            .findFirst()
            .orElse(0);
    }

    /**
     * Sums actual points using each pick's stored multiplier (which already encodes captaincy).
     *
     * @param picksWithPoints all picks for the gameweek
     * @return total actual points for the gameweek
     */
    private int computeActualPoints(List<PickWithPoints> picksWithPoints) {
        return picksWithPoints.stream()
            .mapToInt(pwp -> pwp.gwPoints() * pwp.pick().getMultiplier())
            .sum();
    }

    /**
     * Derives the formation string from the starters (non-benched picks, GKP excluded).
     * Returns {@code "N/A"} if the outfield count is not exactly 10.
     *
     * @param starters the starting picks for the gameweek
     * @return a formation string like {@code "4-3-3"}, or {@code "N/A"}
     */
    private String deriveFormation(List<PickWithPoints> starters) {
        long def = countByPosition(starters, "DEF");
        long mid = countByPosition(starters, "MID");
        long fwd = countByPosition(starters, "FWD");
        return (def + mid + fwd == 10) ? def + "-" + mid + "-" + fwd : "N/A";
    }

    /**
     * Counts the starters for a given position.
     *
     * @param starters starters list to count from
     * @param position the position code to match
     * @return count of starters in that position
     */
    private long countByPosition(List<PickWithPoints> starters, String position) {
        return starters.stream()
            .filter(pwp -> position.equals(pwp.pick().getPlayer().getPosition()))
            .count();
    }

    /**
     * Returns the FPL ID of the designated captain for the gameweek, or {@code -1} if not found.
     *
     * @param gwPicks the picks for the gameweek
     * @return captain's FPL ID
     */
    private int findCaptainFplId(List<UserPick> gwPicks) {
        return gwPicks.stream()
            .filter(UserPick::isCaptain)
            .mapToInt(pick -> pick.getPlayer().getFplId())
            .findFirst()
            .orElse(-1);
    }

    /**
     * Returns the FPL ID of the vice-captain for the gameweek, or {@code -1} if not found.
     *
     * @param gwPicks the picks for the gameweek
     * @return vice-captain's FPL ID
     */
    private int findVcFplId(List<UserPick> gwPicks) {
        return gwPicks.stream()
            .filter(UserPick::isViceCaptain)
            .mapToInt(pick -> pick.getPlayer().getFplId())
            .findFirst()
            .orElse(-1);
    }

    /**
     * Iterates all seven valid FPL formations and returns the lineup that maximises total points.
     *
     * @param all15            all 15 picks with their GW points
     * @param captainFplId     FPL ID of the designated captain
     * @param vcFplId          FPL ID of the vice-captain
     * @param captainMultiplier captaincy multiplier (2 or 3 for TC)
     * @return the best formation result, or an empty fallback if no valid formation can be filled
     */
    private OptimalResult findOptimalLineup(List<PickWithPoints> all15, int captainFplId, int vcFplId, int captainMultiplier) {
        Map<String, List<PickWithPoints>> byPosition = groupAndSortByPosition(all15);

        OptimalResult best = null;
        int bestTotal = Integer.MIN_VALUE;

        for (int i = 0; i < FORMATIONS.length; i++) {
            OptimalResult candidate = evaluateFormation(FORMATIONS[i], FORMATION_NAMES[i], byPosition, captainFplId, vcFplId, captainMultiplier);
            if (candidate != null && candidate.totalPoints() > bestTotal) {
                bestTotal = candidate.totalPoints();
                best = candidate;
            }
        }

        return best != null ? best : new OptimalResult("N/A", computeActualPoints(all15), all15);
    }

    /**
     * Groups all 15 picks by position and sorts each bucket by GW points descending.
     *
     * @param all15 all 15 picks for the gameweek
     * @return map of position code to sorted picks list
     */
    private Map<String, List<PickWithPoints>> groupAndSortByPosition(List<PickWithPoints> all15) {
        Map<String, List<PickWithPoints>> byPosition = all15.stream()
            .collect(Collectors.groupingBy(pwp -> pwp.pick().getPlayer().getPosition()));
        byPosition.values().forEach(list ->
            list.sort(Comparator.comparingInt(PickWithPoints::gwPoints).reversed())
        );
        return byPosition;
    }

    /**
     * Evaluates a single formation by selecting the top-scoring players per slot.
     * Returns {@code null} if the squad does not have enough players to fill the formation.
     *
     * @param slots            [GKP, DEF, MID, FWD] slot counts for this formation
     * @param formationName    display name for this formation (e.g. "4-3-3")
     * @param byPosition       position-grouped picks sorted by points descending
     * @param captainFplId     FPL ID of the designated captain
     * @param vcFplId          FPL ID of the vice-captain
     * @param captainMultiplier captaincy multiplier to apply
     * @return an {@link OptimalResult} for this formation, or {@code null} if unfillable
     */
    private OptimalResult evaluateFormation(int[] slots, String formationName,
                                            Map<String, List<PickWithPoints>> byPosition,
                                            int captainFplId, int vcFplId, int captainMultiplier) {
        List<PickWithPoints> gkp = topN(byPosition.get("GKP"), slots[0]);
        List<PickWithPoints> def = topN(byPosition.get("DEF"), slots[1]);
        List<PickWithPoints> mid = topN(byPosition.get("MID"), slots[2]);
        List<PickWithPoints> fwd = topN(byPosition.get("FWD"), slots[3]);

        if (gkp.size() < slots[0] || def.size() < slots[1] || mid.size() < slots[2] || fwd.size() < slots[3]) {
            return null;
        }

        List<PickWithPoints> starting11 = new ArrayList<>(11);
        starting11.addAll(gkp);
        starting11.addAll(def);
        starting11.addAll(mid);
        starting11.addAll(fwd);

        Set<Integer> starting11Ids = starting11.stream()
            .map(pwp -> pwp.pick().getPlayer().getFplId())
            .collect(Collectors.toSet());

        int totalPoints = computeOptimalPoints(starting11, starting11Ids, captainFplId, vcFplId, captainMultiplier);
        return new OptimalResult(formationName, totalPoints, starting11);
    }

    /**
     * Computes total points for a proposed starting 11, applying the captaincy multiplier to
     * the captain (or the vice-captain if the captain is not in the proposed 11).
     *
     * @param starting11        the proposed 11-player lineup
     * @param starting11Ids     FPL IDs of players in the proposed 11 (for fast lookup)
     * @param captainFplId      FPL ID of the designated captain
     * @param vcFplId           FPL ID of the vice-captain
     * @param captainMultiplier captaincy multiplier (2 or 3 for TC)
     * @return total effective points for this lineup
     */
    private int computeOptimalPoints(List<PickWithPoints> starting11, Set<Integer> starting11Ids,
                                     int captainFplId, int vcFplId, int captainMultiplier) {
        boolean captainInTeam = starting11Ids.contains(captainFplId);
        int total = 0;
        for (PickWithPoints pwp : starting11) {
            int fplId = pwp.pick().getPlayer().getFplId();
            int multiplier = 1;
            if (fplId == captainFplId) {
                multiplier = captainMultiplier;
            } else if (!captainInTeam && fplId == vcFplId) {
                multiplier = captainMultiplier;
            }
            total += pwp.gwPoints() * multiplier;
        }
        return total;
    }

    /**
     * Assembles the final {@link GwFormationLossDto} from precomputed values.
     *
     * @param gwNumber         the gameweek number
     * @param actualPoints     the actual points scored
     * @param actualFormation  the actual formation used
     * @param optimalResult    the best possible lineup
     * @param allPicks         all 15 picks used to derive swap lists
     * @param optimalStarterIds FPL IDs of players in the optimal starting 11
     * @return fully populated DTO
     */
    private GwFormationLossDto buildDto(int gwNumber, int actualPoints, String actualFormation,
                                        OptimalResult optimalResult, List<PickWithPoints> allPicks,
                                        Set<Integer> optimalStarterIds) {
        List<String> playersToStart = allPicks.stream()
            .filter(pwp -> pwp.pick().isBenched() && optimalStarterIds.contains(pwp.pick().getPlayer().getFplId()))
            .map(pwp -> pwp.pick().getPlayer().getWebName())
            .toList();

        List<String> playersToBench = allPicks.stream()
            .filter(pwp -> !pwp.pick().isBenched() && !optimalStarterIds.contains(pwp.pick().getPlayer().getFplId()))
            .map(pwp -> pwp.pick().getPlayer().getWebName())
            .toList();

        GwFormationLossDto dto = new GwFormationLossDto();
        dto.setGameWeek(gwNumber);
        dto.setActualPoints(actualPoints);
        dto.setOptimalPoints(optimalResult.totalPoints());
        dto.setPointsLost(optimalResult.totalPoints() - actualPoints);
        dto.setActualFormation(actualFormation);
        dto.setOptimalFormation(optimalResult.formationName());
        dto.setPlayersToStart(playersToStart);
        dto.setPlayersToBench(playersToBench);
        return dto;
    }

    /**
     * Returns the first {@code n} elements from the list, or fewer if the list is shorter.
     *
     * @param picks the candidate picks sorted by points descending; may be {@code null}
     * @param n     the desired count
     * @return sub-list of at most {@code n} elements
     */
    private List<PickWithPoints> topN(List<PickWithPoints> picks, int n) {
        if (picks == null) return List.of();
        return picks.stream().limit(n).toList();
    }
}
