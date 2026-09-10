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
        playerDto.setFplId(player.getFplId());
        playerDto.setName(player.getWebName());
        playerDto.setPosition(player.getPosition());
        playerDto.setCode(player.getCode());
        playerDto.setNowCost(player.getNowCost());
        playerDto.setStatus(player.getStatus());

        if (player.getTeam() != null) {
            playerDto.setTeamName(player.getTeam().getShortName());
        }

        Map<Integer, PlayerHistory> historyByGw = player.getPlayerHistories().stream()
                .collect(Collectors.toMap(
                        ph -> ph.getGameWeek().getGameWeekNumber(),
                        ph -> ph,
                        (a, b) -> a));

        List<GameWeekPerformance> performances = new ArrayList<>();
        int totalPoints = 0;

        for (UserPick pick : playerPicks) {
            int gwNumber = pick.getGameWeek().getGameWeekNumber();
            PlayerHistory history = historyByGw.get(gwNumber);

            GameWeekPerformance.Builder builder = new GameWeekPerformance.Builder()
                    .gameWeek(gwNumber)
                    .wasInMyTeam(true)
                    .wasCaptain(pick.isCaptain())
                    .wasViceCaptain(pick.isViceCaptain())
                    .wasTripleCaptain(pick.isTripleCaptain())
                    .wasBenched(pick.isBenched())
                    .multiplier(pick.getMultiplier());

            if (history != null) {
                int gwPoints = history.getPoints();
                if (pick.getMultiplier() > 0) {
                    totalPoints += gwPoints * pick.getMultiplier();
                }
                builder.points(gwPoints)
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

            performances.add(builder.build());
        }

        playerDto.setPerformances(performances);
        playerDto.setTotalPointsForTeam(totalPoints);
        playerDto.setAvgPoints(performances.isEmpty() ? 0 :
                (double) totalPoints / performances.size());
        return playerDto;
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
        return new GameWeekPerformance.Builder()
                .gameWeek(history.getGameWeek().getGameWeekNumber())
                .points(history.getPoints())
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
                .selected(history.getSelected())
                .wasInMyTeam(false)
                .wasCaptain(false)
                .wasViceCaptain(false)
                .wasTripleCaptain(false)
                .wasBenched(false)
                .build();
    }
}
