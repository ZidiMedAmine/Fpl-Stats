import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserInfo} from "../models/UserInfo.model";

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  protected apiUrl = 'http://localhost:8080/api/fpl';

  constructor(protected http: HttpClient) { }

  getTeamPlayers(teamId: number): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.apiUrl}/team-players/${teamId}`);
  }
}
