# FPL Stats — Backend Standards

> Quick-reference for Claude. Authoritative rules for this project.

## Architecture

| Layer | Package | Responsibility |
|---|---|---|
| Controllers | `controller` | HTTP endpoints only — zero business logic |
| Services (interfaces) | `services` | Business logic contracts |
| Services (impl) | `services.impl` | Business logic — only layer allowed to use `@Transactional` |
| Sync Services | `services.fpl.sync` | FPL API → DB synchronization |
| FPL API Services | `services.fpl` | FPL API query helpers (cached) |
| Repositories | `repository` | Persistence only — no logic beyond queries |
| Entities | `domain` | JPA entities only — extend `BaseEntity` |
| DTOs | `services.dto` | Plain data carriers — no JPA annotations |
| Mappers | `services.mapper` | Entity <> DTO conversion (manual, no MapStruct) |
| Config | `config` | Spring `@Configuration` beans only |
| Exceptions | `exception` | Custom exceptions |
| Utils | `services.util` | Stateless helper methods |

**Hard rules:**
- Controllers must never contain business logic or repository calls
- `@Transactional` belongs only in the service layer — never in controllers or repositories
- Never use field injection (`@Autowired` on fields) — always use constructor injection
- Mark injected fields `private final`
- Query endpoints read from the database only — zero FPL API calls on the read path
- Sync services write to the database — triggered by scheduler or manual POST endpoints

## Naming Conventions

- Controllers: `FooController`
- Services: `FooService` (interface) + `FooServiceImpl` (implementation)
- Sync services: `FooSyncService`
- Repositories: `FooRepository`
- Entities: `Foo` (no suffix), extends `BaseEntity`
- DTOs: `FooDto`
- Mappers: `FooMapper`

**Method / parameter naming:**
- Never use generic names like `dto`, `entity`, `obj`, `request`, `data`, `item` as parameter or variable names
- Name parameters after their type: `PlayerDto` -> `playerDto`, `UUID id` -> `playerId`
- Local variables holding results must describe what they represent, not `result` or `saved`

## Entity Rules

- All entities extend `BaseEntity` (UUID PK + `createdAt`/`updatedAt` audit timestamps)
- All FPL IDs must have `@Column(nullable = false, unique = true)`
- Junction-like entities (PlayerHistory, UserPick) must have composite `@UniqueConstraint`
- Use `FetchType.LAZY` by default — never EAGER unless explicitly justified
- Use `@EntityGraph` or `JOIN FETCH` in repository queries when eager loading is needed
- `equals()`/`hashCode()` use FPL business key only — never relationships or mutable fields
- Numeric FPL IDs are `int` or `long` — never `String`

## Database

- Flyway migrations in `src/main/resources/db/migration/`
- Migration naming: `V1__description.sql`, `V2__description.sql`, etc.
- Hibernate batch inserts enabled (`batch_size=50`)
- Always use `saveAll()` for bulk operations — never `save()` in a loop

## Caching

- Caffeine in-memory cache via Spring `@Cacheable`
- `BootstrapDataService` is the single cached source for `/bootstrap-static/` — never call this endpoint directly
- Use `@CacheEvict` after sync operations to invalidate stale data
- Cache TTL: 10 minutes for bootstrap data

## FPL API Integration

- Base URL: `https://fantasy.premierleague.com/api`
- All external calls go through `FplApiClient`
- Global sync services (Player, Team, GameWeek) receive data as parameters — they don't fetch from the API
- User-specific sync services (UserTeam, UserPick) fetch their own data per-user
- `PlayerHistorySyncService` uses parallel batching for `/element-summary/` calls
- Triple captain week is fetched once per user sync, not per pick

## Clean Code

- Small functions (max 20 lines)
- Clear, intention-revealing  
- No magic numbers — use named constants
- Avoid deep nesting
- No duplicated code (DRY)
- Prefer readability over cleverness (KISS)
- Use SLF4J: `private static final Logger log = LoggerFactory.getLogger(Foo.class);` — never `System.out`

## Documentation

- JavaDoc on **all methods and classes** — public and private
- Minimum: one-line summary + `@param` / `@return` / `@throws` where applicable
- Getters, setters, and builders do not require JavaDoc
- On `@Override` methods in `*ServiceImpl`, use `/** {@inheritDoc} */` instead of duplicating the interface's JavaDoc

## Security
- Never log passwords, tokens, OTP codes, or secrets
- Store secrets only in environment variables — never hardcode them in code or config files

## Git Commit Message Format

Follow Conventional Commits:
```
feat: add player sync scheduler
fix: resolve null pointer in pick sync
refactor: simplify bootstrap data caching
```

## Testing

- Every new feature must include tests for the main success scenario and important failure cases
- Use `@DataJpaTest` for repository tests
- Use `@SpringBootTest` for integration tests
- Test names describe expected behavior: `shouldReturnPlayerWhenFplIdExists()`
