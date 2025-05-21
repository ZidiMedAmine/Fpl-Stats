import {Component, Inject, OnInit} from '@angular/core';
import {Player} from "../../../core/models/player.model";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {GameWeekPerformance} from "../../../core/models/game-week-performance.model";

@Component({
  selector: 'app-player-details',
  templateUrl: './player-details.component.html',
  styleUrls: ['./player-details.component.scss']
})
export class PlayerDetailsComponent implements OnInit {
  displayedColumns: string[] = [];

  constructor(@Inject(MAT_DIALOG_DATA) public data: { player: Player }) {}

  ngOnInit(): void {
    const hasCaptaincy = this.data.player.performances.some(p =>
      p.wasCaptain || p.wasViceCaptain || p.wasTripleCaptain
    );

    this.displayedColumns = ['gameWeek'];
    if (hasCaptaincy) {
      this.displayedColumns.push('captaincy');
    }
    this.displayedColumns.push('points');

    if (hasCaptaincy) {
      this.displayedColumns.push('bonus');
    }

    if(this.data.player.position !== 'Manager'){
      this.displayedColumns.push('goals');
      this.displayedColumns.push('assists');
      if(this.data.player.position !== 'FWD'){
        this.displayedColumns.push('cleanSheet');
      }
      this.displayedColumns.push('minutes');
    }
    this.displayedColumns.push('cards');
  }

  /**
   * Returns an array of performances where the player was part of the user's team.
   *
   * @returns An array of performance objects filtered to those where `wasInMyTeam` is true.
   */
  getFilteredPerformances() {
    return this.data.player.performances.filter(perf => perf.wasInMyTeam);
  }

  /**
   * Calculates the total points accumulated from performances
   * where the player was benched.
   *
   * @returns The sum of points from all benched performances.
   */
  getBenchedPoints(): number {
    return this.getFilteredPerformances()
      .filter(p => p.wasBenched)
      .reduce((sum, p) => sum + p.points, 0);
  }

  /**
   * Calculates the total points for the selected game week where the player
   * was part of the team, considering manager-specific bench rules:
   *
   * - For players in the 'Manager' position, all performances where they were in the team count.
   * - For other players, only performances where they were not benched count.
   *
   * @returns The sum of points from selected performances in the game week.
   */
  getSelectedGwPoints(): number {
    return this.getFilteredPerformances()
      .filter(p => p.wasInMyTeam && (this.data.player?.position === 'Manager' ? true : !p.wasBenched))
      .reduce((sum, p) => sum + p.points, 0);
  }

  /**
   * Calculates the effective points for a performance,
   * applying multipliers for captain and triple captain status.
   *
   * @param perf - The performance object containing points and captaincy flags.
   * @returns The adjusted points considering captain multipliers.
   */
  calculatePoints(perf: GameWeekPerformance): number {
    if (perf.wasTripleCaptain) {
      return perf.points * 3;
    } else if (perf.wasCaptain) {
      return perf.points * 2;
    }
    return perf.points;
  }
}
