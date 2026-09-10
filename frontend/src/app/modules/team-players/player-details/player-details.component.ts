import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, Inject, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Player, Position } from '../../../core/models/player.model';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { GameWeekPerformance } from '../../../core/models/game-week-performance.model';
import { PlayerComparisonService } from '../../../core/services/player-comparison.service';

@Component({
  selector: 'app-player-details',
  templateUrl: './player-details.component.html',
  styleUrls: ['./player-details.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayerDetailsComponent implements OnInit {
  readonly Position = Position;
  displayedColumns: string[] = [];
  showMissedGWs = false;
  showAllGWs = false;
  loadingHistory = false;

  private globalHistoryMap = new Map<number, GameWeekPerformance>();
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { player: Player; currentGameWeek: number },
    private readonly playerComparisonService: PlayerComparisonService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  /** TrackBy function for index-based iterations with no unique identifier (e.g. card icons). */
  trackByIndex(index: number): number { return index; }

  ngOnInit(): void {
    // Always show: GW, Points, Bonus, Goals, Assists, (CS for non-FWD), Minutes, Cards
    // Captaincy only if player has ever been captain/vc/tc
    const hasCaptaincy = this.data.player.performances.some(p =>
      p.wasCaptain || p.wasViceCaptain || p.wasTripleCaptain
    );

    this.displayedColumns = ['gameWeek'];
    if (hasCaptaincy) this.displayedColumns.push('captaincy');
    this.displayedColumns.push('points', 'bonus');

    if (this.data.player.position !== Position.Manager) {
      this.displayedColumns.push('goals', 'assists');
      if (this.data.player.position !== Position.FWD) this.displayedColumns.push('cleanSheet');
      if (this.data.player.position === Position.GKP) this.displayedColumns.push('goalsConceded');
      if (this.data.player.position === Position.DEF || this.data.player.position === Position.MID) {
        this.displayedColumns.push('defensiveContribution');
      }
      this.displayedColumns.push('minutes');
    }
    this.displayedColumns.push('cards');

    if (this.getMissedGWCount() > 0) {
      this.fetchGlobalHistory();
    }
  }

  /**
   * Fetches the player's full season history from the comparison service
   * to populate missed gameweek data when the player was not in the user's team.
   */
  private fetchGlobalHistory(): void {
    this.loadingHistory = true;
    this.playerComparisonService.getPlayerDetail(this.data.player.fplId).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (detail) => {
        this.globalHistoryMap.clear();
        detail.performances.forEach(performance => this.globalHistoryMap.set(performance.gameWeek, performance));
        this.loadingHistory = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.loadingHistory = false;
        this.cdr.markForCheck();
      }
    });
  }

  /** Returns the player's initials (up to 2 characters) in uppercase. */
  get initials(): string {
    return this.data.player.name
      .split(' ')
      .map(w => w[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  /**
   * Hides the broken player image element and replaces its parent's text
   * content with the player's initials as a fallback.
   *
   * @param event - The image error event.
   */
  onImgError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
    // An <img> in the rendered DOM always has a parent element, so the non-null assertion is safe here.
    img.parentElement!.textContent = this.initials;
  }

  /**
   * Returns the dialog subtitle showing the player's team, price, and number of
   * gameweeks they were in the user's squad.
   */
  getSubtitle(): string {
    const price = this.data.player.nowCost.toFixed(1);
    const inTeamCount = this.getInTeamCount();
    return `${this.data.player.teamName} · £${price}m · in your team ${inTeamCount} of ${this.data.currentGameWeek} GWs`;
  }

  /** Returns only the performances where the player was in the user's team. */
  getFilteredPerformances(): GameWeekPerformance[] {
    return this.data.player.performances.filter(p => p.wasInMyTeam);
  }

  // KEY FIX: only GWs where wasInMyTeam=true count as "picked"
  private getInTeamGWs(): Set<number> {
    return new Set(this.data.player.performances.filter(p => p.wasInMyTeam).map(p => p.gameWeek));
  }

  /**
   * Returns the performances to show in the table, applying the current
   * `showMissedGWs` and `showAllGWs` filters. Defaults to the last 10 rows.
   */
  getDisplayedPerformances(): GameWeekPerformance[] {
    let perfs: GameWeekPerformance[];
    if (this.showMissedGWs) {
      perfs = [...this.getFilteredPerformances(), ...this.getMissedPerformances()];
    } else {
      perfs = this.getFilteredPerformances();
    }
    const sorted = perfs.slice().sort((a, b) => a.gameWeek - b.gameWeek);
    if (!this.showAllGWs && !this.showMissedGWs) {
      return sorted.slice(-10);
    }
    return sorted;
  }

  /**
   * Returns synthetic performance objects for gameweeks the player was not
   * in the user's team. Uses global history data when available, otherwise
   * returns a zeroed-out placeholder.
   */
  getMissedPerformances(): GameWeekPerformance[] {
    const inTeamGWs = this.getInTeamGWs();
    const missed: GameWeekPerformance[] = [];
    for (let gw = 1; gw <= this.data.currentGameWeek; gw++) {
      if (!inTeamGWs.has(gw)) {
        const global = this.globalHistoryMap.get(gw);
        missed.push(global
          ? { ...global, wasInMyTeam: false, wasBenched: false, wasCaptain: false, wasViceCaptain: false, wasTripleCaptain: false, multiplier: 1 }
          : {
              gameWeek: gw, wasInMyTeam: false, points: 0,
              wasBenched: false, wasCaptain: false, wasViceCaptain: false,
              wasTripleCaptain: false, bonusPoints: 0, bps: 0, saves: 0,
              expectedGoals: '0.00', expectedAssists: '0.00', minutesPlayed: 0,
              cleanSheet: false, goalsScored: 0, yellowCards: 0, redCards: 0,
              assists: 0, multiplier: 1,
              goalsConceded: 0, expectedGoalsConceded: '0.00',
              clearancesBlocksInterceptions: 0, recoveries: 0, tackles: 0, defensiveContribution: 0,
              influence: '0.0', creativity: '0.0', threat: '0.0', ictIndex: '0.0',
              wasHome: false, value: 0, transfersIn: 0, transfersOut: 0, transfersBalance: 0, selected: 0
            }
        );
      }
    }
    return missed;
  }

  /** Returns the total points the player scored in gameweeks they were not in the user's team. */
  getMissedPoints(): number {
    return this.getMissedPerformances().reduce((sum, p) => sum + p.points, 0);
  }

  /** Returns the number of gameweeks (up to the current GW) the player was not in the user's team. */
  getMissedGWCount(): number {
    const inTeamGWs = this.getInTeamGWs();
    let count = 0;
    for (let gw = 1; gw <= this.data.currentGameWeek; gw++) {
      if (!inTeamGWs.has(gw)) count++;
    }
    return count;
  }

  /** Returns the total points scored while the player was on the bench (multiplier = 0). */
  getBenchedPoints(): number {
    return this.getFilteredPerformances()
      .filter(p => p.wasBenched && p.multiplier === 0)
      .reduce((sum, p) => sum + p.points, 0);
  }

  /** Returns the total effective points contributed while the player was in the active squad (captain multiplier applied). */
  getSelectedGwPoints(): number {
    return this.getFilteredPerformances()
      .filter(p => p.wasInMyTeam && (this.data.player.position === Position.Manager ? p.wasBenched : p.multiplier > 0))
      .reduce((sum, p) => sum + this.calculatePoints(p), 0);
  }

  /**
   * Returns the effective points for a performance, applying the captain multiplier.
   *
   * @param perf - The gameweek performance to calculate points for.
   */
  calculatePoints(perf: GameWeekPerformance): number {
    return perf.points * (perf.multiplier ?? 1);
  }

  /** Returns the number of gameweeks the player appeared in the user's team. */
  getInTeamCount(): number {
    return this.getInTeamGWs().size;
  }
}
