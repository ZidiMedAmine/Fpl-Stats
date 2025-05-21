import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SharedService {
  pageLoader?: HTMLElement;
   userName :BehaviorSubject<string> = new BehaviorSubject<string>("");
  teamName :BehaviorSubject<string> = new BehaviorSubject<string>("");
  totalPoints :BehaviorSubject<number> = new BehaviorSubject<number>(0);
  overallRank :BehaviorSubject<number> = new BehaviorSubject<number>(0);

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

  /**
   * Updates the overall rank asynchronously using a zero-delay `setTimeout`.
   *
   * This defers the call to `setOverallRanks` to the next JavaScript event loop tick,
   * ensuring that any required change detection or rendering has completed beforehand.
   *
   * @param _overallRank - The new overall rank to be passed to `setOverallRanks`.
   */
  public updateOverallRank(_overallRank: number): void {
    setTimeout(() => {
      this.setOverallRanks(_overallRank);
    }, 0);
  }

  /**
   * Emits a new value for the `overallRank` observable.
   *
   * @param overallRank - The new overall rank to emit.
   */
  private setOverallRanks(overallRank: number): void {
    this.overallRank.next(overallRank);
  }

  /**
   * Updates the total points asynchronously using a zero-delay `setTimeout`.
   *
   * This defers the execution of `setTotalPoints` to the next JavaScript event loop cycle,
   * ensuring it runs after the current synchronous code completes, which may help avoid
   * timing issues with Angular's change detection or rendering.
   *
   * @param _totalPoints - The new total points value to be passed to `setTotalPoints`.
   */
  public updateTotalPoints(_totalPoints: number): void {
    setTimeout(() => {
      this.setTotalPoints(_totalPoints);
    }, 0);
  }

  /**
   * Emits a new value for the `totalPoints` observable.
   *
   * @param totalPoints - The new total points value to emit.
   */
  private setTotalPoints(totalPoints: number): void {
    this.totalPoints.next(totalPoints);
  }

  /**
   * Asynchronously updates the user's name using a zero-delay `setTimeout`.
   *
   * This defers the execution of `setUserName` to the next JavaScript event loop tick,
   * which can help avoid timing issues related to Angular's change detection or UI rendering.
   *
   * @param _userName - The new username to update.
   */
  public updateUserName(_userName: string): void {
    setTimeout(() => {
      this.setUserName(_userName);
    }, 0);
  }

  /**
   * Emits a new value for the `userName` observable.
   *
   * @param userName - The new userName to emit.
   */
  private setUserName(userName: string): void {
    this.userName.next(userName);
  }

  /**
   * Asynchronously updates the team name using a zero-delay `setTimeout`.
   *
   * This defers the call to `setTeamName` to the next event loop cycle,
   * which can help prevent timing issues with Angular's change detection.
   *
   * @param _teamName - The new team name to update.
   */
  public updateTeamName(_teamName: string): void {
    setTimeout(() => {
      this.setTeamName(_teamName);
    }, 0);
  }

  /**
   * Emits a new value for the `teamName` observable.
   *
   * @param teamName - The new team name to emit.
   */
  private setTeamName(teamName: string): void {
    this.teamName.next(teamName);
  }
}
