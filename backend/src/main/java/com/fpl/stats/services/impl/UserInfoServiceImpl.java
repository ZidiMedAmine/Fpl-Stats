package com.fpl.stats.services.impl;

import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.UserPick;
import com.fpl.stats.domain.UserTeam;
import com.fpl.stats.domain.UserTeamRankHistory;
import com.fpl.stats.exception.TeamNotFoundException;
import com.fpl.stats.repository.GameWeekRepository;
import com.fpl.stats.repository.PlayerRepository;
import com.fpl.stats.repository.UserPickRepository;
import com.fpl.stats.repository.UserTeamRankHistoryRepository;
import com.fpl.stats.repository.UserTeamRepository;
import com.fpl.stats.services.UserInfoService;
import com.fpl.stats.services.dto.CompareDto;
import com.fpl.stats.services.dto.PlayerDto;
import com.fpl.stats.services.dto.RankHistoryDto;
import com.fpl.stats.services.dto.UserTeamDto;
import com.fpl.stats.services.mapper.PlayerMapper;
import com.fpl.stats.services.mapper.UserTeamMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link UserInfoService} that reads user team data from the local database.
 * All reads use a read-only transaction for Hibernate optimisation.
 */
@Service
@Transactional(readOnly = true)
public class UserInfoServiceImpl implements UserInfoService {

    private static final Logger log = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    private final UserTeamRankHistoryRepository userTeamRankHistoryRepository;
    private final UserTeamRepository userTeamRepository;
    private final UserPickRepository userPickRepository;
    private final GameWeekRepository gameWeekRepository;
    private final PlayerRepository playerRepository;

    /**
     * Constructs a {@code UserInfoServiceImpl} with its required repository dependencies.
     *
     * @param userTeamRankHistoryRepository repository for per-gameweek rank history
     * @param userTeamRepository            repository for user team metadata
     * @param userPickRepository            repository for user gameweek picks
     * @param gameWeekRepository            repository for gameweek data and averages
     * @param playerRepository              repository for player entities with history
     */
    public UserInfoServiceImpl(UserTeamRankHistoryRepository userTeamRankHistoryRepository,
                               UserTeamRepository userTeamRepository,
                               UserPickRepository userPickRepository,
                               GameWeekRepository gameWeekRepository,
                               PlayerRepository playerRepository) {
        this.userTeamRankHistoryRepository = userTeamRankHistoryRepository;
        this.userTeamRepository = userTeamRepository;
        this.userPickRepository = userPickRepository;
        this.gameWeekRepository = gameWeekRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserTeamDto getUserTeamInfo(long fplTeamId) {
        UserTeam userTeam = userTeamRepository.findByFplTeamId(fplTeamId)
                .orElseThrow(() -> new TeamNotFoundException(fplTeamId));
        return buildUserTeamDto(userTeam);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompareDto compareTeams(long fplTeamId1, long fplTeamId2) {
        UserTeam userTeam1 = userTeamRepository.findByFplTeamId(fplTeamId1)
                .orElseThrow(() -> new TeamNotFoundException(fplTeamId1));
        UserTeam userTeam2 = userTeamRepository.findByFplTeamId(fplTeamId2)
                .orElseThrow(() -> new TeamNotFoundException(fplTeamId2));

        UserTeamDto team1Dto = buildUserTeamDto(userTeam1);
        UserTeamDto team2Dto = buildUserTeamDto(userTeam2);

        Set<Integer> currentSquad1 = currentSquadFplIds(userTeam1);
        Set<Integer> currentSquad2 = currentSquadFplIds(userTeam2);

        List<PlayerDto> squad1Players = team1Dto.getPlayers().stream()
                .filter(p -> currentSquad1.contains(p.getFplId()))
                .toList();
        List<PlayerDto> squad2Players = team2Dto.getPlayers().stream()
                .filter(p -> currentSquad2.contains(p.getFplId()))
                .toList();

        List<PlayerDto> sharedPlayers = squad1Players.stream()
                .filter(p -> currentSquad2.contains(p.getFplId()))
                .toList();
        List<PlayerDto> team1Differentials = squad1Players.stream()
                .filter(p -> !currentSquad2.contains(p.getFplId()))
                .toList();
        List<PlayerDto> team2Differentials = squad2Players.stream()
                .filter(p -> !currentSquad1.contains(p.getFplId()))
                .toList();

        Map<Integer, int[]> pointsByGameWeek = new TreeMap<>();
        addPointsByGwFromHistory(pointsByGameWeek, team1Dto.getRankHistory(), 0);
        addPointsByGwFromHistory(pointsByGameWeek, team2Dto.getRankHistory(), 1);

        CompareDto compareDto = new CompareDto();
        compareDto.setTeam1(team1Dto);
        compareDto.setTeam2(team2Dto);
        compareDto.setSharedPlayers(sharedPlayers);
        compareDto.setTeam1Differentials(team1Differentials);
        compareDto.setTeam2Differentials(team2Differentials);
        compareDto.setPointsByGameWeek(pointsByGameWeek);
        return compareDto;
    }

    /**
     * Builds a {@link UserTeamDto} from an already-resolved {@link UserTeam} entity.
     * Extracted to avoid redundant database lookups when multiple DTOs are built in one operation.
     *
     * @param userTeam the resolved user team entity
     * @return a fully populated {@link UserTeamDto}
     */
    private UserTeamDto buildUserTeamDto(UserTeam userTeam) {
        List<UserPick> picks = userPickRepository.findAllByUserTeam(userTeam);

        Map<Integer, List<UserPick>> picksByPlayer = picks.stream()
                .collect(Collectors.groupingBy(pick -> pick.getPlayer().getFplId()));

        Map<Integer, Player> enrichedPlayers = playerRepository
                .findByFplIdInWithHistory(picksByPlayer.keySet())
                .stream()
                .collect(Collectors.toMap(Player::getFplId, p -> p));

        List<PlayerDto> playerDtos = picksByPlayer.entrySet().stream()
                .map(entry -> PlayerMapper.toPlayerDto(entry.getValue(), enrichedPlayers.get(entry.getKey())))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PlayerDto::getPosition))
                .toList();

        Map<Integer, Integer> gameWeekAverages = gameWeekRepository.findAllGameWeekAverages().stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Integer) row[1]
                ));

        List<UserTeamRankHistory> rankHistory =
                userTeamRankHistoryRepository.findAllByUserTeam_FplTeamIdOrderByGameWeekAsc(
                        userTeam.getFplTeamId());

        Integer rankChange = computeRankChange(rankHistory);

        return UserTeamMapper.toDto(userTeam, playerDtos, gameWeekAverages, rankChange, rankHistory);
    }

    /**
     * Returns the FPL IDs of players in the team's most recently synced gameweek squad.
     *
     * @param userTeam the team to query
     * @return set of current squad player FPL IDs
     */
    private Set<Integer> currentSquadFplIds(UserTeam userTeam) {
        return userPickRepository
                .findAllByUserTeamAndGameWeek_GameWeekNumber(userTeam, userTeam.getLastSyncedGameWeek())
                .stream()
                .map(pick -> pick.getPlayer().getFplId())
                .collect(Collectors.toSet());
    }

    /**
     * Populates a gameweek → two-team points map from one team's rank history.
     *
     * @param pointsByGameWeek the shared accumulator map keyed by gameweek number
     * @param rankHistory      the rank history for one team
     * @param teamIndex        {@code 0} for team 1, {@code 1} for team 2
     */
    private void addPointsByGwFromHistory(Map<Integer, int[]> pointsByGameWeek,
                                          List<RankHistoryDto> rankHistory, int teamIndex) {
        for (RankHistoryDto entry : rankHistory) {
            int[] gwPoints = pointsByGameWeek.computeIfAbsent(entry.getGameWeek(), k -> new int[2]);
            gwPoints[teamIndex] = entry.getGwPoints();
        }
    }

    /**
     * Computes the overall rank change between the two most recent gameweeks.
     * A negative value means the rank improved (lower number is better).
     *
     * @param history the full rank history ordered by gameweek ascending
     * @return the rank change, or {@code null} if fewer than two entries exist
     */
    private Integer computeRankChange(List<UserTeamRankHistory> history) {
        if (history.size() < 2) return null;
        UserTeamRankHistory latest = history.get(history.size() - 1);
        UserTeamRankHistory previous = history.get(history.size() - 2);
        return previous.getOverallRank() - latest.getOverallRank();
    }
}
