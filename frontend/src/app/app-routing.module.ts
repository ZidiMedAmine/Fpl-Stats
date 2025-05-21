import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: 'team-players',
    loadChildren: () =>
      import('./modules/team-players/team-players.module').then(m => m.TeamPlayersModule)
  },
  { path: '', redirectTo: 'team-players/3514734', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
