import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, NgZone, OnChanges, ViewChild } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, ChartDataset, registerables, ScriptableLineSegmentContext } from 'chart.js';
import ChartDataLabels, { Context } from 'chartjs-plugin-datalabels';
import { UserInfo } from '../../../core/models/UserInfo.model';
import { Position } from '../../../core/models/player.model';
import { CHART_COLORS } from '../../../core/chart-colors.constants';

Chart.register(...registerables, ChartDataLabels);

interface WeeklyGWData {
  gw: number;
  goals: number;
  assists: number;
  cleanSheets: number;
  bonus: number;
  total: number;
  goalCount: number;
  assistCount: number;
  cleanSheetCount: number;
  bonusCount: number;
}

interface DoughnutLabelContext {
  chart: {
    data: {
      labels?: unknown[];
    };
  };
  dataset?: {
    data?: number[];
  };
  dataIndex: number;
}

@Component({
  selector: 'app-performance-charts',
  templateUrl: './performance-charts.component.html',
  styleUrls: ['./performance-charts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PerformanceChartsComponent implements OnChanges {
  @Input() user!: UserInfo;
  @Input() teamValue: number | null = null;
  @ViewChild('weeklyBarChart') weeklyBarChartRef?: BaseChartDirective;
  @ViewChild('benchLineChart') benchLineChartRef?: BaseChartDirective;

  activeChart = 'total';

  lineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, align: 'end', labels: { color: 'transparent', boxWidth: 0 } },
      datalabels: {
        anchor: 'end',
        align: 'top',
        offset: 1,
        font: { size: 10 }
      }
    },
  };

  singleLineOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, labels: { color: 'transparent', boxWidth: 0 } },
      datalabels: {
        anchor: 'end',
        align: 'top',
        offset: 4,
        font: { size: 10 }
      }
    },
  };

  teamValueOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      datalabels: { display: false },
      tooltip: {
        callbacks: {
          label: (item) => ` ${item.dataset.label}: £${Number(item.raw).toFixed(1)}m`
        }
      }
    },
    scales: {
      y: {
        ticks: { callback: (value) => `£${Number(value).toFixed(1)}m` }
      }
    }
  };

  rankHistoryOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      datalabels: {
        display: true,
        font: { weight: 'bold', size: 9 },
        anchor: 'end',
        align: 'top',
        offset: 4,
        color: (context: Context) => {
          const index = context.dataIndex;
          if (index === 0) return 'transparent';
          const data = (context.dataset as ChartDataset<'line'>).data as number[];
          const diff = data[index - 1] - data[index];
          if (diff > 0) return CHART_COLORS.success;
          if (diff < 0) return CHART_COLORS.danger;
          return 'transparent';
        },
        formatter: (value: number, context: Context) => {
          const index = context.dataIndex;
          if (index === 0) return '';
          const data = (context.dataset as ChartDataset<'line'>).data as number[];
          const diff = data[index - 1] - value;
          if (diff === 0) return '';
          return diff > 0 ? `+${diff.toLocaleString()}` : `-${Math.abs(diff).toLocaleString()}`;
        }
      }
    },
    scales: {
      y: {
        reverse: true,
        min: 1,
        ticks: {
          includeBounds: true,
          callback: (value) => `${Number(value).toLocaleString()}`
        }
      }
    },
  };

  gwRankOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      datalabels: {
        display: true,
        align: 'top',
        anchor: 'center',
        formatter: (value: number) => `#${Number(value).toLocaleString()}`,
        font: { size: 10 },
        color: CHART_COLORS.primary
      },
      tooltip: {
        callbacks: {
          label: (item) => ` GW Rank: #${Number(item.raw).toLocaleString()}`
        }
      }
    },
    scales: {
      y: {
        reverse: true,
        min: 1,
        ticks: { callback: (value) => `#${Number(value).toLocaleString()}` }
      }
    }
  };

  barOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: true } },
    scales: { x: { stacked: true }, y: { stacked: true } }
  };

  doughnutOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    layout: { padding: { top: 10, right: 8, bottom: 8, left: 8 } },
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          color: CHART_COLORS.ui.tickLabel,
          padding: 16,
          boxWidth: 12,
          usePointStyle: true,
          font: { size: 12, weight: 'bold' }
        }
      }
    },
    elements: {
      arc: {
        borderWidth: 2,
        borderColor: CHART_COLORS.ui.white
      }
    },
    cutout: '60%'
  };

  pointsByPositionOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          color: CHART_COLORS.ui.tickLabel,
          padding: 16,
          boxWidth: 12,
          usePointStyle: true,
          generateLabels: (chart) => {
            const dataset = chart.data.datasets[0];
            const values = dataset.data as number[];
            return (chart.data.labels as string[]).map((label, index) => ({
              text: `${label}  ${values[index]} pts`,
              fillStyle: (dataset.backgroundColor as string[])[index],
              strokeStyle: (dataset.backgroundColor as string[])[index],
              lineWidth: 0,
              hidden: false,
              index,
              pointStyle: 'circle' as const,
            }));
          }
        }
      },
      datalabels: {
        display: true,
        color: CHART_COLORS.ui.axisLabel,
        font: { weight: 'bold', size: 11 },
        anchor: 'center',
        align: 'center',
        formatter: (value: number, context: unknown) => this.formatDoughnutPercentLabel(value, context)
      }
    },
    elements: { arc: { borderWidth: 2, borderColor: CHART_COLORS.ui.white } },
    cutout: '62%'
  };

  investmentByPositionOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          color: CHART_COLORS.ui.tickLabel,
          padding: 16,
          boxWidth: 12,
          usePointStyle: true,
          generateLabels: (chart) => {
            const dataset = chart.data.datasets[0];
            const values = dataset.data as number[];
            return (chart.data.labels as string[]).map((label, index) => ({
              text: `${label}  £${(values[index] as number).toFixed(1)}m`,
              fillStyle: (dataset.backgroundColor as string[])[index],
              strokeStyle: (dataset.backgroundColor as string[])[index],
              lineWidth: 0,
              hidden: false,
              index,
              pointStyle: 'circle' as const,
            }));
          }
        }
      },
      datalabels: {
        display: true,
        color: CHART_COLORS.ui.axisLabel,
        font: { weight: 'bold', size: 11 },
        anchor: 'center',
        align: 'center',
        formatter: (value: number, context: unknown) => this.formatDoughnutPercentLabel(value, context)
      }
    },
    elements: { arc: { borderWidth: 2, borderColor: CHART_COLORS.ui.white } },
    cutout: '62%'
  };

  efficiencyByPositionOptions: ChartConfiguration<'polarArea'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom',
        labels: {
          color: CHART_COLORS.ui.tickLabel,
          padding: 16,
          boxWidth: 12,
          usePointStyle: true,
          generateLabels: (chart) => {
            const dataset = chart.data.datasets[0];
            const values = dataset.data as number[];
            return (chart.data.labels as string[]).map((label, index) => ({
              text: `${label}  ${(values[index] as number).toFixed(2)} pts/£m`,
              fillStyle: (dataset.backgroundColor as string[])[index],
              strokeStyle: (dataset.backgroundColor as string[])[index],
              lineWidth: 0,
              hidden: false,
              index,
              pointStyle: 'circle' as const,
            }));
          }
        }
      },
      datalabels: {
        display: true,
        color: CHART_COLORS.ui.axisLabel,
        font: { weight: 'bold', size: 11 },
        anchor: 'center',
        align: 'center',
        formatter: (value: number) => `${value.toFixed(2)}`
      },
      tooltip: {
        callbacks: {
          label: (item) => `${Number(item.raw).toFixed(2)} pts/£m`
        }
      }
    },
    scales: { r: { ticks: { display: false }, grid: { color: CHART_COLORS.ui.gridLine } } }
  };

  weeklyBarOptions: ChartConfiguration<'bar'>['options'];

  totalPointsChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  rankHistoryChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  captainPointsChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  benchPointsChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  pointsByPositionChart: ChartConfiguration<'doughnut'>['data'] = { labels: [], datasets: [] };
  investmentByPositionChart: ChartConfiguration<'doughnut'>['data'] = { labels: [], datasets: [] };
  efficiencyByPositionChart: ChartConfiguration<'polarArea'>['data'] = { labels: [], datasets: [] };
  weeklyOverviewChart: ChartConfiguration<'bar'>['data'] = { labels: [], datasets: [] };
  teamValueChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  gwRankChart: ChartConfiguration<'line'>['data'] = { labels: [], datasets: [] };
  rankMode: 'overall' | 'gw' = 'overall';
  positionSummary = { pointsPerMillion: 0, averagePlayerValue: 0 };

  topCaptain = '';
  topCaptainCount = 0;
  topCaptainPhoto = 0;
  totalBenchWasted = 0;
  benchWastedWeeks = 0;
  bestGWScore = 0;
  bestGWNumber = 0;

  weeklyGWData: WeeklyGWData[] = [];
  weeklyMode: 'gw' | 'cumulative' = 'gw';
  benchGWData: number[] = [];
  benchMode: 'gw' | 'cumulative' = 'gw';
  totalBenchPoints = 0;
  weeklyHoveredGW: WeeklyGWData | null = null;
  weeklySeasonSummary = { total: 0, avgPerGW: 0, bestGW: 0, bestGWNum: 0, worstGW: 0, worstGWNum: 0 };

  private readonly positionColors: Record<string, string> = {
    [Position.GKP]: CHART_COLORS.position.gkp,
    [Position.DEF]: CHART_COLORS.position.def,
    [Position.MID]: CHART_COLORS.position.mid,
    [Position.FWD]: CHART_COLORS.position.fwd,
  };

  private readonly positionDisplayNames: Record<string, string> = {
    [Position.GKP]: 'Goalkeepers',
    [Position.DEF]: 'Defenders',
    [Position.MID]: 'Midfielders',
    [Position.FWD]: 'Forwards',
  };

  constructor(private readonly zone: NgZone, private readonly cdr: ChangeDetectorRef) {
    this.weeklyBarOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        datalabels: {
          color: CHART_COLORS.ui.white,
          font: { size: 10, weight: 'bold' },
          anchor: 'center',
          align: 'center',
          offset: 0,
          formatter: (value: number) => value > 0 ? value : ''
        }
      },
      scales: {
        x: { stacked: false, ticks: { autoSkip: true, maxRotation: 0, color: CHART_COLORS.ui.axisLabel }, grid: { color: CHART_COLORS.ui.gridLine } },
        y: { stacked: false, ticks: { color: CHART_COLORS.ui.axisLabel }, grid: { color: CHART_COLORS.ui.gridLine } }
      },
      onHover: (_event, elements, chart) => {
        this.zone.run(() => {
          if (elements.length > 0) {
            const index = elements[0].index;
            this.weeklyHoveredGW = this.weeklyGWData[index] ?? null;
            this.dimWeeklyBars(index, chart);
          } else {
            this.weeklyHoveredGW = null;
            this.dimWeeklyBars(-1, chart);
          }
          this.cdr.markForCheck();
        });
      }
    };
  }

  /**
   * Rebuilds all chart data whenever the `user` input reference changes.
   */
  ngOnChanges(): void {
    if (this.user) {
      this.prepareChartData();
      this.prepareRankHistoryChart();
      this.prepareTeamValueChart();
      this.prepareWeeklyStats();
      this.preparePositionCharts();
      this.prepareCaptainHighlight();
      this.prepareBenchWasted();
      this.prepareBestGW();
    }
  }

  /**
   * Prepares the overall rank history and GW rank chart data
   * with segment coloring to indicate rank improvement or decline.
   */
  prepareRankHistoryChart(): void {
    const history = this.user.rankHistory ?? [];
    const labels = history.map(rank => `GW${rank.gameWeek}`);
    const overallRanks = history.map(rank => rank.overallRank);
    const gwRanks = history.map(rank => rank.gwRank);

    this.gwRankChart = {
      labels,
      datasets: [{
        data: gwRanks,
        label: 'GW Rank',
        borderColor: CHART_COLORS.primary,
        borderDash: [6, 4],
        fill: 'start',
        pointRadius: 5,
        tension: 0.3,
        segment: {
          borderColor: (ctx: ScriptableLineSegmentContext) => {
            const prev = ctx.p0.parsed.y ?? 0;
            const curr = ctx.p1.parsed.y ?? 0;
            if (curr < prev) return CHART_COLORS.success;
            if (curr > prev) return CHART_COLORS.danger;
            return CHART_COLORS.primary;
          },
          backgroundColor: (ctx: ScriptableLineSegmentContext) => {
            const prev = ctx.p0.parsed.y ?? 0;
            const curr = ctx.p1.parsed.y ?? 0;
            if (curr < prev) return CHART_COLORS.successAlpha15;
            if (curr > prev) return CHART_COLORS.dangerAlpha15;
            return CHART_COLORS.primaryAlpha15;
          }
        },
        pointBackgroundColor: gwRanks.map((rank, index) => {
          if (index === 0) return CHART_COLORS.primary;
          return rank < gwRanks[index - 1] ? CHART_COLORS.success : rank > gwRanks[index - 1] ? CHART_COLORS.danger : CHART_COLORS.primary;
        })
      }]
    };

    this.rankHistoryChart = {
      labels,
      datasets: [{
        data: overallRanks,
        label: 'Overall Rank',
        borderColor: CHART_COLORS.primary,
        borderDash: [6, 4],
        fill: 'start',
        pointRadius: 5,
        tension: 0.3,
        segment: {
          borderColor: (ctx: ScriptableLineSegmentContext) => {
            const prev = ctx.p0.parsed.y ?? 0;
            const curr = ctx.p1.parsed.y ?? 0;
            if (curr < prev) return CHART_COLORS.success; // rank improved (lower number)
            if (curr > prev) return CHART_COLORS.danger;  // rank worsened
            return CHART_COLORS.primary;                  // unchanged
          },
          backgroundColor: (ctx: ScriptableLineSegmentContext) => {
            const prev = ctx.p0.parsed.y ?? 0;
            const curr = ctx.p1.parsed.y ?? 0;
            if (curr < prev) return CHART_COLORS.successAlpha15;
            if (curr > prev) return CHART_COLORS.dangerAlpha15;
            return CHART_COLORS.primaryAlpha15;
          }
        },
        pointBackgroundColor: overallRanks.map((rank, index) => {
          if (index === 0) return CHART_COLORS.primary;
          return rank < overallRanks[index - 1] ? CHART_COLORS.success : rank > overallRanks[index - 1] ? CHART_COLORS.danger : CHART_COLORS.primary;
        })
      }]
    };
  }

  /**
   * Toggles between overall rank and GW rank chart display.
   */
  toggleRankMode(): void {
    this.rankMode = this.rankMode === 'overall' ? 'gw' : 'overall';
  }

  /**
   * Prepares the team value and total wealth (value + bank) line chart data
   * from the user's rank history, excluding entries missing value or bank data.
   */
  prepareTeamValueChart(): void {
    const history = (this.user.rankHistory ?? []).filter(rank => rank.teamValue != null && rank.bank != null);
    if (!history.length) return;

    const labels = history.map(rank => `GW${rank.gameWeek}`);
    const teamValues = history.map(rank => rank.teamValue);
    const totals     = history.map((rank, index) => +(teamValues[index] + rank.bank).toFixed(1));

    this.teamValueChart = {
      labels,
      datasets: [
        {
          label: 'Squad Value',
          data: teamValues,
          borderColor: CHART_COLORS.primary,
          backgroundColor: CHART_COLORS.primaryAlpha07,
          pointBackgroundColor: CHART_COLORS.primary,
          fill: false,
          pointRadius: 5,
          tension: 0.3
        },
        {
          label: 'Total Wealth',
          data: totals,
          borderColor: CHART_COLORS.gold,
          backgroundColor: CHART_COLORS.goldAlpha07,
          pointBackgroundColor: CHART_COLORS.gold,
          fill: false,
          pointRadius: 5,
          tension: 0.3,
          borderDash: [2, 2]
        }
      ]
    };
  }

  /**
   * Calculates the user's best single-gameweek score and stores the
   * corresponding gameweek number.
   */
  prepareBestGW(): void {
    const gameWeekCount = this.user.currentGameWeek;
    const totalPoints = new Array(gameWeekCount).fill(0);

    this.user.players.forEach(player => {
      player.performances.forEach(performance => {
        const index = performance.gameWeek - 1;
        if (index < 0 || index >= gameWeekCount) return;
        if (performance.wasInMyTeam && performance.multiplier > 0) {
          totalPoints[index] += performance.points * performance.multiplier;
        }
        if (player.position === Position.Manager && performance.wasInMyTeam && performance.wasBenched) {
          totalPoints[index] += performance.points;
        }
      });
    });

    let maxScore = 0;
    let maxGameWeek = 1;
    totalPoints.forEach((points, index) => {
      if (points > maxScore) {
        maxScore = points;
        maxGameWeek = index + 1;
      }
    });

    this.bestGWScore = maxScore;
    this.bestGWNumber = maxGameWeek;
  }

  /**
   * Prepares the total points, captain points, and bench points chart data
   * by iterating over all player performances.
   */
  prepareChartData(): void {
    const gameWeekCount = this.user.currentGameWeek;
    const labels = Array.from({ length: gameWeekCount }, (_, index) => `GW${index + 1}`);
    const totalPoints   = new Array(gameWeekCount).fill(0);
    const captainPoints = new Array(gameWeekCount).fill(0);
    const benchPoints   = new Array(gameWeekCount).fill(0);

    this.user.players.forEach(player =>
      this.accumulatePlayerPoints(player, totalPoints, captainPoints, benchPoints, gameWeekCount)
    );

    const averages = Array.from({ length: gameWeekCount }, (_, index) =>
      this.user.gameWeekAverages?.[index + 1] ?? null
    );

    this.totalPointsChart = {
      labels,
      datasets: [
        {
          data: totalPoints,
          label: 'Total Points',
          borderColor: CHART_COLORS.primary,
          backgroundColor: CHART_COLORS.primaryAlpha07,
          fill: true,
          pointRadius: 5,
          tension: 0.3
        },
        {
          data: averages,
          label: 'GW Average',
          borderDash: [6, 4],
          borderColor: CHART_COLORS.pink,
          backgroundColor: 'transparent',
          fill: false,
          pointRadius: 5,
          tension: 0.3
        }
      ]
    };
    this.captainPointsChart = {
      labels,
      datasets: [
        {
          data: captainPoints,
          label: 'Captain Points',
          fill: true,
          pointRadius: 5,
          tension: 0.3,

        }
      ]
    };
    this.benchGWData = benchPoints;
    this.totalBenchPoints = benchPoints.reduce((sum, value) => sum + value, 0);
    this.benchMode = 'gw';
    this.buildBenchChart();
  }

  /**
   * Builds the bench points chart in either per-GW or cumulative mode.
   */
  buildBenchChart(): void {
    const labels = Array.from({ length: this.benchGWData.length }, (_, index) => `GW${index + 1}`);
    const data = this.computeSeriesData(this.benchGWData, this.benchMode);
    this.benchPointsChart = {
      labels,
      datasets: [{
        data,
        label: this.benchMode === 'gw' ? 'Bench Points' : 'Cumulative Bench Pts',
        borderColor: CHART_COLORS.pink,
        backgroundColor: CHART_COLORS.pinkAlpha25,
        fill: true,
        pointRadius: 5,
        tension: 0.3
      }]
    };
  }

  /**
   * Toggles between per-GW and cumulative bench points display,
   * updating the existing chart in-place when the chart reference is available.
   */
  toggleBenchMode(): void {
    this.benchMode = this.benchMode === 'gw' ? 'cumulative' : 'gw';
    const chart = this.benchLineChartRef?.chart;
    if (!chart) {
      this.buildBenchChart();
      return;
    }
    const data = this.computeSeriesData(this.benchGWData, this.benchMode);
    chart.data.datasets[0].data = data;
    (chart.data.datasets[0] as ChartDataset<'line'>).label =
      this.benchMode === 'gw' ? 'Bench Points' : 'Cumulative Bench Pts';
    chart.update('none');
  }

  /**
   * Prepares the per-gameweek breakdown of goal, assist, clean sheet, and bonus
   * points contributed by outfield starters, and builds the weekly overview chart.
   */
  prepareWeeklyStats(): void {
    const gameWeekCount = this.user.currentGameWeek;
    const goals           = new Array(gameWeekCount).fill(0);
    const assists         = new Array(gameWeekCount).fill(0);
    const cleanSheets     = new Array(gameWeekCount).fill(0);
    const bonus           = new Array(gameWeekCount).fill(0);
    const goalCount       = new Array(gameWeekCount).fill(0);
    const assistCount     = new Array(gameWeekCount).fill(0);
    const cleanSheetCount = new Array(gameWeekCount).fill(0);
    const bonusCount      = new Array(gameWeekCount).fill(0);

    this.user.players.forEach(player => {
      if (player.position === Position.Manager) return;
      player.performances.forEach(performance => {
        const index = performance.gameWeek - 1;
        if (index < 0 || index >= gameWeekCount || !performance.wasInMyTeam || performance.multiplier === 0) return;

        goals[index]   += performance.goalsScored * this.getGoalPoints(player.position);
        assists[index] += performance.assists * 3;
        if (performance.cleanSheet) cleanSheets[index] += this.getCleanSheetPoints(player.position);
        bonus[index]   += performance.bonusPoints;
        goalCount[index]   += performance.goalsScored;
        assistCount[index] += performance.assists;
        if (performance.cleanSheet) cleanSheetCount[index]++;
        if (performance.bonusPoints > 0) bonusCount[index]++;
      });
    });

    this.weeklyGWData = Array.from({ length: gameWeekCount }, (_, index) => ({
      gw: index + 1,
      goals: goals[index],
      assists: assists[index],
      cleanSheets: cleanSheets[index],
      bonus: bonus[index],
      total: goals[index] + assists[index] + cleanSheets[index] + bonus[index],
      goalCount: goalCount[index],
      assistCount: assistCount[index],
      cleanSheetCount: cleanSheetCount[index],
      bonusCount: bonusCount[index],
    }));

    const totals = this.weeklyGWData.map(data => data.total);
    const seasonTotal = totals.reduce((sum, value) => sum + value, 0);
    const nonZeroCount = totals.filter(value => value > 0).length;
    let bestScore = 0, bestGameWeek = 0, worstScore = Infinity, worstGameWeek = 0;
    this.weeklyGWData.forEach(data => {
      if (data.total > bestScore) { bestScore = data.total; bestGameWeek = data.gw; }
      if (data.total < worstScore) { worstScore = data.total; worstGameWeek = data.gw; }
    });

    this.weeklySeasonSummary = {
      total: seasonTotal,
      avgPerGW: nonZeroCount > 0 ? Math.round(seasonTotal / nonZeroCount) : 0,
      bestGW: bestScore,
      bestGWNum: bestGameWeek,
      worstGW: worstScore === Infinity ? 0 : worstScore,
      worstGWNum: worstGameWeek
    };

    this.buildWeeklyOverviewChart();
  }

  /**
   * Builds the weekly overview bar chart in either per-GW or cumulative mode.
   */
  buildWeeklyOverviewChart(): void {
    const labels = this.weeklyGWData.map(data => `GW${data.gw}`);
    const data = this.computeSeriesData(
      this.weeklyGWData.map(entry => entry.total),
      this.weeklyMode
    );
    this.weeklyOverviewChart = {
      labels,
      datasets: [{
        data,
        label: this.weeklyMode === 'gw' ? 'FPL Pts' : 'Cumulative Pts',
        backgroundColor: this.weeklyGWData.map(() => CHART_COLORS.primary),
        borderRadius: 4
      }]
    };
  }

  /**
   * Toggles between per-GW and cumulative weekly stats display,
   * updating the existing chart in-place when the chart reference is available.
   */
  toggleWeeklyMode(): void {
    this.weeklyMode = this.weeklyMode === 'gw' ? 'cumulative' : 'gw';
    this.weeklyHoveredGW = null;

    const chart = this.weeklyBarChartRef?.chart;
    if (!chart) {
      this.buildWeeklyOverviewChart();
      return;
    }

    const data = this.computeSeriesData(
      this.weeklyGWData.map(entry => entry.total),
      this.weeklyMode
    );
    chart.data.datasets[0].data = data;
    const dataset = chart.data.datasets[0] as ChartDataset<'bar'>;
    dataset.label = this.weeklyMode === 'gw' ? 'FPL Pts' : 'Cumulative Pts';
    dataset.backgroundColor = this.weeklyGWData.map(() => CHART_COLORS.primary);
    chart.update('none');
  }

  /**
   * Dims all bars in the weekly chart except the one at `hoveredIndex`.
   * Pass `-1` to reset all bars to full opacity.
   *
   * @param hoveredIndex - Index of the hovered bar, or -1 to clear the highlight.
   * @param chart - The Chart.js instance to update.
   */
  dimWeeklyBars(hoveredIndex: number, chart: Chart): void {
    chart.data.datasets[0].backgroundColor = this.weeklyGWData.map((_, index) =>
      hoveredIndex === -1 || index === hoveredIndex ? CHART_COLORS.primary : CHART_COLORS.primaryAlpha25
    );
    chart.update('none');
  }

  /**
   * Prepares points, squad investment, and efficiency breakdown doughnut/polar charts
   * grouped by player position.
   */
  preparePositionCharts(): void {
    const { labels, points, costs, colors } = this.computePositionBreakdown();
    const totalCost    = costs.reduce((sum, value) => sum + value, 0);
    const totalPoints  = points.reduce((sum, value) => sum + value, 0);
    const totalPlayers = this.user.players.filter(player => player.position !== Position.Manager).length;

    this.positionSummary = {
      pointsPerMillion: totalCost > 0 ? totalPoints / totalCost : 0,
      averagePlayerValue: totalPlayers > 0 ? totalCost / totalPlayers : 0,
    };
    this.pointsByPositionChart      = { labels, datasets: [{ data: points, backgroundColor: colors }] };
    this.investmentByPositionChart  = { labels, datasets: [{ data: costs,  backgroundColor: colors }] };
    this.efficiencyByPositionChart  = {
      labels,
      datasets: [{ data: points.map((pts, index) => costs[index] > 0 ? pts / costs[index] : 0), backgroundColor: colors }]
    };
  }

  /**
   * Aggregates per-position labels, total points, squad costs, and display colors
   * for the four outfield positions.
   *
   * @returns An object with parallel arrays: `labels`, `points`, `costs`, `colors`.
   */
  private computePositionBreakdown(): { labels: string[]; points: number[]; costs: number[]; colors: string[] } {
    const labels: string[] = [];
    const points: number[] = [];
    const costs:  number[] = [];
    const colors: string[] = [];

    [Position.GKP, Position.DEF, Position.MID, Position.FWD].forEach(position => {
      const playersInPosition = this.user.players.filter(player => player.position === position);
      if (playersInPosition.length === 0) return;

      const totalPoints = playersInPosition.reduce((sum, player) => {
        return sum + player.performances
          .filter(perf => perf.wasInMyTeam && perf.multiplier > 0)
          .reduce((pts, perf) => pts + perf.points * perf.multiplier, 0);
      }, 0);

      labels.push(this.getPositionDisplayLabel(position));
      points.push(totalPoints);
      costs.push(playersInPosition.reduce((sum, player) => sum + player.nowCost, 0));
      colors.push(this.positionColors[position] || CHART_COLORS.ui.fallback);
    });

    return { labels, points, costs, colors };
  }

  /**
   * Calculates and stores the most-captained player's name, code (for photo),
   * and the number of times they were captained.
   */
  prepareCaptainHighlight(): void {
    let maxCount = 0;
    let topPlayer = '';
    let topCode = 0;

    this.user.players.forEach(player => {
      if (player.position === Position.Manager) return;
      const count = player.performances.filter(performance => performance.wasCaptain).length;
      if (count > maxCount) {
        maxCount = count;
        topPlayer = player.name;
        topCode = player.code;
      }
    });

    this.topCaptain = topPlayer;
    this.topCaptainCount = maxCount;
    this.topCaptainPhoto = topCode;
  }

  /**
   * Calculates total bench points wasted (bench outscored the worst starter)
   * and the number of gameweeks this occurred, excluding bench boost gameweeks.
   */
  prepareBenchWasted(): void {
    const gameWeekCount = this.user.currentGameWeek;
    let totalWasted = 0;
    let weeksWasted = 0;

    for (let gameWeek = 1; gameWeek <= gameWeekCount; gameWeek++) {
      const isBenchBoostGameWeek = this.user.players.some(player =>
        player.performances.some(performance =>
          performance.gameWeek === gameWeek && performance.wasInMyTeam && performance.wasBenched && performance.multiplier > 0
        )
      );

      if (isBenchBoostGameWeek) continue;

      let benchTotal = 0;
      let lowestStarterPoints = Infinity;

      this.user.players.forEach(player => {
        if (player.position === Position.Manager) return;
        const performance = player.performances.find(perf => perf.gameWeek === gameWeek && perf.wasInMyTeam);
        if (!performance) return;

        if (performance.wasBenched) {
          benchTotal += performance.points;
        } else if (performance.multiplier > 0 && performance.points < lowestStarterPoints) {
          lowestStarterPoints = performance.points;
        }
      });

      if (benchTotal > 0 && lowestStarterPoints !== Infinity && benchTotal > lowestStarterPoints) {
        totalWasted += benchTotal - lowestStarterPoints;
        weeksWasted++;
      }
    }

    this.totalBenchWasted = totalWasted;
    this.benchWastedWeeks = weeksWasted;
  }

  /**
   * Switches the active chart tab to the given chart type.
   *
   * @param type - The chart type identifier to activate.
   */
  showChart(type: string): void { this.activeChart = type; }

  /**
   * Returns the display label for a position including its abbreviation,
   * e.g. "Goalkeepers (GKP)".
   *
   * @param position - The position key from {@link Position}.
   */
  private getPositionDisplayLabel(position: string): string {
    const baseLabel = this.positionDisplayNames[position] ?? position;
    return `${baseLabel} (${position})`;
  }

  /**
   * Returns the FPL goal scoring points for the given position.
   *
   * @param position - The position key from {@link Position}.
   */
  private getGoalPoints(position: string): number {
    if (position === Position.GKP || position === Position.DEF) return 6;
    if (position === Position.MID) return 5;
    return 4;
  }

  /**
   * Returns the FPL clean sheet points for the given position.
   *
   * @param position - The position key from {@link Position}.
   */
  private getCleanSheetPoints(position: string): number {
    if (position === Position.GKP || position === Position.DEF) return 4;
    if (position === Position.MID) return 1;
    return 0;
  }

  /**
   * Formats a doughnut/pie slice value as a percentage of the total dataset.
   * Used as the shared `datalabels.formatter` for position breakdown charts.
   *
   * @param value - The slice value to format.
   * @param context - The Chart.js datalabels context containing dataset info.
   * @returns A percentage string like `"34%"`, or `"0%"` if total is zero.
   */
  private formatDoughnutPercentLabel(value: number, context: unknown): string {
    const chartContext = context as DoughnutLabelContext;
    const dataValues = chartContext.dataset?.data ?? [];
    const total = dataValues.reduce((sum, current) => sum + (Number(current) || 0), 0);
    return total > 0 ? `${((value / total) * 100).toFixed(0)}%` : '0%';
  }

  /**
   * Accumulates total, captain, and bench point contributions for a single player
   * into the provided pre-allocated arrays.
   *
   * @param player - The player whose performances to aggregate.
   * @param totalPoints - Running total points array indexed by gameweek.
   * @param captainPoints - Running captain points array indexed by gameweek.
   * @param benchPoints - Running bench points array indexed by gameweek.
   * @param gameWeekCount - Upper bound for valid gameweek indices.
   */
  private accumulatePlayerPoints(
    player: UserInfo['players'][number],
    totalPoints: number[],
    captainPoints: number[],
    benchPoints: number[],
    gameWeekCount: number
  ): void {
    player.performances.forEach(performance => {
      const index = performance.gameWeek - 1;
      if (index < 0 || index >= gameWeekCount) return;
      const { points, wasInMyTeam, wasBenched } = performance;
      if (wasInMyTeam && performance.multiplier > 0) totalPoints[index] += points * performance.multiplier;
      if (player.position === Position.Manager && wasInMyTeam && wasBenched) totalPoints[index] += points;
      // multiplier > 1 covers: captain played, TC played, VC took over (captain/TC didn't play)
      if (performance.multiplier > 1 && wasInMyTeam && !wasBenched) captainPoints[index] += points * performance.multiplier;
      if (wasBenched && performance.multiplier === 0) benchPoints[index] += points;
    });
  }

  /**
   * Converts a raw data array into either a per-gameweek or cumulative series.
   *
   * @param rawData - The per-gameweek values to transform.
   * @param mode - `'gw'` returns the data as-is; `'cumulative'` returns a running total.
   */
  private computeSeriesData(rawData: number[], mode: 'gw' | 'cumulative'): number[] {
    if (mode !== 'cumulative') return [...rawData];
    let running = 0;
    return rawData.map(value => { running += value; return running; });
  }
}
