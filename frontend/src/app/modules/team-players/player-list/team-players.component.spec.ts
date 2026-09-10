import {TeamPlayersComponent} from './team-players.component';
import {GameWeekPerformance} from '../../../core/models/game-week-performance.model';
import {Player, Position} from '../../../core/models/player.model';

describe('TeamPlayersComponent', () => {
  let component: TeamPlayersComponent;

  const makePerf = (overrides: Partial<GameWeekPerformance> = {}): GameWeekPerformance => ({
    points: 5,
    gameWeek: 1,
    wasBenched: false,
    wasCaptain: false,
    bonusPoints: 0,
    bps: 0,
    saves: 0,
    expectedGoals: '0.0',
    expectedAssists: '0.0',
    wasInMyTeam: true,
    wasViceCaptain: false,
    wasTripleCaptain: false,
    minutesPlayed: 90,
    cleanSheet: false,
    goalsScored: 0,
    yellowCards: 0,
    redCards: 0,
    assists: 0,
    multiplier: 1,
    ...overrides,
  });

  const makePlayer = (overrides: Partial<Player> = {}): Player => ({
    fplId: 1,
    name: 'Test Player',
    position: Position.MID,
    teamName: 'Test FC',
    code: 12345,
    nowCost: 8.0,
    status: 'a',
    avgPoints: 5.0,
    totalPointsForTeam: 50,
    performances: [],
    ...overrides,
  });

  beforeEach(() => {
    component = new TeamPlayersComponent(
      {} as any, // sharedService
      {} as any, // teamService
      {} as any, // route
      {} as any, // dialog
      {} as any, // snackBar
    );
  });

  describe('getNameColorStyle', () => {
    it('should return gray for zero points', () => {
      expect(component.getNameColorStyle(0)).toEqual({backgroundColor: 'var(--color-avg-none)'});
    });

    it('should return green for avgPoints > 4.9', () => {
      expect(component.getNameColorStyle(6.2)).toEqual({backgroundColor: 'var(--color-avg-good)'});
    });

    it('should return yellow for avgPoints between 4 and 4.9', () => {
      expect(component.getNameColorStyle(4.5)).toEqual({backgroundColor: 'var(--color-avg-ok)'});
    });

    it('should return red for avgPoints <= 4', () => {
      expect(component.getNameColorStyle(3.0)).toEqual({backgroundColor: 'var(--color-avg-bad)'});
    });
  });

  describe('enrichPlayerStats (via loadTeamPlayers pipeline)', () => {
    it('should calculate timesSelected excluding benched games', () => {
      component.players = [
        makePlayer({
          performances: [
            makePerf({wasInMyTeam: true, wasBenched: false, points: 6}),
            makePerf({wasInMyTeam: true, wasBenched: true, points: 3}),
            makePerf({wasInMyTeam: false, wasBenched: false, points: 8}),
          ],
        }),
      ];
      (component as any).enrichPlayerStats();
      const enriched = component.players[0] as any;

      expect(enriched.timesSelected).toBe(1);
      expect(enriched.timesOnBench).toBe(1);
      expect(enriched.playedPoints).toBe(6);
      expect(enriched.benchPoints).toBe(3);
    });

    it('should handle Manager position — selected = benched performances', () => {
      component.players = [
        makePlayer({
          position: Position.Manager,
          performances: [
            makePerf({wasInMyTeam: true, wasBenched: true, points: 10}),
            makePerf({wasInMyTeam: true, wasBenched: false, points: 5}),
          ],
        }),
      ];
      (component as any).enrichPlayerStats();
      const enriched = component.players[0] as any;

      expect(enriched.timesSelected).toBe(1);
      expect(enriched.playedPoints).toBe(10);
    });

    it('should return zero stats for player with no performances', () => {
      component.players = [makePlayer({performances: []})];
      (component as any).enrichPlayerStats();
      const enriched = component.players[0] as any;

      expect(enriched.timesSelected).toBe(0);
      expect(enriched.timesOnBench).toBe(0);
      expect(enriched.playedPoints).toBe(0);
      expect(enriched.benchPoints).toBe(0);
    });
  });

  describe('calculateCaptaincyStats', () => {
    it('should count captain and vice-captain selections', () => {
      const players = [
        makePlayer({
          performances: [
            makePerf({wasCaptain: true}),
            makePerf({wasCaptain: true}),
            makePerf({wasViceCaptain: true}),
            makePerf({}),
          ],
        }),
      ];

      const result = (component as any).calculateCaptaincyStats(players);
      expect(result[0].timesCaptained).toBe(2);
      expect(result[0].timesViceCaptained).toBe(1);
    });

    it('should return zero when never captained', () => {
      const players = [makePlayer({performances: [makePerf(), makePerf()]})];
      const result = (component as any).calculateCaptaincyStats(players);
      expect(result[0].timesCaptained).toBe(0);
      expect(result[0].timesViceCaptained).toBe(0);
    });
  });

  describe('sortPlayersByPosition', () => {
    it('should sort players in GKP, DEF, MID, FWD order', () => {
      component.players = [
        makePlayer({name: 'Forward', position: Position.FWD}),
        makePlayer({name: 'Midfielder', position: Position.MID}),
        makePlayer({name: 'Goalkeeper', position: Position.GKP}),
        makePlayer({name: 'Defender', position: Position.DEF}),
      ];

      (component as any).sortPlayersByPosition();

      expect(component.players.map(p => p.name)).toEqual([
        'Goalkeeper', 'Defender', 'Midfielder', 'Forward',
      ]);
    });
  });
});
