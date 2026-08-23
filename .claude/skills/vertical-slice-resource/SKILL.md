---
name: vertical-slice-resource
description: Use this skill whenever a task asks to add a new REST resource / entity to the Article Management System — anything of the form "add a X resource", "create an entity for X", "expose X over the API", or a request to wire a new persisted concept through the full stack. It codifies the exact multi-step, multi-layer procedure for building one vertical slice (entity → persistence → service → API → tests) in the correct order, so the slice is complete and consistent instead of assembled ad hoc. Trigger it even when the request only names the concept ("we need Comments", "let's track Tags") without spelling out the layers, because the layers are implied. This skill covers the PROCEDURE and ordering; it defers every convention decision (DTO placement, endpoint shape, status codes, test slicing, config safety) to the project Rules under .claude/rules/.
---

# Vertical Slice: Adding a REST Resource

## What this skill is for

Adding a new persisted concept to the Article Management System is not a single edit — it is a repeating, multi-layer procedure. The same shape recurred across the Article and Author work: an entity, its repository, a service, a controller with DTOs, and a sliced test suite, plus any relationship wiring to existing resources. When that procedure is executed from memory each time, the risk is not bad code — the agent writes reasonable code — it is an *incomplete or out-of-order* slice: a layer skipped, a relationship half-wired, tests missing for one tier, or the existing resource left untouched when it needed a new field.

This skill exists to make the procedure explicit and repeatable. It says **what to do and in what order**. It deliberately says almost nothing about **how each layer should look** — those decisions already live in the project Rules, and this skill points to them rather than repeating (or contradicting) them.

## Relationship to the Rules

This skill is the recipe; the Rules are the constraints. At each step, follow the relevant Rule:

- **`configuration-safety.md`** — never change Java version, dependencies, or other config to make something compile. If a conflict appears, stop and ask.
- **`api-request-response-boundary.md`** — never expose entities from controllers; DTOs live in a separate `dto` package as top-level records with explicit `from()` mapping.
- **`rest-endpoint-conventions.md`** — `/api/{resource}` plural, filtering via query param, `201 + Location` on create, `204` on delete, `404` on delete-not-found, and the `400` vs `404` distinction.
- **`testing-conventions.md`** — no `EntityManager` in tests; use the repository; slice the tests (`@DataJpaTest` / mocked-service unit test / `@WebMvcTest`).

If this skill and a Rule ever seem to disagree, the Rule wins — tell the user about the mismatch so the skill can be corrected.

## The procedure

Work top-down through the layers in this order. Do not skip ahead to the controller before the entity and service exist; each layer depends on the one before it, and building out of order is where slices go wrong.

### 1. Plan the slice before writing code
State, briefly, the pieces this specific resource needs: the entity's fields and which are required, whether it relates to any existing resource and in which direction, which endpoints are in scope, and whether an existing resource must gain a field to hold the relationship. Confirm the Java version target matches config (per `configuration-safety.md`) — do not silently adjust it.

### 2. Entity
Create the entity in the `model` package. Mark required fields as non-nullable; leave optional fields nullable. Add invariants that clearly belong to the concept (e.g. a unique natural key) but do not invent constraints the task did not imply — note them to the user if you add any.

### 3. Persistence
Add the Spring Data JPA repository. Add derived query methods only for the reads this slice actually needs (e.g. a lookup by a relationship id). Do not pre-build queries no endpoint calls.

### 4. Relationship wiring (only if the resource relates to an existing one)
This is the step most often left half-done. If the new resource relates to an existing one:
- add the owning-side field to the correct entity (per the relationship direction chosen in step 1),
- extend the existing resource's repository/service with the read the relationship enables,
- and remember that touching the existing resource means its DTO/response and tests may also need updating.
Handle both sides of what the task asked (e.g. "assign a category to an article" *and* "list the articles in a category") — these are two separate operations, easy to implement one and forget the other.

### 5. Service
Create the service with exactly the operations the endpoints require (typically create / find-by-id / find-all / delete, plus any relationship operations). Keep business rules here, not in the controller. Return domain results; let the controller handle HTTP shape.

### 6. API layer (controller + DTOs)
Create the controller and its DTOs. Per the Rules: DTOs go in the separate `dto` package as top-level records with explicit `from()` mapping — never expose the entity. Apply the endpoint conventions (plural path, `201 + Location`, `204`, `404` semantics, query-param filtering). Represent a relationship in the response the way the existing controllers do; if there is no precedent, choose the flattest representation that satisfies the task and note the choice.

### 7. Tests (sliced)
Write tests for every layer you touched, sliced per `testing-conventions.md`:
- entity persistence tests,
- service tests (mocked repository for unit behavior, or `@DataJpaTest`-backed where persistence matters),
- controller tests with `@WebMvcTest`.
If step 4 modified an existing resource, add or update that resource's tests too — a slice is not complete if the change it caused to a neighbor is untested.

### 8. Verify
Run the full suite yourself and report the result (counts, pass/fail). Do not claim success without running it. If something fails to compile, fix the code — do not change config to force it through (per `configuration-safety.md`).

## Completeness checklist

Before declaring the slice done, confirm every item. A missing item is the failure mode this skill is meant to prevent:

- [ ] Entity created, required/optional fields correct
- [ ] Repository created, only needed queries added
- [ ] If related: owning-side field added AND existing resource's read exposed AND both directions of the requested operation implemented
- [ ] Service covers exactly the needed operations
- [ ] Controller present; DTOs in `dto` package, entity not exposed
- [ ] Endpoint conventions applied (path, status codes, Location, filtering)
- [ ] Tests for every touched layer, sliced correctly
- [ ] Existing resource's tests updated if it was modified
- [ ] Full suite run and result reported
- [ ] No config changed to make things compile

## Example (shape, not prescription)

**Task:** "Add a Category resource; an article optionally belongs to a category; support assigning a category to an article and listing a category's articles."

**Slice this produces:**
- `model/Category` (name required + unique, description optional)
- `repository/CategoryRepository`
- relationship: `@ManyToOne` category field on `Article`; `findByCategoryId` on the Article side; `assignCategory` + `findByCategory` exposed
- `service/CategoryService` (create / findById / findAll / delete)
- `controller/CategoryController` + DTOs in `dto` package; `/api/categories` CRUD; `GET /api/categories/{id}/articles`; `PUT /api/articles/{id}/category`
- sliced tests for Category's three layers *and* updated Article tests for the new field/endpoint

The point of the example is the *ordering and completeness*, not these exact names — a different resource fills the same skeleton differently.
