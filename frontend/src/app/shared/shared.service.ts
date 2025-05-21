import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SharedService {
  pageLoader?: HTMLElement;

  constructor() {
    this.pageLoader = document.getElementById('page-loader') as HTMLElement;
  }

  /**
   * Removes the loader by adding the 'loader--hidden' class to the loader element.
   */
  public removeLoader(): void {
    this.pageLoader?.classList.add('loader--hidden');
  }

  /**
   * Displays the loader by removing the 'loader--hidden' class from the loader element.
   */
  public displayPageLoader(): void {
    this.pageLoader?.classList.remove('loader--hidden');
  }


}
