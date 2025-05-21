import {GameWeekPerformance} from "./game-week-performance.model";

export interface Player {
  id: number;
  name: string;
  position: string;
  avgPoints: number;
  photoCode: string;
  totalPointsForTeam: number;
  performances: GameWeekPerformance[];
}
