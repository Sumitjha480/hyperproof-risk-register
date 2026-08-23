# Risk Register

A complete full-stack implementation of the Risk Management take-home assignment: a Spring Boot API backed by PostgreSQL and a React/TypeScript dashboard for creating, scoring, mitigating, filtering, editing, and deleting risks.

The repository intentionally implements the core assignment only. Framework mappings, next-review dates, and optimistic updates are not included, but the domain, persistence, service, and DTO boundaries leave clean extension points for them.

## Run the complete application

### Prerequisite

Install Docker Desktop, or Docker Engine with Docker Compose v2.

### One command

```bash
./run.sh
```

Equivalent command on any platform:

```bash
docker compose up --build
```

Then open:

- Web application: `http://localhost:3000`
- Backend API: `http://localhost:8080/api/risks`

The Compose stack starts PostgreSQL, applies the Flyway migration, starts the API, builds the frontend, and loads a small demo register only when the database is empty.

Stop the application with:

```bash
./stop.sh
```

or:

```bash
docker compose down
```

To also remove persisted local data and return to the original demo state:

```bash
docker compose down -v
```

## Run all automated checks

```bash
./test-all.sh
```

The command runs:

1. Backend unit and API integration tests.
2. Frontend scoring tests.
3. A production frontend TypeScript/build check.

When Docker is installed, the script uses pinned Maven and Node containers. Without Docker, it falls back to the included Maven bootstrap script plus local Node/npm.

## What is implemented

### Backend

- Java 21 and Spring Boot.
- PostgreSQL persistence with a versioned Flyway migration.
- Risk create, list, get, update, and delete endpoints.
- Category and status filtering.
- Residual-score ascending or descending sorting, descending by default.
- Mitigation create, update, and delete endpoints scoped under a risk.
- Bean validation, strict JSON integer parsing, and database check constraints for every 1–5 scale.
- Structured JSON errors for validation, not-found, malformed input, and business-rule conflicts.
- Pure scoring component with boundary-focused unit tests.
- MockMvc integration tests covering the complete create-risk → add-mitigation → fetch-risk flow.

### Frontend

- React and strict TypeScript using Vite.
- Dashboard table with category, status, inherent score, residual score, and mitigation count.
- Readable Low / Medium / High / Critical color treatment.
- Category and status filters plus residual-score ordering.
- Create and edit forms with live inherent-score calculation.
- Risk detail view with ownership, lifecycle, score comparison, and mitigations.
- Add, edit, and delete mitigation controls; the detail view reloads the server-derived residual score after each mutation.
- Risk deletion and a clear empty-state experience.
- Responsive layout without a third-party component library.

## Scoring decisions

### Inherent risk

```text
inherent = likelihood × impact
```

Both inputs are integers from 1 through 5, so the result is 1 through 25.

### Residual risk

For a risk with inherent score `I` and mitigation effectiveness values `e1 ... en`:

```text
residual = max(1, ceil(I × product(1 - 0.10 × ei)))
```

Examples:

```text
Inherent 20, no mitigations       => 20
Inherent 20, one effectiveness 5  => ceil(20 × 0.50) = 10
Inherent 20, effectiveness 3 + 4  => ceil(20 × 0.70 × 0.60) = 9
```

Rationale:

- No mitigations leave residual equal to inherent.
- Each effectiveness point removes 10% of the risk still remaining, so effectiveness 5 is a meaningful 50% reduction.
- Compounding models diminishing returns and prevents several controls from producing an impossible negative score.
- Rounding upward is deliberately conservative for a compliance product; a fractional result never understates exposure.
- The final `max(1, ...)` preserves the required lower bound.

This is a deliberately transparent heuristic, not a claim that controls are statistically independent. A production product could make the model configurable by organization or framework.

### Severity bands

| Score | Band |
|---:|---|
| 1–5 | Low |
| 6–12 | Medium |
| 13–19 | High |
| 20–25 | Critical |

Scores are derived at read time rather than persisted. This prevents stale values when a mitigation changes.

## Closed-risk business rule

A risk cannot be marked `CLOSED` while it has zero mitigations. The API returns HTTP `409 Conflict` with code:

```text
RISK_CANNOT_CLOSE_WITHOUT_MITIGATION
```

For a compliance system, closure should represent an evidenced treatment decision rather than an administrative status change. Requiring at least one recorded mitigation creates a minimal audit trail and avoids presenting an untreated risk as resolved.

The same invariant is protected in reverse: the API blocks deletion of the final mitigation from a closed risk. The user must first reopen the risk, so no supported API sequence can leave a closed risk with no controls.

## API

Base path: `/api`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/risks` | Create a risk |
| `GET` | `/risks` | List risks |
| `GET` | `/risks/{riskId}` | Get risk details |
| `PUT` | `/risks/{riskId}` | Replace editable risk fields |
| `DELETE` | `/risks/{riskId}` | Delete a risk and its mitigations |
| `POST` | `/risks/{riskId}/mitigations` | Add a mitigation |
| `PUT` | `/risks/{riskId}/mitigations/{mitigationId}` | Update a mitigation |
| `DELETE` | `/risks/{riskId}/mitigations/{mitigationId}` | Delete a mitigation |

List query parameters:

```text
category=SECURITY
status=OPEN
sort=residualScore,desc
```

`sort` accepts `residualScore,desc` or `residualScore,asc`.

Example error body:

```json
{
  "timestamp": "2026-08-19T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "One or more fields are invalid",
  "path": "/api/risks",
  "fieldErrors": {
    "likelihood": "likelihood must be an integer between 1 and 5"
  }
}
```

A ready-to-use request collection is in [`requests.http`](./requests.http).

## Local development without the full Compose stack

### Backend

Requirements: Java 21 and a PostgreSQL database.

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5432/risk_register \
DB_USERNAME=risk_user \
DB_PASSWORD=risk_password \
./mvnw spring-boot:run
```

The Maven bootstrap script downloads Maven on first use when a local `mvn` command is not available.

Useful environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/risk_register` |
| `DB_USERNAME` | `risk_user` |
| `DB_PASSWORD` | `risk_password` |
| `PORT` | `8080` |
| `DEMO_DATA_ENABLED` | `false` |

### Frontend

Requirements: Node 22 recommended; Node 20.19 or newer is supported by the configured toolchain.

```bash
cd frontend
npm install
npm run dev
```

The Vite development server runs at `http://localhost:5173` and proxies `/api` to `http://localhost:8080`.

## Design and organization

```text
backend/
  api/          HTTP controllers, request/response DTOs, error mapping
  domain/       JPA entities and enums
  repository/   Persistence queries
  scoring/      Pure score and severity logic
  service/      Transactions and business invariants
  config/       Optional demo seeding

frontend/src/
  api/          Typed HTTP client
  components/   Reusable forms, badges, and table
  pages/        Dashboard, create, edit, and detail routes
  types/        API contracts
  utils/        Formatting and frontend live-score helpers
```

Entities are not serialized directly. API DTOs isolate persistence from the public contract and make later fields or relationships easier to add without exposing lazy JPA state.

## Assumptions and trade-offs

- PostgreSQL is the runtime database. H2 is used only in automated tests to keep `./test-all.sh` fast and independent of Docker; PostgreSQL-specific behavior is covered by Flyway schema checks during normal startup.
- Residual sorting is performed in the service after one filtered fetch with mitigations. That keeps the score formula in one pure component and avoids storing derived values. For very large registers, I would move the calculation to a database view/materialized column or maintain a transactionally updated score projection so sorting and pagination stay database-side.
- Risk updates use `PUT` with a complete editable representation. A production API could add PATCH and optimistic locking for high-concurrency editing.
- The demo data switch is enabled only by Compose. Normal backend startup leaves an empty register.
- Authentication, tenant boundaries, production observability, CI/CD, and exhaustive concurrency handling are intentionally outside this assignment.

## How the optional scope can be added later

No optional feature is implemented, but the extension path is intentionally narrow:

### Compliance-framework mappings

Add a `FrameworkControl` reference catalog and a `RiskFrameworkMapping` join entity/repository/service. Expose it through a nested `/risks/{id}/framework-mappings` controller and add a `frameworkMappings` field to the detail DTO. The scoring service and existing risk table do not need to change.

### Next review date

Add `next_review_date` through a new Flyway migration and expose it on the risk request/detail DTO. Derive `overdue` in a small review-policy component using an injected clock. This keeps time-based presentation logic out of the entity and makes it deterministic in tests.

### Richer request-state behavior

The frontend API client already centralizes errors and mutations. A query-cache layer can replace page-local fetching later, enabling optimistic updates and cache invalidation without changing page routes or backend contracts.

## What I would add with more time

- PostgreSQL Testcontainers coverage in addition to the fast H2 integration test.
- Optimistic locking with a version field and a clear stale-update response.
- Pagination and database-side residual-score ordering for large registers.
- OpenAPI generation and contract tests for the TypeScript client.
- Accessibility checks and a small browser-level end-to-end suite.
- Configurable scoring policies and explicit assumptions about control independence.
