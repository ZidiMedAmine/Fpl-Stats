# FPL Stats — Frontend Standards

> Quick-reference for Claude. Authoritative rules for this Angular 18.2 project.

---

## Project Stack

| Tool | Version |
|------|---------|
| Angular | 18.2 |
| Angular Material | 18.2 |
| Chart.js / ng2-charts | 4.x / 6.x |
| RxJS | 7.8 |
| TypeScript | 5.4 |
| Testing | Karma + Jasmine |
| Linting | ESLint + angular-eslint |

---

## Architecture

This project uses **NgModule architecture** — not standalone components.

| Layer | Location | Responsibility |
|-------|----------|----------------|
| App Module | `app.module.ts` | Root module, global providers |
| Feature Modules | `modules/` | Lazy-loaded feature areas |
| Core | `core/` | Singleton services, interceptors, guards |
| Shared | `shared/` | Shared components, pipes, directives |
| Layouts | `layouts/` | Header, footer, home |

**Hard rules:**
- New features go in `modules/` as lazy-loaded `NgModule`s
- Singleton services go in `core/` with `providedIn: 'root'`
- Shared UI components go in `shared/`
- Components only render UI — zero business logic
- Services handle all business logic and HTTP calls

---

## NgModule Rules

- Declare components in the feature module they belong to
- Import `SharedModule` in feature modules that need shared components
- Never import `BrowserModule` outside `AppModule` — use `CommonModule` instead
- Keep module imports lean — only import what the module actually uses
- Lazy-load all feature modules via the router

```ts
{
  path: 'team',
  loadChildren: () => import('./modules/team-players/team-players.module')
    .then(m => m.TeamPlayersModule)
}
```

---

## HTTP & Services

- Use `HttpClient` via constructor injection — never use `fetch` directly
- Use class-based interceptors registered with `HTTP_INTERCEPTORS`

```ts
{ provide: HTTP_INTERCEPTORS, useClass: LoaderInterceptor, multi: true }
```

- Handle errors at the service layer — map HTTP errors to domain errors
- Use `shareReplay(1)` for HTTP responses that don't change often
- Use `providedIn: 'root'` for all singleton services

---

## RxJS Patterns

- Use `Observable` for all async data from HTTP and services
- Use `AsyncPipe` in templates — avoid manual `subscribe()` in components
- Always unsubscribe from subscriptions — use `takeUntilDestroyed()` or `DestroyRef`
- Use `catchError` in service layer, not in components
- Prefer `switchMap` for cancellable requests, `mergeMap` for parallel ones
- Never nest `subscribe()` calls — use `pipe()` operators instead

```ts
this.teamService.getUserInfo(teamId).pipe(
  takeUntilDestroyed(this.destroyRef),
  catchError(error => {
    this.handleError(error);
    return EMPTY;
  })
).subscribe(userInfo => this.userInfo = userInfo);
```

---

## Angular Material

- Use Angular Material components for all UI elements — do not build custom equivalents
- Import only the Material modules needed in the feature module
- Use `MatTableModule` for data tables, `MatDialogModule` for modals
- Use `MatProgressSpinnerModule` for loading states
- Use the `LoaderInterceptor` + `LoaderService` for global loading indicator
- Follow Material theming — do not override Material styles with inline styles

---

## Charts (Chart.js / ng2-charts)

- Use `ng2-charts` `BaseChartDirective` for all charts
- Register chart types via `provideCharts(withDefaultRegisterables())` in `AppModule`
- Define chart data and options in the component class — not in the template
- Use `ChartConfiguration` types for type safety

---

## Routing

- Define routes in a dedicated `*-routing.module.ts` file per feature
- Use `ActivatedRoute` to read route params and query params
- Handle 404s with a wildcard route at the bottom of the root route array
- Use route guards (`CanActivate`) for protected routes

---

## Forms

- Prefer Reactive Forms over Template-driven Forms for anything beyond trivial inputs
- Use `FormBuilder` for concise form construction
- Type forms with `FormGroup<T>` for compile-time safety
- Extract validators into pure functions or a dedicated `validators/` folder

---

## Performance

- Use `OnPush` change detection on all components

```ts
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})
```

- Use `trackBy` in `*ngFor` to avoid unnecessary DOM re-renders

```html
<tr *ngFor="let player of players; trackBy: trackByPlayerId">
```

- Lazy-load all feature modules — never eager-load feature modules
- Avoid large inline templates — always use `templateUrl`

---

## Testing

- Use **Karma + Jasmine** (already configured)
- Use `TestBed` for component and service tests
- Test names describe expected behavior: `shouldReturnPlayerWhenIdExists()`
- Mock HTTP calls with `HttpClientTestingModule` and `HttpTestingController`
- Aim for coverage on services and state logic; for components, test interactions

---

## Code Style & Conventions

- **Strict TypeScript** — `strict: true` is enabled, keep it that way
- **No `any`** — use `unknown` and narrow types explicitly
- **No `var`** — use `const` and `let`
- **No inline `style="..."`** in HTML templates — use component SCSS instead
- **Strict equality** — always use `===` and `!==` in templates and TypeScript
- **No abbreviations** in identifiers — use full descriptive names

```
// Bad
btn, msg, usr, cfg, idx, req, res

// Good
button, message, user, config, index, request, response
```

Allowed abbreviations: `id`, `url`, `api`, `http`, `dto`

---

## Naming Conventions

- Components: `FooComponent`
- Services: `FooService`
- Pipes: `FooPipe`
- Directives: `FooDirective`
- Guards: `FooGuard`
- Interceptors: `FooInterceptor`
- Modules: `FooModule`
- File naming: `foo-bar.component.ts`, `foo-bar.service.ts`

---

## TSDoc

- **Every method — public, protected, or private — must have a TSDoc comment**
- Minimum: one-line summary. Add `@param` / `@returns` / `@throws` when non-obvious

```ts
/**
 * Fetches the user team info for the given FPL team ID.
 *
 * @param fplTeamId - The FPL team ID to fetch info for.
 * @returns An observable emitting the {@link UserTeamDto}.
 */
getUserInfo(fplTeamId: number): Observable<UserTeamDto> { … }
```

---

## Clean Code

- Methods ≤ 20 lines — extract helpers if longer
- One responsibility per class — components render, services orchestrate
- No duplicated logic — extract to shared service, pipe, or utility
- No logic in constructors — use `ngOnInit` for initialization
- Use SLF4J equivalent: `console` only in development; use proper error handling in production

---

## Security

- Never use `bypassSecurityTrust*` unless absolutely necessary
- Avoid `innerHTML` binding — use Angular template binding
- Use `HttpClient` (not raw `fetch`) so Angular's XSRF protection applies
- Never store sensitive data in `localStorage`

---

## Language

- All comments, TSDoc, and inline documentation must be in **English**
- HTML comments, SCSS comments, TypeScript comments — all English

---

## Quick Reference Checklist

| # | Rule | Priority |
|---|------|----------|
| 1 | NgModule architecture — no standalone components | Must |
| 2 | OnPush change detection on all components | Must |
| 3 | Lazy-load all feature modules | Must |
| 4 | Strict TypeScript (`strict: true`) | Must |
| 5 | Methods ≤ 20 lines (SRP) | Must |
| 6 | DRY — no duplicated logic | Must |
| 7 | TSDoc on all methods | Must |
| 8 | AsyncPipe over manual subscribe | Must |
| 9 | No inline styles | Must |
| 10 | No abbreviations in identifiers | Must |
| 11 | trackBy in all *ngFor | Should |
| 12 | HttpClientTestingModule for HTTP tests | Should |
| 13 | catchError in service layer only | Should |

---

> **Angular Version:** 18.2
> **Last Updated:** September 2026
