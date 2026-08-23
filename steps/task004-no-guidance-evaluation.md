# Task 004 — Category Resource (no-guidance baseline) — Evaluation

## Execution Environment
- **Agent:** Claude Code, Opus 4.6, medium reasoning
- **Branch:** no-guidance baseline (no `CLAUDE.md`, no `.claude/rules/`, no Skills)
- **Project:** Article Management System (Spring Boot)
- **Language target:** Java 17 (specified explicitly in prompt)
- **Context window at completion:** 63.8k / 200k (32%)
- **Prompt:** identical to the full-guidance branch (guidance is the only intended independent variable)

## Initial State
Before this task the repository already contained the Article + Author slice (Task 001), the Article service (Task 002), and the Article REST API / controller (Task 003). Critically for this run, the codebase is a **single, small, internally consistent slice**: one entity family, one repository pattern, one service pattern, one controller pattern, all visible in a handful of files. There is no competing pattern for the Agent to choose between.

## Task
Add a full `Category` resource: entity (name required, description optional), persistence, service, and REST API for create / list-all / get-by-id / delete. Additionally wire a one-to-many-style relationship so an article optionally belongs to a category, expose assigning a category to an article, and expose listing the articles within a category. Tests required. No procedural steps, layer order, DTO placement, endpoint shape, or test style were prescribed in the prompt — those were left open on purpose so that any divergence would come from guidance (or its absence), not from instructions.

## Agent Behavior
The Agent opened with an explicit **exploration-first** approach rather than generating code immediately. Its stated process was: explore the existing structure, read the specific files it would need to modify, implement new files plus modifications together, then run the tests to verify. In prose the Agent described reaching "a clear picture of the codebase" and "the full picture" before writing anything, and it framed its own work as *following the existing patterns*.

This is the central behavioral observation of the run: **with no guidance present, the Agent substituted the existing codebase as its guidance.** Pattern conformity that, in the full-guidance branch, would be produced by Rules was here produced by imitation of the surrounding code. Because the codebase is small and consistent, that imitation had a single unambiguous target to copy.

## Test Strategy
The Agent produced sliced tests without being told to, mirroring the layering it observed:
- `CategoryEntityTest` — 6 entity persistence tests
- `CategoryServiceTest` — 11 service / integration tests
- `CategoryControllerTest` — 13 controller tests

All new tests plus the pre-existing suite ran green: **51 tests, 0 failures, 0 errors.** The Agent ran the suite itself as a verification step rather than asserting success without evidence.

## Scope Compliance
Scope adherence was high. Every element the prompt asked for was delivered — Category entity, repository, service, controller, the four CRUD endpoints, the article↔category assignment, and the "articles in a category" listing — and nothing materially beyond scope was introduced. The Agent did touch existing Article-layer files (see Files), which was necessary to satisfy the relationship requirement rather than scope creep.

## Files
**New**
| File | Role |
|---|---|
| `model/Category.java` | Entity: `name` (required, unique), `description` (optional) |
| `repository/CategoryRepository.java` | Spring Data JPA repository |
| `service/CategoryService.java` | `create`, `findById`, `findAll`, `deleteById` |
| `controller/CategoryController.java` | `/api/categories` CRUD + `GET /{id}/articles` |
| `model/CategoryEntityTest.java` | 6 entity persistence tests |
| `service/CategoryServiceTest.java` | 11 service / integration tests |
| `controller/CategoryControllerTest.java` | 13 controller tests |

**Modified**
| File | Change |
|---|---|
| `model/Article.java` | Added optional `@ManyToOne` category field |
| `repository/ArticleRepository.java` | Added `findByCategoryId` |
| `service/ArticleService.java` | Added `findByCategory` and `assignCategory` |
| `controller/ArticleController.java` | Added `PUT /{id}/category`; expanded `ArticleResponse` with `categoryId` / `categoryName` |

## Autonomous Decisions
Even without guidance, the Agent made several unprompted decisions — but every one of them coincided with the conventions already established in the existing code rather than diverging from them:
1. **Unique constraint on `name`.** Not requested; the Agent inferred it as a sensible category invariant.
2. **DTO / response shape.** It expanded the existing `ArticleResponse` and represented the relationship as `categoryId` + `categoryName` rather than a nested object — echoing the flat response style already present in the Article controller.
3. **Relationship representation.** It chose `categoryId` on assignment (`PUT /{id}/category`) and a derived repository query (`findByCategoryId`) instead of a bidirectional collection, keeping the ownership direction simple.
4. **Endpoint naming.** `/api/categories` (plural), `GET /{id}/articles` for the sub-resource — consistent with the Article controller's existing shape.
5. **Sliced test layout.** Entity / service / controller split, chosen by imitation of the existing test files.

The notable thing is what is *absent*: there was no "I'm guessing here" hedging of the kind seen in Task 003's no-guidance controller run (where DTO placement was uncertain). Here the surrounding code answered those questions for the Agent, so it proceeded without hedging.

## Metrics
| Metric | Value |
|---|---|
| Context window at completion | 63.8k / 200k (32%) |
| New files | 7 |
| Modified files | 4 |
| Total tests (suite) | 51, all passing |
| New tests added | 30 (6 + 11 + 13) |
| Self-run verification | Yes (Agent ran the suite) |
| Explicit hedging / guessing | None observed |

## Cross-Branch Comparison
The full-guidance run is not yet evaluated, so this section states the **prediction and its rationale** to be confirmed against that run:

The experimenter predicted *before* this run that divergence would be minimal, because (a) the codebase is a single small consistent slice, and (b) the prompt implicitly references the existing structure. This run supports that prediction: the Agent explicitly adopted "follow the existing patterns" as its operating principle, effectively using the codebase as a stand-in for guidance.

The methodological consequence is important. In Task 003, the controller layer produced divergence because there was genuine under-determination — multiple defensible choices with no established local precedent. By Task 004 that precedent **exists in the code itself**, so the no-guidance branch already converges toward the same conventions the Rules encode (flat DTO fields, plural `/api` endpoints, sliced tests). The expected delta between branches is therefore small and concentrated in the few places Rules speak but code is silent — most plausibly DTO *placement* (separate `dto` package as top-level records, per `api-request-response-boundary.md`) versus the inline/expanded `ArticleResponse` the Agent reused here, and possibly status-code and `Location`-header details on the new `POST /api/categories` (per `rest-endpoint-conventions.md`), which this baseline did not explicitly report.

| Dimension | No-guidance (this run) | Full-guidance (predicted) |
|---|---|---|
| Source of convention | Imitation of existing code | Rules (explicit) |
| DTO placement | Reused/expanded inline `ArticleResponse` | Separate `dto` package, top-level records, explicit `from()` |
| Endpoint shape | Plural `/api`, sub-resource path | Same, plus explicit 201 + `Location`, 204, 404 semantics |
| Test slicing | Sliced by imitation | Sliced by rule (`@DataJpaTest` / mocked unit / `@WebMvcTest`) |
| Config safety | Java 17 honored | Java 17 honored |
| Expected divergence | — | **Low**, confined to DTO placement + REST status/header details |

## Key Learning
**When the codebase is small and internally consistent, the codebase itself acts as guidance, and the marginal effect of Rules shrinks.** The no-guidance Agent did not produce arbitrary or defensible-but-different decisions here the way it did at the controller layer in Task 003; instead it read the surrounding code and conformed to it. This is a direct, live instance of the experiment's known limitation (a): comparability and signal both weaken as the two branches evolve on top of a shared, convention-bearing codebase.

Two implications for the series:
1. The strongest evidence for guidance's effect appears at points of genuine under-determination — new decision surfaces with no local precedent — not on repetitions of an already-established pattern. Vertical-slice repetition on a tidy codebase is, by construction, low-divergence.
2. This weakens the case for introducing a "vertical slice" **Skill** *on this codebase right now*: a Skill earns its place where a procedure recurs *and* the Agent executes it inconsistently. Here the procedure recurs but the Agent already executes it consistently by imitation. The honest reading is that the procedural gap a Skill would fill is currently narrow — worth revisiting only if a future task enlarges the codebase enough that imitation stops being a reliable guide, or if the full-guidance run reveals a procedural (not just conventional) divergence that Rules cannot express.
