import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TeamPlayersComponent } from '../player-list/team-players.component';

const routes: Routes = [{ path: ':id', component: TeamPlayersComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TeamPlayersRoutingModule { }
