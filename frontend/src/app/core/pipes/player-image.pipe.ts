import {Pipe, PipeTransform} from '@angular/core';
import {FPL_PLAYER_IMAGE_PRIMARY_URL} from '../fpl-api.constants';

/**
 * Transforms a player code into the Premier League player image URL.
 */
@Pipe({
  name: 'playerImage'
})
export class PlayerImagePipe implements PipeTransform {
  /**
   * Returns the player image URL for the given player code.
   *
   * @param code - The Premier League player photo code.
   * @returns The full image URL string.
   */
  transform(code: number): string {
    return FPL_PLAYER_IMAGE_PRIMARY_URL(code);
  }
}
