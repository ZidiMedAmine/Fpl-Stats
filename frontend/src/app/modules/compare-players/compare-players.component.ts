import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, HostListener, Inject, OnInit, inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ChartData, ChartOptions } from 'chart.js';
import { CHART_COLORS } from '../../core/chart-colors.constants';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PlayerComparisonService } from '../../core/services/player-comparison.service';
import { PlayerDetail } from '../../core/models/player-detail.model';
import { PlayerSummary } from '../../core/models/player-summary.model';
import { GameWeekPerformance } from '../../core/models/game-week-performance.model';
import { Player } from '../../core/models/player.model';

export interface StatDef {
  key: string;
  label: string;
  category: string;
  getValue: (player: PlayerDetail) => number;
}

@Component({
  selector: 'app-compare-players',
  templateUrl: './compare-players.component.html',
  styleUrls: ['./compare-players.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ComparePlayersComponent implements OnInit {
  playerA: PlayerDetail | null = null;
  playerB: PlayerDetail | null = null;
  searchResults: PlayerSummary[] = [];
  searchQuery = '';
  loading = false;
  searchLoading = false;

  showStatDropdown = false;
  statSearchQuery = '';
  selectedStats: StatDef[] = [];

  readonly allStats: StatDef[] = [
    // Overview
    { key: 'totalPoints',    label: 'Total Points',    category: 'Overview',
      getValue: (player) => player.totalPoints },
    { key: 'avgPoints',      label: 'Avg pts/GW',      category: 'Overview',
      getValue: (player) => Math.round(player.avgPoints * 10) / 10 },
    { key: 'ptsPerMillion',  label: 'Pts per £m',      category: 'Overview',
      getValue: (player) => player.nowCost > 0 ? Math.round(player.totalPoints / player.nowCost * 10) / 10 : 0 },
    { key: 'ownershipPct',   label: 'Ownership %',     category: 'Overview',
      getValue: (player) => player.selectedByPercent },

    // Playing Time
    { key: 'minutes', label: 'Minutes', category: 'Playing Time',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.minutesPlayed, 0) },
    { key: 'starts',  label: 'Starts',  category: 'Playing Time',
      getValue: (player) => player.performances.filter(gw => gw.minutesPlayed > 0).length },

    // Attacking
    { key: 'goals',   label: 'Goals',   category: 'Attacking',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.goalsScored, 0) },
    { key: 'assists', label: 'Assists', category: 'Attacking',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.assists, 0) },
    { key: 'xg',      label: 'xG',      category: 'Attacking',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) => sum + parseFloat(gw.expectedGoals || '0'), 0) * 100) / 100 },
    { key: 'xa',      label: 'xA',      category: 'Attacking',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) => sum + parseFloat(gw.expectedAssists || '0'), 0) * 100) / 100 },
    { key: 'xgi',     label: 'xGI',     category: 'Attacking',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) =>
        sum + parseFloat(gw.expectedGoals || '0') + parseFloat(gw.expectedAssists || '0'), 0) * 100) / 100 },
    { key: 'goalsPer90',   label: 'Goals/90',   category: 'Attacking',
      getValue: (player) => {
        const minutes = player.performances.reduce((sum, gw) => sum + gw.minutesPlayed, 0);
        const goals   = player.performances.reduce((sum, gw) => sum + gw.goalsScored, 0);
        return minutes > 0 ? Math.round(goals / minutes * 90 * 100) / 100 : 0;
      }},
    { key: 'assistsPer90', label: 'Assists/90', category: 'Attacking',
      getValue: (player) => {
        const minutes  = player.performances.reduce((sum, gw) => sum + gw.minutesPlayed, 0);
        const assists  = player.performances.reduce((sum, gw) => sum + gw.assists, 0);
        return minutes > 0 ? Math.round(assists / minutes * 90 * 100) / 100 : 0;
      }},
    { key: 'xgPer90',  label: 'xG/90',  category: 'Attacking',
      getValue: (player) => {
        const minutes = player.performances.reduce((sum, gw) => sum + gw.minutesPlayed, 0);
        const xg      = player.performances.reduce((sum, gw) => sum + parseFloat(gw.expectedGoals || '0'), 0);
        return minutes > 0 ? Math.round(xg / minutes * 90 * 100) / 100 : 0;
      }},
    { key: 'xaPer90',  label: 'xA/90',  category: 'Attacking',
      getValue: (player) => {
        const minutes = player.performances.reduce((sum, gw) => sum + gw.minutesPlayed, 0);
        const xa      = player.performances.reduce((sum, gw) => sum + parseFloat(gw.expectedAssists || '0'), 0);
        return minutes > 0 ? Math.round(xa / minutes * 90 * 100) / 100 : 0;
      }},
    { key: 'xgiPer90', label: 'xGI/90', category: 'Attacking',
      getValue: (player) => {
        const minutes = player.performances.reduce((sum, gw) => sum + gw.minutesPlayed, 0);
        const xgi     = player.performances.reduce((sum, gw) =>
          sum + parseFloat(gw.expectedGoals || '0') + parseFloat(gw.expectedAssists || '0'), 0);
        return minutes > 0 ? Math.round(xgi / minutes * 90 * 100) / 100 : 0;
      }},

    // Defensive
    { key: 'cleanSheets', label: 'Clean Sheets', category: 'Defensive',
      getValue: (player) => player.performances.filter(gw => gw.cleanSheet).length },
    { key: 'saves',       label: 'Saves',        category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.saves, 0) },
    { key: 'goalsConceded', label: 'Goals Conceded', category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + (gw.goalsConceded ?? 0), 0) },
    { key: 'xgc', label: 'xGC', category: 'Defensive',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) => sum + parseFloat(gw.expectedGoalsConceded || '0'), 0) * 100) / 100 },
    { key: 'defensiveContribution', label: 'Defensive Contributions', category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + (gw.defensiveContribution ?? 0), 0) },
    { key: 'tackles',     label: 'Tackles',      category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + (gw.tackles ?? 0), 0) },
    { key: 'recoveries',  label: 'Recoveries',   category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + (gw.recoveries ?? 0), 0) },
    { key: 'cbi', label: 'Clearances/Blocks/Int', category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + (gw.clearancesBlocksInterceptions ?? 0), 0) },
    { key: 'yellowCards', label: 'Yellow Cards', category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.yellowCards, 0) },
    { key: 'redCards',    label: 'Red Cards',    category: 'Defensive',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.redCards, 0) },

    // ICT Index
    { key: 'influence',  label: 'Influence',  category: 'ICT',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) => sum + parseFloat(gw.influence || '0'), 0) * 10) / 10 },
    { key: 'creativity', label: 'Creativity', category: 'ICT',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) => sum + parseFloat(gw.creativity || '0'), 0) * 10) / 10 },
    { key: 'threat',     label: 'Threat',     category: 'ICT',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) => sum + parseFloat(gw.threat || '0'), 0) * 10) / 10 },
    { key: 'ictIndex',   label: 'ICT Index',  category: 'ICT',
      getValue: (player) => Math.round(player.performances.reduce((sum, gw) => sum + parseFloat(gw.ictIndex || '0'), 0) * 10) / 10 },

    // FPL
    { key: 'bonusPoints',    label: 'Bonus Points',  category: 'FPL',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.bonusPoints, 0) },
    { key: 'bps',            label: 'BPS',            category: 'FPL',
      getValue: (player) => player.performances.reduce((sum, gw) => sum + gw.bps, 0) },
    { key: 'haulGWs',        label: 'Haul GWs',       category: 'FPL',
      getValue: (player) => player.performances.filter(gw => gw.points >= 10).length },
    { key: 'blankGWs',       label: 'Blank GWs',      category: 'FPL',
      getValue: (player) => player.performances.filter(gw => gw.points <= 1).length },
    { key: 'consistencyPct', label: 'Consistency %',  category: 'FPL',
      getValue: (player) => player.performances.length
        ? Math.round(player.performances.filter(gw => gw.points >= 4).length / player.performances.length * 100)
        : 0 },
    { key: 'bestGW',     label: 'Best GW',        category: 'FPL',
      getValue: (player) => player.performances.length ? Math.max(...player.performances.map(gw => gw.points)) : 0 },
    { key: 'formLast5',  label: 'Form (last 5)',  category: 'FPL',
      getValue: (player) => player.performances.slice(-5).reduce((sum, gw) => sum + gw.points, 0) },
  ];

  lineChartData: ChartData<'line'> = { labels: [], datasets: [] };
  radarChartData: ChartData<'radar'> = { labels: [], datasets: [] };
  radarRawA: number[] = [];
  radarRawB: number[] = [];

  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: { usePointStyle: true, pointStyle: 'circle', boxWidth: 6, boxHeight: 6 }
      },
      tooltip: { mode: 'index', intersect: false },
      datalabels: { display: false }
    },
    scales: { y: { beginAtZero: true, title: { display: true, text: 'Points' } } }
  };

  radarChartOptions: ChartOptions<'radar'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: { usePointStyle: true, pointStyle: 'circle', boxWidth: 6, boxHeight: 6 }
      },
      datalabels: { display: false },
      tooltip: {
        mode: 'nearest',
        intersect: false,
        callbacks: {
          label: (item) => {
            const raw = item.datasetIndex === 0 ? this.radarRawA : this.radarRawB;
            const value = raw[item.dataIndex] ?? item.raw;
            const rounded = typeof value === 'number' ? +value.toFixed(2) : value;
            return ` ${item.dataset.label}: ${rounded}`;
          }
        }
      }
    },
    scales: {
      r: {
        beginAtZero: true,
        ticks: { display: false },
        grid: { color: CHART_COLORS.ui.gridLineMd },
        pointLabels: { font: { size: 11 } }
      }
    }
  };

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { player: Player },
    private readonly playerComparisonService: PlayerComparisonService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  /** TrackBy function for the player search results list, keyed by FPL ID. */
  trackByPlayerFplId(_index: number, player: PlayerSummary): number { return player.fplId; }

  /** TrackBy function for the selected stats chips, keyed by stat key. */
  trackByStatKey(_index: number, stat: StatDef): string { return stat.key; }

  /** TrackBy function for stat category groups, keyed by category name. */
  trackByGroupCategory(_index: number, group: { category: string }): string { return group.category; }

  /**
   * Initializes the component by loading player A's detail data
   * and setting the default selected stats for the radar chart.
   */
  ngOnInit(): void {
    const fplId = this.data.player.fplId;
    this.loading = true;
    this.playerComparisonService.getPlayerDetail(fplId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: player => {
        this.playerA = player;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
    const defaultKeys = ['totalPoints', 'goals', 'assists', 'xg', 'xa', 'xgi', 'formLast5', 'consistencyPct'];
    this.selectedStats = defaultKeys.map(key => this.allStats.find(stat => stat.key === key)!);
  }

  /**
   * Searches for players in the same position as player A that match the current search query.
   * Normalizes the query to handle accented and special characters.
   */
  onSearch(): void {
    if (!this.playerA || this.searchQuery.trim().length < 2) return;
    this.searchLoading = true;
    this.playerComparisonService.getPlayersByPosition(this.playerA.position).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: players => {
        const normalize = (input: string) => input
          .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
          .replace(/[øØ]/g, 'o').replace(/[æÆ]/g, 'ae')
          .replace(/[þÞ]/g, 'th').replace(/[ðÐ]/g, 'd')
          .replace(/[łŁ]/g, 'l').replace(/[ß]/g, 'ss')
          .toLowerCase();
        const query = normalize(this.searchQuery);
        this.searchResults = players.filter(player =>
          normalize(player.webName).includes(query) && player.fplId !== this.playerA!.fplId
        );
        this.searchLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.searchLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * Selects a player as player B, fetches their detail data, and rebuilds all charts.
   *
   * @param summary - The player summary to load as player B.
   */
  selectPlayerB(summary: PlayerSummary): void {
    this.searchResults = [];
    this.searchQuery = summary.webName;
    this.loading = true;
    this.playerComparisonService.getPlayerDetail(summary.fplId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: player => {
        this.playerB = player;
        this.loading = false;
        this.buildCharts();
        this.cdr.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  // ── Computed stats ────────────────────────────────────────────────

  /**
   * Returns the number of gameweeks where the player scored 10 or more points.
   *
   * @param performances - The player's gameweek performance records.
   */
  haulGWs(performances: GameWeekPerformance[]): number {
    return performances.filter(performance => performance.points >= 10).length;
  }

  /**
   * Returns the number of gameweeks where the player scored 1 or fewer points.
   *
   * @param performances - The player's gameweek performance records.
   */
  blankGWs(performances: GameWeekPerformance[]): number {
    return performances.filter(performance => performance.points <= 1).length;
  }

  /**
   * Returns the percentage of gameweeks where the player scored 4 or more points.
   *
   * @param performances - The player's gameweek performance records.
   * @returns Consistency percentage rounded to the nearest integer, or 0 if no data.
   */
  consistency(performances: GameWeekPerformance[]): number {
    if (!performances.length) return 0;
    return Math.round(performances.filter(performance => performance.points >= 4).length / performances.length * 100);
  }

  /**
   * Returns the total points scored in the last 5 gameweeks.
   *
   * @param performances - The player's gameweek performance records.
   */
  form(performances: GameWeekPerformance[]): number {
    return performances.slice(-5).reduce((sum, performance) => sum + performance.points, 0);
  }

  /**
   * Returns the highest points scored in a single gameweek.
   *
   * @param performances - The player's gameweek performance records.
   * @returns The max points, or 0 if no data.
   */
  bestGW(performances: GameWeekPerformance[]): number {
    return performances.length ? Math.max(...performances.map(performance => performance.points)) : 0;
  }

  /**
   * Returns the player's total points per £1m of their current cost.
   *
   * @param player - The player detail to calculate value for.
   * @returns Points per million rounded to 1 decimal place, or 0 if cost is zero.
   */
  ptsPerMillion(player: PlayerDetail): number {
    return player.nowCost > 0 ? Math.round(player.totalPoints / player.nowCost * 10) / 10 : 0;
  }

  /**
   * Returns the player's current cost formatted as a price string.
   *
   * @param player - The player detail to format the price for.
   * @returns A formatted string like "£7.5m".
   */
  price(player: PlayerDetail): string {
    return `£${player.nowCost.toFixed(1)}m`;
  }

  /**
   * Returns the total goals scored across all gameweeks.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalGoals(performances: GameWeekPerformance[]): number {
    return performances.reduce((sum, performance) => sum + performance.goalsScored, 0);
  }

  /**
   * Returns the total assists across all gameweeks.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalAssists(performances: GameWeekPerformance[]): number {
    return performances.reduce((sum, performance) => sum + performance.assists, 0);
  }

  /**
   * Returns the total bonus points accumulated across all gameweeks.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalBonus(performances: GameWeekPerformance[]): number {
    return performances.reduce((sum, performance) => sum + performance.bonusPoints, 0);
  }

  /**
   * Returns the total minutes played across all gameweeks.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalMinutes(performances: GameWeekPerformance[]): number {
    return performances.reduce((sum, performance) => sum + performance.minutesPlayed, 0);
  }

  /**
   * Returns the total expected goals (xG) rounded to 2 decimal places.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalXg(performances: GameWeekPerformance[]): number {
    return Math.round(performances.reduce((sum, performance) => sum + parseFloat(performance.expectedGoals || '0'), 0) * 100) / 100;
  }

  /**
   * Returns the total expected assists (xA) rounded to 2 decimal places.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalXa(performances: GameWeekPerformance[]): number {
    return Math.round(performances.reduce((sum, performance) => sum + parseFloat(performance.expectedAssists || '0'), 0) * 100) / 100;
  }

  /**
   * Returns true if both player A and player B are defenders or midfielders.
   */
  isDefOrMid(): boolean {
    return (this.playerA?.position === 'DEF' || this.playerA?.position === 'MID') &&
           (this.playerB?.position === 'DEF' || this.playerB?.position === 'MID');
  }

  /**
   * Returns the total defensive contribution score across all gameweeks.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalDefensiveContribution(performances: GameWeekPerformance[]): number {
    return performances.reduce((sum, performance) => sum + (performance.defensiveContribution ?? 0), 0);
  }

  /**
   * Returns the number of gameweeks where the player kept a clean sheet
   * and played at least 60 minutes.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalCleanSheets(performances: GameWeekPerformance[]): number {
    return performances.filter(performance => performance.cleanSheet && performance.minutesPlayed >= 60).length;
  }

  /**
   * Returns the total number of goals conceded across all gameweeks.
   *
   * @param performances - The player's gameweek performance records.
   */
  totalGoalsConceded(performances: GameWeekPerformance[]): number {
    return performances.reduce((sum, performance) => sum + (performance.goalsConceded ?? 0), 0);
  }

  /**
   * Determines which player wins a stat comparison.
   *
   * @param valueA - Stat value for player A.
   * @param valueB - Stat value for player B.
   * @param lowerIsBetter - When true, the lower value wins (e.g. goals conceded).
   * @returns `'a'`, `'b'`, or `'draw'`.
   */
  wins(valueA: number, valueB: number, lowerIsBetter = false): 'a' | 'b' | 'draw' {
    if (valueA === valueB) return 'draw';
    return lowerIsBetter ? (valueA < valueB ? 'a' : 'b') : (valueA > valueB ? 'a' : 'b');
  }

  /**
   * Returns true if player A has more total points than player B,
   * or if player B has not been selected yet.
   */
  playerAWins(): boolean {
    if (!this.playerA || !this.playerB) return true;
    return this.playerA.totalPoints > this.playerB.totalPoints;
  }

  /**
   * Returns true if both players have the same total points.
   */
  isEvenPoints(): boolean {
    if (!this.playerA || !this.playerB) return false;
    return this.playerA.totalPoints === this.playerB.totalPoints;
  }

  /**
   * Returns a verdict string describing the points gap between the two players.
   *
   * @returns An empty string if either player is not loaded.
   */
  verdictPoints(): string {
    if (!this.playerA || !this.playerB) return '';
    const diff = Math.abs(this.playerA.totalPoints - this.playerB.totalPoints);
    if (diff === 0) return 'Both players are level on points.';
    const better = this.playerA.totalPoints > this.playerB.totalPoints ? this.playerA.webName : this.playerB.webName;
    return `${better} leads by ${diff} pts this season.`;
  }

  /**
   * Returns a verdict string describing which player has better value per million.
   *
   * @returns An empty string if either player is not loaded.
   */
  verdictValue(): string {
    if (!this.playerA || !this.playerB) return '';
    const valueA = this.ptsPerMillion(this.playerA);
    const valueB = this.ptsPerMillion(this.playerB);
    const diff = Math.abs(valueA - valueB).toFixed(1);
    if (Number(diff) === 0) return 'Both players have equal value.';
    const winner = valueA > valueB ? this.playerA.webName : this.playerB.webName;
    return `${winner} has better value at ${diff} more pts/£m.`;
  }

  // ── Stat selector ─────────────────────────────────────────────────

  /**
   * Returns true if the given stat is currently selected for the radar chart.
   *
   * @param stat - The stat definition to check.
   */
  isSelected(stat: StatDef): boolean {
    return this.selectedStats.some(selected => selected.key === stat.key);
  }

  /**
   * Adds a stat to the radar chart selection if it is not already selected
   * and the selection has fewer than 10 stats.
   *
   * @param stat - The stat definition to add.
   */
  addStat(stat: StatDef): void {
    if (this.isSelected(stat) || this.selectedStats.length >= 10) return;
    this.selectedStats = [...this.selectedStats, stat];
    this.rebuildRadarIfReady();
  }

  /**
   * Removes a stat from the radar chart selection, enforcing a minimum of 3 selected stats.
   *
   * @param stat - The stat definition to remove.
   */
  removeStat(stat: StatDef): void {
    if (this.selectedStats.length <= 3) return;
    this.selectedStats = this.selectedStats.filter(selected => selected.key !== stat.key);
    this.rebuildRadarIfReady();
  }

  /**
   * Returns all stats filtered by the current stat search query.
   *
   * @returns All stats if the query is empty, otherwise those whose label matches the query.
   */
  filteredStats(): StatDef[] {
    const query = this.statSearchQuery.toLowerCase().trim();
    if (!query) return this.allStats;
    return this.allStats.filter(stat => stat.label.toLowerCase().includes(query));
  }

  /**
   * Groups the filtered stats by their category for grouped display in the dropdown.
   *
   * @returns An array of category objects, each containing a label and its matching stats.
   */
  statsByCategory(): { category: string; stats: StatDef[] }[] {
    const filtered = this.filteredStats();
    const map = new Map<string, StatDef[]>();
    for (const stat of filtered) {
      if (!map.has(stat.category)) map.set(stat.category, []);
      map.get(stat.category)!.push(stat);
    }
    return Array.from(map.entries()).map(([category, stats]) => ({ category, stats }));
  }

  /**
   * Toggles the stat selector dropdown visibility, stopping event propagation.
   *
   * @param event - The originating DOM event.
   */
  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.showStatDropdown = !this.showStatDropdown;
  }

  /** Closes the stat selector dropdown when the user clicks anywhere outside it. */
  @HostListener('document:click')
  onDocumentClick(): void {
    this.showStatDropdown = false;
  }

  /** Closes the stat selector dropdown when the Escape key is pressed. */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.showStatDropdown = false;
  }

  /**
   * Rebuilds the radar chart if both player A and player B are loaded.
   */
  private rebuildRadarIfReady(): void {
    if (this.playerA && this.playerB) this.buildRadarChart();
  }

  // ── Charts ────────────────────────────────────────────────────────

  /**
   * Builds both the GW-by-GW line chart and the stat radar chart
   * from the loaded player data.
   */
  private buildCharts(): void {
    if (!this.playerA || !this.playerB) return;
    this.buildLineChart();
    this.buildRadarChart();
  }

  /**
   * Builds the GW-by-GW points line chart, aligning both players
   * on a shared gameweek axis and filling gaps with zero.
   */
  private buildLineChart(): void {
    const allGameWeeks = new Set([
      ...this.playerA!.performances.map(performance => performance.gameWeek),
      ...this.playerB!.performances.map(performance => performance.gameWeek)
    ]);
    const sortedGameWeeks = Array.from(allGameWeeks).sort((a, b) => a - b);
    const mapA = new Map(this.playerA!.performances.map(performance => [performance.gameWeek, performance.points]));
    const mapB = new Map(this.playerB!.performances.map(performance => [performance.gameWeek, performance.points]));

    this.lineChartData = {
      labels: sortedGameWeeks.map(gw => `GW${gw}`),
      datasets: [
        {
          label: this.playerA!.webName,
          data: sortedGameWeeks.map(gw => mapA.get(gw) ?? 0),
          borderColor: CHART_COLORS.primary,
          backgroundColor: CHART_COLORS.primaryAlpha10,
          pointBackgroundColor: CHART_COLORS.primary,
          tension: 0.3,
          fill: false
        },
        {
          label: this.playerB!.webName,
          data: sortedGameWeeks.map(gw => mapB.get(gw) ?? 0),
          borderColor: CHART_COLORS.playerB,
          backgroundColor: CHART_COLORS.playerBAlpha10,
          pointBackgroundColor: CHART_COLORS.playerB,
          tension: 0.3,
          borderDash: [5, 5],
          fill: false
        }
      ]
    };
  }

  /**
   * Builds the radar chart for the currently selected stats,
   * normalizing each stat value as a percentage of the higher player's value
   * so all axes share the same 0–100 scale.
   */
  private buildRadarChart(): void {
    const playerA = this.playerA!;
    const playerB = this.playerB!;

    const rawA = this.selectedStats.map(stat => stat.getValue(playerA));
    const rawB = this.selectedStats.map(stat => stat.getValue(playerB));

    const normA = rawA.map((valueA, index) => {
      const max = Math.max(valueA, rawB[index]);
      return max === 0 ? 0 : Math.round(valueA / max * 100);
    });
    const normB = rawB.map((valueB, index) => {
      const max = Math.max(rawA[index], valueB);
      return max === 0 ? 0 : Math.round(valueB / max * 100);
    });

    this.radarRawA = rawA;
    this.radarRawB = rawB;

    this.radarChartData = {
      labels: this.selectedStats.map(stat => stat.label),
      datasets: [
        {
          label: playerA.webName,
          data: normA,
          borderColor: CHART_COLORS.gold,
          backgroundColor: CHART_COLORS.goldAlpha20,
          pointBackgroundColor: CHART_COLORS.gold,
          pointRadius: 5,
          pointHoverRadius: 7,
        },
        {
          label: playerB.webName,
          data: normB,
          borderColor: CHART_COLORS.teal,
          backgroundColor: CHART_COLORS.tealAlpha20,
          pointBackgroundColor: CHART_COLORS.teal,
          pointRadius: 5,
          pointHoverRadius: 7,
        }
      ]
    };
  }
}
