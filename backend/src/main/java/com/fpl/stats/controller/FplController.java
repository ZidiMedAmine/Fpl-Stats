package com.fpl.stats.controller;

import com.fpl.stats.domain.UserInfo;
import com.fpl.stats.services.UserInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fpl")
@CrossOrigin(origins = "http://localhost:4200")
public class FplController {
    private final UserInfoService teamPlayersService;

    public FplController(UserInfoService teamPlayersService) {
        this.teamPlayersService = teamPlayersService;
    }

    /**
     * REST endpoint to retrieve user and team information for a given team ID.
     * <p>
     * This includes details such as the manager's name, team name, region, rank, points,
     * and current game week.
     *
     * @param teamId the unique identifier of the Fantasy Premier League team
     * @return a {@link ResponseEntity} containing the {@link UserInfo} object with team details
     */
    @GetMapping("/team-players/{teamId}")
    public ResponseEntity<UserInfo> getTeamPlayers(@PathVariable int teamId) {
        UserInfo result = teamPlayersService.getUserInfo(teamId);
        return ResponseEntity.ok(result);
    }
}