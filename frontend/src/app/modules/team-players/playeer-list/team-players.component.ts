import {Component, OnInit} from '@angular/core';
import {Player} from "../../../core/models/player.model";
import {TeamService} from "../../../core/services/team.service";
import {ActivatedRoute} from "@angular/router";
import {GameWeekPerformance} from "../../../core/models/game-week-performance.model";
import {UserInfo} from "../../../core/models/UserInfo.model";
import {PlayerDetailsComponent} from "../player-details/player-details.component";
import {MatDialog} from '@angular/material/dialog';
import {SharedService} from "../../../shared/shared.service";

@Component({
  selector: 'app-team-players',
  templateUrl: './team-players.component.html',
  styleUrls: ['./team-players.component.scss'],
})
export class TeamPlayersComponent implements OnInit {
  user?: UserInfo;
  activeChart = '';
  isLoading = true;
  players: Player[] = [];
  displayedColumns: string[] = ['photo', 'name', 'position', 'avgPoints', 'timesSelected', 'timesCaptained', 'timesViceCaptained', 'benchPoints', 'timesOnBench', 'playedPoints', 'totalPointsForTeam', 'details'];

  constructor(protected sharedService: SharedService,
              protected teamService: TeamService,
              protected route: ActivatedRoute,
              protected dialog: MatDialog) { }

  public ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const teamId = params.get('id');
      console.log("loading user",  teamId);
      if (teamId) {
        this.loadTeamPlayers(+teamId);
      }
    });
  }

  /**
   * Returns a background color style object based on a player's average points.
   *
   * - Gray (`#adadad`) if no points or zero.
   * - Green (`#b5e6a2`) if average points > 4.9.
   * - Yellow (`#ffc000`) if average points > 4 but ≤ 4.9.
   * - Red (`#ff0000`) if average points ≤ 4.
   *
   * @param avgPoints - The average points scored by the player.
   * @returns An object representing the CSS `backgroundColor` style.
   */
  public getNameColorStyle(avgPoints: number): Record<string, string> {
    if (!avgPoints || avgPoints === 0) {
      return { backgroundColor: '#adadad' };
    }

    if (avgPoints > 4.9) {
      return { backgroundColor: '#b5e6a2' };
    } else if (avgPoints > 4) {
      return { backgroundColor: '#ffc000' };
    } else {
      return { backgroundColor: '#ff0000' };
    }
  }

  /**
   * Loads the players for a given team by team ID.
   *
   * Fetches the team data from the `teamService`, sets the local user and players properties,
   * calculates captaincy stats, sorts the players by position, calculates playtime,
   * and enriches player statistics. Handles loading state and logs any errors.
   *
   * @param teamId - The unique identifier of the team whose players should be loaded.
   */
  public loadTeamPlayers(teamId: number): void {
    this.isLoading = true;
    this.sharedService.displayPageLoader();
    this.teamService.getTeamPlayers(teamId).subscribe({
      next: (user) => {
        this.user = user;
        this.players = user.players;
        this.updateNavBarDetails(this.user);
        this.players = this.calculateCaptaincyStats(user.players);
        this.sortPlayersByPosition();
        this.calculatedPlayedTime();
        this.enrichPlayerStats();
        this.isLoading = false;
        this.sharedService.removeLoader();
      },
      error: (err) => {
        console.error('Error loading team players', err);
        this.isLoading = false;
      }
    });
  }

  /**
   * Opens a dialog displaying detailed information about a player.
   *
   * @param player - The player object containing data to be passed to the dialog.
   */
  public openPlayerDetails(player: Player): void {
    this.dialog.open(PlayerDetailsComponent, {
      width: '90%',
      maxWidth: '800px',
      height: '90vh',
      data: { player },
      panelClass: 'custom-dialog-container',
      autoFocus: false,
      disableClose: false
    });
  }

  /**
   * Sets the currently active chart type to be displayed.
   *
   * @param chartType - A string representing the type of chart to be shown (e.g., 'bar', 'line', 'pie').
   */
  public showChart(chartType: string) {
    this.activeChart = chartType;
  }

  /**
   * Sorts the `players` array in-place based on their positions,
   * following a predefined order: Manager, Goalkeeper (GK), Defender (DEF), Midfielder (MID), Forward (FWD).
   *
   * Players with positions not listed in the order will be placed at the end.
   */
  private sortPlayersByPosition(): void {
    const positionOrder = ["Manager", "GK", "DEF", "MID", "FWD"];
    this.players.sort((a: Player, b: Player) => {
      return positionOrder.indexOf(a.position) - positionOrder.indexOf(b.position);
    });
  }

  /**
   * Calculates how many times each player was selected in the starting team
   * (i.e., in the team and not benched), and updates the `players` array
   * with this `timesSelected` value.
   *
   * @remarks
   * - This does **not** include times the player was on the bench.
   * - The result is stored in a new `timesSelected` property on each player.
   */
  private calculatedPlayedTime(): void {
    this.players = this.players.map(player => {
      const timesSelected = player.performances.filter(
        p => p.wasInMyTeam && !p.wasBenched
      ).length;

      return { ...player, timesSelected };
    });
  }

  /**
   * Calculates and adds captaincy statistics to each player.
   *
   * For each player, it computes:
   * - `timesCaptained`: Number of times the player was captain.
   * - `timesViceCaptained`: Number of times the player was vice-captain.
   *
   * @param players - An array of `Player` objects to enrich with captaincy stats.
   * @returns A new array of `Player` objects including the added captaincy statistics.
   */
  private calculateCaptaincyStats(players: Player[]): Player[] {
    return players.map(player => {
      return {
        ...player,
        timesCaptained: this.countTimesCaptained(player.performances),
        timesViceCaptained: this.countTimesViceCaptained(player.performances)
      };
    });
  }

  /**
   * Counts the number of times a player was designated as captain
   * based on their gameWeek performances.
   *
   * @param performances - An array of gameWeek performance records.
   * @returns The number of times the player was captain.
   */
  private countTimesCaptained(performances: GameWeekPerformance[]): number {
    return performances.filter(perf => perf.wasCaptain).length;
  }

  /**
   * Counts the number of times a player was designated as vice-captain
   * based on their gameWeek performances.
   *
   * @param performances - An array of gameWeek performance records.
   * @returns The number of times the player was vice-captain.
   */
  private countTimesViceCaptained(performances: GameWeekPerformance[]): number {
    return performances.filter(perf => perf.wasViceCaptain).length;
  }

  /**
   * Enriches each player's statistics by calculating:
   *
   * - `timesSelected`: Number of times the player was selected in the team (not benched).
   * - `timesOnBench`: Number of times the player was selected but benched.
   * - `playedPoints`: Total points accumulated while the player was in the active team.
   * - `benchPoints`: Total points accumulated while the player was benched.
   *
   * Special handling is applied for players with the `'Manager'` position:
   * - For managers, `timesSelected` and `playedPoints` are calculated using benched performances instead.
   *
   * Updates the `players` array with the enriched data.
   */
  private enrichPlayerStats(): void {
    this.players = this.players.map(player => {
      const performances = player.performances;

      const isManager = player.position === 'Manager';

      const selectedPerformances = performances.filter(p =>
        isManager
          ? p.wasInMyTeam && p.wasBenched
          : p.wasInMyTeam && !p.wasBenched
      );

      const benchPerformances = performances.filter(
        p => p.wasInMyTeam && p.wasBenched
      );

      const timesSelected = selectedPerformances.length;
      const timesOnBench = benchPerformances.length;

      const playedPoints = selectedPerformances.reduce(
        (sum, p) => sum + p.points, 0
      );

      const benchPoints = benchPerformances.reduce(
        (sum, p) => sum + p.points, 0
      );

      return {
        ...player,
        timesSelected,
        timesOnBench,
        benchPoints,
        playedPoints
      };
    });
  }

  private updateNavBarDetails(user: UserInfo):  void {
    this.sharedService.updateUserName(user.name);
    this.sharedService.updateTeamName(user.teamName);
    this.sharedService.updateTotalPoints(user.totalPoints);
    this.sharedService.updateOverallRank(user.overallRank);
  }
}
