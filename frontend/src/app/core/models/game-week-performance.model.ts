export interface GameWeekPerformance {
  points: number;
  gameWeek: number;
  wasBenched: boolean;
  wasCaptain: boolean;
  bonusPoints: number;
  bps: number;
  saves: number;
  expectedGoals: string;
  expectedAssists: string;
  wasInMyTeam: boolean;
  wasViceCaptain: boolean;
  wasTripleCaptain: boolean;
  minutesPlayed: number;
  cleanSheet: boolean;
  goalsScored: number;
  yellowCards: number;
  redCards: number;
  assists: number;
  multiplier: number;
  // Defensive stats
  goalsConceded: number;
  expectedGoalsConceded: string;
  clearancesBlocksInterceptions: number;
  recoveries: number;
  tackles: number;
  defensiveContribution: number;
  // ICT Index
  influence: string;
  creativity: string;
  threat: string;
  ictIndex: string;
  // Fixture context
  wasHome: boolean;
  value: number;
  transfersIn: number;
  transfersOut: number;
  transfersBalance: number;
  selected: number;
}
