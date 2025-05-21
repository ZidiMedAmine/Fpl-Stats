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

  searchTeam() {
    if (this.fplId) {
      this.router.navigate(['/team-players', this.fplId]).then();
    }
  }
}
