import {PlayerDetailsComponent} from './player-details.component';
import {GameWeekPerformance} from '../../../core/models/game-week-performance.model';
import {Position} from '../../../core/models/player.model';

describe('PlayerDetailsComponent', () => {
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

  const makeComponent = (position: string, performances: GameWeekPerformance[]): PlayerDetailsComponent => {
    return new PlayerDetailsComponent({
      player: {
        fplId: 1, name: 'Test', position, teamName: 'FC', code: 1,
        nowCost: 8.0, status: 'a', avgPoints: 5, totalPointsForTeam: 50,
        performances,
      },
    } as any);
  };

  describe('calculatePoints', () => {
    it('should return base points for regular player', () => {
      const comp = makeComponent(Position.MID, []);
      expect(comp.calculatePoints(makePerf({points: 7}))).toBe(7);
    });

    it('should double points for captain', () => {
      const comp = makeComponent(Position.MID, []);
      expect(comp.calculatePoints(makePerf({points: 7, wasCaptain: true, multiplier: 2}))).toBe(14);
    });

    it('should triple points for triple captain', () => {
      const comp = makeComponent(Position.MID, []);
      expect(comp.calculatePoints(makePerf({points: 7, wasTripleCaptain: true, multiplier: 3}))).toBe(21);
    });

    it('should double points for VC when captain did not play', () => {
      const comp = makeComponent(Position.MID, []);
      expect(comp.calculatePoints(makePerf({points: 9, wasViceCaptain: true, multiplier: 2}))).toBe(18);
    });

    it('should triple points for VC when TC captain did not play', () => {
      const comp = makeComponent(Position.MID, []);
      expect(comp.calculatePoints(makePerf({points: 9, wasViceCaptain: true, multiplier: 3}))).toBe(27);
    });
  });

  describe('getBenchedPoints', () => {
    it('should sum points from benched performances only', () => {
      const comp = makeComponent(Position.DEF, [
        makePerf({wasInMyTeam: true, wasBenched: true, points: 3}),
        makePerf({wasInMyTeam: true, wasBenched: false, points: 8}),
        makePerf({wasInMyTeam: true, wasBenched: true, points: 2}),
        makePerf({wasInMyTeam: false, wasBenched: true, points: 10}),
      ]);
      expect(comp.getBenchedPoints()).toBe(5);
    });
  });

  describe('getSelectedGwPoints', () => {
    it('should sum non-benched points for outfield player', () => {
      const comp = makeComponent(Position.FWD, [
        makePerf({wasInMyTeam: true, wasBenched: false, points: 6}),
        makePerf({wasInMyTeam: true, wasBenched: true, points: 3}),
      ]);
      expect(comp.getSelectedGwPoints()).toBe(6);
    });

    it('should include all in-team points for Manager', () => {
      const comp = makeComponent(Position.Manager, [
        makePerf({wasInMyTeam: true, wasBenched: false, points: 6}),
        makePerf({wasInMyTeam: true, wasBenched: true, points: 3}),
      ]);
      expect(comp.getSelectedGwPoints()).toBe(9);
    });
  });

  describe('getFilteredPerformances', () => {
    it('should return only wasInMyTeam performances', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true}),
        makePerf({wasInMyTeam: false}),
        makePerf({wasInMyTeam: true}),
      ]);
      expect(comp.getFilteredPerformances().length).toBe(2);
    });
  });

  describe('getDisplayedPerformances', () => {
    it('should return only in-team performances when showMissedGWs is false', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true,  gameWeek: 3}),
        makePerf({wasInMyTeam: false, gameWeek: 1}),
        makePerf({wasInMyTeam: true,  gameWeek: 2}),
      ]);
      comp.showMissedGWs = false;
      const result = comp.getDisplayedPerformances();
      expect(result.length).toBe(2);
      expect(result.every(p => p.wasInMyTeam)).toBeTrue();
    });

    it('should return all performances sorted by gameWeek when showMissedGWs is true', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true,  gameWeek: 3}),
        makePerf({wasInMyTeam: false, gameWeek: 1}),
        makePerf({wasInMyTeam: true,  gameWeek: 2}),
      ]);
      comp.showMissedGWs = true;
      const result = comp.getDisplayedPerformances();
      expect(result.length).toBe(3);
      expect(result.map(p => p.gameWeek)).toEqual([1, 2, 3]);
    });

    it('should sort in-team-only results by gameWeek ascending', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true, gameWeek: 5}),
        makePerf({wasInMyTeam: true, gameWeek: 2}),
      ]);
      comp.showMissedGWs = false;
      expect(comp.getDisplayedPerformances().map(p => p.gameWeek)).toEqual([2, 5]);
    });
  });

  describe('getMissedPoints', () => {
    it('should sum points from not-in-team performances', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true,  points: 10}),
        makePerf({wasInMyTeam: false, points: 6}),
        makePerf({wasInMyTeam: false, points: 8}),
      ]);
      expect(comp.getMissedPoints()).toBe(14);
    });

    it('should return 0 when player was always in team', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true, points: 10}),
      ]);
      expect(comp.getMissedPoints()).toBe(0);
    });
  });

  describe('getMissedGWCount', () => {
    it('should count performances where wasInMyTeam is false', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true}),
        makePerf({wasInMyTeam: false}),
        makePerf({wasInMyTeam: false}),
      ]);
      expect(comp.getMissedGWCount()).toBe(2);
    });

    it('should return 0 when player was always in team', () => {
      const comp = makeComponent(Position.MID, [
        makePerf({wasInMyTeam: true}),
      ]);
      expect(comp.getMissedGWCount()).toBe(0);
    });
  });
});
