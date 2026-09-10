import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { LoaderService } from './loader.service';

/**
 * HTTP interceptor that shows and hides the global loader around every request.
 * Requests with the X-Skip-Loader header bypass the loader entirely.
 */
@Injectable()
export class LoaderInterceptor implements HttpInterceptor {
  constructor(private readonly loader: LoaderService) {}

  /**
   * Intercepts outgoing HTTP requests to manage the global loading state.
   *
   * @param req - The outgoing HTTP request.
   * @param next - The next handler in the interceptor chain.
   * @returns An observable of the HTTP event stream.
   */
  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (req.headers.has('X-Skip-Loader')) {
      return next.handle(req);
    }
    this.loader.show();
    return next.handle(req).pipe(finalize(() => this.loader.hide()));
  }
}
