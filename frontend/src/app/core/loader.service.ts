import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

/**
 * Manages the global loading state for HTTP requests.
 * Uses a counter to correctly handle multiple concurrent requests.
 */
@Injectable({ providedIn: 'root' })
export class LoaderService {
  private count = 0;
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  /** Observable emitting true while at least one HTTP request is in progress. */
  readonly loading$: Observable<boolean> = this.loadingSubject.asObservable();

  /**
   * Increments the active request counter and shows the loader.
   */
  show(): void {
    this.count++;
    this.loadingSubject.next(true);
  }

  /**
   * Decrements the active request counter and hides the loader when all requests complete.
   */
  hide(): void {
    this.count = Math.max(0, this.count - 1);
    if (this.count === 0) {
      this.loadingSubject.next(false);
    }
  }
}
