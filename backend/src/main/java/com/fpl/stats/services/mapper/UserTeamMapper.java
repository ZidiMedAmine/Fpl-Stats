package com.fpl.stats.services.mapper;

import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.domain.UserTeamRankHistory;
import com.fpl.stats.services.dto.PlayerDto;
import com.fpl.stats.services.dto.RankHistoryDto;
import com.fpl.stats.services.dto.UserTeamDto;

import java.util.List;
import java.util.Map;

/**
 * Static utility class for mapping {@link UserTeam} domain objects and related entities
 * to their corresponding DTOs.
 */
public final class UserTeamMapper {

    /**
     * Private constructor — this is a static utility class and must not be instantiated.
     */
    private UserTeamMapper() {
    }

    /**
     * Maps a {@link UserTeam} and its associated data to a {@link UserTeamDto}.
     *
     * @param userTeam         the user team entity
     * @param players          the list of player DTOs for this team's current picks
     * @param gameWeekAverages map of gameweek number to the FPL global average score for that week
     * @param rankChange       the change in overall rank since the previous gameweek (negative = improved)
     * @param rankHistory      the per-gameweek rank history entries for this team
     * @return a fully populated {@link UserTeamDto}
     */
    public static UserTeamDto toDto(UserTeam userTeam, List<PlayerDto> players,
                                    Map<Integer, Integer> gameWeekAverages, Integer rankChange,
                                    List<UserTeamRankHistory> rankHistory) {
        UserTeamDto dto = new UserTeamDto();
        dto.setFplTeamId(userTeam.getFplTeamId());
        dto.setName(userTeam.getPlayerFirstName() + " " + userTeam.getPlayerLastName());
        dto.setTeamName(userTeam.getTeamName());
        dto.setRegion(userTeam.getRegion());
        dto.setOverallRank(userTeam.getOverallRank());
        dto.setTotalPoints(userTeam.getTotalPoints());
        dto.setCurrentGameWeek(userTeam.getLastSyncedGameWeek());
        dto.setPlayers(players);
        dto.setGameWeekAverages(gameWeekAverages);
        dto.setTeamValue(userTeam.getTeamValue());
        dto.setBank(userTeam.getBank());
        dto.setTotalTransfers(userTeam.getTotalTransfers());
        dto.setRankChange(rankChange);
        dto.setRankHistory(rankHistory.stream().map(UserTeamMapper::toRankHistoryDto).toList());
        return dto;
    }

    /**
     * Maps a {@link UserTeamRankHistory} entry to a {@link RankHistoryDto}.
     *
     * @param history the rank history entry for a single gameweek
     * @return a populated {@link RankHistoryDto}
     */
    private static RankHistoryDto toRankHistoryDto(UserTeamRankHistory history) {
        RankHistoryDto dto = new RankHistoryDto();
        dto.setGameWeek(history.getGameWeek());
        dto.setOverallRank(history.getOverallRank());
        dto.setGwRank(history.getGwRank());
        dto.setGwPoints(history.getGwPoints());
        dto.setTotalPoints(history.getTotalPoints());
        dto.setBank(history.getBank());
        dto.setTeamValue(history.getTeamValue() != null ? history.getTeamValue() : 0.0);
        dto.setEventTransfers(history.getEventTransfers());
        dto.setPointsOnBench(history.getPointsOnBench());
        dto.setChipUsed(history.getChipUsed());
        return dto;
    }
}
