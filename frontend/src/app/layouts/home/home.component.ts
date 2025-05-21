import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  fplId = '';

  constructor(protected router: Router) {}

  /**
   * Navigates to the team players page using the provided FPL ID.
   *
   * If `fplId` is defined, the router navigates to the `/team-players/:fplId` route.
   */
  searchTeam(): void {
    if (this.fplId) {
      this.router.navigate(['/team-players', this.fplId]).then();
    }
  }
}
