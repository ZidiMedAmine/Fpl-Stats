import {Player} from "./player.model";

export interface UserInfo {
  id: number;
  name: string;
  teamName: string;
  region: string;
  overallRank: number;
  totalPoints: number;
  currentGameWeek: number;
  gameWeekPoints: number;
  gameWeekRank: number;
  players: Player[];
}
