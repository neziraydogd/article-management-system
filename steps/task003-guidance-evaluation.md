# Task 003 — Evaluation Result

## Experiment 3: Full Guidance (CLAUDE.md + Rules)

**Branch:** full-guidance (CLAUDE.md + `.claude/rules/`)

## Objective

Measure whether project-level Rules change Agent behavior on the REST API
task, compared against both the no-guidance baseline and the CLAUDE.md-only
run. This is the first task where Rules that deliberately encode **best
practice** — rather than freezing whatever an earlier run produced — are in
effect, so the key question is whether the Agent's unspecified default
decisions now converge on the rule-defined conventions.

At this stage, the project contains:

- CLAUDE.md ✅
- Rules ✅ (`configuration-safety`, `api-request-response-boundary`,
  `rest-endpoint-conventions`, `testing-conventions`)
- Skills ❌
- Subagents ❌
- Hooks ❌

---

## 1. Execution Environment

| Metric | Result |
|---|---|
| Agent | Claude Code |
| Model | Opus 4.6 |
| Effort Level | Medium |
| Guidance Level | CLAUDE.md + Rules |
| Initial Context | 0 |
| Final Context | 65.8k / 200k |
| Context Usage | 33% |
| Approximate Duration | ~5 minutes |
| Tests Passed | 19 |

---

## 2. Initial Project State

The project entered Task 003 carrying the completed Task 001 + Task 002 output
plus the guidance files:

- Java 17, Spring Boot 4.1.0, Maven, H2, Lombok, Spring Data JPA
- `Article` and `Author` entities
- `ArticleRepository`, `ArticleService`
- Existing entity and service tests
- CLAUDE.md
- `.claude/rules/` with four rules (configuration safety, API
  request/response boundary, REST endpoint conventions, testing conventions)

At the beginning of the experiment, the project contained the domain and
service layers but no REST/API layer.

---

## 3. Task

The Agent was asked to expose the article functionality over a REST API,
supporting create, retrieve-by-id, list-by-author, and delete operations.

The task prompt was **identical** across all branches. The only changed
variable relative to the CLAUDE.md-only run is the presence of the four Rules.

---

## 4. Agent Behavior

### 4.1 Project Inspection and Planning

Before writing code, the Agent laid out a plan that **explicitly reflected the
Rules**:

> "I need to create: (1) `AuthorRepository`, (2) Request/Response DTOs **in a
> `dto` package**, (3) `ArticleController` with the four endpoints, (4)
> `@WebMvcTest` controller tests."

This is a direct behavioral signal: the DTO-package and `@WebMvcTest`
decisions — which were **guesses / divergences** on the earlier branches —
appeared here as up-front, deliberate plan items driven by the rules.

**Evaluation: Good — plan visibly shaped by the rules**

### 4.2 Supporting Repository Decision

As on both earlier branches, the Agent independently created:

- `AuthorRepository` — to resolve `authorId` to an `Author` on create

**Evaluation: Good autonomous technical decision**

### 4.3 DTO / Boundary Decision — Rule Effect Confirmed

The Agent created DTOs as **top-level records in a dedicated `dto` package**:

- `com.na.article.dto.CreateArticleRequest` (record: `title`, `content`,
  `authorId`)
- `com.na.article.dto.ArticleResponse` (record with a `from(Article)` factory
  method)

> **This is the clearest rule effect in the experiment so far.** On the
> CLAUDE.md-only branch the Agent placed DTOs as **inner records inside the
> controller** and explicitly said it was guessing. With the
> `api-request-response-boundary` rule in place, it instead used a separate
> `dto` package with an explicit `from(...)` mapping factory — exactly the
> best-practice convention the rule defines. The guess is gone; the behavior
> is now deterministic and correct.

**Evaluation: Rule applied correctly**

### 4.4 Endpoint and Status-Code Decisions — Rule Effect Confirmed

The controller at `/api/articles` implemented:

- `POST /api/articles` — create, returns **201 + `Location` header** (404 if
  author not found)
- `GET /api/articles/{id}` — retrieve (404 if not found)
- `GET /api/articles?authorId={authorId}` — list by author via **query
  parameter** (404 if author not found)
- `DELETE /api/articles/{id}` — delete, returns **204** (404 if not found)

> Two rule effects are visible here. First, the list-by-author endpoint uses a
> **query parameter** (`?authorId=`), matching the `rest-endpoint-conventions`
> rule — whereas the no-guidance branch used a path segment
> (`/by-author/{id}`). Second, the create endpoint now returns a **`Location`
> header** and delete now returns **404 on a missing id**, both of which were
> specified by the rule and were **not** present on the earlier runs. The
> Agent adopted the best-practice behaviors introduced by the rule rather than
> its own earlier defaults.

**Evaluation: Rule applied correctly**

### 4.5 Test Strategy — Rule Effect Confirmed

The Agent wrote controller tests using **`@WebMvcTest` with the service and
repository mocked**:

- `ArticleControllerTest` — 8 tests, covering happy paths and error cases for
  all endpoints.

> On the no-guidance branch the controller test used `@SpringBootTest` +
> MockMvc (full-context integration test). Here, under the
> `testing-conventions` rule, the Agent used the sliced `@WebMvcTest` with
> mocked collaborators — the best-practice, faster approach the rule requires.

**Evaluation: Rule applied correctly**

### 4.6 Error Detection and Recovery

The Agent again hit Spring Boot 4.x package relocations, this time more
extensively — Jackson moved to `tools.jackson`, and it verified the locations
of `@WebMvcTest`, `MockitoBean`, and MockMvc request builders/result matchers
before settling on the correct imports. It resolved these autonomously and all
tests passed.

**Evaluation: Good error detection and recovery**

---

## 5. Test Result

**19 tests passed** (8 new controller tests + 11 existing).

Note the new controller test count is **8** here vs. 7 on the earlier
branches — the additional test corresponds to the extra rule-mandated
behaviors (e.g. delete-not-found returning 404, `Location` header), which
create more error/edge cases to cover.

---

## 6. Scope Compliance

Beyond the requested controller, the Agent introduced only the directly
supporting `AuthorRepository` and the DTOs required by the boundary rule. It
did not add authentication, authorization, update endpoints, or pagination.

**Evaluation: Good**

---

## 7. Files Created and Modified

### Created

- `repository/AuthorRepository.java`
- `dto/CreateArticleRequest.java`
- `dto/ArticleResponse.java` (with `from(Article)` factory)
- `controller/ArticleController.java`
- `controller/ArticleControllerTest.java`

### Modified

- Import corrections for Spring Boot 4.x / Jackson `tools.jackson` relocations
  in the test file.

> Note: this branch produced **5 files** (separate `dto` package types) vs. the
> earlier branches' 3 files, because DTOs are now top-level types in their own
> package rather than controller inner records — a direct, expected
> consequence of the boundary rule.

---

## 8. Autonomous Decisions vs. Rule-Governed Decisions

| Decision | Source | Result |
|---|---|---|
| Create `AuthorRepository` | Autonomous | Good (same on all branches) |
| DTOs in a dedicated `dto` package | **Rule** | Applied (was a guess before) |
| Explicit `from(Article)` mapping factory | **Rule** | Applied |
| No entity exposed from controller | **Rule** | Applied |
| List by author via query parameter | **Rule** | Applied (was path segment before) |
| 201 + `Location` header on create | **Rule** | Applied (absent before) |
| 404 on delete of missing id | **Rule** | Applied (absent before) |
| `@WebMvcTest` + mocked collaborators | **Rule** | Applied (was `@SpringBootTest` before) |
| Cover error/edge cases in tests | Autonomous + Rule | Good |

---

## 9. Metrics

### Execution Metrics

```
Agent                    : Claude Code
Model                    : Opus 4.6
Effort                   : Medium
Guidance                 : CLAUDE.md + Rules
Duration                 : ~5 minutes
Initial Context          : 0
Final Context            : 65.8k / 200k
Context Usage            : 33%
Tests Passed             : 19
```

### Quality Metrics

| Metric | Result |
|---|---|
| Requirement Understanding | Good |
| Project Inspection | Good |
| Scope Compliance | Good |
| Autonomous Decision Making | High |
| Rule Adherence | High (all four rules reflected) |
| Test Generation | Good |
| Layering / Architecture | Good |
| Convention Alignment | Good (rule-driven) |
| Error Recovery | Good |
| Unrequested Changes | 0 |
| Human Intervention | None |

---

## 10. Cross-Branch Comparison (Task 003)

| Dimension | No Guidance | CLAUDE.md only | CLAUDE.md + Rules |
|---|---|---|---|
| Final Context | 54.6k (27%) | 61.7k (31%) | 65.8k (33%) |
| DTO location | (inner / minimal) | inner records in controller | **`dto` package (top-level records)** |
| Entity/DTO mapping | — | inner | **explicit `from()` factory** |
| List endpoint | path `/by-author/{id}` | query `?authorId=` | **query `?authorId=`** |
| Create response | 201 | 201 | **201 + `Location` header** |
| Delete of missing id | (204) | (204) | **404** |
| Controller test style | `@SpringBootTest` + MockMvc | `@WebMvcTest` + mocks | **`@WebMvcTest` + mocks** |
| New controller tests | 7 | 7 | **8** |
| Files created | 3 | 3 | **5** (separate dto package) |
| Total tests passed | 21 | 18 | 19 |

---

## 11. Key Learning from Task 003 (Full Guidance)

This run is the experiment's clearest demonstration of what Rules do. The
Agent's plan itself referenced the rules up front ("DTOs in a `dto` package",
"`@WebMvcTest` tests"), and every rule-governed decision landed on the
best-practice convention the rule defined:

- The DTO boundary moved from **inner records the Agent admitted guessing at**
  to a **dedicated `dto` package with explicit mapping**.
- The list endpoint settled on a **query parameter** instead of the
  no-guidance branch's path segment.
- The create endpoint added a **`Location` header** and delete now returns
  **404 for a missing id** — best-practice behaviors that appeared on **no**
  earlier branch.
- Controller tests moved from a full `@SpringBootTest` integration test to a
  sliced **`@WebMvcTest`**.

The central finding of the series is now well-supported: on a task where the
Agent is already competent, the difference guidance makes is **not** "clean
vs. dirty code" — all three branches produced working, reasonable controllers.
The difference is **predictability and best-practice enforcement**. Without
rules, defensible-but-divergent choices appeared across runs (path vs. query,
inner vs. packaged DTOs, integration vs. sliced tests). With rules, those
decisions became deterministic and aligned to the project's chosen best
practices — including behaviors (Location header, 404-on-delete) the Agent
would not have produced on its own.

One measured cost: context usage rose monotonically with guidance
(54.6k → 61.7k → 65.8k), reflecting the additional rule content the Agent
loads and reasons over. This is a modest, expected trade-off and worth noting
in the write-up.

The next mechanism in the series (Skills) should be introduced only when a
task surfaces a genuine, repeatable procedural need that Rules cannot cleanly
express — following the same observation-first discipline used to derive these
rules.
