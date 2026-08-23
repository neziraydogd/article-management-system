# Task 005 — Comment Resource (full-guidance + Skill + Subagent present) — Evaluation

## Execution Environment
- **Agent:** Claude Code, Opus 4.6, medium reasoning
- **Branch:** full-guidance (`CLAUDE.md` + 4 Rules + `vertical-slice-resource` Skill + `security-reviewer` subagent all installed)
- **Skill triggered?** **No** (consistent with Task 004 — see Skill Triggering Analysis)
- **Subagent invoked?** **Yes**, but by explicit prompt instruction, not proactively — see Subagent Analysis
- **Project:** Article Management System (Spring Boot 4.1.0, Java 17)
- **Context at start:** 0
- **Context window at completion:** 79.4k / 200k (40%)
- **Prompt:** single-branch (learning phase, not a two-branch measurement). Plain paragraph, no procedural steps prescribed, with an explicit final instruction to run the security-reviewer subagent.

## Initial State
Repository held Article + Author (Task 001), Article service (Task 002), Article controller (Task 003), Category + relationship (Task 004), the 4 Rules, the `vertical-slice-resource` Skill, and — new this cycle — the `security-reviewer` subagent under `.claude/agents/`. Test baseline before this task: 43 tests.

## Task
Add a full `Comment` resource (entity, persistence, service, REST CRUD), wire a one-to-many article→comment relationship (comment belongs to a single article; author name + body text), expose add-comment-to-article and list-comments-on-article, include tests, then have the security-reviewer subagent review the result. No procedural steps prescribed.

## Purpose of this task
Unlike Tasks 001–004, this was not a divergence measurement. It was a **learning run** designed to observe all three guidance mechanisms operating together in a single execution: Rules and the Skill shaping implementation, and the subagent reviewing the output in an isolated context. The value is in watching how each mechanism behaves, not in comparing branches.

## Agent Behavior
The run opened with the same exploration-first narration seen in every prior task ("understand the current codebase structure, then implement the Comment resource as a full vertical slice"). The Agent sequenced the work correctly — read existing patterns and test files to match them, created source files, then tests, then ran the suite. Behavior was routine and indistinguishable in shape from the Category run: a familiar slice executed by pattern-matching against the existing codebase.

## Three-Mechanism Observation

### 1. Skill — did not trigger (consistent finding)
The process narration contains no reference to reading `SKILL.md`, invoking `vertical-slice-resource`, or following a named "procedure"/"completeness checklist." The language matches the Skill-absent runs almost verbatim. This is now the **second consecutive confirmation** (Task 004 and Task 005) that on a small, internally-consistent codebase the Agent completes a familiar vertical slice without consulting the Skill. The procedural gap the Skill was authored to fill does not exist for a routine slice here, so the on-demand Skill is never demanded. Skill undertriggering is, at this point, an established observation in the series rather than a one-off.

### 2. Rules — invisible but pervasive (working as designed)
Every Rule left its fingerprint on the output, without any narration announcing them:
- **DTO boundary** (`api-request-response-boundary.md`): `CreateCommentRequest` and `CommentResponse` created as records in the `dto` package with a `from()` factory — entity never exposed.
- **Endpoint conventions** (`rest-endpoint-conventions.md`): `/api/comments` plural; `POST` returns 201 + Location; `DELETE` returns 204, or 404 when absent; list-by-article via query param `?articleId=`, 404 when the article is missing.
- **Test slicing** (`testing-conventions.md`): entity / service / controller tests split by layer.
- **Relationship**: correct `@ManyToOne` direction (comment owns the FK to article), `findByArticle` on the repository.

**Rule↔config conflict — recurred and resolved identically to Task 004.** The Agent attempted `@DataJpaTest` (per the testing Rule), found it absent from the classpath (only `spring-boot-starter-webmvc-test` is present in Spring Boot 4.x), and — per `configuration-safety.md` — did **not** add a dependency. It fell back to the existing `@SpringBootTest @Transactional` pattern. The two Rules interacted and config-safety won, exactly as before. The reproducibility of this interaction across two tasks is itself evidence of Rule reliability.

Result: **61 tests, all passing** (18 new Comment tests + 43 existing), Agent-run.

### 3. Subagent — the notable mechanism this run
The `security-reviewer` subagent ran and behaved exactly as designed on three dimensions:

- **Context isolation worked.** The narration shows the reviewer running in the background ("The security reviewer is still running... I'll share its findings as soon as it completes") while the main flow continued. The verbose analysis stayed in the subagent's own window; only a prioritized summary of findings returned to the main conversation. This is the core context-economy behavior subagents exist for, observed live.
- **The custom system prompt was applied faithfully.** Findings came back grouped Critical / Warning / Suggestion. It caught the stack-specific pitfalls the agent file was tuned for: H2 console exposure (Critical, with the `CREATE ALIAS` RCE note), absence of Spring Security / unauthenticated endpoints, missing `@Valid` / Bean Validation, unbounded `findAll()`, missing `@ControllerAdvice`, and the `LazyInitializationException` risk. These map directly to the checklist authored into the subagent — confirming a custom subagent loads and uses its own prompt.
- **Mature triage: pre-existing vs. change-specific.** The reviewer explicitly separated project-wide pre-existing concerns (H2 console, no auth — items 1–2) from the actionable Comment-specific findings (missing validation, unbounded queries, no global error handler — items 3–5). This is the behavior that keeps a security reviewer useful instead of noisy: it flags the standing issues once but foregrounds what this change actually introduced.

**Custom subagents are guidance-aware.** The reviewer respected the project's Rules — it framed entity exposure and dependency-addition findings in terms of the existing rules (e.g. noting that adding `spring-boot-starter-security` must be user-approved per `configuration-safety.md`). This is the concrete payoff of a *custom* subagent over the built-in Explore agent: built-in Explore and Plan skip CLAUDE.md and Rules, whereas this custom agent loaded and reinforced them.

## Files
**New (6 source + 3 test)**
| Layer | File | Purpose |
|---|---|---|
| Entity | `model/Comment.java` | `@ManyToOne` to Article, `authorName`, `body`, auto-set `createdAt` |
| Repository | `repository/CommentRepository.java` | `JpaRepository` + `findByArticle(Article)` |
| Service | `service/CommentService.java` | `create`, `findById`, `findAll`, `findByArticle`, `deleteById` |
| DTOs | `dto/CreateCommentRequest.java`, `dto/CommentResponse.java` | Request/response records with `from()` |
| Controller | `controller/CommentController.java` | `/api/comments` REST |
| Tests | `model/CommentEntityTest.java`, `service/CommentServiceTest.java`, `controller/CommentControllerTest.java` | 18 tests |

**API endpoints:** `POST /api/comments` (201 + Location), `GET /api/comments/{id}` (404 if absent), `GET /api/comments`, `GET /api/comments?articleId={id}` (404 if article absent), `DELETE /api/comments/{id}` (204 / 404).

## Metrics
| Metric | Value |
|---|---|
| Context at completion | 79.4k / 200k (40%) |
| New files | 9 (6 source + 3 test) |
| Total tests | 61 (18 new + 43 existing), all passing |
| Skill triggered | No |
| Subagent invoked | Yes (explicit instruction) |
| Subagent context isolation | Confirmed (ran in background, returned summary only) |
| Rule↔config conflict | Recurred, resolved via config-safety (identical to Task 004) |

## Key Learning
This run let all three mechanisms be observed at once, and each showed its defining behavior clearly:

1. **Skill teaches the procedure but its invocation is not guaranteed.** Second consecutive non-trigger on a routine slice. On this codebase, Rules + code-imitation already produce a complete, correctly-ordered slice, so the Skill is never demanded.
2. **Rules shape every run, silently and reliably.** No narration, but every Rule left a fingerprint, and the two-Rule config-safety interaction reproduced exactly. Rules are the always-on layer.
3. **The subagent isolates the work but its invocation is optional.** It ran only because the prompt explicitly asked for it. Context isolation and the custom prompt both worked, and — being a custom agent — it was guidance-aware where the built-in Explore agent would not be. But nothing guaranteed it would run.

The line that matters for what comes next: the security review happened **because we asked for it by name**. A proactive `description` does not guarantee invocation; it still depends on the model's judgment or an explicit request. This is precisely the boundary a **Hook** addresses — an event-bound, deterministic gate that runs unconditionally, whether or not the model decides to. The series has now seen, live, the industry framing that separates the three: **Skill teaches the how, Subagent isolates the work, Hook enforces the rule.** The natural next step is to build the same security concern as a Hook and observe the difference between "smart but optional" (subagent) and "deterministic but unconditional" (hook).
