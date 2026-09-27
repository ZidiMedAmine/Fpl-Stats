import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map, shareReplay} from 'rxjs/operators';
import {TeamChartData, UserTeamSummary} from '../models/UserInfo.model';
import {CompareResult} from '../models/compare.model';
import {TransferImpact} from '../models/transfer-impact.model';
import {DreamTeam} from '../models/dream-team.model';
import {environment} from '../../../environments/environment';

/**
 * Service responsible for fetching and caching FPL team data.
 */
@Injectable({
  providedIn: 'root'
})
export class TeamService {
  private readonly syncUrl = `${environment.apiBaseUrl}/fpl-sync`;
  private readonly apiUrl = `${environment.apiBaseUrl}/fpl`;

  private readonly summaryCache = new Map<number, Observable<UserTeamSummary>>();
  private readonly chartDataCache = new Map<number, Observable<TeamChartData>>();
  private readonly compareCache = new Map<string, Observable<CompareResult>>();
  private dreamTeamCache: Observable<DreamTeam> | null = null;

  constructor(private readonly http: HttpClient) {}

  /**
   * Triggers a backend sync for the given FPL team and clears its cached data.
   *
   * @param teamId - The FPL team ID to sync.
   * @returns An observable that completes when the sync is done.
   */
  syncUserTeam(teamId: number): Observable<void> {
    this.summaryCache.delete(teamId);
    this.chartDataCache.delete(teamId);
    return this.http.post(`${this.syncUrl}/user-teams/${teamId}`, null, {responseType: 'text'}).pipe(
      map(() => void 0)
    );
  }

  /**
   * Returns the lightweight team summary for the given FPL team ID.
   * This is the fast endpoint — no player history is loaded.
   * Results are cached per team ID.
   *
   * @param teamId - The FPL team ID to fetch.
   * @returns An observable emitting the {@link UserTeamSummary}.
   */
  getTeamSummary(teamId: number): Observable<UserTeamSummary> {
    if (!this.summaryCache.has(teamId)) {
      this.summaryCache.set(teamId,
        this.http.get<UserTeamSummary>(`${this.apiUrl}/user-info/${teamId}`).pipe(shareReplay(1))
      );
    }
    return this.summaryCache.get(teamId)!;
  }

  /**
   * Returns the full chart data for the given FPL team ID.
   * This is the slow endpoint — loads all player histories.
   * Results are cached per team ID.
   *
   * @param teamId - The FPL team ID to fetch.
   * @returns An observable emitting the {@link TeamChartData}.
   */
  getTeamChartData(teamId: number): Observable<TeamChartData>   {
    if (!this.chartDataCache.has(teamId)) {
      this.chartDataCache.set(teamId,
        this.http.get<TeamChartData>(`${this.apiUrl}/user-info/${teamId}/chart-data`).pipe(shareReplay(1))
      );
    }
    return this.chartDataCache.get(teamId)!;
  }

  /**
   * Returns the transfer impact summary for the given FPL team ID.
   *
   * @param teamId - The FPL team ID to fetch transfer impact for.
   * @returns An observable emitting the {@link TransferImpact}.
   */
  getTransferImpact(teamId: number): Observable<TransferImpact> {
    return this.http.get<TransferImpact>(`${this.apiUrl}/user-info/${teamId}/transfer-impact`);
  }

  /**
   * Returns the all-time and last-5-weeks dream team lineups.
   * Result is cached for the lifetime of the service instance.
   *
   * @returns An observable emitting the {@link DreamTeam}.
   */
  getDreamTeam(): Observable<DreamTeam> {
    if (!this.dreamTeamCache) {
      this.dreamTeamCache = this.http.get<DreamTeam>(`${this.apiUrl}/dream-team`).pipe(shareReplay(1));
    }
    return this.dreamTeamCache;
  }

  /**
   * Returns the comparison result for two FPL teams.
   * Results are cached per team ID pair (order-independent).
   *
   * @param team1Id - The first FPL team ID.
   * @param team2Id - The second FPL team ID.
   * @returns An observable emitting the {@link CompareResult}.
   */
  compareTeams(team1Id: number, team2Id: number): Observable<CompareResult> {
    const key = [team1Id, team2Id].sort().join('-');
    if (!this.compareCache.has(key)) {
      this.compareCache.set(key,
        this.http.get<CompareResult>(`${this.apiUrl}/compare`, {
          params: {fplTeamId1: team1Id.toString(), fplTeamId2: team2Id.toString()}
        }).pipe(shareReplay(1))
      );
    }
    return this.compareCache.get(key)!;
  }
}
