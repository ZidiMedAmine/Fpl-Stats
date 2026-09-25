export interface DreamTeamPlayer {
  fplId: number;
  code: number;
  webName: string;
  position: string;
  teamName: string;
  points: number;
  nowCost: number;
}

export interface DreamTeamResult {
  formation: string;
  totalPoints: number;
  players: DreamTeamPlayer[];
}

export interface DreamTeam {
  allTime: DreamTeamResult;
  last5Weeks: DreamTeamResult;
}
