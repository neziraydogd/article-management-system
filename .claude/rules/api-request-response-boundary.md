# API Request/Response Boundary

## Rule

The REST API layer must not expose JPA entities directly in request or
response bodies. Every controller endpoint that accepts or returns data must
use dedicated Data Transfer Objects (DTOs).

Convention for this project:

- Request and response DTOs are defined as **top-level types in a dedicated
  `dto` package** (for example, `com.na.article.<feature>.dto`), not as inner
  records inside the controller.
- DTOs are implemented as Java `record` types.
- Naming: request types end with `Request` (e.g. `CreateArticleRequest`),
  response types end with `Response` (e.g. `ArticleResponse`).
- Do **not** return `Article`, `Author`, or any other entity from a
  controller method.
- Mapping between entities and DTOs is done explicitly (a static factory
  method on the DTO, e.g. `ArticleResponse.from(article)`, or a dedicated
  mapper) — not by exposing the entity.

## Rationale

Returning JPA entities directly from controllers exposes the persistence
model over the API and can cause lazy-loading serialization problems. A DTO
boundary keeps the API contract independent of the entity model.

DTOs are kept in a dedicated `dto` package rather than as controller inner
records because:

- They are reusable across controllers, services, and tests without coupling
  to a single controller class.
- They keep the controller focused on request handling rather than type
  definitions, avoiding bloated controller files.
- A clear package boundary makes the API contract easy to locate and review,
  which is the conventional clean-architecture layout for a Spring project.

This rule exists because, without it, the Agent placed DTOs inconsistently
(inner records in one run) and had to guess where they should live. The rule
removes the guess and fixes the project on the cleaner, more maintainable
convention.
