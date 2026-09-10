import { Component, Input } from '@angular/core';
import { GameWeekPerformance } from '../../../core/models/game-week-performance.model';

@Component({
  selector: 'app-season-heatmap',
  templateUrl: './season-heatmap.component.html',
  styleUrls: ['./season-heatmap.component.scss']
})
export class SeasonHeatmapComponent {
  @Input() performances: GameWeekPerformance[] = [];
  @Input() totalGWs: number = 10;

  get squares(): { gw: number; points: number; color: string; tooltip: string; benched: boolean; inTeam: boolean }[] {
    const perfMap = new Map(this.performances.map(p => [p.gameWeek, p]));
    return Array.from({ length: this.totalGWs }, (_, i) => {
      const gw = i + 1;
      const perf = perfMap.get(gw);
      const inTeam = perf?.wasInMyTeam ?? false;
      const points = inTeam ? (perf!.points ?? 0) : 0;
      return {
        gw,
        points,
        inTeam,
        color: inTeam ? this.pointsToColor(points) : 'var(--color-heatmap-none)',
        tooltip: inTeam
          ? `GW${gw}: ${points} pts${perf?.wasBenched ? ' (benched)' : ''}`
          : `GW${gw}: not in squad`,
        benched: perf?.wasBenched ?? false
      };
    });
  }

  get row1() { return this.squares.slice(0, 19); }
  get row2() { return this.squares.slice(19); }

  shouldShowLabel(gw: number): boolean {
    const mid = Math.ceil(this.totalGWs / 2);
    return gw === 1 || gw === mid || gw === this.totalGWs;
  }

  private pointsToColor(points: number): string {
    if (points <= 3)  return 'var(--color-heatmap-bad)';
    if (points <= 7)  return 'var(--color-heatmap-ok)';
    if (points <= 11) return 'var(--color-heatmap-good)';
    return 'var(--color-heatmap-great)';
  }
}
