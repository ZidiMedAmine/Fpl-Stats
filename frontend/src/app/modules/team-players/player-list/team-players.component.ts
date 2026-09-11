import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Player, Position } from '../../../core/models/player.model';
import { TeamService } from '../../../core/services/team.service';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { GameWeekPerformance } from '../../../core/models/game-week-performance.model';
import { UserInfo } from '../../../core/models/UserInfo.model';
import { PlayerDetailsComponent } from '../player-details/player-details.component';
import { ComparePlayersComponent } from '../../compare-players/compare-players.component';
import { CompareTeamsComponent } from '../../compare-teams/compare-teams.component';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SharedService } from '../../../shared/shared.service';
import { LoaderService } from '../../../core/loader.service';

@Component({
  selector: 'app-team-players',
  templateUrl: './team-players.component.html',
  styleUrls: ['./team-players.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TeamPlayersComponent implements OnInit, OnDestroy {
  user?: UserInfo;
  isLoading = true;
  players: Player[] = [];
  displayedColumns: string[] = ['photo', 'name', 'position', 'avgPoints', 'timesSelected', 'timesCaptained', 'timesViceCaptained', 'benchPoints', 'timesOnBench', 'playedPoints', 'totalPointsForTeam', 'compare'];

  readonly Position = Position;
  private destroy$ = new Subject<void>();

  constructor(
    private readonly sharedService: SharedService,
    private readonly teamService: TeamService,
    private readonly route: ActivatedRoute,
    private readonly snackBar: MatSnackBar,
    private readonly dialog: MatDialog,
    private readonly cdr: ChangeDetectorRef,
    private readonly loaderService: LoaderService
  ) {}

  /** TrackBy function for the player list, keyed by player code. */
  trackByPlayerCode(_index: number, player: Player): number { return player.code; }

  public ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const teamId = params.get('id');
      if (teamId) {
        this.loadTeamPlayers(+teamId);
      }
    });
  }

  public ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
      return { backgroundColor: 'var(--color-avg-none)' };
    }

    if (avgPoints > 4.9) {
      return { backgroundColor: 'var(--color-avg-good)' };
    } else if (avgPoints > 4) {
      return { backgroundColor: 'var(--color-avg-ok)' };
    } else {
      return { backgroundColor: 'var(--color-avg-bad)' };
    }
  }

  /**
   * Opens the player comparison dialog, pre-loading the given player as player A.
   *
   * @param player - The player to compare against others.
   */
  public openCompareDialog(player: Player): void {
    this.dialog.open(ComparePlayersComponent, {
      width: '95%',
      maxWidth: '900px',
      minHeight: '50vh',
      maxHeight: '90vh',
      data: { player },
      panelClass: 'custom-dialog-container',
      autoFocus: false,
    });
  }

  /**
   * Opens the player details dialog for the given player,
   * passing the current gameweek for missed-GW calculation.
   *
   * @param player - The player whose details to display.
   */
  public openPlayerDetails(player: Player): void {
    this.dialog.open(PlayerDetailsComponent, {
      width: '90%',
      maxWidth: '800px',
      maxHeight: '90vh',
      data: { player, currentGameWeek: this.user?.currentGameWeek ?? 0 },
      panelClass: 'custom-dialog-container',
      autoFocus: false,
      disableClose: false
    });
  }

  /**
   * Opens the compare teams dialog, pre-populating team 1 with the current user's FPL team ID.
   */
  public openCompareTeamsDialog(): void {
    this.dialog.open(CompareTeamsComponent, {
      width: '95%',
      maxWidth: '900px',
      maxHeight: '90vh',
      data: { team1Id: this.user?.fplTeamId },
      panelClass: 'custom-dialog-container',
      autoFocus: false,
    });
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
  private loadTeamPlayers(teamId: number): void {
    this.isLoading = true;
    this.loaderService.show();
    this.teamService.syncUserTeam(teamId).pipe(
      switchMap(() => this.teamService.getTeamPlayers(teamId))
    ).subscribe({
      next: (user) => {
        this.user = user;
        this.players = user.players;
        this.updateNavBarDetails(this.user);
        this.players = this.calculateCaptaincyStats(user.players);
        this.sortPlayersByPosition();
        this.enrichPlayerStats();
        this.isLoading = false;
        this.loaderService.hide();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isLoading = false;
        this.loaderService.hide();
        this.cdr.markForCheck();
        const message = err?.status === 0
          ? 'Could not reach the server. Please try again later.'
          : 'Failed to load team data. Please check the team ID and try again.';
        this.snackBar.open(message, 'Dismiss', {
          duration: 5000,
          panelClass: 'error-snackbar'
        });
      }
    });
  }

  /**
   * Sorts the `players` array in-place based on their positions,
   * following a predefined order: Manager, Goalkeeper (GK), Defender (DEF), Midfielder (MID), Forward (FWD).
   *
   * Players with positions not listed in the order will be placed at the end.
   */
  private sortPlayersByPosition(): void {
    const positionOrder: string[] = [Position.GKP, Position.DEF, Position.MID, Position.FWD];
    this.players.sort((a: Player, b: Player) => {
      return positionOrder.indexOf(a.position) - positionOrder.indexOf(b.position);
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

      const isManager = player.position === Position.Manager;

      const selectedPerformances = performances.filter(p =>
        isManager
          ? p.wasInMyTeam && p.wasBenched
          : p.wasInMyTeam && p.multiplier > 0
      );

      const benchPerformances = performances.filter(
        p => p.wasInMyTeam && p.wasBenched && p.multiplier === 0
      );

      const timesSelected = selectedPerformances.length;
      const timesOnBench = performances.filter(p => p.wasInMyTeam && p.wasBenched).length;

      const playedPoints = selectedPerformances.reduce(
        (sum, p) => sum + p.points * (p.multiplier ?? 1), 0
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

  /**
   * Updates the shared navigation bar with the loaded user's name, team name,
   * total points, and overall rank.
   *
   * @param user - The loaded user info to broadcast to the navbar.
   */
  private updateNavBarDetails(user: UserInfo): void {
    this.sharedService.updateUserName(user.name);
    this.sharedService.updateTeamName(user.teamName);
    this.sharedService.updateTotalPoints(user.totalPoints ?? 0);
    this.sharedService.updateOverallRank(user.overallRank ?? 0);
  }
}
