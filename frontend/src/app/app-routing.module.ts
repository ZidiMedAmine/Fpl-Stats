import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {HomeComponent} from "./layouts/home/home.component";

const routes: Routes = [
  { path: '', component: HomeComponent },
  {
    path: 'team-players',
    loadChildren: () =>
      import('./modules/team-players/team-players.module').then(m => m.TeamPlayersModule)
  },
  {
    path: 'compare',
    loadChildren: () =>
      import('./modules/compare-teams/compare-teams.module').then(m => m.CompareTeamsModule)
  },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
