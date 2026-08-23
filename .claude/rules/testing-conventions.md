---
paths:
  - src/test/**
---

# Testing Conventions

## Rule

Tests in this project follow a consistent, best-practice style per layer.

### Persistence in tests

- Use **Spring Data JPA repositories** to arrange and verify persisted state
  (for example, `articleRepository.save(...)`, `articleRepository.findById(...)`).
- Do **not** inject or use `EntityManager` or `TestEntityManager` in tests.
  Tests should exercise the same data-access path the application uses
  (repositories), not a lower-level persistence API.

### Test types per layer

- **Repository / persistence tests:** use `@DataJpaTest` (sliced, in-memory,
  fast) where the goal is to verify repository queries and mappings. Fall back
  to `@SpringBootTest` only when a slice is genuinely insufficient.
- **Service tests:** unit-test the service with its collaborators **mocked**
  (Mockito). Do not stand up the Spring context or the database to test
  service logic.
- **Controller / API tests:** use `@WebMvcTest` with the service (and any
  repository collaborators) mocked. Do **not** use a full `@SpringBootTest` +
  MockMvc integration test for controller-layer tests.

### General

- **Assertions:** use AssertJ.
- **Isolation:** each test is independent and does not rely on state left by
  another test.
- **Coverage:** every new layer includes tests for the happy path and the
  relevant error cases (for example, not-found on retrieve, referenced
  resource not found on create).
- Prefer the narrowest test slice that still gives confidence; reserve
  full-context (`@SpringBootTest`) tests for genuine end-to-end scenarios.

## Rationale

The goal is a fast, well-layered test suite that mirrors how the application
actually accesses data. Persisting through repositories (not `EntityManager`)
keeps tests aligned with production data access. Slicing tests per layer
(`@DataJpaTest`, mocked service unit tests, `@WebMvcTest`) keeps each test
focused and fast, and avoids the slow, brittle habit of loading the whole
Spring context for every test.

Two divergences motivated this rule. First, controller tests varied between
`@WebMvcTest` and a full `@SpringBootTest` + MockMvc integration test. Second,
the existing entity/service tests were written against `EntityManager`. This
rule fixes the project on the best-practice approach (repositories + sliced
tests) rather than freezing whatever an earlier run happened to produce.
