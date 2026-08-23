# task002
claude code -> opus 4.6 -> medium -> contex 0

#  result 
Context window 79k / 200k (40%)

#   Add a new Category resource to the Article Management System. 
A category has a name and an optional description, and an article belongs to one category 
(an article's category is optional). Implement the full slice: the entity, persistence, service layer, 
and REST API for creating a category, listing all categories, fetching a single category by id, and deleting a category.
Also make it possible to assign a category to an article and to list the articles in a given category. 
Include tests. Java 17.

#  Process: 
I'll explore the current codebase structure first, then implement the full Category slice.

Good, I have a clear picture. I'll now implement the full Category slice. Let me read the key files I'll need to modify.

Now I'll create all the new files and modify existing ones. Starting with the Category entity, repository, service, DTOs, and controller — then updating Article to support the optional category relationship.

Now I'll update the Article entity to add the optional Category relationship, update the repository, service, controller, and DTOs.

Now updating the DTOs and the ArticleController to handle the optional category.

Now updating the ArticleController to handle the optional categoryId on create, and to support filtering articles by categoryId.

I need to add a `findAll` method to ArticleService since the GET /api/articles endpoint now supports no-filter listing.

Now creating all the test files.

Now I need to update the existing ArticleControllerTest to account for the changes (new constructor param, updated DTO, category fields in responses, and the new categoryId filter endpoint).

Now I need to update the existing ArticleServiceTest — it uses `EntityManager` directly and calls `articleService.create` with 3 args, which now takes 4 (with the optional category).

I also need to add `findByCategory` tests to ArticleServiceTest and add a test for `findByCategory` in the repository. Let me also add the findByCategory test to the service test.

Now let's run the tests.

Spring Boot 4.x moved `@DataJpaTest`. Let me check the correct package.

The project doesn't have a `spring-boot-starter-data-jpa-test` dependency. The existing entity test uses `@SpringBootTest`. Let me check the pom.xml for what test dependencies exist, and look at the existing entity test's approach.

The project doesn't have `@DataJpaTest` available — only `spring-boot-starter-webmvc-test` is in test scope, not a JPA test starter. The existing entity tests use `@SpringBootTest`. I'll switch the CategoryEntityTest to match the existing pattern using `@SpringBootTest` with repositories.

All 43 tests pass (up from 19). Here's what was implemented:

**New files:**
- [Category.java](src/main/java/com/na/article/model/Category.java) — entity with `name` (required) and `description` (optional)
- [CategoryRepository.java](src/main/java/com/na/article/repository/CategoryRepository.java) — Spring Data JPA repository
- [CategoryService.java](src/main/java/com/na/article/service/CategoryService.java) — create, findById, findAll, deleteById
- [CreateCategoryRequest.java](src/main/java/com/na/article/dto/CreateCategoryRequest.java) — DTO record
- [CategoryResponse.java](src/main/java/com/na/article/dto/CategoryResponse.java) — DTO record with `from()` factory
- [CategoryController.java](src/main/java/com/na/article/controller/CategoryController.java) — REST API at `/api/categories`
- [CategoryEntityTest.java](src/test/java/com/na/article/model/CategoryEntityTest.java) — 4 persistence tests
- [CategoryServiceTest.java](src/test/java/com/na/article/service/CategoryServiceTest.java) — 6 unit tests (Mockito)
- [CategoryControllerTest.java](src/test/java/com/na/article/controller/CategoryControllerTest.java) — 7 MockMvc tests

**Modified files:**
- [Article.java](src/main/java/com/na/article/model/Article.java) — added optional `@ManyToOne` category relationship
- [ArticleRepository.java](src/main/java/com/na/article/repository/ArticleRepository.java) — added `findByCategory()`
- [ArticleService.java](src/main/java/com/na/article/service/ArticleService.java) — `create()` now accepts optional category; added `findAll()` and `findByCategory()`
- [CreateArticleRequest.java](src/main/java/com/na/article/dto/CreateArticleRequest.java) — added optional `categoryId`
- [ArticleResponse.java](src/main/java/com/na/article/dto/ArticleResponse.java) — added `categoryId` and `categoryName` (null when no category)
- [ArticleController.java](src/main/java/com/na/article/controller/ArticleController.java) — handles categoryId on create (404 if not found); `GET /api/articles` now supports `?categoryId=` filter alongside `?authorId=`; both params are optional
- [ArticleControllerTest.java](src/test/java/com/na/article/controller/ArticleControllerTest.java) — updated to 12 tests covering category scenarios

**API endpoints added:**
- `POST /api/categories` — 201 + Location
- `GET /api/categories` — list all
- `GET /api/categories/{id}` — 200 or 404
- `DELETE /api/categories/{id}` — 204 or 404
- `GET /api/articles?categoryId={id}` — filter articles by category