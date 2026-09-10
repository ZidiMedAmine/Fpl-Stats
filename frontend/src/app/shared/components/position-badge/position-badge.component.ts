import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-position-badge',
  template: `<span class="position-badge" [class]="'pos-' + position.toLowerCase()">{{ position }}</span>`,
  styles: [`
    .position-badge {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 12px;
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 0.4px;
      color: #fff;
      text-transform: uppercase;
    }
    .pos-gkp { background: var(--color-gkp); color: #333; }
    .pos-def { background: var(--color-def); }
    .pos-mid { background: var(--color-mid); }
    .pos-fwd { background: var(--color-fwd); }
    .pos-manager { background: #9c27b0; }
  `]
})
export class PositionBadgeComponent {
  @Input() position: string = '';
}
