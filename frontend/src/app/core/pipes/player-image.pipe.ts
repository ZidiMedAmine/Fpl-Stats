import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'playerImage'
})
export class PlayerImagePipe implements PipeTransform {
  transform(photoCode: string): string {
    return `https://resources.premierleague.com/premierleague/photos/players/110x140/${photoCode}.png`;
  }
}
