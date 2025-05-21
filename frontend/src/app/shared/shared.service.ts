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

  public updateOverallRank(_overallRank: number): void {
    setTimeout(() => {
      this.setOverallRanks(_overallRank);
    }, 0);
  }

  private setOverallRanks(overallRank: number): void {
    this.overallRank.next(overallRank);
  }

  public updateTotalPoints(_totalPoints: number): void {
    setTimeout(() => {
      this.setTotalPoints(_totalPoints);
    }, 0);
  }

  private setTotalPoints(totalPoints: number): void {
    this.totalPoints.next(totalPoints);
  }

  public updateUserName(_userName: string): void {
    setTimeout(() => {
      this.setUserName(_userName);
    }, 0);
  }

  private setUserName(userName: string): void {
    this.userName.next(userName);
  }

  public updateTeamName(_teamName: string): void {
    setTimeout(() => {
      this.setTeamName(_teamName);
    }, 0);
  }

  private setTeamName(teamName: string): void {
    this.teamName.next(teamName);
  }

}
