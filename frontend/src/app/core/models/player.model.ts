import {GameWeekPerformance} from "./game-week-performance.model";

export enum Position {
  GKP = 'GKP',
  DEF = 'DEF',
  MID = 'MID',
  FWD = 'FWD',
  Manager = 'Manager',
}

export interface Player {
  fplId: number;
  name: string;
  position: string;
  teamName: string;
  code: number;
  nowCost: number;
  status: string;
  avgPoints: number;
  totalPointsForTeam: number;
  performances: GameWeekPerformance[];
}
