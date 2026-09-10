import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PositionBadgeComponent } from './components/position-badge/position-badge.component';
import { SeasonHeatmapComponent } from './components/season-heatmap/season-heatmap.component';
import { LoadingOverlayComponent } from './components/loading-overlay/loading-overlay.component';
import { PlayerImagePipe } from '../core/pipes/player-image.pipe';
import { PlayerImageDirective } from '../core/directives/player-image.directive';

@NgModule({
  declarations: [
    PositionBadgeComponent,
    SeasonHeatmapComponent,
    LoadingOverlayComponent,
    PlayerImagePipe,
    PlayerImageDirective,
  ],
  imports: [CommonModule],
  exports: [
    PositionBadgeComponent,
    SeasonHeatmapComponent,
    LoadingOverlayComponent,
    PlayerImagePipe,
    PlayerImageDirective,
  ]
})
export class SharedModule {}
