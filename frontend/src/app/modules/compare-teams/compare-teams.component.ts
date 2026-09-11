import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, Inject, Optional, inject } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { CHART_COLORS } from '../../core/chart-colors.constants';
import { forkJoin, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { TeamService } from '../../core/services/team.service';
import { CompareResult } from '../../core/models/compare.model';
import { UserInfo } from '../../core/models/UserInfo.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

interface H2H {
  wins1: number;
  wins2: number;
  draws: number;
}

interface ChipEvent {
  gameWeek: number;
  chip: string;
  playerName?: string;
  playerPoints?: number;
}

const CHIP_LABELS: Record<string, string> = {
  wc: 'Wildcard',
  fh: 'Free Hit',
  bboost: 'Bench Boost',
  '3xc': 'Triple Captain',
};

@Component({
  selector: 'app-compare-teams',
  templateUrl: './compare-teams.component.html',
  styleUrls: ['./compare-teams.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CompareTeamsComponent {
  teamId1 = '';
  teamId2 = '';
  isLoading = false;
  isModal = false;
  result?: CompareResult;
  gameWeeks: number[] = [];

  h2h: H2H = { wins1: 0, wins2: 0, draws: 0 };

  activeChart: 'totalPoints' | 'gw' | 'rank' | 'teamValue' | 'captain' = 'totalPoints';
  chipEvents1: ChipEvent[] = [];
  chipEvents2: ChipEvent[] = [];
  readonly chipLabels = CHIP_LABELS;

  // ── GW Points ─────────────────────────────────────────────────────────
  gwLineChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  gwLineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, labels: { usePointStyle: true }, onClick: () => undefined },
      tooltip: { mode: 'index', intersect: false },
      datalabels: { display: false }
    },
    scales: { y: { beginAtZero: false, grace: '10%', title: { display: true, text: 'Points' }, min: 40 } }
  };

  // ── Total Points ──────────────────────────────────────────────────────
  totalPointsChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  totalPointsOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, labels: { usePointStyle: true }, onClick: () => undefined },
      datalabels: { display: false },
      tooltip: { mode: 'index', intersect: false }
    },
    scales: { y: { beginAtZero: false, grace: '5%', title: { display: true, text: 'Total Points' } } }
  };

  // ── Rank History ──────────────────────────────────────────────────────
  rankChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  rankOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, labels: { usePointStyle: true }, onClick: () => undefined },
      datalabels: { display: false },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          label: (item) => ` ${item.dataset.label}: #${Number(item.raw).toLocaleString()}`
        }
      }
    },
    scales: {
      y: {
        reverse: true,
        ticks: { callback: (value) => `#${Number(value).toLocaleString()}` }
      }
    }
  };

  // ── Team Value ────────────────────────────────────────────────────────
  teamValueChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  teamValueOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, labels: { usePointStyle: true }, onClick: () => undefined },
      datalabels: { display: false },
      tooltip: {
        mode: 'index',
        intersect: false,
        callbacks: {
          label: (item) => ` ${item.dataset.label}: £${Number(item.raw).toFixed(1)}m`
        }
      }
    },
    scales: {
      y: { ticks: { callback: (value) => `£${Number(value).toFixed(1)}m` } }
    }
  };

  // ── Captain ───────────────────────────────────────────────────────────
  captainChart: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [] };
  captainOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, labels: { usePointStyle: true }, onClick: () => undefined },
      datalabels: {
        display: true,
        anchor: 'center',
        align: 'center',
        color: CHART_COLORS.ui.white,
        font: { size: 10, weight: 'bold' },
        formatter: (value: number) => value > 0 ? value : ''
      },
      tooltip: {
        callbacks: {
          label: (item) => ` ${item.dataset.label}: ${item.raw} pts`
        }
      }
    },
    scales: {
      x: { stacked: false },
      y: { beginAtZero: true, title: { display: true, text: 'Captain Points' } }
    }
  };

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly teamService: TeamService,
    private readonly snackBar: MatSnackBar,
    private readonly cdr: ChangeDetectorRef,
    @Optional() @Inject(MAT_DIALOG_DATA) private readonly dialogData?: { team1Id: number }
  ) {
    if (dialogData?.team1Id) {
      this.teamId1 = String(dialogData.team1Id);
      this.isModal = true;
    }
  }

  /** TrackBy function for gameweek number lists. */
  trackByGameWeek(_index: number, gameWeek: number): number { return gameWeek; }

  /** TrackBy function for player lists, keyed by player code. */
  trackByPlayerCode(_index: number, player: { code: number }): number { return player.code; }

  /** TrackBy function for chip event lists, keyed by gameweek. */
  trackByChipGameWeek(_index: number, chipEvent: ChipEvent): number { return chipEvent.gameWeek; }

  /**
   * Syncs both teams from the FPL API, then fetches the comparison result
   * and all team player data used to build the charts.
   */
  compare(): void {
    const id1 = Number(this.teamId1);
    const id2 = Number(this.teamId2);
    if (!id1 || !id2) {
      this.snackBar.open('Please enter two valid FPL team IDs.', 'Dismiss', { duration: 3000 });
      return;
    }
    if (id1 === id2) {
      this.snackBar.open('Please enter two different team IDs.', 'Dismiss', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    this.result = undefined;
    forkJoin([
      this.teamService.syncUserTeam(id1),
      this.teamService.syncUserTeam(id2)
    ]).pipe(
      switchMap(() => forkJoin([
        this.teamService.compareTeams(id1, id2),
        this.teamService.getTeamPlayers(id1),
        this.teamService.getTeamPlayers(id2)
      ])),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ([compareResult, user1, user2]) => {
        this.onCompareLoaded(compareResult, user1, user2);
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
        this.snackBar.open('Failed to compare teams. Please check both team IDs and try again.', 'Dismiss', {
          duration: 5000,
          panelClass: 'error-snackbar',
        });
      },
    });
  }

  /**
   * Populates all result state and rebuilds all charts after a successful comparison load.
   *
   * @param compareResult - The comparison data returned by the API.
   * @param user1 - First team's full user info.
   * @param user2 - Second team's full user info.
   */
  private onCompareLoaded(compareResult: CompareResult, user1: UserInfo, user2: UserInfo): void {
    this.result = compareResult;
    this.gameWeeks = Object.keys(compareResult.pointsByGameWeek)
      .map(Number)
      .sort((a, b) => a - b);
    this.h2h = this.computeH2H();
    this.buildTotalPointsChart(user1, user2);
    this.buildGWLineChart();
    this.buildRankChart(user1, user2);
    this.buildTeamValueChart(user1, user2);
    this.buildCaptainChart(user1, user2);
    this.chipEvents1 = this.extractChipEvents(user1);
    this.chipEvents2 = this.extractChipEvents(user2);
    this.isLoading = false;
    this.cdr.markForCheck();
  }

  /**
   * Returns the total points difference between team 1 and team 2.
   *
   * @returns Positive if team 1 is ahead, negative if team 2 is ahead.
   */
  getTotalDiff(): number {
    if (!this.result) return 0;
    return (this.result.team1.totalPoints ?? 0) - (this.result.team2.totalPoints ?? 0);
  }

  /**
   * Returns the points difference for a specific gameweek.
   *
   * @param gameWeek - The gameweek number to query.
   * @returns Positive if team 1 won the gameweek, negative if team 2 won.
   */
  getPointsDiff(gameWeek: number): number {
    if (!this.result) return 0;
    const points = this.result.pointsByGameWeek[gameWeek];
    return points[0] - points[1];
  }

  /**
   * Returns the last 5 gameweeks played.
   */
  lastFiveGWs(): number[] {
    return this.gameWeeks.slice(-5);
  }

  /**
   * Returns the head-to-head result for a specific gameweek from team 1's perspective.
   *
   * @param gameWeek - The gameweek number to query.
   * @returns `'W'` if team 1 won, `'L'` if team 1 lost, `'D'` for a draw.
   */
  gwResult(gameWeek: number): 'W' | 'L' | 'D' {
    const diff = this.getPointsDiff(gameWeek);
    if (diff > 0) return 'W';
    if (diff < 0) return 'L';
    return 'D';
  }

  /**
   * Computes the head-to-head win/loss/draw record across all gameweeks.
   *
   * @returns An {@link H2H} object with counts for team 1 wins, team 2 wins, and draws.
   */
  private computeH2H(): H2H {
    const record: H2H = { wins1: 0, wins2: 0, draws: 0 };
    if (!this.result) return record;
    for (const gameWeek of this.gameWeeks) {
      const diff = this.getPointsDiff(gameWeek);
      if (diff > 0) record.wins1++;
      else if (diff < 0) record.wins2++;
      else record.draws++;
    }
    return record;
  }

  /**
   * Extracts chip usage events from a user's rank history, enriching triple captain
   * events with the captain's name and multiplied points.
   *
   * @param user - The user whose rank history to scan for chip events.
   * @returns A sorted array of {@link ChipEvent} objects.
   */
  private extractChipEvents(user: UserInfo): ChipEvent[] {
    return (user.rankHistory ?? [])
      .filter(rank => rank.chipUsed)
      .map(rank => {
        const event: ChipEvent = { gameWeek: rank.gameWeek, chip: rank.chipUsed! };
        if (rank.chipUsed === '3xc') {
          for (const player of user.players) {
            const performance = player.performances.find(perf => perf.gameWeek === rank.gameWeek && perf.wasTripleCaptain);
            if (performance) {
              event.playerName = player.name;
              event.playerPoints = performance.points * (performance.multiplier ?? 3);
              break;
            }
          }
        }
        return event;
      })
      .sort((a, b) => a.gameWeek - b.gameWeek);
  }

  /**
   * Builds the cumulative total points line chart for both teams
   * using their rank history.
   *
   * @param user1 - First team's user info.
   * @param user2 - Second team's user info.
   */
  private buildTotalPointsChart(user1: UserInfo, user2: UserInfo): void {
    const history1 = (user1.rankHistory ?? []).sort((a, b) => a.gameWeek - b.gameWeek);
    const history2 = (user2.rankHistory ?? []).sort((a, b) => a.gameWeek - b.gameWeek);
    const labels = history1.map(rank => `GW${rank.gameWeek}`);
    this.totalPointsChart = {
      labels,
      datasets: [
        {
          label: user1.teamName,
          data: history1.map(rank => rank.totalPoints),
          borderColor: CHART_COLORS.primary,
          backgroundColor: CHART_COLORS.primaryAlpha10,
          pointBackgroundColor: CHART_COLORS.primary,
          tension: 0.3,
          fill: true,
          pointRadius: 5,
        },
        {
          label: user2.teamName,
          data: history2.map(rank => rank.totalPoints),
          borderColor: CHART_COLORS.team2,
          backgroundColor: CHART_COLORS.team2Alpha10,
          pointBackgroundColor: CHART_COLORS.team2,
          borderDash: [6, 4],
          tension: 0.3,
          fill: true,
          pointRadius: 5,
        }
      ]
    };
  }

  /**
   * Builds the per-gameweek points line chart for both teams
   * using the comparison result's `pointsByGameWeek` map.
   */
  private buildGWLineChart(): void {
    if (!this.result) return;
    this.gwLineChart = {
      labels: this.gameWeeks.map(gw => `GW${gw}`),
      datasets: [
        {
          label: this.result.team1.teamName,
          data: this.gameWeeks.map(gw => this.result!.pointsByGameWeek[gw][0]),
          borderColor: CHART_COLORS.primary,
          backgroundColor: CHART_COLORS.primaryAlpha10,
          pointBackgroundColor: CHART_COLORS.primary,
          tension: 0.3,
          fill: true,
          pointRadius: 5,
        },
        {
          label: this.result.team2.teamName,
          data: this.gameWeeks.map(gw => this.result!.pointsByGameWeek[gw][1]),
          borderColor: CHART_COLORS.team2,
          backgroundColor: CHART_COLORS.team2Alpha10,
          pointBackgroundColor: CHART_COLORS.team2,
          borderDash: [6, 4],
          tension: 0.3,
          pointRadius: 5,
          fill: true,
        }
      ]
    };
  }

  /**
   * Builds the overall rank history line chart for both teams
   * using their rank history records.
   *
   * @param user1 - First team's user info.
   * @param user2 - Second team's user info.
   */
  private buildRankChart(user1: UserInfo, user2: UserInfo): void {
    const history1 = (user1.rankHistory ?? []).sort((a, b) => a.gameWeek - b.gameWeek);
    const history2 = (user2.rankHistory ?? []).sort((a, b) => a.gameWeek - b.gameWeek);
    const labels = history1.map(rank => `GW${rank.gameWeek}`);
    this.rankChart = {
      labels,
      datasets: [
        {
          label: user1.teamName,
          data: history1.map(rank => rank.overallRank),
          borderColor: CHART_COLORS.primary,
          backgroundColor: CHART_COLORS.primaryAlpha10,
          pointBackgroundColor: CHART_COLORS.primary,
          tension: 0.3,
          fill: 'start',
          pointRadius: 5,
        },
        {
          label: user2.teamName,
          data: history2.map(rank => rank.overallRank),
          borderColor: CHART_COLORS.team2,
          backgroundColor: CHART_COLORS.team2Alpha10,
          pointBackgroundColor: CHART_COLORS.team2,
          borderDash: [6, 4],
          tension: 0.3,
          fill: 'start',
          pointRadius: 5,
        }
      ]
    };
  }

  /**
   * Builds the team value history line chart for both teams,
   * excluding gameweeks where team value data is unavailable.
   *
   * @param user1 - First team's user info.
   * @param user2 - Second team's user info.
   */
  private buildTeamValueChart(user1: UserInfo, user2: UserInfo): void {
    const history1 = (user1.rankHistory ?? []).filter(rank => rank.teamValue != null).sort((a, b) => a.gameWeek - b.gameWeek);
    const history2 = (user2.rankHistory ?? []).filter(rank => rank.teamValue != null).sort((a, b) => a.gameWeek - b.gameWeek);
    const labels = history1.map(rank => `GW${rank.gameWeek}`);
    this.teamValueChart = {
      labels,
      datasets: [
        {
          label: user1.teamName,
          data: history1.map(rank => rank.teamValue),
          borderColor: CHART_COLORS.primary,
          backgroundColor: CHART_COLORS.primaryAlpha10,
          pointBackgroundColor: CHART_COLORS.primary,
          tension: 0.3,
          fill: true,
          pointRadius: 5,
        },
        {
          label: user2.teamName,
          data: history2.map(rank => rank.teamValue),
          borderColor: CHART_COLORS.team2,
          backgroundColor: CHART_COLORS.team2Alpha10,
          pointBackgroundColor: CHART_COLORS.team2,
          borderDash: [6, 4],
          tension: 0.3,
          fill: true,
          pointRadius: 5,
        }
      ]
    };
  }

  /**
   * Builds the captain points bar chart for both teams,
   * computing each captain's effective points per gameweek (including TC multiplier).
   *
   * @param user1 - First team's user info.
   * @param user2 - Second team's user info.
   */
  private buildCaptainChart(user1: UserInfo, user2: UserInfo): void {
    const cap1 = this.getCaptainPointsPerGW(user1);
    const cap2 = this.getCaptainPointsPerGW(user2);
    const gameWeeks = Array.from(new Set([...cap1.keys(), ...cap2.keys()])).sort((a, b) => a - b);

    this.captainChart = {
      labels: gameWeeks.map(gw => `GW${gw}`),
      datasets: [
        {
          label: user1.teamName,
          data: gameWeeks.map(gw => cap1.get(gw) ?? 0),
          backgroundColor: CHART_COLORS.primaryAlpha70,
          borderColor: CHART_COLORS.primary,
          borderWidth: 1,
          borderRadius: 3,
        },
        {
          label: user2.teamName,
          data: gameWeeks.map(gw => cap2.get(gw) ?? 0),
          backgroundColor: CHART_COLORS.team2Alpha70,
          borderColor: CHART_COLORS.team2,
          borderWidth: 1,
          borderRadius: 3,
        }
      ]
    };
  }

  /**
   * Builds a map of gameweek → effective captain points for a user,
   * accounting for the triple captain multiplier.
   *
   * @param user - The user whose captain picks to aggregate.
   * @returns A map of gameweek number to the captain's effective points that week.
   */
  private getCaptainPointsPerGW(user: UserInfo): Map<number, number> {
    const map = new Map<number, number>();
    for (const player of user.players) {
      for (const performance of player.performances) {
        if (performance.wasCaptain || performance.wasTripleCaptain) {
          map.set(
            performance.gameWeek,
            performance.points * (performance.multiplier ?? (performance.wasTripleCaptain ? 3 : 2))
          );
        }
      }
    }
    return map;
  }
}
