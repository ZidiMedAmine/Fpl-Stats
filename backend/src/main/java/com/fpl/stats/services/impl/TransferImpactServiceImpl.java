package com.fpl.stats.services.impl;

import com.fpl.stats.domain.PlayerHistory;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.domain.UserTeamRankHistory;
import com.fpl.stats.exception.TeamNotFoundException;
import com.fpl.stats.repository.PlayerHistoryRepository;
import com.fpl.stats.repository.UserPickRepository;
import com.fpl.stats.repository.UserTeamRankHistoryRepository;
import com.fpl.stats.repository.UserTeamRepository;
import com.fpl.stats.services.TransferImpactService;
import com.fpl.stats.services.dto.TransferImpactDto;
import com.fpl.stats.services.dto.TransferImpactGwDto;
import com.fpl.stats.services.dto.TransferSwapDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computes transfer impact by inferring player swaps from consecutive gameweek picks
 * and reading hit penalties from the stored rank history.
 */
@Service
@Transactional(readOnly = true)
public class TransferImpactServiceImpl implements TransferImpactService {

    private static final Logger log = LoggerFactory.getLogger(TransferImpactServiceImpl.class);

    private static final Set<String> CHIP_SKIP = Set.of("wildcard", "freehit");

    private final UserTeamRepository userTeamRepository;
    private final UserPickRepository userPickRepository;
    private final UserTeamRankHistoryRepository rankHistoryRepository;
    private final PlayerHistoryRepository playerHistoryRepository;

    /**
     * @param userTeamRepository      resolves the user team by FPL ID
     * @param userPickRepository      loads all picks for the team
     * @param rankHistoryRepository   provides per-GW transfer count, hit cost, and chip info
     * @param playerHistoryRepository loads player points for each relevant GW
     */
    public TransferImpactServiceImpl(UserTeamRepository userTeamRepository,
                                     UserPickRepository userPickRepository,
                                     UserTeamRankHistoryRepository rankHistoryRepository,
                                     PlayerHistoryRepository playerHistoryRepository) {
        this.userTeamRepository = userTeamRepository;
        this.userPickRepository = userPickRepository;
        this.rankHistoryRepository = rankHistoryRepository;
        this.playerHistoryRepository = playerHistoryRepository;
    }

    /** {@inheritDoc} */
    @Override
    public TransferImpactDto getTransferImpact(long fplTeamId) {
        UserTeam userTeam = userTeamRepository.findByFplTeamId(fplTeamId)
                .orElseThrow(() -> new TeamNotFoundException(fplTeamId));

        List<UserTeamRankHistory> rankHistory = rankHistoryRepository
                .findAllByUserTeam_FplTeamIdOrderByGameWeekAsc(fplTeamId);

        Map<Integer, UserTeamRankHistory> rankByGw = rankHistory.stream()
                .collect(Collectors.toMap(UserTeamRankHistory::getGameWeek, r -> r));

        List<UserPick> allPicks = userPickRepository.findAllByUserTeam(userTeam);
        Map<Integer, Set<Integer>> playerIdsByGw = groupPickedPlayerIdsByGw(allPicks);

        Set<Integer> allInvolvedPlayerIds = collectInvolvedPlayerIds(playerIdsByGw, rankByGw);
        Map<Integer, Map<Integer, Integer>> pointsByPlayerAndGw =
                loadPointsByPlayerAndGw(allInvolvedPlayerIds);

        List<TransferImpactGwDto> perGameWeek = new ArrayList<>();
        int totalSwapPointsLost = 0;
        int totalHitPointsLost = 0;

        List<Integer> sortedGws = playerIdsByGw.keySet().stream().sorted().toList();
        for (int i = 1; i < sortedGws.size(); i++) {
            int gw = sortedGws.get(i);
            int prevGw = sortedGws.get(i - 1);
            UserTeamRankHistory gwRank = rankByGw.get(gw);
            if (gwRank == null || gwRank.getEventTransfers() == 0) continue;
            if (gwRank.getChipUsed() != null && CHIP_SKIP.contains(gwRank.getChipUsed())) continue;

            Set<Integer> prevIds = playerIdsByGw.getOrDefault(prevGw, Set.of());
            Set<Integer> currIds = playerIdsByGw.getOrDefault(gw, Set.of());

            List<Integer> soldIds = prevIds.stream().filter(id -> !currIds.contains(id)).toList();
            List<Integer> boughtIds = currIds.stream().filter(id -> !prevIds.contains(id)).toList();

            List<TransferSwapDto> swaps = buildSwaps(soldIds, boughtIds, gw, pointsByPlayerAndGw, allPicks);

            int gwSwapImpact = swaps.stream().mapToInt(TransferSwapDto::getSwapImpact).sum();
            int gwHitCost = -gwRank.getEventTransfersCost();

            TransferImpactGwDto gwDto = new TransferImpactGwDto();
            gwDto.setGameWeek(gw);
            gwDto.setSwapImpact(gwSwapImpact);
            gwDto.setHitCost(gwHitCost);
            gwDto.setTransfers(swaps);
            perGameWeek.add(gwDto);

            totalSwapPointsLost += gwSwapImpact;
            totalHitPointsLost += gwHitCost;
        }

        TransferImpactDto result = new TransferImpactDto();
        result.setTotalSwapPointsLost(totalSwapPointsLost);
        result.setTotalHitPointsLost(totalHitPointsLost);
        result.setPerGameWeek(perGameWeek);
        return result;
    }

    /**
     * Groups the FPL player IDs of all picks by their gameweek number.
     *
     * @param allPicks all picks for the user team
     * @return map of gameweek number to set of picked player FPL IDs
     */
    private Map<Integer, Set<Integer>> groupPickedPlayerIdsByGw(List<UserPick> allPicks) {
        return allPicks.stream()
                .collect(Collectors.groupingBy(
                        pick -> pick.getGameWeek().getGameWeekNumber(),
                        Collectors.mapping(pick -> pick.getPlayer().getFplId(), Collectors.toSet())
                ));
    }

    /**
     * Collects the union of all player FPL IDs that appear as sold or bought
     * in any transfer gameweek, for bulk history loading.
     *
     * @param playerIdsByGw map of gameweek number to picked player IDs
     * @param rankByGw      map of gameweek number to rank history (for transfer/chip info)
     * @return set of all player FPL IDs involved in transfers
     */
    private Set<Integer> collectInvolvedPlayerIds(Map<Integer, Set<Integer>> playerIdsByGw,
                                                   Map<Integer, UserTeamRankHistory> rankByGw) {
        List<Integer> sortedGws = playerIdsByGw.keySet().stream().sorted().toList();
        Set<Integer> involved = new java.util.HashSet<>();
        for (int i = 1; i < sortedGws.size(); i++) {
            int gw = sortedGws.get(i);
            int prevGw = sortedGws.get(i - 1);
            UserTeamRankHistory gwRank = rankByGw.get(gw);
            if (gwRank == null || gwRank.getEventTransfers() == 0) continue;
            if (gwRank.getChipUsed() != null && CHIP_SKIP.contains(gwRank.getChipUsed())) continue;
            involved.addAll(playerIdsByGw.getOrDefault(prevGw, Set.of()));
            involved.addAll(playerIdsByGw.getOrDefault(gw, Set.of()));
        }
        return involved;
    }

    /**
     * Loads all history records for the given player IDs and builds a lookup map:
     * {@code playerFplId -> gameWeekNumber -> points}.
     *
     * @param playerFplIds set of FPL player IDs to load histories for
     * @return nested map for O(1) point lookups
     */
    private Map<Integer, Map<Integer, Integer>> loadPointsByPlayerAndGw(Set<Integer> playerFplIds) {
        if (playerFplIds.isEmpty()) return Map.of();
        List<PlayerHistory> histories = playerHistoryRepository.findAllByPlayerFplIds(playerFplIds);
        return histories.stream().collect(Collectors.groupingBy(
                ph -> ph.getPlayer().getFplId(),
                Collectors.toMap(ph -> ph.getGameWeek().getGameWeekNumber(), PlayerHistory::getPoints)
        ));
    }

    /**
     * Pairs sold and bought player IDs and constructs a swap DTO for each pair.
     * Pairing is positional (index-matched); the order of pairing does not affect the
     * total swap impact sum.
     *
     * @param soldIds            FPL IDs of players who left the squad
     * @param boughtIds          FPL IDs of players who joined the squad
     * @param gameWeek           the gameweek in which the transfers occurred
     * @param pointsByPlayerAndGw lookup map for player points per GW
     * @param allPicks           all picks (used to resolve player display names)
     * @return list of swap DTOs, one per transfer pair
     */
    private List<TransferSwapDto> buildSwaps(List<Integer> soldIds,
                                              List<Integer> boughtIds,
                                              int gameWeek,
                                              Map<Integer, Map<Integer, Integer>> pointsByPlayerAndGw,
                                              List<UserPick> allPicks) {
        Map<Integer, String> playerNames = allPicks.stream()
                .collect(Collectors.toMap(
                        pick -> pick.getPlayer().getFplId(),
                        pick -> pick.getPlayer().getWebName(),
                        (a, b) -> a
                ));

        int pairCount = Math.min(soldIds.size(), boughtIds.size());
        List<TransferSwapDto> swaps = new ArrayList<>(pairCount);
        for (int index = 0; index < pairCount; index++) {
            int soldId = soldIds.get(index);
            int boughtId = boughtIds.get(index);
            int soldPoints  = pointsByPlayerAndGw.getOrDefault(soldId, Map.of()).getOrDefault(gameWeek, 0);
            int boughtPoints = pointsByPlayerAndGw.getOrDefault(boughtId, Map.of()).getOrDefault(gameWeek, 0);

            TransferSwapDto swap = new TransferSwapDto();
            swap.setPlayerOut(playerNames.getOrDefault(soldId, "Unknown"));
            swap.setPlayerIn(playerNames.getOrDefault(boughtId, "Unknown"));
            swap.setPointsOut(soldPoints);
            swap.setPointsIn(boughtPoints);
            swap.setSwapImpact(boughtPoints - soldPoints);
            swaps.add(swap);
        }
        return swaps;
    }
}
