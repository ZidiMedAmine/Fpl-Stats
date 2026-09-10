import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { PlayerSummary } from '../models/player-summary.model';
import { PlayerDetail } from '../models/player-detail.model';

/**
 * Service responsible for fetching and caching player comparison data.
 */
@Injectable({
  providedIn: 'root'
})
export class PlayerComparisonService {
  private readonly apiUrl = `${environment.apiBaseUrl}/fpl`;

  private readonly positionCache = new Map<string, Observable<PlayerSummary[]>>();
  private readonly playerDetailCache = new Map<number, Observable<PlayerDetail>>();

  constructor(private readonly http: HttpClient) {}

  /**
   * Returns all players for the given position.
   * Results are cached per position.
   *
   * @param position - The player position (e.g. GKP, DEF, MID, FWD).
   * @returns An observable emitting the list of {@link PlayerSummary}.
   */
  getPlayersByPosition(position: string): Observable<PlayerSummary[]> {
    if (!this.positionCache.has(position)) {
      this.positionCache.set(position,
        this.http.get<PlayerSummary[]>(`${this.apiUrl}/players`, {
          params: { position }
        }).pipe(shareReplay(1))
      );
    }
    return this.positionCache.get(position)!;
  }

  /**
   * Returns the full detail for a player by their FPL ID.
   * Results are cached per player ID.
   *
   * @param fplId - The FPL player ID.
   * @returns An observable emitting the {@link PlayerDetail}.
   */
  getPlayerDetail(fplId: number): Observable<PlayerDetail> {
    if (!this.playerDetailCache.has(fplId)) {
      this.playerDetailCache.set(fplId,
        this.http.get<PlayerDetail>(`${this.apiUrl}/players/${fplId}`).pipe(shareReplay(1))
      );
    }
    return this.playerDetailCache.get(fplId)!;
  }
}
