import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SharedService {
  readonly userName$ = new BehaviorSubject<string>('');
  readonly teamName$ = new BehaviorSubject<string>('');
  readonly totalPoints$ = new BehaviorSubject<number>(0);
  readonly overallRank$ = new BehaviorSubject<number>(0);

  /** Emits a new overall rank value to all subscribers. */
  public updateOverallRank(overallRank: number): void {
    this.overallRank$.next(overallRank);
  }

  /** Emits a new total points value to all subscribers. */
  public updateTotalPoints(totalPoints: number): void {
    this.totalPoints$.next(totalPoints);
  }

  /** Emits a new user name value to all subscribers. */
  public updateUserName(userName: string): void {
    this.userName$.next(userName);
  }

  /** Emits a new team name value to all subscribers. */
  public updateTeamName(teamName: string): void {
    this.teamName$.next(teamName);
  }
}
