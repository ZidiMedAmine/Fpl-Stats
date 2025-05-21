import {Component, Input, OnChanges} from '@angular/core';
import {ChartConfiguration} from 'chart.js';
import {UserInfo} from "../../../core/models/UserInfo.model";

@Component({
  selector: 'app-performance-charts',
  templateUrl: './performance-charts.component.html',
  styleUrls: ['./performance-charts.component.scss']
})
export class PerformanceChartsComponent implements OnChanges{
  @Input() user!: UserInfo;
  @Input() activeChart!: string;
  chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true
      }
    }
  };
  benchPointsChart: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Bench Points' }]
  };
  totalPointsChart: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Total Points' }]
  };
  captainPointsChart: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Captain Points' }]
  };

  ngOnChanges(): void {
    if (this.user) {
      this.prepareChartData();
    }
  }

  /**
   * Prepares chart data for visualization based on the user's players' performances across game weeks.
   *
   * - Generates labels for each game week in the format "GW1", "GW2", ..., up to the current game week.
   * - Calculates total points per game week, considering captain multipliers and manager position bonuses.
   * - Separately accumulates points earned as captain (with triple captain multiplier if applicable).
   * - Accumulates points earned while benched.
   *
   * The computed data is assigned to `totalPointsChart`, `captainPointsChart`, and `benchPointsChart`
   * properties, each containing labels and datasets compatible with charting libraries.
   */
  prepareChartData(): void {
    const gameWeeks = this.user.currentGameWeek;

    const labels = Array.from({ length: gameWeeks }, (_, i) => `GW${i + 1}`);
    const totalPoints = new Array(gameWeeks).fill(0);
    const captainPoints = new Array(gameWeeks).fill(0);
    const benchPoints = new Array(gameWeeks).fill(0);

    this.user.players.forEach(player => {
      player.performances.forEach(perf => {
        const index = perf.gameWeek - 1;
        if (index < 0 || index >= gameWeeks) return;

        const { points, wasInMyTeam, wasBenched, wasCaptain, wasTripleCaptain } = perf;

        if (wasInMyTeam && !wasBenched) {
          totalPoints[index] += points * this.getMultiplier(wasCaptain, wasTripleCaptain);
        }

        if (player.position === 'Manager' && wasInMyTeam && wasBenched) {
          totalPoints[index] += points;
        }

        if (wasCaptain) {
          captainPoints[index] += points * this.getMultiplier(wasCaptain, wasTripleCaptain);
        }

        if (wasBenched) {
          benchPoints[index] += points;
        }
      });
    });
    // Assign chart data
    this.totalPointsChart = {
      labels,
      datasets: [{ data: totalPoints, label: 'Total Points' }]
    };

    this.captainPointsChart = {
      labels,
      datasets: [{ data: captainPoints, label: 'Captain Points' }]
    };

    this.benchPointsChart = {
      labels,
      datasets: [{ data: benchPoints, label: 'Bench Points' }]
    };
  }

  private getMultiplier(wasCaptain: boolean, wasTripleCaptain: boolean): number {
    if (wasTripleCaptain) return 3;
    if (wasCaptain) return 2;
    return 1;
  }
}
