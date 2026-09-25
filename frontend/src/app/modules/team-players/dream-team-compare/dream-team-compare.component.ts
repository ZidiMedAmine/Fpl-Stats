import { ChangeDetectionStrategy, Component, Input, OnChanges } from '@angular/core';
import { DreamTeam, DreamTeamPlayer, DreamTeamResult } from '../../../core/models/dream-team.model';
import { Player, Position } from '../../../core/models/player.model';

/** A player slot in the comparison table with resolved context. */
interface ComparisonPlayer {
  fplId: number;
  webName: string;
  teamName: string;
  nowCost: number;
  /** Points to display — changes per window (allTime / last5Weeks). */
  points: number;
  /** Points used for XI selection — all-time totals for the allTime window, last-5-GW sum for the last5Weeks window. */
  selectionPoints: number;
  code: number;
}

/** One row in a position section: left = user player, right = dream player. */
interface ComparisonRow {
  userPlayer: ComparisonPlayer | null;
  dreamPlayer: ComparisonPlayer | null;
  isShared: boolean;
}

/** A grouped position section (GKP / DEF / MID / FWD) with its comparison rows. */
interface PositionSection {
  position: string;
  label: string;
  rows: ComparisonRow[];
  userTotal: number;
  dreamTotal: number;
  /** Difference between user position total and dream position total. */
  positionDiff: number;
  userCount: number;
  dreamCount: number;
}

/** The result of running the formation optimizer over a player pool. */
interface UserXiResult {
  formation: string;
  totalPoints: number;
  playersByPosition: Map<string, ComparisonPlayer[]>;
}

/** Formation slot counts: [GKP, DEF, MID, FWD]. */
interface Formation {
  key: string;
  gkp: number;
  def: number;
  mid: number;
  fwd: number;
}

const FORMATIONS: Formation[] = [
  { key: '3-4-3', gkp: 1, def: 3, mid: 4, fwd: 3 },
  { key: '3-5-2', gkp: 1, def: 3, mid: 5, fwd: 2 },
  { key: '4-3-3', gkp: 1, def: 4, mid: 3, fwd: 3 },
  { key: '4-4-2', gkp: 1, def: 4, mid: 4, fwd: 2 },
  { key: '4-5-1', gkp: 1, def: 4, mid: 5, fwd: 1 },
  { key: '5-3-2', gkp: 1, def: 5, mid: 3, fwd: 2 },
  { key: '5-4-1', gkp: 1, def: 5, mid: 4, fwd: 1 },
];

const POSITION_ORDER = [Position.GKP, Position.DEF, Position.MID, Position.FWD];

const POSITION_LABELS: Record<string, string> = {
  [Position.GKP]: 'GOALKEEPERS',
  [Position.DEF]: 'DEFENDERS',
  [Position.MID]: 'MIDFIELDERS',
  [Position.FWD]: 'FORWARDS',
};

@Component({
  selector: 'app-dream-team-compare',
  templateUrl: './dream-team-compare.component.html',
  styleUrls: ['./dream-team-compare.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DreamTeamCompareComponent implements OnChanges {

  /** The dream team data containing allTime and last5Weeks results. */
  @Input() dreamTeam!: DreamTeam;

  /** All players the user has had in their squad this season. */
  @Input() userPlayers: Player[] = [];

  /** The current gameweek number, used to resolve the last-5-GW window. */
  @Input() currentGameWeek = 0;

  activeWindow: 'allTime' | 'last5Weeks' = 'allTime';

  activeResult!: DreamTeamResult;
  userXiResult!: UserXiResult;
  positionSections: PositionSection[] = [];
  overlap = 0;
  gap = 0;
  gapPercent = 0;

  /** @returns true when the last5Weeks window is selected. */
  get isLast5Weeks(): boolean {
    return this.activeWindow === 'last5Weeks';
  }

  /**
   * Rebuilds the comparison whenever inputs change.
   */
  ngOnChanges(): void {
    if (!this.dreamTeam) return;
    this.rebuildComparison();
  }

  /**
   * Switches the active window and rebuilds the comparison.
   *
   * @param selectedWindow - The window to activate: 'allTime' or 'last5Weeks'.
   */
  selectWindow(selectedWindow: 'allTime' | 'last5Weeks'): void {
    this.activeWindow = selectedWindow;
    this.rebuildComparison();
  }

  /** TrackBy for position sections, keyed by position string. */
  trackBySectionPosition(_index: number, section: PositionSection): string {
    return section.position;
  }

  /** TrackBy for comparison rows, keyed by row index. */
  trackByRowIndex(index: number): number {
    return index;
  }

  /**
   * Resolves the active dream team result and user XI, then builds all comparison data.
   */
  private rebuildComparison(): void {
    this.activeResult = this.activeWindow === 'allTime'
      ? this.dreamTeam.allTime
      : this.dreamTeam.last5Weeks;

    const candidates = this.buildCandidates();
    this.userXiResult = this.selectBestFormation(candidates);
    this.positionSections = this.buildPositionSections();
    this.overlap = this.computeOverlap();
    this.gap = this.userXiResult.totalPoints - this.activeResult.totalPoints;
    this.gapPercent = this.activeResult.totalPoints > 0
      ? Math.round((this.userXiResult.totalPoints / this.activeResult.totalPoints) * 100)
      : 0;
  }

  /**
   * Builds a map of position → sorted candidate list from ALL players that have ever
   * played for the user (wasInMyTeam=true at least once), excluding managers.
   * Candidates are sorted by window-appropriate points: all-time for allTime, last-5-GW for last5Weeks.
   *
   * @returns Map of position string to sorted candidate players.
   */
  private buildCandidates(): Map<string, ComparisonPlayer[]> {
    const map = new Map<string, ComparisonPlayer[]>();
    const eligiblePlayers = this.userPlayers.filter(player =>
      player.position !== Position.Manager &&
      player.performances.some(perf => perf.wasInMyTeam)
    );

    for (const player of eligiblePlayers) {
      const displayPoints = this.activeWindow === 'allTime'
        ? this.computeAllTimePointsForTeam(player)
        : this.computeLast5GwPoints(player);

      const candidate: ComparisonPlayer = {
        fplId: player.fplId,
        webName: player.name,
        teamName: player.teamName,
        nowCost: player.nowCost,
        points: displayPoints,
        selectionPoints: displayPoints,
        code: player.code,
      };

      const bucket = map.get(player.position) ?? [];
      bucket.push(candidate);
      map.set(player.position, bucket);
    }

    for (const bucket of map.values()) {
      bucket.sort((a, b) => b.selectionPoints - a.selectionPoints);
    }
    return map;
  }

  /**
   * Sums a player's points across all gameweeks they were in the user's team.
   *
   * @param player - The user squad player.
   * @returns Total points earned while playing for the user's team.
   */
  private computeAllTimePointsForTeam(player: Player): number {
    return player.performances
      .filter(perf => perf.wasInMyTeam)
      .reduce((sum, perf) => sum + perf.points, 0);
  }

  /**
   * Sums a player's raw points (excluding captaincy multiplier) across the last 5 finished gameweeks.
   *
   * @param player - The user squad player.
   * @returns Total points across last 5 GWs.
   */
  private computeLast5GwPoints(player: Player): number {
    const windowStart = Math.max(1, this.currentGameWeek - 5);
    return player.performances
      .filter(perf => perf.gameWeek >= windowStart && perf.gameWeek <= this.currentGameWeek && perf.wasInMyTeam)
      .reduce((sum, perf) => sum + perf.points, 0);
  }

  /**
   * Runs the formation optimizer over the candidate pool.
   * Formation selection uses all-time (selectionPoints) so the XI is window-independent.
   * The returned totalPoints reflects the active window's display points.
   *
   * @param candidatesByPosition - Map of position to sorted candidate list.
   * @returns The best formation result, or a fallback if no formation can be filled.
   */
  private selectBestFormation(candidatesByPosition: Map<string, ComparisonPlayer[]>): UserXiResult {
    let bestFormation: Formation = FORMATIONS[0];
    let bestSelectionTotal = -1;

    for (const formation of FORMATIONS) {
      const gkp = this.topN(candidatesByPosition.get(Position.GKP) ?? [], formation.gkp);
      const def = this.topN(candidatesByPosition.get(Position.DEF) ?? [], formation.def);
      const mid = this.topN(candidatesByPosition.get(Position.MID) ?? [], formation.mid);
      const fwd = this.topN(candidatesByPosition.get(Position.FWD) ?? [], formation.fwd);

      if (gkp.length < formation.gkp || def.length < formation.def
        || mid.length < formation.mid || fwd.length < formation.fwd) {
        continue;
      }

      const selectionTotal = this.sumSelectionPoints(gkp) + this.sumSelectionPoints(def)
        + this.sumSelectionPoints(mid) + this.sumSelectionPoints(fwd);
      if (selectionTotal > bestSelectionTotal) {
        bestSelectionTotal = selectionTotal;
        bestFormation = formation;
      }
    }

    const gkp = this.topN(candidatesByPosition.get(Position.GKP) ?? [], bestFormation.gkp);
    const def = this.topN(candidatesByPosition.get(Position.DEF) ?? [], bestFormation.def);
    const mid = this.topN(candidatesByPosition.get(Position.MID) ?? [], bestFormation.mid);
    const fwd = this.topN(candidatesByPosition.get(Position.FWD) ?? [], bestFormation.fwd);

    const playersByPosition = new Map<string, ComparisonPlayer[]>([
      [Position.GKP, gkp],
      [Position.DEF, def],
      [Position.MID, mid],
      [Position.FWD, fwd],
    ]);

    const displayTotal = this.sumPoints(gkp) + this.sumPoints(def) + this.sumPoints(mid) + this.sumPoints(fwd);

    return { formation: bestFormation.key, totalPoints: displayTotal, playersByPosition };
  }

  /**
   * Builds the position sections for the comparison table by pairing user XI players
   * with dream XI players. Shared players appear first, then the remaining are paired by index.
   *
   * @returns Array of position sections in the order GKP, DEF, MID, FWD.
   */
  private buildPositionSections(): PositionSection[] {
    const dreamPlayersByPosition = this.groupDreamPlayersByPosition();

    return POSITION_ORDER.map(position => {
      const userPlayers = this.userXiResult.playersByPosition.get(position) ?? [];
      const dreamPlayers = dreamPlayersByPosition.get(position) ?? [];
      const rows = this.buildRows(userPlayers, dreamPlayers);

      const userTotal = this.sumPoints(userPlayers);
      const dreamTotal = this.sumPoints(dreamPlayers);
      return {
        position,
        label: POSITION_LABELS[position],
        rows,
        userTotal,
        dreamTotal,
        positionDiff: userTotal - dreamTotal,
        userCount: userPlayers.length,
        dreamCount: dreamPlayers.length,
      };
    });
  }

  /**
   * Groups the active dream team players by position.
   *
   * @returns Map of position string to dream team player list.
   */
  private groupDreamPlayersByPosition(): Map<string, DreamTeamPlayer[]> {
    const map = new Map<string, DreamTeamPlayer[]>();
    for (const player of this.activeResult.players) {
      const bucket = map.get(player.position) ?? [];
      bucket.push(player);
      map.set(player.position, bucket);
    }
    return map;
  }

  /**
   * Pairs user and dream players for a single position.
   * Shared players (in both) appear first, then the non-shared remainder is paired by index.
   *
   * @param userPlayers  - User XI players for this position.
   * @param dreamPlayers - Dream XI players for this position.
   * @returns Ordered list of comparison rows.
   */
  private buildRows(userPlayers: ComparisonPlayer[], dreamPlayers: DreamTeamPlayer[]): ComparisonRow[] {
    const sharedFplIds = new Set(
      userPlayers
        .filter(user => dreamPlayers.some(dream => dream.fplId === user.fplId))
        .map(user => user.fplId)
    );

    const sharedRows = this.buildSharedRows(userPlayers, dreamPlayers, sharedFplIds);
    const remainingUser = userPlayers.filter(p => !sharedFplIds.has(p.fplId));
    const remainingDream = dreamPlayers.filter(p => !sharedFplIds.has(p.fplId));
    const nonSharedRows = this.buildNonSharedRows(remainingUser, remainingDream);

    return [...sharedRows, ...nonSharedRows];
  }

  /**
   * Builds one row per shared player (same player on both sides).
   *
   * @param userPlayers  - User XI players for this position.
   * @param dreamPlayers - Dream XI players for this position.
   * @param sharedFplIds - Set of FPL IDs present in both lineups.
   * @returns Shared rows.
   */
  private buildSharedRows(
    userPlayers: ComparisonPlayer[],
    dreamPlayers: DreamTeamPlayer[],
    sharedFplIds: Set<number>
  ): ComparisonRow[] {
    return userPlayers
      .filter(user => sharedFplIds.has(user.fplId))
      .map(user => {
        const dream = dreamPlayers.find(d => d.fplId === user.fplId)!;
        return {
          userPlayer: user,
          dreamPlayer: this.toDreamComparison(dream),
          isShared: true,
        };
      });
  }

  /**
   * Pairs non-shared user players with non-shared dream players by index.
   * If one list is longer, the excess rows have null on the shorter side.
   *
   * @param remainingUser  - User players not in the dream XI for this position.
   * @param remainingDream - Dream players not in the user XI for this position.
   * @returns Non-shared rows.
   */
  private buildNonSharedRows(
    remainingUser: ComparisonPlayer[],
    remainingDream: DreamTeamPlayer[]
  ): ComparisonRow[] {
    const count = Math.max(remainingUser.length, remainingDream.length);
    const rows: ComparisonRow[] = [];

    for (let index = 0; index < count; index++) {
      const userPlayer = remainingUser[index] ?? null;
      const dreamPlayerRaw = remainingDream[index] ?? null;
      const dreamPlayer = dreamPlayerRaw ? this.toDreamComparison(dreamPlayerRaw) : null;

      rows.push({ userPlayer, dreamPlayer, isShared: false });
    }
    return rows;
  }

  /**
   * Converts a {@link DreamTeamPlayer} to the shared {@link ComparisonPlayer} interface.
   *
   * @param dreamTeamPlayer - The dream team player to convert.
   * @returns A comparison-compatible player object.
   */
  private toDreamComparison(dreamTeamPlayer: DreamTeamPlayer): ComparisonPlayer {
    return {
      fplId: dreamTeamPlayer.fplId,
      webName: dreamTeamPlayer.webName,
      teamName: dreamTeamPlayer.teamName,
      nowCost: dreamTeamPlayer.nowCost,
      points: dreamTeamPlayer.points,
      selectionPoints: dreamTeamPlayer.points,
      code: dreamTeamPlayer.code,
    };
  }

  /**
   * Counts how many of the user's XI players also appear in the dream XI.
   *
   * @returns Number of shared players (0–11).
   */
  private computeOverlap(): number {
    const dreamFplIds = new Set(this.activeResult.players.map(p => p.fplId));
    let count = 0;
    for (const players of this.userXiResult.playersByPosition.values()) {
      count += players.filter(p => dreamFplIds.has(p.fplId)).length;
    }
    return count;
  }

  /**
   * Returns the first {@code n} items from the array.
   *
   * @param items - Source array.
   * @param count - Maximum number of items to return.
   * @returns Sub-array of length at most {@code count}.
   */
  private topN<T>(items: T[], count: number): T[] {
    return items.slice(0, count);
  }

  /**
   * Sums the display points of all players in the list.
   *
   * @param players - The list to sum.
   * @returns Total display points.
   */
  private sumPoints(players: { points: number }[]): number {
    return players.reduce((sum, player) => sum + player.points, 0);
  }

  /**
   * Sums the all-time selection points of all players in the list.
   * Used only by the formation optimizer so XI selection is window-independent.
   *
   * @param players - The list to sum.
   * @returns Total selection points.
   */
  private sumSelectionPoints(players: ComparisonPlayer[]): number {
    return players.reduce((sum, player) => sum + player.selectionPoints, 0);
  }
}
