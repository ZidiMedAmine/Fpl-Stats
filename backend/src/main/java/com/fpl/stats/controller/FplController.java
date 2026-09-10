package com.fpl.stats.controller;

import com.fpl.stats.services.PlayerService;
import com.fpl.stats.services.UserInfoService;
import com.fpl.stats.services.dto.CompareDto;
import com.fpl.stats.services.dto.PlayerDetailDto;
import com.fpl.stats.services.dto.PlayerSummaryDto;
import com.fpl.stats.services.dto.UserTeamDto;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * REST controller for FPL data queries.
 *
 * <p>Exposes read-only endpoints for user team info, player listings, and team comparisons.
 * All data is served from the local database — no FPL API calls are made on the read path.</p>
 */
@RestController
@RequestMapping(FplController.BASE_PATH)
public class FplController {

    /** Base path for all FPL query endpoints. */
    static final String BASE_PATH = "/api/fpl";

    private static final Set<String> VALID_POSITIONS = Set.of("GKP", "DEF", "MID", "FWD");

    /** Cache-Control for user-specific responses (private, 5 minutes). */
    private static final CacheControl USER_CACHE_CONTROL =
            CacheControl.maxAge(5, TimeUnit.MINUTES).cachePrivate();

    /** Cache-Control for shared player data (public, 10 minutes). */
    private static final CacheControl PLAYER_CACHE_CONTROL =
            CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic();

    private final UserInfoService userInfoService;
    private final PlayerService playerService;

    /**
     * Constructs a {@code FplController} with its required service dependencies.
     *
     * @param userInfoService service for user team information
     * @param playerService   service for player data
     */
    public FplController(UserInfoService userInfoService, PlayerService playerService) {
        this.userInfoService = userInfoService;
        this.playerService = playerService;
    }

    /**
     * Returns the full team information for the given FPL team ID.
     *
     * @param fplTeamId the FPL team identifier
     * @return 200 with the team DTO, or 404 if the team is not found
     */
    @GetMapping("/user-info/{fplTeamId}")
    public ResponseEntity<UserTeamDto> getUserTeamInfo(@PathVariable long fplTeamId) {
        UserTeamDto teamInfo = userInfoService.getUserTeamInfo(fplTeamId);
        return ResponseEntity.ok().cacheControl(USER_CACHE_CONTROL).body(teamInfo);
    }

    /**
     * Compares two FPL teams side by side.
     *
     * @param fplTeamId1 the FPL ID of the first team
     * @param fplTeamId2 the FPL ID of the second team
     * @return 200 with the comparison DTO, or 404 if either team is not found
     */
    @GetMapping("/compare")
    public ResponseEntity<CompareDto> compareTeams(@RequestParam long fplTeamId1,
                                                   @RequestParam long fplTeamId2) {
        CompareDto comparison = userInfoService.compareTeams(fplTeamId1, fplTeamId2);
        return ResponseEntity.ok().cacheControl(USER_CACHE_CONTROL).body(comparison);
    }

    /**
     * Returns all players for a given position.
     *
     * @param position the FPL position code — one of {@code GKP}, {@code DEF}, {@code MID}, {@code FWD}
     * @return 200 with the list of matching players
     * @throws ResponseStatusException 400 if the position code is not recognised
     */
    @GetMapping("/players")
    public ResponseEntity<List<PlayerSummaryDto>> getPlayersByPosition(@RequestParam String position) {
        if (!VALID_POSITIONS.contains(position)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid position '" + position + "'. Must be one of: " + VALID_POSITIONS);
        }
        List<PlayerSummaryDto> players = playerService.getPlayersByPosition(position);
        return ResponseEntity.ok().cacheControl(PLAYER_CACHE_CONTROL).body(players);
    }

    /**
     * Returns the detailed profile for a single player.
     *
     * @param fplId the player's FPL element ID
     * @return 200 with the player detail DTO, or 404 if the player is not found
     */
    @GetMapping("/players/{fplId}")
    public ResponseEntity<PlayerDetailDto> getPlayerDetail(@PathVariable int fplId) {
        PlayerDetailDto playerDetail = playerService.getPlayerDetail(fplId);
        return ResponseEntity.ok().cacheControl(PLAYER_CACHE_CONTROL).body(playerDetail);
    }
}
