# Task 003 — Evaluation Result

## Experiment 1: No Agent Guidance

**Branch:** no-guidance

## Objective

Observe AI Agent behavior when implementing a REST API layer on top of the
existing service, with **no** project-level guidance mechanism (no CLAUDE.md,
no Rules, no Skills). This establishes the baseline against which the
full-guidance branch is compared for the controller layer — the first task
where the Agent must make many unspecified default decisions (status codes,
not-found handling, DTO vs. entity exposure, endpoint shape).

At this stage, the project contains no:

- CLAUDE.md
- Rules
- Skills
- Subagents
- Hooks

---

## 1. Execution Environment

| Metric | Result |
|---|---|
| Agent | Claude Code |
| Model | Opus 4.6 |
| Effort Level | Medium |
| Guidance Level | No Agent Guidance |
| Initial Context | 0 |
| Final Context | 54.6k / 200k |
| Context Usage | 27% |
| Plan Usage | 27% |
| Start Time | 20:30 |
| Approximate Duration | ~5 minutes |
| Tests Passed | 21 |

---

## 2. Initial Project State

The project entered Task 003 carrying the completed Task 001 + Task 002
output:

- Java 17
- Spring Boot 4.1.0
- Maven, Jar packaging
- Spring Web, H2 Database, Lombok
- Spring Data JPA
- `Article` and `Author` entities
- `ArticleRepository`, `ArticleService`
- Existing entity and service tests

At the beginning of the experiment, the project contained the domain and
service layers but no REST/API layer.

---

## 3. Task

The Agent was asked to expose the article functionality over a REST API,
supporting create, retrieve-by-id, list-by-author, and delete operations.

The Agent was instructed to follow the existing structure and conventions of
the project, to include tests for the API layer, and not to implement
anything beyond what was described.

The task prompt was **identical** to the full-guidance branch.

---

## 4. Agent Behavior

### 4.1 Project Inspection

The Agent first explored the existing project structure and conventions.

> "Ran a command, read 6 files"

It then inspected application properties and searched for an existing
`AuthorRepository`:

> "Found files, read application.properties" / "Searched AuthorRepository"

Notably, the Agent explicitly reasoned about the existing conventions before
writing code, observing that the project uses Spring Boot 4.1, JPA with H2,
Lombok, **constructor injection**, and that **the service layer takes raw
parameters (not DTOs)**. It stated it would follow the same conventions.

**Evaluation: Good — thorough context-gathering and explicit convention reading**

### 4.2 Supporting Repository Decision

To resolve `authorId` to an `Author` when creating an article, the Agent
independently created:

- `AuthorRepository` — JPA repository for looking up authors by id

Same autonomous decision as the full-guidance branch.

**Evaluation: Good autonomous technical decision**

### 4.3 Controller Implementation

The Agent created:

- `ArticleController` at `/api/articles`

with the following endpoints and status-code decisions:

- `POST /api/articles` — create (body: `title`, `content`, `authorId`), returns **201**
- `GET /api/articles/{id}` — retrieve single article, returns **404** if not found
- `GET /api/articles/by-author/{authorId}` — list by author
- `DELETE /api/articles/{id}` — delete, returns **204**

> All idiomatic REST defaults, chosen autonomously. Status codes (201 / 204 /
> 404) match the full-guidance branch. **One observable difference:** the
> list-by-author endpoint uses a path variable
> (`/api/articles/by-author/{authorId}`) here, versus a query parameter
> (`/api/articles?authorId=...`) on the full-guidance branch.

**Evaluation: Good — idiomatic REST defaults**

### 4.4 DTO / Entity Exposure Decision — Key Comparison Point

The Agent's inspection explicitly noted that the service takes raw parameters,
not DTOs. The controller was implemented following the existing conventions.

> This is the decision point flagged as the candidate for the next Rule. See
> Section 10 for the cross-branch comparison of how the request/response
> boundary was handled here versus on the full-guidance branch.

---

## 5. Test Strategy

The Agent created tests for the API layer:

- `ArticleControllerTest` — 7 integration tests using **MockMvc** (with
  `@SpringBootTest` + `AutoConfigureMockMvc`), covering all endpoints plus
  error cases (author not found, article not found, empty list).

> Note a testing-style difference from the full-guidance branch, which used
> `@WebMvcTest` with mocked collaborators. Here the Agent used
> `@SpringBootTest` + MockMvc (full-context integration test) — consistent
> with the existing service/entity tests' `@SpringBootTest` style.

### Error detection and recovery

The Agent hit the same Spring Boot 4.x compatibility issue as the other
branch: `AutoConfigureMockMvc` / `@WebMvcTest`-related annotations moved
packages in Spring Boot 4.1. After several inspection commands it corrected
the import to `org.springframework.boot.webmvc.test.autoconfigure`.

The final result was:

**21 tests passed** (7 controller + 7 service + 6 entity + 1 app context)

**Evaluation: Good error detection and recovery; good test coverage including error cases**

---

## 6. Scope Compliance

Beyond the requested controller, the Agent introduced only `AuthorRepository`
— a directly supporting layer required for the create operation. It did not
create authentication, authorization, update endpoints, or pagination.

**Evaluation: Good**

---

## 7. Files Created and Modified

### Created

- `AuthorRepository.java`
- `ArticleController.java`
- `ArticleControllerTest.java`

Total: 3 files, +209 lines.

### Modified

- `ArticleControllerTest.java` — corrected moved import for Spring Boot 4.1

---

## 8. Autonomous Decisions

| Decision | Explicitly Requested? | Evaluation |
|---|---|---|
| Create `AuthorRepository` | No | Good |
| Base path `/api/articles` | No | Good |
| Return 201 on create | No | Good |
| Return 204 on delete | No | Good |
| Return 404 on not found | No | Good |
| List endpoint as path variable (`/by-author/{id}`) | No | Reasonable |
| `@SpringBootTest` + MockMvc test style | No | Good |
| Cover error cases in tests | No | Good |

---

## 9. Metrics

### Execution Metrics

```
Agent                    : Claude Code
Model                    : Opus 4.6
Effort                   : Medium
Guidance                 : None
Duration                 : ~5 minutes
Initial Context          : 0
Final Context            : 54.6k / 200k
Context Usage            : 27%
Plan Usage               : 27%
Tests Passed             : 21
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
| Convention Alignment | Good |
| Error Recovery | Good |
| Unrequested Changes | 0 |
| Human Intervention | None |

---

## 10. Cross-Branch Comparison (Task 003)

| Dimension | No Guidance | Full Guidance (CLAUDE.md) |
|---|---|---|
| Final Context | 54.6k (27%) | 61.7k (31%) |
| Files inspected | 6 + properties | 7 + properties |
| `AuthorRepository` created | Yes | Yes |
| Base path | `/api/articles` | `/api/articles` |
| Create status | 201 | 201 |
| Delete status | 204 | 204 |
| Not-found status | 404 | 404 |
| List endpoint style | path var `/by-author/{id}` | query param `?authorId=` |
| Request/response boundary | (see note) | inner records `CreateArticleRequest` / `ArticleResponse` |
| Controller test style | `@SpringBootTest` + MockMvc | `@WebMvcTest` + mocked collaborators |
| Service controller tests | 7 | 7 |
| Total tests passed | 21 | 18 |
| Lines added | +209 | +91 (+141 test) |

### Observations

The two branches converged on nearly all core REST decisions: identical base
path, identical status-code scheme (201 / 204 / 404), same supporting
`AuthorRepository`, same error-case coverage. The Agent produced clean,
idiomatic controller code on **both** branches — the no-guidance branch did
**not** collapse into an obvious anti-pattern.

The observable differences are narrower than expected and fall into three
areas:

1. **List endpoint shape:** path variable (`/by-author/{id}`) vs. query
   parameter (`?authorId=`).
2. **Controller test style:** `@SpringBootTest` + MockMvc (no-guidance) vs.
   `@WebMvcTest` + mocked collaborators (guidance).
3. **Request/response boundary:** how DTOs are handled — the single decision
   the Agent itself flagged as one it "guessed" on.

These are **consistency divergences**, not correctness failures. Both are
defensible; the point is that they differ between branches with no rule to
pin them down — exactly the kind of divergence a Rule is meant to eliminate.

---

## 11. Key Learning from Task 003 (No Guidance)

The no-guidance branch produced clean, idiomatic REST code and did not fall
into an obvious anti-pattern — so the hypothesis that "without a rule the code
comes out clearly dirty" is **not** supported at the level of gross code
quality. What the comparison *does* reveal is a set of small but real
**consistency divergences** between the branches: list-endpoint shape,
controller test style, and DTO/boundary handling. None is wrong; each is a
place where the two branches made different defensible choices.

This is the actual, evidence-based case for the next Rule: not "guidance fixes
dirty code," but "guidance eliminates unpredictable divergence on decisions
the project wants to standardize." The strongest single candidate is the
request/response boundary, because it is the one decision the Agent explicitly
reported guessing on — but the list-endpoint and test-style divergences are
equally valid rule targets. A Rule should be written to lock down whichever of
these the project most wants to keep consistent, then its effect measured by
re-running this task on the rule-equipped branch.
