import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SharedService {
  private readonly pageLoader?: HTMLElement;
  readonly userName$ = new BehaviorSubject<string>('');
  readonly teamName$ = new BehaviorSubject<string>('');
  readonly totalPoints$ = new BehaviorSubject<number>(0);
  readonly overallRank$ = new BehaviorSubject<number>(0);

  constructor() {
    this.pageLoader = document.getElementById('page-loader') as HTMLElement;
  }

  /**
   * Removes the loader by adding the 'loader--hidden' class to the loader element.
   */
  public removeLoader(): void {
    this.pageLoader?.classList.add('loader--hidden');
  }

  /**
   * Displays the loader by removing the 'loader--hidden' class from the loader element.
   */
  public displayPageLoader(): void {
    this.pageLoader?.classList.remove('loader--hidden');
  }

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
