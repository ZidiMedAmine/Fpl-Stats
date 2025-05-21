import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {TeamPlayersRoutingModule} from './routing/team-players-routing.module';
import {TeamPlayersComponent} from './playeer-list/team-players.component';
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatTableModule} from "@angular/material/table";
import {MatToolbarModule} from "@angular/material/toolbar";
import {PlayerImagePipe} from "../../core/pipes/player-image.pipe";
import { PlayerDetailsComponent } from './player-details/player-details.component';
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import {MatTooltipModule} from "@angular/material/tooltip";
import { PerformanceChartsComponent } from './performance-charts/performance-charts.component';
import {NgChartsModule} from "ng2-charts";


@NgModule({
  declarations: [
    TeamPlayersComponent,PlayerImagePipe, PlayerDetailsComponent, PerformanceChartsComponent
  ],
  exports:[PlayerImagePipe],
  imports: [
    CommonModule,
    MatTableModule,
    MatToolbarModule,
    MatProgressSpinnerModule,
    TeamPlayersRoutingModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    MatTooltipModule,
    NgChartsModule,
  ]
})
export class TeamPlayersModule { }
