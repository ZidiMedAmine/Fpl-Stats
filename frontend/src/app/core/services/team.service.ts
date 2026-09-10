import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map, shareReplay} from 'rxjs/operators';
import {UserInfo} from '../models/UserInfo.model';
import {CompareResult} from '../models/compare.model';
import {environment} from '../../../environments/environment';

/**
 * Service responsible for fetching and caching FPL team data.
 */
@Injectable({
  providedIn: 'root'
})
export class TeamService {
  private readonly apiUrl = `${environment.apiBaseUrl}/fpl`;
  private readonly syncUrl = `${environment.apiBaseUrl}/fpl-sync`;

  private readonly teamCache = new Map<number, Observable<UserInfo>>();
  private readonly compareCache = new Map<string, Observable<CompareResult>>();

  constructor(private readonly http: HttpClient) {}

  /**
   * Triggers a backend sync for the given FPL team and clears its cached data.
   *
   * @param teamId - The FPL team ID to sync.
   * @returns An observable that completes when the sync is done.
   */
  syncUserTeam(teamId: number): Observable<void> {
    this.teamCache.delete(teamId);
    return this.http.post(`${this.syncUrl}/user-teams/${teamId}`, null, {responseType: 'text'}).pipe(
      map(() => void 0)
    );
  }

  /**
   * Returns the team players and info for the given FPL team ID.
   * Results are cached per team ID.
   *
   * @param teamId - The FPL team ID to fetch.
   * @returns An observable emitting the {@link UserInfo}.
   */
  getTeamPlayers(teamId: number): Observable<UserInfo> {
    if (!this.teamCache.has(teamId)) {
      this.teamCache.set(teamId,
        this.http.get<UserInfo>(`${this.apiUrl}/user-info/${teamId}`).pipe(shareReplay(1))
      );
    }
    return this.teamCache.get(teamId)!;
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
