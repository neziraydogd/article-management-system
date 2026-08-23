# Task 004 — Category Resource (full-guidance + Skill present) — Evaluation

## Execution Environment
- **Agent:** Claude Code, Opus 4.6, medium reasoning
- **Branch:** full-guidance (`CLAUDE.md` + 4 Rules) **with `vertical-slice-resource` Skill installed** at `.claude/skills/vertical-slice-resource/SKILL.md`
- **Skill triggered?** **No.** See Skill Triggering Analysis — no marker of Skill consultation appears in the run.
- **Project:** Article Management System (Spring Boot; note: Spring Boot 4.x in this run — see Rule Interaction)
- **Language target:** Java 17 (explicit in prompt)
- **Context at start:** 0
- **Context window at completion:** 79k / 200k (40%)
- **Prompt:** identical plain-paragraph prompt used in both prior Task 004 runs (only the Skill's presence changed)

## Initial State
Repository held Article + Author (Task 001), Article service (Task 002), Article controller (Task 003), the 4 Rules, and — new this run — the `vertical-slice-resource` Skill on disk. Test baseline before this task: 19 tests. Small, consistent single-slice codebase, as before.

## Task
Add a full `Category` resource (entity, persistence, service, REST CRUD), wire an optional article→category relationship, expose assign-category-to-article and list-articles-in-category. Tests required. No procedural steps prescribed in the prompt.

## Agent Behavior
The run opened with the same exploration-first narration as both prior runs ("explore the current codebase structure first, then implement the full Category slice"). The Agent sequenced the work correctly — Category slice first, then Article-side integration, then tests — and handled both directions of the relationship (assign on create via optional `categoryId`, and filter articles by category). It also proactively added a `findAll()` to `ArticleService` when it realized the unfiltered list endpoint needed it.

Behaviorally this run is **indistinguishable from the Skill-absent full-guidance run**. The correct ordering, both-directions handling, and neighbor-test updates are all present — but they were equally present without the Skill, and the run shows no sign the Skill produced them.

## Skill Triggering Analysis
**The Skill did not trigger.** Evidence:
- The process narration contains no reference to reading `SKILL.md`, consulting `vertical-slice-resource`, or following a named "procedure" / "completeness checklist."
- The opening and step language matches the earlier Skill-absent run almost verbatim.
- The context breakdown shows the 4 Rules loaded as Memory files but no Skill in context — expected, because Skills load on demand, but combined with the narration it confirms the Skill was never consulted.

**Why this is the expected outcome, not a setup error.** Skills are consulted at the Agent's discretion based on their description, and Claude consults a skill mainly for tasks it cannot already handle well. Here the 4 Rules plus an internally consistent codebase already let the Agent produce a complete, correctly-ordered slice on its own. From the Agent's vantage point there was no capability gap for the Skill to fill, so it proceeded directly — the classic skill **undertriggering** case that the skill-authoring guidance explicitly warns about. The Skill being *correct* is not enough; it also has to be *needed at the moment of decision*, and on this codebase it is not.

This is itself the key finding of the run (see Key Learning): on a small consistent codebase, a procedural Skill is not merely redundant in effect — it is not even invoked, because nothing signals to the Agent that the procedure is at risk.

## Rule Interaction (new observation) — testing-conventions vs configuration-safety
This run surfaced the first genuine **conflict between a Rule and the actual project config**, and the resolution is a clean demonstration of two Rules interacting:
- `testing-conventions.md` prescribes sliced tests including `@DataJpaTest`.
- The Agent attempted `@DataJpaTest`, then discovered the project has no JPA test starter (only a webmvc test starter is on the test classpath), and that Spring Boot 4.x had relocated the annotation.
- Per `configuration-safety.md`, the Agent did **not** add a dependency or alter config to satisfy the testing Rule. Instead it fell back to the existing `@SpringBootTest` pattern already used by the entity tests.

So when the testing Rule and config safety collided, **config safety won**, exactly as the Skill's own "if a Rule and reality disagree, don't change config" guidance (and `configuration-safety.md`) intend. The Agent honored the *spirit* of the testing Rule (sliced, repository-based, no `EntityManager` misuse) while not letting it force an unsafe config change. This is arguably the most substantive guidance effect in the entire Task 004 series — not a convention imposed on open ground, but two Rules resolving a real conflict correctly.

## Test Strategy
- `CategoryEntityTest` — 4 persistence tests, `@SpringBootTest` (fallback from `@DataJpaTest` due to the config conflict above)
- `CategoryServiceTest` — 6 unit tests, Mockito
- `CategoryControllerTest` — 7 MockMvc tests
- `ArticleControllerTest` — updated to 12 tests (category scenarios)
- `ArticleServiceTest` — updated; note it used `EntityManager` directly and had to be adjusted for the new 4-arg `create` signature and `findByCategory`
- Full suite: **43 tests, all passing** (up from 19), Agent-run.

## Scope Compliance
High. Everything requested delivered; existing Article resource extended only as far as the relationship required (new field, DTO fields, repository query, controller wiring, updated tests). The extra `findAll()` on `ArticleService` was a necessary consequence of the unfiltered-list endpoint, not scope creep.

## Files
**New**
| File | Role |
|---|---|
| `model/Category.java` | Entity: `name` (required), `description` (optional) |
| `repository/CategoryRepository.java` | JPA repository |
| `service/CategoryService.java` | create / findById / findAll / deleteById |
| `dto/CreateCategoryRequest.java` | Request record |
| `dto/CategoryResponse.java` | Response record with `from()` factory |
| `controller/CategoryController.java` | `/api/categories` REST |
| `model/CategoryEntityTest.java` | 4 persistence tests (`@SpringBootTest`) |
| `service/CategoryServiceTest.java` | 6 Mockito unit tests |
| `controller/CategoryControllerTest.java` | 7 MockMvc tests |

**Modified**
| File | Change |
|---|---|
| `model/Article.java` | Optional `@ManyToOne` category |
| `repository/ArticleRepository.java` | `findByCategory()` |
| `service/ArticleService.java` | `create` takes optional category; added `findAll()` + `findByCategory()` |
| `dto/CreateArticleRequest.java` | Optional `categoryId` |
| `dto/ArticleResponse.java` | `categoryId` + `categoryName` (null when none) |
| `controller/ArticleController.java` | categoryId on create (404 if missing); `GET /api/articles?categoryId=` alongside `?authorId=`, both optional |
| `controller/ArticleControllerTest.java` | 12 tests |
| `service/ArticleServiceTest.java` | Adjusted for 4-arg create + category tests |

## Autonomous Decisions
1. **DTOs in `dto` package as records with `from()` factory** — matches `api-request-response-boundary.md`.
2. **`201 + Location`, `204/404`** on category endpoints — matches `rest-endpoint-conventions.md`.
3. **Query-param filtering** — `GET /api/articles?categoryId=` added alongside the existing `?authorId=`, both optional. Consistent with the Rule and with the earlier Skill-absent run.
4. **`404` on unknown categoryId at create** — the `400`-vs-`404` distinction applied to an unprompted input.
5. **`@SpringBootTest` fallback for entity tests** — forced by the config conflict; chose config safety over literal Rule compliance.
6. **Category-on-create modeling** (optional `categoryId` in create request) plus category filter — same modeling as the Skill-absent full-guidance run.

## Metrics — three-branch comparison
| Metric | No-guidance | Full-guidance (no Skill) | Full-guidance (Skill present) |
|---|---|---|---|
| Context at completion | 63.8k (32%) | 72.3k (36%) | 79k (40%) |
| Skill triggered | n/a | n/a | **No** |
| DTO location | Inline `ArticleResponse` | `dto` package | `dto` package |
| Create status/header | Unreported | 201 + Location | 201 + Location |
| Delete semantics | Unreported | 204 / 404 | 204 / 404 |
| List-by-category | Sub-resource path | Query param | Query param |
| Relationship on create | Separate assign endpoint | Optional `categoryId` | Optional `categoryId` |
| Test slicing | By imitation | By rule | By rule, with `@SpringBootTest` fallback |
| Rule↔config conflict hit | No | No | **Yes — resolved via config safety** |
| Suite | 51 | 39 | 43 |

The rising context cost across the three columns (63.8k → 72.3k → 79k) is worth noting for the presentation: each added guidance layer costs context even when it does not change the output. The Skill column is the most expensive and produced the least additional output effect — a concrete illustration of guidance's cost/benefit curve.

## Cross-Branch Comparison
The two full-guidance runs (Skill-absent vs Skill-present) are **behaviorally equivalent** on output: same DTO placement, same REST semantics, same relationship modeling, same query-param filtering. The only material difference between them — the `@SpringBootTest` fallback — was driven by a Rule↔config conflict, **not** by the Skill. Against the no-guidance baseline, both full-guidance runs diverge in exactly the Rule-attributable places predicted in the Task 004 baseline evaluation.

There is no observable Skill contribution in this run. That is not because the Skill is wrong — its content correctly describes what the Agent did — but because the Agent completed the procedure without ever consulting it.

## Key Learning
**A correctly-authored procedural Skill produced no measurable effect on this task, and — more pointedly — was never triggered, because on a small, internally-consistent codebase the Rules plus code-imitation already close the capability gap the Skill was meant to fill.** Two distinct findings sit inside this:

1. **Skill non-triggering is a first-class result.** Rules are always-on (they loaded into memory unconditionally); the Skill is on-demand and the Agent declined to demand it. This makes the Rule/Skill distinction concrete: a Rule shapes every run whether or not the Agent "feels" it needs it; a Skill only helps when the Agent recognizes a procedure it can't reliably do unaided. Manufacturing that recognition requires either a harder task or a codebase where imitation fails — neither of which this series has yet created.

2. **The richest guidance effect this task showed came from Rules interacting, not from the Skill.** The `testing-conventions` ↔ `configuration-safety` conflict, resolved in favor of config safety, is the clearest "guidance changed the decision" moment in Task 004 — and it happened entirely at the Rules layer.

For the presentation, the honest arc is: **CLAUDE.md → marginal (Task 002); Rules → demonstrable, and they even resolve conflicts among themselves correctly (Task 003–004); Skills → correct but, on a codebase this size, neither needed nor triggered.** The next experiment, if the goal is to see a Skill actually fire, must deliberately create the condition Skills exist for: a task or codebase where an unaided Agent would execute a known multi-step procedure *incompletely*. Until that condition exists, the disciplined conclusion is that this project does not yet warrant a Skill — which is exactly the observation-first principle applied honestly to a negative result.
