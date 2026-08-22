# Task 002: Article Service

-> Context window 0
-> opus 4.6  medium
Context window 45.3k / 200k (23%)

# Task 002: Article Service

Create a service layer for managing articles.

The service should support:
- Creating a new article for a given author
- Retrieving an article by its id
- Listing all articles by a given author
- Deleting an article by its id

Requirements:
- Follow the existing structure and conventions in this project.
- Include tests for the service.

Do not implement anything beyond what is described above.

#  process:
I'll explore the existing project structure to understand conventions before implementing the service.
Ran a command, read 5 files
Now I have a clear picture of the project. It's a Spring Boot 4.1 project with JPA, H2, and Lombok. I need to create a repository, a service, and tests. Let me create these files.
Created 3 files
+163
-0
Now let me run the tests to verify everything works.
Ran Maven tests
All 14 tests pass (7 new service tests + 6 existing entity tests + 1 application context test).
Here's what I created:
ArticleRepository.java — Spring Data JPA repository with a findByAuthorId query method
ArticleService.java — Service with create, findById, findByAuthor, and deleteById methods
ArticleServiceTest.java — 7 integration tests covering all four operations plus edge cases (not found, empty list, author isolation)
