import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ComparePlayersComponent } from './compare-players.component';

const routes: Routes = [
  { path: '', component: ComparePlayersComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ComparePlayersRoutingModule {}
