# Risk Register

A small full-stack Risk Register application built as a take-home assignment for a Software Engineer, Risk Management role.

The application allows users to create and manage organizational risks, calculate inherent and residual risk scores, attach mitigations, filter and sort risks, and view the most important risks from a dashboard.

The core implementation follows the requirements in the assignment, with the optional stretch goals also implemented.

## Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Data JPA / Hibernate
* PostgreSQL
* Flyway
* Maven
* JUnit 5
* Spring Boot integration testing

### Frontend

* React
* TypeScript
* Vite
* CSS
* Vitest

### Local Runtime

* Docker
* Docker Compose

Docker is provided as a convenience for running the full application locally. Authentication, deployment infrastructure, and CI/CD are intentionally outside the scope of the assignment.

---

## Features

### Core Features

* Create, view, edit, and delete risks.
* Risk fields:

  * Title
  * Description
  * Category
  * Owner
  * Likelihood
  * Impact
  * Status
  * Created timestamp
  * Updated timestamp
* Risk categories:

  * Operational
  * Financial
  * Compliance
  * Security
  * Strategic
* Risk statuses:

  * Open
  * Mitigating
  * Closed
* Add, edit, and delete mitigations.
* Mitigation effectiveness from 1–5.
* Automatic inherent and residual risk scoring.
* Severity bands:

  * Low
  * Medium
  * High
  * Critical
* Dashboard filtering by category and status.
* Dashboard sorting by residual risk.
* Risk detail page with mitigations and score information.
* Validation for likelihood, impact, and mitigation effectiveness.
* PostgreSQL persistence.
* API and scoring tests.

### Stretch Goals

The optional features from the assignment are also implemented:

1. NIST CSF framework mapping.
2. Next review date with overdue indication.
3. Loading and error states.
4. Optimistic updates for mitigation operations with rollback on failure.

---

# Running the Application

## Prerequisites

The easiest way to run the complete application is with Docker Desktop.

You will need:

* Docker Desktop
* Docker Compose

Verify Docker is available:

```bash
docker --version
docker compose version
```

---

## Start the Application

From the project root:

```bash
docker compose up --build
```

The first build may take longer because Docker needs to download the Maven, Node, PostgreSQL, and Nginx images and dependencies.

Once the containers are running:

### Frontend

```text
http://localhost:3000
```

### Backend API

```text
http://localhost:8080
```

### PostgreSQL

```text
localhost:5432
```

The application creates and migrates the PostgreSQL schema automatically using Flyway.

---

## Start Using the Application

Open:

```text
http://localhost:3000
```

From the dashboard you can:

1. Create a risk.
2. Set its likelihood and impact.
3. View the calculated inherent score.
4. Add mitigations.
5. Observe the residual score change.
6. Filter and sort risks.
7. Open the detail page.
8. Add or edit mitigations.
9. Map the risk to NIST CSF functions.
10. Set its next review date.

---

## Stop the Application

To stop the containers:

```bash
docker compose down
```

To stop the application and also remove the local PostgreSQL data volume:

```bash
docker compose down -v
```

`docker compose down -v` deletes the local database data, so use it only when you want to reset the application to a clean state.

---

# Backend API

The main endpoints are:

## Risks

```text
POST   /api/risks
GET    /api/risks
GET    /api/risks/{id}
PUT    /api/risks/{id}
DELETE /api/risks/{id}
```

The list endpoint supports filtering by category and status and sorting by residual score.

Example:

```bash
curl "http://localhost:8080/api/risks?sort=residualScore,desc"
```

## Mitigations

```text
POST   /api/risks/{riskId}/mitigations
PUT    /api/risks/{riskId}/mitigations/{mitigationId}
DELETE /api/risks/{riskId}/mitigations/{mitigationId}
```

---

# Risk Scoring

## Inherent Risk

The inherent risk score is:

```text
Likelihood × Impact
```

Since both values are constrained to 1–5:

```text
Minimum = 1
Maximum = 25
```

Examples:

```text
Likelihood = 2
Impact = 3

Inherent score = 2 × 3 = 6
```

```text
Likelihood = 5
Impact = 5

Inherent score = 5 × 5 = 25
```

---

## Residual Risk

The assignment intentionally leaves the residual-risk formula open and asks for a reasonable formula that reduces the inherent risk based on mitigation effectiveness.

The implementation uses:

```text
residual = max(
    1,
    ceil(
        inherent × product(1 - 0.10 × effectiveness)
    )
)
```

where each mitigation has an effectiveness from 1–5.

This gives the following reduction for each mitigation:

```text
Effectiveness 1 → 10% reduction
Effectiveness 2 → 20% reduction
Effectiveness 3 → 30% reduction
Effectiveness 4 → 40% reduction
Effectiveness 5 → 50% reduction
```

Multiple mitigations compound.

### Example

Suppose:

```text
Likelihood = 4
Impact = 5
```

Then:

```text
Inherent = 4 × 5 = 20
```

With one mitigation of effectiveness 5:

```text
Residual = ceil(20 × 0.50)
         = 10
```

With two mitigations of effectiveness 5:

```text
Residual = ceil(20 × 0.50 × 0.50)
         = 5
```

The residual score is always floored at 1.

### Why this formula?

The formula was chosen because it satisfies all required sanity checks:

* A risk with zero mitigations has residual = inherent.
* A highly effective mitigation meaningfully reduces the risk.
* Additional mitigations provide additional reduction.
* Residual risk can never fall below 1.

Using a multiplicative approach also avoids simply subtracting arbitrary fixed points from a score and reflects the idea that controls reduce the exposure that remains after previous controls.

---

# Severity Bands

The severity bands are applied to both inherent and residual scores.

```text
1–5    → Low
6–12   → Medium
13–19  → High
20–25  → Critical
```

Examples:

```text
Score 4  → Low
Score 10 → Medium
Score 16 → High
Score 23 → Critical
```

The frontend uses visually distinct severity indicators so high-risk items can be recognized without reading every number.

---

# Business Rules

## Closing a Risk

A risk cannot be moved to `CLOSED` when it has no mitigations.

This was treated as a compliance-oriented business rule because a closed risk should represent a risk that has been actively addressed rather than merely recorded.

Attempting to close a risk without a mitigation returns a `409 Conflict` response.

## Deleting the Final Mitigation

A closed risk cannot have its final mitigation removed.

This prevents the system from ending up with a closed risk that has no documented control.

## Validation

Likelihood, impact, and mitigation effectiveness must each be integer values from 1 to 5.

Invalid requests are rejected with a clear 4xx response.

Validation is applied at multiple layers where appropriate so invalid data cannot silently enter the system.

---

# Optional Stretch Goals

## 1. NIST CSF Mapping

A risk can be mapped to one or more NIST Cybersecurity Framework functions:

```text
GV  Govern
ID  Identify
PR  Protect
DE  Detect
RS  Respond
RC  Recover
```

The mappings are persisted independently of the scoring calculation.

This keeps framework-specific metadata separate from the core risk-scoring model.

---

## 2. Next Review Date

A risk can have a next review date.

The backend determines whether the review is overdue.

For active risks:

```text
Review date in the future → not overdue
Review date in the past   → overdue
```

Closed risks are not treated as active overdue reviews.

The review logic uses an injected `Clock`, which makes the behavior deterministic and straightforward to test.

---

## 3. Loading and Error States

The frontend handles common API states explicitly:

```text
Loading
Success
Empty
Error
Retry
```

For example, if the backend is unavailable, the dashboard displays an error state instead of failing silently.

To test this manually:

```bash
docker compose stop backend
```

Refresh the frontend and the dashboard should show an API error state.

Restart the backend:

```bash
docker compose start backend
```

Then use the Retry action.

---

## 4. Optimistic Updates

Mitigation create, edit, and delete operations use optimistic updates.

Instead of waiting for the API response before updating the UI:

```text
User action
    ↓
Update UI immediately
    ↓
Send API request
    ↓
Success → reconcile with server state
Failure → restore previous state
```

This makes the interface feel more responsive while still maintaining consistency when an API call fails.

The implementation also contains rollback behavior for failed mutations.

---

# Database

PostgreSQL is used for persistence.

Flyway manages schema migrations.

Current migrations include:

```text
V1 - create risk register
V2 - add risk review and framework mapping
```

The second migration introduces the storage required for:

* next review dates
* framework mappings

The database schema is automatically initialized when the backend starts against a new database.

---

# Project Structure

```text
.
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/hyperproof/riskregister/
│       │   └── resources/
│       │       └── db/migration/
│       └── test/
│
├── frontend/
│   ├── package.json
│   ├── package-lock.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── api/
│       ├── components/
│       ├── pages/
│       ├── types/
│       └── utils/
│
├── docker-compose.yml
├── run.sh
├── test-all.sh
├── .gitignore
└── README.md
```

The frontend and backend are kept separately so that the API contract remains independent of the UI implementation.

---

# Testing

The project contains automated tests for the most important correctness areas.

## Backend tests

The backend tests cover:

* Inherent-risk calculation.
* Residual-risk calculation.
* Severity boundaries.
* Zero-mitigation behavior.
* Mitigation effectiveness.
* Residual score floor.
* API validation.
* Risk creation and retrieval.
* Mitigation creation.
* Residual-score recalculation.
* Business-rule validation.

Run backend tests from the project root:

```bash
cd backend
mvn test
```

---

## Frontend tests

Run frontend tests:

```bash
cd frontend
npm test
```

---

## Run all tests

The project also provides:

```bash
./test-all.sh
```

This runs the backend tests and frontend validation/build steps together.

If the script is not executable:

```bash
chmod +x test-all.sh
./test-all.sh
```

---

# Development Notes

## Frontend Dependency Versions

The frontend uses pinned Vite/plugin versions and installs dependencies using the Docker build configuration in order to keep the local Docker build reproducible.

The repository includes `package-lock.json` and should retain it in version control.

## Docker

Docker is not required by the assignment itself; it is included here purely to make the reviewer setup easier and deterministic.

---

# Key Assumptions and Trade-offs

## Single organization

Authentication and multi-tenant access control are intentionally not implemented because they are explicitly outside the assignment scope.

## Free-text owner

The owner is represented as free text rather than a separate user entity.

This keeps the data model aligned with the assignment while avoiding unnecessary user-management complexity.

## Residual-risk model

The residual score uses compound percentage reductions rather than a fixed-point deduction.

This keeps the score within the expected range and lets multiple controls have cumulative impact.

## Framework mappings

Framework functions are stored separately from the main risk record so the compliance mapping feature does not become coupled to the scoring model.

This makes it easier to replace the hardcoded NIST list with another framework or a richer framework catalog later.

## Review logic

Review-overdue logic is kept in a dedicated policy component and uses an injected clock.

This avoids embedding date logic directly inside persistence or UI code and makes future policy changes easier.

## Optimistic updates

Only mitigation mutations use optimistic updates because these interactions benefit most from immediate UI feedback while remaining relatively easy to roll back.

---

# What I Would Add With More Time

The assignment intentionally allows trade-offs and TODOs when appropriate.

With more time, I would consider:

* Pagination for larger risk registers.
* Server-side full-text search.
* More detailed audit history for score and mitigation changes.
* Role-based permissions.
* A framework catalog rather than hardcoded NIST functions.
* Scheduled review notifications.
* More granular optimistic-update handling and retry behavior.
* More extensive contract testing between frontend and backend.
* CI checks for tests, formatting, and builds.
* Production deployment configuration.

These were intentionally not prioritized because the assignment emphasizes correctness, scoring logic, API design, testing, and clear communication over building a larger product surface.

---

# Manual Verification Checklist

A reviewer can verify the main workflows in a few minutes:

```text
1. Start the application with Docker Compose.
2. Open http://localhost:3000.
3. Create a risk.
4. Change likelihood and impact and verify the inherent score.
5. Open the risk detail page.
6. Add a mitigation and verify the residual score changes.
7. Edit the mitigation and verify the residual score changes again.
8. Delete the mitigation and verify the residual score returns accordingly.
9. Filter risks by category and status.
10. Sort by residual score.
11. Attempt to close a risk with no mitigations and verify the business rule.
12. Set a past next-review date and verify the overdue indicator.
13. Add one or more NIST CSF mappings.
14. Stop the backend and verify the frontend error state and Retry behavior.
15. Restart the backend and verify the application recovers.
```
