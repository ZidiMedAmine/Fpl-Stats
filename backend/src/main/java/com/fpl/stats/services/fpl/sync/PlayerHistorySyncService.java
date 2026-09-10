package com.fpl.stats.services.fpl.sync;

import com.fpl.stats.domain.GameWeek;
import com.fpl.stats.domain.Player;
import com.fpl.stats.domain.PlayerHistory;
import com.fpl.stats.repository.GameWeekRepository;
import com.fpl.stats.repository.PlayerHistoryRepository;
import com.fpl.stats.repository.PlayerRepository;
import com.fpl.stats.services.fpl.FixtureDataService;
import com.fpl.stats.services.util.FplApiClient;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Synchronizes per-player gameweek history from the FPL element-summary endpoint.
 * Uses parallel {@link CompletableFuture} batching for throughput.
 */
@Service
public class PlayerHistorySyncService {

    private static final Logger log = LoggerFactory.getLogger(PlayerHistorySyncService.class);
    private static final int BATCH_SIZE = 50;
    private static final int THREAD_POOL_SIZE = 10;

    private final FplApiClient fplApiClient;
    private final PlayerRepository playerRepository;
    private final PlayerHistoryRepository playerHistoryRepository;
    private final GameWeekRepository gameWeekRepository;
    private final FixtureDataService fixtureDataService;
    private final ExecutorService historyFetchExecutor;

    /**
     * Constructs a {@code PlayerHistorySyncService} and initialises the shared thread pool
     * used for parallel history fetching.
     *
     * @param fplApiClient             used to fetch per-player history from element-summary
     * @param playerRepository         provides the list of players to sync
     * @param playerHistoryRepository  persists and checks existing history records
     * @param gameWeekRepository       resolves gameweek entities by number
     * @param fixtureDataService       used to derive the last truly completed gameweek
     */
    public PlayerHistorySyncService(FplApiClient fplApiClient,
                                    PlayerRepository playerRepository,
                                    PlayerHistoryRepository playerHistoryRepository,
                                    GameWeekRepository gameWeekRepository,
                                    FixtureDataService fixtureDataService) {
        this.fplApiClient = fplApiClient;
        this.playerRepository = playerRepository;
        this.playerHistoryRepository = playerHistoryRepository;
        this.gameWeekRepository = gameWeekRepository;
        this.fixtureDataService = fixtureDataService;
        this.historyFetchExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Shuts down the shared thread pool when the Spring context closes.
     */
    @PreDestroy
    public void shutdown() {
        historyFetchExecutor.shutdown();
    }

    /**
     * Fetches and persists player history for every player up to the last completed gameweek.
     * Skips records that already exist. Processes players in parallel batches.
     */
    public void syncPlayerHistory() {
        List<Player> allPlayers = playerRepository.findAll();
        syncPlayerHistoryForPlayers(allPlayers);
    }

    /**
     * Fetches and persists player history for a specific list of players up to the last
     * completed gameweek. Skips records that already exist.
     *
     * @param players the players whose history should be synced
     */
    public void syncPlayerHistoryForPlayers(List<Player> players) {
        int lastCompleted = fixtureDataService.getLastCompletedGameWeek();
        if (lastCompleted == 0) {
            log.info("No completed gameweeks -- skipping player history sync");
            return;
        }

        Map<Integer, GameWeek> gameWeekMap = gameWeekRepository.findAll().stream()
                .collect(Collectors.toMap(GameWeek::getGameWeekNumber, gw -> gw));

        log.info("Syncing history for {} players up to GW{}", players.size(), lastCompleted);

        for (int i = 0; i < players.size(); i += BATCH_SIZE) {
            List<Player> batch = players.subList(i, Math.min(i + BATCH_SIZE, players.size()));

            List<CompletableFuture<List<PlayerHistory>>> futures = batch.stream()
                    .map(player -> CompletableFuture.supplyAsync(
                            () -> fetchAndMapHistory(player, gameWeekMap, lastCompleted),
                            historyFetchExecutor))
                    .toList();

            List<PlayerHistory> batchHistories = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .toList();

            if (!batchHistories.isEmpty()) {
                playerHistoryRepository.saveAll(batchHistories);
            }
        }

        log.info("Player history sync completed for {} players", players.size());
    }

    /**
     * Fetches and maps the gameweek history for a single player from the FPL element-summary endpoint.
     * Skips gameweeks beyond {@code lastCompleted} and entries that already exist in the database.
     *
     * @param player        the player whose history to fetch
     * @param gameWeekMap   lookup map of gameweek number to {@link GameWeek} entity
     * @param lastCompleted the highest completed gameweek number to sync up to
     * @return list of new {@link PlayerHistory} records to persist, or an empty list on failure
     */
    @SuppressWarnings("unchecked")
    private List<PlayerHistory> fetchAndMapHistory(Player player,
                                                    Map<Integer, GameWeek> gameWeekMap,
                                                    int lastCompleted) {
        try {
            Map<String, Object> response = fplApiClient.get(
                    "/element-summary/" + player.getFplId() + "/");
            List<Map<String, Object>> historyList =
                    (List<Map<String, Object>>) response.get("history");

            if (historyList == null) return List.of();

            List<PlayerHistory> histories = new ArrayList<>();
            for (Map<String, Object> historyData : historyList) {
                int gwNumber = ((Number) historyData.get("round")).intValue();
                if (gwNumber > lastCompleted) continue;

                GameWeek gameWeek = gameWeekMap.get(gwNumber);
                if (gameWeek == null) continue;

                if (playerHistoryRepository.existsByPlayerAndGameWeek(player, gameWeek)) {
                    continue;
                }

                PlayerHistory playerHistory = new PlayerHistory();
                mapHistoryFields(playerHistory, historyData, player, gameWeek);
                histories.add(playerHistory);
            }
            return histories;
        } catch (Exception e) {
            log.error("Failed to fetch history for player {}", player.getFplId(), e);
            return List.of();
        }
    }

    /**
     * Maps raw FPL element-summary history data onto a {@link PlayerHistory} entity.
     *
     * @param playerHistory the entity to populate
     * @param historyData   the raw per-gameweek map from the FPL {@code /element-summary/} {@code history} array
     * @param player        the owning player entity
     * @param gameWeek      the gameweek entity this history entry belongs to
     */
    private void mapHistoryFields(PlayerHistory playerHistory, Map<String, Object> historyData,
                                  Player player, GameWeek gameWeek) {
        playerHistory.setPlayer(player);
        playerHistory.setGameWeek(gameWeek);
        playerHistory.setPoints(((Number) historyData.get("total_points")).intValue());
        playerHistory.setMinutesPlayed(((Number) historyData.get("minutes")).intValue());
        playerHistory.setGoalsScored(((Number) historyData.get("goals_scored")).intValue());
        playerHistory.setAssists(((Number) historyData.get("assists")).intValue());
        playerHistory.setCleanSheets(((Number) historyData.get("clean_sheets")).intValue());
        playerHistory.setYellowCards(((Number) historyData.get("yellow_cards")).intValue());
        playerHistory.setRedCards(((Number) historyData.get("red_cards")).intValue());
        playerHistory.setBonus(((Number) historyData.get("bonus")).intValue());
        playerHistory.setBps(((Number) historyData.get("bps")).intValue());
        playerHistory.setSaves(((Number) historyData.get("saves")).intValue());
        playerHistory.setOwnGoals(((Number) historyData.get("own_goals")).intValue());
        playerHistory.setPenaltiesSaved(((Number) historyData.get("penalties_saved")).intValue());
        playerHistory.setPenaltiesMissed(((Number) historyData.get("penalties_missed")).intValue());
        playerHistory.setStarts(((Number) historyData.getOrDefault("starts", 0)).intValue());
        playerHistory.setExpectedGoals(
                String.valueOf(historyData.getOrDefault("expected_goals", "0.00")));
        playerHistory.setExpectedAssists(
                String.valueOf(historyData.getOrDefault("expected_assists", "0.00")));
        playerHistory.setExpectedGoalInvolvements(
                String.valueOf(historyData.getOrDefault("expected_goal_involvements", "0.00")));
        playerHistory.setGoalsConceded(((Number) historyData.getOrDefault("goals_conceded", 0)).intValue());
        playerHistory.setExpectedGoalsConceded(
                String.valueOf(historyData.getOrDefault("expected_goals_conceded", "0.00")));
        playerHistory.setClearancesBlocksInterceptions(
                ((Number) historyData.getOrDefault("clearances_blocks_interceptions", 0)).intValue());
        playerHistory.setRecoveries(((Number) historyData.getOrDefault("recoveries", 0)).intValue());
        playerHistory.setTackles(((Number) historyData.getOrDefault("tackles", 0)).intValue());
        playerHistory.setDefensiveContribution(
                ((Number) historyData.getOrDefault("defensive_contribution", 0)).intValue());
        playerHistory.setInfluence(String.valueOf(historyData.getOrDefault("influence", "0.0")));
        playerHistory.setCreativity(String.valueOf(historyData.getOrDefault("creativity", "0.0")));
        playerHistory.setThreat(String.valueOf(historyData.getOrDefault("threat", "0.0")));
        playerHistory.setIctIndex(String.valueOf(historyData.getOrDefault("ict_index", "0.0")));
        playerHistory.setWasHome((Boolean) historyData.getOrDefault("was_home", false));
        playerHistory.setValue(((Number) historyData.getOrDefault("value", 0)).intValue());
        playerHistory.setTransfersIn(((Number) historyData.getOrDefault("transfers_in", 0)).intValue());
        playerHistory.setTransfersOut(((Number) historyData.getOrDefault("transfers_out", 0)).intValue());
        playerHistory.setTransfersBalance(((Number) historyData.getOrDefault("transfers_balance", 0)).intValue());
        playerHistory.setSelected(((Number) historyData.getOrDefault("selected", 0)).intValue());
    }
}
