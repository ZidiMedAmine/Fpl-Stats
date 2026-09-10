import { Directive, ElementRef, HostListener, Input, OnChanges } from '@angular/core';
import { FPL_PLAYER_IMAGE_FALLBACK_URL, FPL_PLAYER_IMAGE_PRIMARY_URL } from '../fpl-api.constants';

/**
 * Directive that sets a player image on an img element with automatic fallback
 * to a legacy URL if the primary image fails to load.
 */
@Directive({ selector: 'img[appPlayerImage]' })
export class PlayerImageDirective implements OnChanges {

  /** The Premier League player photo code used to build the image URL. */
  @Input() appPlayerImage!: number;

  private triedFallback = false;

  constructor(private readonly el: ElementRef<HTMLImageElement>) {}

  /**
   * Resets the fallback state and sets the primary image URL whenever the input changes.
   */
  ngOnChanges(): void {
    this.triedFallback = false;
    this.el.nativeElement.src = FPL_PLAYER_IMAGE_PRIMARY_URL(this.appPlayerImage);
  }

  /**
   * Falls back to the legacy image URL on the first load error.
   * Subsequent errors are ignored to prevent infinite retry loops.
   */
  @HostListener('error')
  onError(): void {
    if (!this.triedFallback) {
      this.triedFallback = true;
      this.el.nativeElement.src = FPL_PLAYER_IMAGE_FALLBACK_URL(this.appPlayerImage);
    }
  }
}
