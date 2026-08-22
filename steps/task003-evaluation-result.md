# Task 003 — Evaluation Result

## Experiment 2: CLAUDE.md Guidance

**Branch:** `origin/agent-only-claude-md-guidance`

## Objective

Observe AI Agent behavior when implementing a REST API layer on top of the
existing service, with a project-level `CLAUDE.md` present but no Rules or
Skills. This task deliberately moves into controller territory, where the
Agent must make many default decisions (status codes, not-found handling, DTO
vs. entity exposure, validation) that were not specified in the prompt — a
setting more likely to surface behavior the project may want to constrain
later.

At this stage, the project contains:

- CLAUDE.md ✅
- Rules ❌
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
| Guidance Level | CLAUDE.md |
| Initial Context | 0 |
| Final Context | 61.7k / 200k |
| Context Usage | 31% |
| Approximate Duration | ~5 minutes |
| Tests Passed | 18 |

---

## 2. Initial Project State

The project entered Task 003 carrying the completed Task 001 + Task 002 output
plus the guidance file:

- Java 17
- Spring Boot 4.1.0
- Maven, Jar packaging
- Spring Web, H2 Database, Lombok
- Spring Data JPA
- `Article` and `Author` entities
- `ArticleRepository`, `ArticleService`
- Existing entity and service tests
- CLAUDE.md

At the beginning of the experiment, the project contained the domain and
service layers but no REST/API layer.

---

## 3. Task

The Agent was asked to expose the article functionality over a REST API,
supporting create, retrieve-by-id, list-by-author, and delete operations.

The Agent was instructed to follow the existing structure and conventions of
the project, to include tests for the API layer, and not to implement
anything beyond what was described.

The task prompt was **identical** across all branches.

---

## 4. Agent Behavior

### 4.1 Project Inspection

The Agent first explored the existing project structure and code to
understand the conventions established so far.

> "Found files, read 7 files"

It then specifically searched for an `AuthorRepository` and inspected
`application.properties`:

> "Searched code, found files, read application.properties"

This shows a broader inspection than earlier tasks (7 files vs. 3–5),
consistent with the larger surface area of an API layer that must integrate
with the existing service and entities.

**Evaluation: Good — thorough context-gathering before implementation**

### 4.2 Supporting Repository Decision

The task prompt asked only for a REST API. To resolve `authorId` to an
`Author` when creating an article, the Agent independently decided a new
repository was required and created:

- `AuthorRepository` — JPA repository for looking up authors by id

This layer was not explicitly requested but is a direct prerequisite for the
create operation.

**Evaluation: Good autonomous technical decision**

### 4.3 Controller Implementation

The Agent created:

- `ArticleController` — REST controller at `/api/articles`

with the following endpoints and status-code decisions:

- `POST /api/articles` — create (body: `authorId`, `title`, `content`),
  returns **201**
- `GET /api/articles/{id}` — retrieve single article, returns **404** if not found
- `GET /api/articles?authorId={authorId}` — list by author, returns **404** if
  author not found
- `DELETE /api/articles/{id}` — delete, returns **204**

> These are all **autonomous default decisions**. The prompt did not specify
> the base path (`/api/articles`), status codes (201 / 204 / 404), or the
> not-found behavior. The Agent chose idiomatic REST conventions on its own.

**Evaluation: Good — idiomatic REST defaults**

### 4.4 DTO / Entity Exposure Decision

The Agent chose **not** to expose JPA entities directly. Instead it
introduced inner records:

- `CreateArticleRequest`
- `ArticleResponse`

Its stated reasoning was to keep the API layer clean and avoid lazy-loading
serialization issues with JPA entities.

> This is a significant autonomous architectural decision. Directly returning
> entities is a common default; the Agent instead added a request/response
> boundary. Notably, this decision was made **without** any rule requiring it
> — worth watching for consistency across branches and future tasks.

**Evaluation: Good — avoided a common anti-pattern autonomously**

---

## 5. Test Strategy

The Agent created tests for the API layer:

- `ArticleControllerTest` — 7 tests using `@WebMvcTest` with a mocked service
  and repository, covering all endpoints plus error cases (author not found on
  create, article not found on get, author not found on list).

### Error detection and recovery

During execution the Agent hit a Spring Boot 4.x compatibility issue:

- It initially used `@WebMvcTest`, but the annotation was not available from
  the expected test starter.
- After inspecting what was available (`Ran 4 commands`), it discovered the
  package had moved in Spring Boot 4.x and corrected the import to
  `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`.

> This mirrors the Task 001 pattern where the Agent adapted to Spring Boot
> 4.x test-infrastructure changes (`@DataJpaTest` → `@SpringBootTest`). Again
> the Agent detected the problem and recovered without human intervention.

The final result was:

**18 tests passed** (7 new controller tests + 11 existing)

**Evaluation: Good error detection and recovery; good test coverage including error cases**

---

## 6. Scope Compliance

The Agent was instructed not to implement anything beyond the described REST
API.

Beyond the requested controller, it introduced only `AuthorRepository` — a
directly supporting layer required for the create operation. It did not create:

- Authentication ❌
- Authorization ❌
- Update endpoints ❌
- Pagination ❌
- A separate global exception handler ❌ (handled not-found inline via status codes)

The implementation remained focused on the controller, its supporting
repository, request/response records, and tests.

**Evaluation: Good**

---

## 7. Files Created and Modified

### Created

- `AuthorRepository.java`
- `ArticleController.java` (with inner `CreateArticleRequest` / `ArticleResponse` records)
- `ArticleControllerTest.java`

Line counts reported: +91 (repository + controller), +141 (controller test),
+1/−1 (import fix).

### Modified

- `ArticleControllerTest.java` — corrected `@WebMvcTest` import for Spring Boot 4.x

---

## 8. Autonomous Decisions

| Decision | Explicitly Requested? | Evaluation |
|---|---|---|
| Create `AuthorRepository` | No | Good |
| Base path `/api/articles` | No | Good |
| Return 201 on create | No | Good |
| Return 204 on delete | No | Good |
| Return 404 on not found (article / author) | No | Good |
| Use request/response records instead of exposing entities | No | Good |
| `@WebMvcTest` with mocked collaborators | No | Good |
| Cover error cases in tests | No | Good |

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
Final Context            : 61.7k / 200k
Context Usage            : 31%
Tests Passed             : 18
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

## 10. Key Learning from Task 003 (CLAUDE.md)

Task 003 pushed the Agent into controller territory, where — as anticipated —
it had to make a large number of unspecified default decisions. Under
`CLAUDE.md` guidance alone, the Agent made consistently idiomatic choices:
REST-standard status codes (201 / 204 / 404), a clean `/api/articles` base
path, and notably a **request/response record boundary instead of exposing
JPA entities** — avoiding a common anti-pattern without being told to.

The most important observation for the experiment series is that these were
all **autonomous decisions not governed by any rule.** They happen to be good
defaults here, but they are exactly the kind of decisions a project might want
to *guarantee* rather than leave to chance — for example, always using a DTO
boundary, or a consistent not-found strategy. This makes Task 003 a strong
candidate source for the **next reactive Rule**: any point where the branches
diverge (e.g. entity-exposure choice, status-code conventions, or not-found
handling) is a place where a Rule could enforce consistency.

The Agent again demonstrated Spring Boot 4.x error recovery (correcting the
`@WebMvcTest` import), consistent with earlier tasks.

To complete the comparison, the same task should be run on the no-guidance
branch and the CLAUDE.md + Rules branch, and the controller-level decisions
(status codes, DTO vs. entity, not-found handling) compared across all three.
