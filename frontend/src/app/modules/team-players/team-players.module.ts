import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule} from '@angular/router';

import {TeamPlayersRoutingModule} from './routing/team-players-routing.module';
import {TeamPlayersComponent} from './player-list/team-players.component';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatTableModule} from "@angular/material/table";
import {MatToolbarModule} from "@angular/material/toolbar";
import { PlayerDetailsComponent } from './player-details/player-details.component';
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import { PerformanceChartsComponent } from './performance-charts/performance-charts.component';
import {BaseChartDirective} from "ng2-charts";
import {SharedModule} from "../../shared/shared.module";
import {ComparePlayersModule} from "../compare-players/compare-players.module";
import {CompareTeamsModule} from "../compare-teams/compare-teams.module";


@NgModule({
  declarations: [
    TeamPlayersComponent, PlayerDetailsComponent, PerformanceChartsComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatToolbarModule,
    MatProgressSpinnerModule,
    TeamPlayersRoutingModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    MatTooltipModule,
    MatSnackBarModule,
    BaseChartDirective,
    SharedModule,
    ComparePlayersModule,
    CompareTeamsModule,
  ]
})
export class TeamPlayersModule { }
