import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {MatTableModule} from '@angular/material/table';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatDialogModule} from '@angular/material/dialog';
import {BaseChartDirective} from 'ng2-charts';

import {CompareTeamsComponent} from './compare-teams.component';
import {SharedModule} from "../../shared/shared.module";

const routes: Routes = [
  {path: '', component: CompareTeamsComponent},
];

@NgModule({
  declarations: [CompareTeamsComponent],
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatSnackBarModule,
    RouterModule.forChild(routes),
    SharedModule,
    BaseChartDirective,
    MatDialogModule,
  ],
  exports: [CompareTeamsComponent],
})
export class CompareTeamsModule {}
