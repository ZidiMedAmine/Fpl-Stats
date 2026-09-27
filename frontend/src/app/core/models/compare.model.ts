import {Player} from './player.model';
import {RankHistory} from './UserInfo.model';

export interface CompareTeam {
  fplTeamId: number;
  name: string;
  teamName: string;
  region: string;
  overallRank: number | null;
  totalPoints: number | null;
  teamValue: number | null;
  currentGameWeek: number;
  players: Player[];
  rankHistory: RankHistory[];
}

export interface CompareResult {
  team1: CompareTeam;
  team2: CompareTeam;
  sharedPlayers: Player[];
  team1Differentials: Player[];
  team2Differentials: Player[];
  pointsByGameWeek: Record<number, number[]>;
}
