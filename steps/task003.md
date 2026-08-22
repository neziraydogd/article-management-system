# task002
claude code -> opus 4.6 -> medium -> contex 0

#  result 
Context window  61.7k / 200k (31%)

# # Task 003: Article REST API

Expose the article functionality over a REST API.

The API should allow clients to:
- Create a new article for a given author
- Retrieve a single article by its id
- List all articles by a given author
- Delete an article by its id

Requirements:
- Follow the existing structure and conventions in this project.
- Include tests for the API layer.

Do not implement anything beyond what is described above.

#  Process: 
I'll start by exploring the existing project structure and code to understand the conventions established so far.

Found files, read 7 files

Let me check for an AuthorRepository and application properties.

Searched code, found files, read application.properties

Now I have a clear picture of the codebase. I'll create the REST controller, an AuthorRepository (needed to look up authors when creating articles), and controller tests.

Created 2 files
+91
-0

Now I'll create the controller test.

Created
ArticleControllerTest.java
+141
-0

Now let me run the tests to verify everything works.

Ran a command

The @WebMvcTest annotation isn't available in this test starter. Let me check what's available.

Ran 4 commands

The package moved in Spring Boot 4.x. The correct import is org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest.

Edited ArticleControllerTest.java, ran a command
+1
-1

All 18 tests pass (7 new controller tests + 11 existing). Here's what I implemented:

New files:

AuthorRepository.java — JPA repository for looking up authors by id (needed by the controller to resolve authorId to an Author entity)
ArticleController.java — REST controller at /api/articles with:
POST /api/articles — create an article (request body: authorId, title, content), returns 201
GET /api/articles/{id} — retrieve a single article, returns 404 if not found
GET /api/articles?authorId={authorId} — list all articles by author, returns 404 if author not found
DELETE /api/articles/{id} — delete an article, returns 204
ArticleControllerTest.java — 7 tests using @WebMvcTest with mocked service and repository, covering all endpoints and error cases (author not found on create, article not found on get, author not found on list)

The controller uses inner records (CreateArticleRequest, ArticleResponse) to keep the API layer clean and avoid lazy-loading serialization issues with JPA entities.