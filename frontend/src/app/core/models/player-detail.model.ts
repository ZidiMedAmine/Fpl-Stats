import { GameWeekPerformance } from './game-week-performance.model';

export interface PlayerDetail {
  fplId: number;
  webName: string;
  position: string;
  teamName: string;
  code: number;
  nowCost: number;
  totalPoints: number;
  avgPoints: number;
  selectedByPercent: number;
  performances: GameWeekPerformance[];
}
