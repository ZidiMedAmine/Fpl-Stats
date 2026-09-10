import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

const RECENT_KEY = 'fpl_recent_searches';

interface RecentSearch {
  fplId: string;
  teamName: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  fplId = '';
  recentSearches: RecentSearch[] = [];

  constructor(protected router: Router) {}

  ngOnInit(): void {
    this.recentSearches = this.loadRecent();
  }

  searchTeam(): void {
    if (this.fplId) {
      this.router.navigate(['/team-players', this.fplId]).then();
    }
  }

  openRecent(search: RecentSearch): void {
    this.router.navigate(['/team-players', search.fplId]).then();
  }

  saveRecent(fplId: string, teamName: string): void {
    const existing = this.loadRecent().filter(r => r.fplId !== fplId);
    const updated = [{ fplId, teamName }, ...existing].slice(0, 3);
    localStorage.setItem(RECENT_KEY, JSON.stringify(updated));
  }

  private loadRecent(): RecentSearch[] {
    try {
      return JSON.parse(localStorage.getItem(RECENT_KEY) ?? '[]');
    } catch {
      return [];
    }
  }
}
