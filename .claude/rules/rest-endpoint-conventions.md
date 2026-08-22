# REST Endpoint Conventions

## Rule

REST endpoints in this project follow standard REST conventions.

### URLs and resources

- **Base path:** controllers are mounted under `/api/{resource}`, using a
  **plural noun** for the resource (for example, `/api/articles`).
- **A single resource** is addressed by its id as a path variable:
  `/api/articles/{id}`.
- **Filtering a collection** (including listing by a related field) uses a
  **query parameter**, not a path segment. The collection endpoint serves both
  the full list and the filtered list:
  - `GET /api/articles` — all articles
  - `GET /api/articles?authorId={authorId}` — articles for one author
  - Do **not** encode the filter as a path segment
    (`/api/articles/by-author/{authorId}`).

### HTTP methods and status codes

- `POST /api/articles` — create. Returns **201 Created** with a `Location`
  header pointing at the new resource (`/api/articles/{id}`), and the created
  representation in the body.
- `GET` — retrieve. Returns **200 OK**. A single-resource GET returns
  **404 Not Found** when the id does not exist.
- `DELETE /api/articles/{id}` — delete. Returns **204 No Content** on success.
  Deleting a non-existent id returns **404 Not Found** (the client is told the
  resource was not there).
- Return **404 Not Found** when a referenced resource (such as the author on
  create) does not exist. Reserve **400 Bad Request** for malformed input /
  validation failures.

### General

- Endpoints are thin: they delegate to the service layer and do not contain
  business logic.
- Accept and return DTOs, never entities (see the API request/response
  boundary rule).

## Rationale

Without a fixed convention, the "list by author" endpoint diverged between
runs — one used a path variable (`/by-author/{id}`), another a query
parameter. Filtering a collection is conventionally expressed as a query
parameter, which also lets a single collection endpoint serve both the full
and filtered lists, so this rule fixes that form. Standardising the base path,
status codes (201 + `Location`, 204, 404 vs. 400), and the resource/filter
URL shape keeps the API contract predictable and idiomatic across features and
across Agent runs.
