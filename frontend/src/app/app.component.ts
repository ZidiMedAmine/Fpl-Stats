import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LoaderService } from './core/loader.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    protected readonly loader: LoaderService,
    private readonly router: Router
  ) {}

  /** Shows the global loader on navigation start and hides it when navigation settles. */
  public ngOnInit(): void {
    this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(event => {
      if (event instanceof NavigationStart) {
        this.loader.show();
      } else if (event instanceof NavigationEnd || event instanceof NavigationCancel || event instanceof NavigationError) {
        this.loader.hide();
      }
    });
  }
}
