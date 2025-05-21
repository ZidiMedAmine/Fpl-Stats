import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserInfo} from "../models/UserInfo.model";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  protected apiUrl = `${environment.apiBaseUrl}/fpl`;

  constructor(protected http: HttpClient) {}

  getTeamPlayers(teamId: number): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.apiUrl}/team-players/${teamId}`);
  }
}
