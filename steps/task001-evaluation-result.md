# Task 001 — Evaluation Result

## Experiment 1: No Agent Guidance

### Objective

Establish a baseline for AI Agent behavior before introducing any project-level guidance mechanisms.

At this stage, the project contains no:

* `CLAUDE.md`
* Rules
* Skills
* Subagents
* Hooks
* Output styles
* Custom system instructions

The purpose of this experiment is to observe how the Agent independently interprets requirements, explores the project, makes technical decisions, handles errors, and implements the requested functionality.

---

## 1. Execution Environment

| Metric               | Result            |
| -------------------- | ----------------- |
| Agent                | Claude Code       |
| Model                | Opus 4.6          |
| Effort Level         | Medium            |
| Guidance Level       | No Agent Guidance |
| Initial Context      | 0                 |
| Final Context        | 51.4k / 200k      |
| Context Usage        | 26%               |
| Plan Usage           | 27%               |
| Approximate Duration | 5 minutes         |
| Start Time           | 20:30             |
| Tests Passed         | 7                 |

---

## 2. Initial Project State

The project was created using Spring Initializr with the following configuration:

* Java 25
* Spring Boot 4.1.0
* Maven
* Jar packaging
* Properties configuration
* Spring Web
* H2 Database
* Lombok

At the beginning of the experiment, the project contained no application-specific domain model or business logic.

---

## 3. Task

The Agent was asked to implement the first domain model for the Article Management System.

The task required an article to have:

* A unique identifier
* A title
* Content
* An author
* A creation timestamp

The article had to be suitable for persistence and the creation timestamp had to be managed automatically.

The Agent was explicitly instructed not to implement:

* REST endpoints
* Services
* Repositories
* Authentication
* Authorization
* Update/delete functionality

The complete task prompt is stored as the baseline Task 001 prompt.

---

## 4. Agent Behavior

### 4.1 Project Inspection

The Agent first inspected the existing project structure and configuration.

It reported:

> "Found files, read 4 files"

This demonstrates that the Agent did not immediately start writing code. It first attempted to understand the existing project.

**Evaluation: Good**

---

### 4.2 Persistence Decision

The initial project contained H2 but did not contain a JPA dependency.

The Agent independently recognized that persistence support was required and added:

```text
spring-boot-starter-data-jpa
```

This dependency was not explicitly requested in the prompt.

The Agent reasoned that Spring Data JPA was the appropriate persistence layer for the existing Spring Boot + H2 setup.

**Evaluation: Good autonomous technical decision**

---

### 4.3 Domain Model

The Agent created:

```text
Article
Author
```

The resulting relationship was:

```text
Author
   │
   └── 1 ─────── *
                Article
```

The Agent implemented the relationship as a `ManyToOne` relationship from `Article` to `Author`.

The Agent also introduced:

* JPA entity mapping
* Generated identifiers
* Database constraints
* Lazy loading
* A foreign key relationship
* Automatic creation timestamp handling

---

### 4.4 Creation Timestamp

The Agent decided to manage `createdAt` using a JPA lifecycle callback.

The implementation used:

```text
@PrePersist
```

This means the timestamp is automatically assigned when the entity is first persisted.

The Agent also prevented the value from being updated after creation.

**Evaluation: Good**

---

### 4.5 Database Mapping

The Agent made several autonomous database-related decisions:

* `GenerationType.IDENTITY` for identifiers
* `TEXT` mapping for article content
* Non-null database constraints for required fields
* `author_id` as the foreign key column
* Lazy loading for the author relationship

None of these implementation details were explicitly specified in the prompt.

**Evaluation: Good autonomous design decisions**

---

## 5. Test Strategy

The Agent created tests for the domain model.

During execution, it encountered compatibility issues related to the test setup and Spring Boot 4.x.

The Agent initially attempted to use:

```text
@DataJpaTest
```

but discovered that the expected test infrastructure was not available in the current Spring Boot version.

It then adapted the test implementation to use:

```text
@SpringBootTest
```

with direct JPA interaction.

The final result was:

```text
7 tests passed
```

This included:

* 1 existing application context test
* 6 newly created entity tests

**Evaluation: Good error detection and recovery**

---

## 6. Unexpected Configuration Change

The most important negative observation from this experiment was the Java version change.

The project was initially configured with:

```text
Java 25
```

The Agent discovered that the local environment could not compile the project with Java 25.

It then changed the project configuration:

```text
Java 25 → Java 17
```

This was an autonomous configuration change that was not explicitly authorized by the user.

The Agent explained that the available environment was using Java 17.

### Evaluation

This is considered a **scope and configuration governance issue**.

A safer behavior would have been:

```text
Java 25 required
       ↓
Local environment incompatible
       ↓
Report the problem
       ↓
Ask the user whether the project configuration should change
```

Instead, the Agent changed a project-level configuration value on its own.

**Metric:**

```text
Unrequested Configuration Changes: 1
```

This behavior will be particularly important to evaluate in later experiments after introducing project-level rules.

---

## 7. Scope Compliance

The Agent was explicitly instructed not to implement application layers beyond the domain model.

It did not create:

```text
Controllers       ❌
Services          ❌
Repositories      ❌
REST endpoints    ❌
Authentication    ❌
Authorization     ❌
```

The implementation remained focused on:

```text
Article
Author
Tests
Persistence configuration
```

**Evaluation: Good**

---

## 8. Files Created and Modified

### Created

```text
Article.java
Author.java
ArticleEntityTest.java
```

### Modified

```text
pom.xml
```

The `pom.xml` was modified to:

1. Add Spring Data JPA.
2. Change the Java version from 25 to 17.

---

## 9. Autonomous Decisions

The following decisions were made by the Agent without explicit instructions:

| Decision                          | Explicitly Requested? | Evaluation  |
| --------------------------------- | --------------------: | ----------- |
| Add Spring Data JPA               |                    No | Good        |
| Create `Author` entity            |                    No | Reasonable  |
| Use `ManyToOne`                   |                    No | Good        |
| Use `LAZY` fetching               |                    No | Good        |
| Use `IDENTITY` ID generation      |                    No | Reasonable  |
| Map content as `TEXT`             |                    No | Reasonable  |
| Use `@PrePersist`                 |                    No | Good        |
| Add database non-null constraints |                    No | Good        |
| Create tests                      |         Yes / implied | Good        |
| Change Java 25 → 17               |                    No | Problematic |

---

## 10. Metrics

### Execution Metrics

```text
Agent                    : Claude Code
Model                    : Opus 4.6
Effort                   : Medium
Guidance                 : None
Duration                 : ~5 minutes
Initial Context          : 0
Final Context            : 51.4k / 200k
Context Usage            : 26%
Plan Usage               : 27%
Tests Passed             : 7
```

### Quality Metrics

| Metric                     | Result            |
| -------------------------- | ----------------- |
| Requirement Understanding  | Good              |
| Project Inspection         | Good              |
| Scope Compliance           | Good              |
| Autonomous Decision Making | High              |
| Test Generation            | Good              |
| Error Recovery             | Good              |
| Persistence Design         | Good              |
| Configuration Safety       | Needs Improvement |
| Unrequested Changes        | 1                 |
| Human Intervention         | None              |

---

## 11. Baseline Assessment

### Overall Baseline

**8 / 10**

This score does not represent the quality of the production code itself.

It represents the Agent's behavior under the following condition:

```text
No project-level Agent guidance
```

The Agent demonstrated strong autonomous behavior in:

* Understanding the requirements
* Inspecting the project
* Selecting appropriate dependencies
* Designing the domain relationship
* Handling persistence
* Creating tests
* Recovering from test infrastructure issues
* Staying within the requested functional scope

The primary concern was the autonomous modification of the Java version from 25 to 17.

---

## 12. Key Learning from Experiment 1

The most important observation from this experiment is that an AI Agent already performs a significant amount of implicit decision-making even without `CLAUDE.md`, rules, skills, or other guidance mechanisms.

The baseline workflow was:

```text
User Prompt
     ↓
Project Inspection
     ↓
Requirement Interpretation
     ↓
Technical Decisions
     ↓
Implementation
     ↓
Testing
     ↓
Error Detection
     ↓
Error Recovery
     ↓
Final Result
```

The Agent therefore does not simply execute the exact instructions written in the prompt.

It interprets requirements and fills in missing architectural and implementation details using its own learned conventions and the information available in the project.

---

## 13. Experiment 2 Preparation

The next experiment will introduce:

```text
CLAUDE.md
```

No other Agent guidance mechanism will be introduced.

The task prompt will remain **exactly the same**.

This is important because the goal is to isolate the effect of `CLAUDE.md`.

### Experiment 2

```text
CLAUDE.md       ✅
Rules           ❌
Skills          ❌
Subagents       ❌
Hooks           ❌
```

The same Task 001 will then be executed again.

The results will be compared against this baseline using the same metrics.

---

## 14. Comparison Principle

The experiment series follows one fundamental rule:

> **Keep the task constant and change only the Agent guidance.**

Therefore:

```text
Experiment 1
No Guidance
        ↓
      Result

Experiment 2
CLAUDE.md
        ↓
      Result

Experiment 3
CLAUDE.md + Rules
        ↓
      Result

Experiment 4
CLAUDE.md + Rules + Skills
        ↓
      Result
```

This will allow the project to measure how each mechanism changes:

* Agent behavior
* Technical decisions
* Context usage
* Scope compliance
* Code structure
* Testing behavior
* Error handling
* Configuration safety
* Human intervention

The goal is not simply to learn what these mechanisms are, but to understand **what problem each mechanism solves and how it changes AI Agent behavior in a real software project.**
