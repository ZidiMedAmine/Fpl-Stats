import {Player} from "./player.model";

export interface RankHistory {
  gameWeek: number;
  overallRank: number;
  gwRank: number;
  gwPoints: number;
  totalPoints: number;
  bank: number;
  teamValue: number;
  eventTransfers: number;
  pointsOnBench: number;
  chipUsed: string | null;
}

export interface UserInfo {
  fplTeamId: number;
  name: string;
  teamName: string;
  region: string;
  overallRank: number | null;
  totalPoints: number | null;
  teamValue: number | null;
  bank: number | null;
  totalTransfers: number | null;
  currentGameWeek: number;
  players: Player[];
  gameWeekAverages: { [gw: number]: number };
  rankChange?: number | null;
  rankHistory: RankHistory[];
}
