# task002
claude code -> opus 4.6 -> medium -> contex 0

#  result 
Context window 65.8k / 200k (33%)
 

#  Task 003: Article REST API Expose the article functionality over a REST API. The API should allow clients to:

* Create a new article for a given author
* Retrieve a single article by its id
* List all articles by a given author
* Delete an article by its id

Requirements:

* Follow the existing structure and conventions in this project.
* Include tests for the API layer.

Do not implement anything beyond what is described above.

#  Process: 
I'll explore the current project structure and existing code to understand the conventions before implementing the REST API.

Now I have a clear picture. I need to create:

1. `AuthorRepository` (needed to look up authors by ID for the create endpoint)
2. Request/Response DTOs in a `dto` package
3. `ArticleController` with the four endpoints
4. `@WebMvcTest` controller tests

Let me read the existing service and entity files for exact details.

Now I have all the context. I'll create the REST API layer: an `AuthorRepository` (for author lookup), DTOs, the controller, and tests.

Now let me write the controller test using `@WebMvcTest` with mocked dependencies.

Now let me run the tests to verify everything works.

The test dependency is `spring-boot-starter-webmvc-test` which may not include Jackson and `@WebMvcTest`. Let me check the pom.xml.

Let me check what the existing test files import to understand the available test packages.

Jackson moved to `tools.jackson` in this version. Let me find the correct packages.

Now I see the package changes in Spring Boot 4.x. Let me also check MockitoBean and the test result matchers.

Good, `MockitoBean` is in `org.springframework.test.context.bean.override.mockito` (correct). The issue is Jackson and `@WebMvcTest` packages. Let me also check the MockMvc request builders location.

Good, those haven't moved. Now let me fix the test file with the correct imports.

All 19 tests pass (8 new controller tests + 11 existing). Here's a summary of what was created:

**New files:**

- [AuthorRepository.java](src/main/java/com/na/article/repository/AuthorRepository.java) — JPA repository for `Author`, needed to resolve `authorId` to `Author` in the controller
- [CreateArticleRequest.java](src/main/java/com/na/article/dto/CreateArticleRequest.java) — request DTO record with `title`, `content`, `authorId`
- [ArticleResponse.java](src/main/java/com/na/article/dto/ArticleResponse.java) — response DTO record with a `from(Article)` factory method
- [ArticleController.java](src/main/java/com/na/article/controller/ArticleController.java) — REST controller at `/api/articles` with four endpoints:
    - `POST /api/articles` — creates an article, returns 201 + Location header (404 if author not found)
    - `GET /api/articles/{id}` — retrieves by id (404 if not found)
    - `GET /api/articles?authorId={authorId}` — lists articles by author (404 if author not found)
    - `DELETE /api/articles/{id}` — deletes by id, returns 204 (404 if not found)
- [ArticleControllerTest.java](src/test/java/com/na/article/controller/ArticleControllerTest.java) — 8 tests using `@WebMvcTest` with mocked service and repository, covering happy paths and error cases for all endpoints