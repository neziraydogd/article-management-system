# Task 002 — Evaluation Result

## Experiment 2: CLAUDE.md Guidance

## Objective

Observe AI Agent behavior when implementing the same service layer as the
no-guidance run, this time with a project-level `CLAUDE.md` present. The task
prompt is identical to the no-guidance run; the only changed variable is the
presence of `CLAUDE.md`.

At this stage, the project contains:

- CLAUDE.md ✅
- Rules ❌
- Skills ❌
- Subagents ❌
- Hooks ❌
- Output styles ❌
- Custom system instructions ❌

The purpose of this experiment is to isolate the effect of `CLAUDE.md` on how
the Agent designs the service layer, decides whether to introduce a
repository, handles not-found cases, and structures its tests — compared
against the no-guidance baseline for the same task.

---

## 1. Execution Environment

| Metric | Result |
|---|---|
| Agent | Claude Code |
| Model | Opus 4.6 |
| Effort Level | Medium |
| Guidance Level | CLAUDE.md |
| Initial Context | 0 |
| Final Context | 47.4k / 200k |
| Context Usage | 24% |
| Approximate Duration | ~5 minutes |
| Tests Passed | 11 |

---

## 2. Initial Project State

The project entered Task 002 (guidance run) carrying the completed Task 001
output plus the guidance file:

- Java 17
- Spring Boot 4.1.0
- Maven, Jar packaging
- Spring Web, H2 Database, Lombok
- Spring Data JPA
- `Article` and `Author` entities
- `ArticleEntityTest`
- **CLAUDE.md** (technology stack, package structure, and the convention:
  "Follow existing project conventions when they exist. Do not assume
  additional architectural conventions that have not yet been established.")

At the beginning of the experiment, the project contained the domain model
but no service, repository, or business-logic layer.

---

## 3. Task

The Agent was asked to implement a service layer for managing articles.

The service was required to support:

- Creating a new article for a given author
- Retrieving an article by its id
- Listing all articles by a given author
- Deleting an article by its id

The Agent was instructed to follow the existing structure and conventions of
the project, to include tests for the service, and not to implement anything
beyond what was described.

The task prompt was **identical** to the no-guidance run.

---

## 4. Agent Behavior

### 4.1 Project Inspection

The Agent first explored the current project structure to understand the
conventions before implementing the service layer.

Notably, this run inspected the project using a **sub-agent-based
exploration**:

> "Ran 2 agents"

followed by reading the existing model and test files:

> "Read 3 files"

The Agent explicitly stated its intent to "match conventions exactly" before
writing any code — a stronger convention-alignment signal than the
no-guidance run, consistent with the CLAUDE.md instruction to follow existing
conventions.

**Evaluation: Good — explicit convention-matching intent**

### 4.2 Repository Decision

The task prompt asked only for a service and did not mention a repository.

As in the no-guidance run, the Agent independently decided a repository was
required and created:

- `ArticleRepository` — a Spring Data JPA repository with a `findByAuthor`
  query method

**Evaluation: Good autonomous technical decision**

> Note: the query method here is `findByAuthor` (taking the `Author`),
> whereas the no-guidance run used `findByAuthorId`. A small but observable
> API-surface difference between the two runs.

### 4.3 Service Implementation

The Agent created:

- `ArticleService`

with four operations matching the task:

- `create(title, content, author)` — creates and persists a new article
- `findById(id)` — returns `Optional<Article>`
- `findByAuthor(author)` — returns all articles for an author
- `deleteById(id)` — deletes an article

The service was implemented on top of the repository, keeping layering
consistent (service → repository).

**Evaluation: Good — correct service → repository layering**

> Note: `findById` returns `Optional<Article>`. This is an observable
> not-found strategy decision worth comparing across future runs.

### 4.4 Convention Alignment

The Agent explicitly followed the existing entity test's conventions,
reusing the same testing stack:

- `@SpringBootTest`
- `@Transactional`
- `EntityManager`
- AssertJ assertions

This mirrors the existing `ArticleEntityTest` setup rather than introducing a
new testing style.

**Evaluation: Good — strong alignment with existing conventions**

---

## 5. Test Strategy

The Agent created tests for the service.

It produced:

- `ArticleServiceTest` — 7 integration tests

These covered all four operations plus edge cases:

- not found
- empty list
- delete does not affect other articles

The final result was:

**11 tests passed**

This included:

- 7 new service tests
- 4 existing tests

**Evaluation: Good test coverage including edge cases**

---

## 6. Scope Compliance

The Agent was instructed not to implement anything beyond the described
service.

Beyond the requested service, it introduced only the repository — a directly
supporting layer required to fulfill the task. It did not create:

- Controllers ❌
- REST endpoints ❌
- DTOs / mappers ❌
- Authentication ❌
- Authorization ❌
- Extra abstractions (interfaces / abstract classes) ❌

The implementation remained focused on the service, its supporting
repository, and tests.

**Evaluation: Good**

---

## 7. Files Created and Modified

### Created

- `ArticleRepository.java`
- `ArticleService.java`
- `ArticleServiceTest.java`

Total: 3 files, +205 lines.

### Modified

- None

---

## 8. Autonomous Decisions

The following decisions were made by the Agent without explicit instructions:

| Decision | Explicitly Requested? | Evaluation |
|---|---|---|
| Create `ArticleRepository` | No | Good |
| Use derived query `findByAuthor` | No | Good |
| Return `Optional<Article>` from `findById` | No | Good |
| Keep service on top of repository | No | Good |
| Reuse existing test conventions (`@SpringBootTest` + `@Transactional` + `EntityManager` + AssertJ) | Implied by CLAUDE.md | Good |
| Cover not-found / empty / isolation edge cases in tests | No | Good |

---

## 9. Metrics

### Execution Metrics

```
Agent                    : Claude Code
Model                    : Opus 4.6
Effort                   : Medium
Guidance                 : CLAUDE.md
Duration                 : ~5 minutes
Initial Context          : 0
Final Context            : 47.4k / 200k
Context Usage            : 24%
Tests Passed             : 11
```

### Quality Metrics

| Metric | Result |
|---|---|
| Requirement Understanding | Good |
| Project Inspection | Good |
| Scope Compliance | Good |
| Autonomous Decision Making | High |
| Test Generation | Good |
| Layering / Architecture | Good |
| Convention Alignment | Good (explicit) |
| Unrequested Changes | 0 |
| Human Intervention | None |

---

## 10. Comparison with No-Guidance Run (same task)

| Dimension | No Guidance | CLAUDE.md |
|---|---|---|
| Final Context | 45.3k (23%) | 47.4k (24%) |
| Project inspection | "read 5 files" | "ran 2 agents" + "read 3 files" |
| Repository created | Yes | Yes |
| Query method | `findByAuthorId` | `findByAuthor` |
| `findById` return | (to confirm from no-guidance source) | `Optional<Article>` |
| Service tests | 7 | 7 |
| Total tests passed | 14 | 11 |
| Lines added | +163 | +205 |
| Extra abstractions | None | None |
| Convention-matching intent | Implicit | **Explicit** ("match conventions exactly") |

> **Note on total-test difference (14 vs 11):** the two runs report different
> totals of pre-existing tests (6+1 vs 4). This reflects differing baseline
> test counts carried into each branch from Task 001, not a difference in
> the service tests themselves (both produced 7 service tests). This is worth
> flagging when the branches are compared, as it ties back to the Task 001
> test-strategy divergence.

---

## 11. Key Learning from Task 002 (CLAUDE.md)

For this standard service-layer task, `CLAUDE.md` did **not** change the core
architectural outcome: both runs independently introduced a repository, kept
clean service → repository layering, avoided over-engineering, and produced 7
service tests with comparable edge-case coverage.

The observable effect of `CLAUDE.md` was on **process and framing rather than
structure**: the guided run showed a more explicit convention-matching
posture ("read the existing model and test files to match conventions
exactly," reusing the exact `@SpringBootTest` + `@Transactional` +
`EntityManager` + AssertJ stack), and used a sub-agent-based exploration step.
Context and line counts were slightly higher in the guided run (47.4k vs
45.3k; +205 vs +163).

This is itself a meaningful finding: for tasks where the Agent is already
competent (idiomatic Spring Data CRUD), `CLAUDE.md` produces marginal
structural difference and mainly reinforces convention alignment. The larger
effects of guidance are expected to appear in tasks that require enforcing a
project-specific decision the Agent would not otherwise make — which is where
the later Rules experiments are aimed.
