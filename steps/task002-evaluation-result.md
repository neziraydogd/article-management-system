# Task 002 — Evaluation Result

## Experiment 1: No Agent Guidance

## Objective

Observe AI Agent behavior when implementing a service layer on top of the
existing domain model, still without any project-level guidance mechanisms.

At this stage, the project contains no:

- CLAUDE.md
- Rules
- Skills
- Subagents
- Hooks
- Output styles
- Custom system instructions

The purpose of this experiment is to observe how the Agent independently
designs a service layer, decides whether to introduce a repository, handles
error and not-found cases, injects dependencies, and structures its tests —
building on top of the Task 001 domain model.

---

## 1. Execution Environment

| Metric | Result |
|---|---|
| Agent | Claude Code |
| Model | Opus 4.6 |
| Effort Level | Medium |
| Guidance Level | No Agent Guidance |
| Initial Context | 0 |
| Final Context | 45.3k / 200k |
| Context Usage | 23% |
| Plan Usage (5h) | 24% |
| Plan Usage (Weekly) | 6% |
| Approximate Duration | ~5 minutes |
| Tests Passed | 14 |

---

## 2. Initial Project State

The project entered Task 002 carrying the completed Task 001 output:

- Java 17
- Spring Boot 4.1.0
- Maven, Jar packaging
- Spring Web, H2 Database, Lombok
- Spring Data JPA (added by the Agent in Task 001)
- `Article` and `Author` entities
- `ArticleEntityTest`

At the beginning of the experiment, the project contained the domain model
but no service, repository, or business-logic layer.

---

## 3. Task

The Agent was asked to implement a service layer for managing articles.

The service was required to support:

- Creating a new article for a given author
- Retrieving an article by its id
- Listing all articles by a given author
- Deleting an article by its id

The Agent was instructed to follow the existing structure and conventions of
the project, to include tests for the service, and not to implement anything
beyond what was described.

The complete task prompt is stored as the Task 002 prompt.

---

## 4. Agent Behavior

### 4.1 Project Inspection

The Agent first explored the existing project structure and conventions
before writing any code.

It reported:

> "Ran a command, read 5 files"

This demonstrates that the Agent again did not immediately start writing
code. It first attempted to understand the existing project and its stack.

**Evaluation: Good**

### 4.2 Stack Recognition

The Agent correctly identified the existing stack as Spring Boot 4.1 with  JPA, H2, and Lombok, and used this understanding to align the new code with the existing setup rather than introducing new patterns.

**Evaluation: Good**

### 4.3 Repository Decision

The task prompt asked only for a service and did not mention a repository.
The Agent independently decided that a repository was required to support the  requested operations and created:

- `ArticleRepository` — a Spring Data JPA repository with a `findByAuthorId`
  query method

This layer was not explicitly requested in the prompt.

**Evaluation: Good autonomous technical decision**

### 4.4 Service Implementation

The Agent created:

- `ArticleService`

with four operations matching the task:

- create (an article for a given author)
- findById
- findByAuthor
- deleteById

The service was implemented on top of the repository rather than accessing the entities directly, keeping the layering consistent.

**Evaluation: Good — correct service → repository layering**

### 4.5 Query Strategy

For the "list all articles by a given author" requirement, the Agent added a derived query method (`findByAuthorId`) on the repository rather than
implementing filtering logic inside the service.

This is idiomatic Spring Data JPA usage.

**Evaluation: Good autonomous design decision**

---

## 5. Test Strategy

The Agent created tests for the service.

It produced:

- `ArticleServiceTest` — 7 integration tests

These covered all four operations plus edge cases:

- not found
- empty list
- author isolation

The final result was:

**14 tests passed**

This included:

- 7 new service tests
- 6 existing entity tests
- 1 existing application context test

**Evaluation: Good test coverage including edge cases**

---

## 6. Scope Compliance

The Agent was instructed not to implement anything beyond the described service.

Beyond the requested service, it introduced only the repository — a directly
supporting layer required to fulfill the task. It did not create:

- Controllers ❌
- REST endpoints ❌
- DTOs / mappers ❌
- Authentication ❌
- Authorization ❌
- Update functionality (beyond create/find/list/delete) ❌

The implementation remained focused on the service, its supporting repository, and tests.

**Evaluation: Good**

---
## 7. Files Created and Modified

### Created

- `ArticleRepository.java`
- `ArticleService.java`
- `ArticleServiceTest.java`

Total: 3 files, +163 lines.

### Modified

- None

---

## 8. Autonomous Decisions

The following decisions were made by the Agent without explicit instructions:

| Decision | Explicitly Requested? | Evaluation |
|---|---|---|
| Create `ArticleRepository` | No | Good |
| Use derived query `findByAuthorId` | No | Good |
| Keep service on top of repository | No | Good |
| Cover not-found / empty / isolation edge cases in tests | No | Good |
| Number and type of tests (7) | Implied | Good |

---

## 9. Metrics

### Execution Metrics

```
Agent                    : Claude Code
Model                    : Opus 4.6
Effort                   : Medium
Guidance                 : None
Duration                 : ~5 minutes
Initial Context          : 0
Final Context            : 45.3k / 200k
Context Usage            : 23%
Plan Usage (5h)          : 24%
Plan Usage (Weekly)      : 6%
Tests Passed             : 14
```

### Quality Metrics

| Metric | Result |
|---|---|
| Requirement Understanding | Good |
| Project Inspection | Good |
| Scope Compliance | Good |
| Autonomous Decision Making | High |
| Test Generation | Good |
| Layering / Architecture | Good |
| Convention Alignment | Good |
| Unrequested Changes | 0 |
| Human Intervention | None |

---

## 10. Key Learning from Task 002 (No Guidance)

Building on the Task 001 finding, this experiment shows that the Agent
continues to perform substantial implicit architectural decision-making even
without any guidance mechanism.

The most notable observation is that the Agent **introduced a repository layer
on its own** — a layer never mentioned in the prompt — and correctly placed
the service on top of it, producing clean layering (service → repository)
without being told to. It did not over-engineer with interfaces, abstract
classes, or unnecessary patterns, and it stayed within scope.

This establishes the no-guidance baseline for Task 002, against which the
CLAUDE.md-guided run (and later Rules / Skills runs) will be compared using
the same metrics.
