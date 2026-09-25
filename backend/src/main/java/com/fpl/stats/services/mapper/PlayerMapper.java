package com.fpl.stats.services.mapper;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.PlayerHistory;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.services.dto.GameWeekPerformance;
import com.fpl.stats.services.dto.PlayerDetailDto;
import com.fpl.stats.services.dto.PlayerDto;
import com.fpl.stats.services.dto.PlayerSummaryDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static utility class for mapping {@link Player} domain objects and related entities
 * to their corresponding DTOs.
 */
public final class PlayerMapper {

    /**
     * Private constructor — this is a static utility class and must not be instantiated.
     */
    private PlayerMapper() {
    }

    /**
     * Maps a {@link Player} to a {@link PlayerSummaryDto} for autocomplete search results.
     *
     * @param player the player entity
     * @return lightweight summary DTO
     */
    public static PlayerSummaryDto toPlayerSummaryDto(Player player) {
        PlayerSummaryDto dto = new PlayerSummaryDto();
        mapBasePlayerFields(player, dto);
        return dto;
    }

    /**
     * Maps a {@link Player} with its histories to a {@link PlayerDetailDto} for the comparison page.
     * Performances are not tied to any user team — wasInMyTeam, wasCaptain, wasBenched are all false.
     *
     * @param player the player entity with playerHistories loaded
     * @return full detail DTO with per-GW performances
     */
    public static PlayerDetailDto toPlayerDetailDto(Player player) {
        PlayerDetailDto dto = new PlayerDetailDto();
        mapBasePlayerFields(player, dto);

        List<GameWeekPerformance> performances = player.getPlayerHistories().stream()
                .sorted(Comparator.comparingInt(h -> h.getGameWeek().getGameWeekNumber()))
                .map(PlayerMapper::toGwPerformance)
                .collect(Collectors.toList());

        dto.setPerformances(performances);
        dto.setAvgPoints(performances.isEmpty() ? 0.0 :
                performances.stream().mapToInt(GameWeekPerformance::getPoints).average().orElse(0.0));
        return dto;
    }

    /**
     * Maps a {@link Player} and the list of user picks for that player to a {@link PlayerDto},
     * including per-gameweek performances enriched with pick context (captain, benched, multiplier).
     *
     * @param playerPicks the user's picks for this player across all synced gameweeks
     * @param player      the player entity with histories loaded
     * @return a fully populated {@link PlayerDto}, or {@code null} if either argument is null or empty
     */
    public static PlayerDto toPlayerDto(List<UserPick> playerPicks, Player player) {
        if (playerPicks == null || playerPicks.isEmpty() || player == null) {
            return null;
        }
        PlayerDto playerDto = new PlayerDto();
        mapPlayerDtoBaseFields(player, playerDto);

        Map<Integer, PlayerHistory> historyByGw = buildHistoryByGwMap(player);

        List<GameWeekPerformance> performances = new ArrayList<>();
        int totalPoints = 0;
        int avgPointsTotal = 0;
        int activeGwCount = 0;

        for (UserPick pick : playerPicks) {
            PlayerHistory history = historyByGw.get(pick.getGameWeek().getGameWeekNumber());
            performances.add(buildPerformanceFromPick(pick, history));

            if (history != null && pick.getMultiplier() > 0) {
                totalPoints += history.getPoints() * pick.getMultiplier();
                if (history.getMinutesPlayed() > 0) {
                    avgPointsTotal += history.getPoints() * Math.min(pick.getMultiplier(), 2);
                    activeGwCount++;
                }
            }
        }

        playerDto.setPerformances(performances);
        playerDto.setTotalPointsForTeam(totalPoints);
        playerDto.setAvgPoints(activeGwCount == 0 ? 0 : (double) avgPointsTotal / activeGwCount);
        return playerDto;
    }

    /**
     * Maps the base fields from a {@link Player} onto a {@link PlayerDto}.
     *
     * @param player    the source player entity
     * @param playerDto the target DTO to populate
     */
    private static void mapPlayerDtoBaseFields(Player player, PlayerDto playerDto) {
        playerDto.setFplId(player.getFplId());
        playerDto.setName(player.getWebName());
        playerDto.setPosition(player.getPosition());
        playerDto.setCode(player.getCode());
        playerDto.setNowCost(player.getNowCost());
        playerDto.setStatus(player.getStatus());
        playerDto.setTotalPoints(player.getTotalPoints());
        if (player.getTeam() != null) {
            playerDto.setTeamName(player.getTeam().getShortName());
        }
    }

    /**
     * Builds a gameweek-number → {@link PlayerHistory} lookup map from the player's histories.
     * When duplicate entries exist for the same gameweek the first one wins.
     *
     * @param player the player entity with histories loaded
     * @return map keyed by gameweek number
     */
    private static Map<Integer, PlayerHistory> buildHistoryByGwMap(Player player) {
        return player.getPlayerHistories().stream()
                .collect(Collectors.toMap(
                        ph -> ph.getGameWeek().getGameWeekNumber(),
                        ph -> ph,
                        (a, b) -> a));
    }

    /**
     * Builds a {@link GameWeekPerformance} from a user pick and the matching player history.
     * Pick context (captain, benched, multiplier) is always applied; history stats are applied
     * only when a matching history entry exists.
     *
     * @param pick    the user pick for the gameweek
     * @param history the player's history for that gameweek, or {@code null} if unavailable
     * @return the assembled gameweek performance
     */
    private static GameWeekPerformance buildPerformanceFromPick(UserPick pick, PlayerHistory history) {
        GameWeekPerformance.Builder builder = new GameWeekPerformance.Builder()
                .gameWeek(pick.getGameWeek().getGameWeekNumber())
                .wasInMyTeam(true)
                .wasCaptain(pick.isCaptain())
                .wasViceCaptain(pick.isViceCaptain())
                .wasTripleCaptain(pick.isTripleCaptain())
                .wasBenched(pick.isBenched())
                .multiplier(pick.getMultiplier());

        if (history != null) {
            applyHistoryToBuilder(history, builder);
        }
        return builder.build();
    }

    /**
     * Maps the common base fields from a {@link Player} entity onto a {@link PlayerSummaryDto}.
     * Populates fplId, webName, position, code, nowCost, totalPoints, selectedByPercent, and teamName.
     *
     * @param player the source player entity
     * @param dto    the target summary DTO to populate
     */
    private static void mapBasePlayerFields(Player player, PlayerSummaryDto dto) {
        dto.setFplId(player.getFplId());
        dto.setWebName(player.getWebName());
        dto.setPosition(player.getPosition());
        dto.setCode(player.getCode());
        dto.setNowCost(player.getNowCost());
        dto.setTotalPoints(player.getTotalPoints());
        dto.setSelectedByPercent(player.getSelectedByPercent());
        if (player.getTeam() != null) {
            dto.setTeamName(player.getTeam().getShortName());
        }
    }

    /**
     * Maps the common base fields from a {@link Player} entity onto a {@link PlayerDetailDto}.
     * Populates fplId, webName, position, code, nowCost, totalPoints, selectedByPercent, and teamName.
     *
     * @param player the source player entity
     * @param dto    the target detail DTO to populate
     */
    private static void mapBasePlayerFields(Player player, PlayerDetailDto dto) {
        dto.setFplId(player.getFplId());
        dto.setWebName(player.getWebName());
        dto.setPosition(player.getPosition());
        dto.setCode(player.getCode());
        dto.setNowCost(player.getNowCost());
        dto.setTotalPoints(player.getTotalPoints());
        dto.setSelectedByPercent(player.getSelectedByPercent());
        if (player.getTeam() != null) {
            dto.setTeamName(player.getTeam().getShortName());
        }
    }

    /**
     * Maps a {@link PlayerHistory} to a {@link GameWeekPerformance} with no user-team context.
     * All ownership flags ({@code wasInMyTeam}, {@code wasCaptain}, etc.) are set to {@code false}.
     *
     * @param history the player history entry for a single gameweek
     * @return a gameweek performance with stats only, no pick context
     */
    private static GameWeekPerformance toGwPerformance(PlayerHistory history) {
        GameWeekPerformance.Builder builder = new GameWeekPerformance.Builder()
                .gameWeek(history.getGameWeek().getGameWeekNumber())
                .wasInMyTeam(false)
                .wasCaptain(false)
                .wasViceCaptain(false)
                .wasTripleCaptain(false)
                .wasBenched(false);
        applyHistoryToBuilder(history, builder);
        return builder.build();
    }

    /**
     * Applies all stat fields from a {@link PlayerHistory} onto a {@link GameWeekPerformance.Builder}.
     * Shared by {@link #toGwPerformance} and {@link #buildPerformanceFromPick}.
     *
     * @param history the source history entry
     * @param builder the builder to populate
     */
    private static void applyHistoryToBuilder(PlayerHistory history, GameWeekPerformance.Builder builder) {
        builder.points(history.getPoints())
                .minutesPlayed(history.getMinutesPlayed())
                .goalsScored(history.getGoalsScored())
                .assists(history.getAssists())
                .cleanSheet(history.getCleanSheets() > 0)
                .yellowCards(history.getYellowCards())
                .redCards(history.getRedCards())
                .bonusPoints(history.getBonus())
                .bps(history.getBps())
                .saves(history.getSaves())
                .expectedGoals(history.getExpectedGoals())
                .expectedAssists(history.getExpectedAssists())
                .expectedGoalInvolvements(history.getExpectedGoalInvolvements())
                .goalsConceded(history.getGoalsConceded())
                .expectedGoalsConceded(history.getExpectedGoalsConceded())
                .clearancesBlocksInterceptions(history.getClearancesBlocksInterceptions())
                .recoveries(history.getRecoveries())
                .tackles(history.getTackles())
                .defensiveContribution(history.getDefensiveContribution())
                .influence(history.getInfluence())
                .creativity(history.getCreativity())
                .threat(history.getThreat())
                .ictIndex(history.getIctIndex())
                .wasHome(history.isWasHome())
                .value(history.getValue())
                .transfersIn(history.getTransfersIn())
                .transfersOut(history.getTransfersOut())
                .transfersBalance(history.getTransfersBalance())
                .selected(history.getSelected());
    }
}
