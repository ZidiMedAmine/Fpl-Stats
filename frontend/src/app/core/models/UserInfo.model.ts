import {Player} from "./player.model";

export interface PlayerStub {
  fplId: number;
  name: string;
  position: string;
  teamName: string;
  code: number;
  nowCost: number;
  status: string;
  totalPoints: number;
}

export interface UserTeamSummary {
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
  players: PlayerStub[];
  rankHistory: RankHistory[];
  rankChange?: number | null;
}

export interface TeamChartData {
  fplTeamId: number;
  players: Player[];
  gameWeekAverages: { [gw: number]: number };
  gameWeekHighScores: { [gw: number]: number };
  formationLoss: GwFormationLoss[];
}

export interface GwFormationLoss {
  gameWeek: number;
  actualPoints: number;
  optimalPoints: number;
  pointsLost: number;
  actualFormation: string;
  optimalFormation: string;
  playersToStart: string[];
  playersToBench: string[];
}

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
  gameWeekHighScores: { [gw: number]: number };
  rankChange?: number | null;
  rankHistory: RankHistory[];
  formationLoss: GwFormationLoss[];
}
