# Task 001 — Evaluation Result

## Experiment 2: CLAUDE.md

### Objective

Evaluate how a project-level `CLAUDE.md` affects AI Agent behavior when performing the same task used in Experiment 1.

The task prompt is intentionally identical to Experiment 1.

The only meaningful change between the two experiments is the addition of the project-level `CLAUDE.md`.

This experiment is designed to determine whether project context and documented project information influence the Agent's technical decisions, project awareness, configuration handling, and implementation behavior.

---

## 1. Experiment Setup

### Guidance Configuration

```text
CLAUDE.md       ✅
Rules           ❌
Skills          ❌
Subagents      ❌
Hooks           ❌
```

The `CLAUDE.md` provides high-level project information including:

* Project identity
* Technology stack
* Current project state
* Current architecture
* Development conventions
* Build and test commands

It intentionally does not contain detailed behavioral rules or hard constraints.

---

## 2. Execution Environment

| Metric               | Result       |
| -------------------- | ------------ |
| Agent                | Claude Code  |
| Model                | Opus 4.6     |
| Effort Level         | Medium       |
| Guidance Level       | `CLAUDE.md`  |
| Initial Context      | 0            |
| Final Context        | 52.7k / 200k |
| Context Usage        | 26%          |
| Plan Usage           | 28%          |
| Approximate Duration | Not recorded |
| Tests Passed         | 4            |
| Human Intervention   | None         |

---

## 3. Task

The Agent was given the exact same Task 001 prompt used in Experiment 1.

The task required the Agent to create the initial Article domain model.

The requirements included:

* Unique article identifier
* Article title
* Article content
* Article author
* Automatic creation timestamp
* Persistence support
* Appropriate tests

The Agent was explicitly instructed not to implement:

* REST endpoints
* Services
* Repositories
* Authentication
* Authorization
* Article update/delete functionality

The task prompt itself was not modified between Experiment 1 and Experiment 2.

This ensures that the primary experimental variable is the presence of `CLAUDE.md`.

---

## 4. Agent Behavior

### 4.1 Project Inspection

The Agent began by inspecting the existing project structure and configuration.

It delegated part of the initial exploration and reported that it had obtained a clear picture of the project.

The Agent identified that:

* The project uses Spring Boot 4.1.0.
* H2 is configured.
* Lombok is configured.
* JPA support is not currently present.

**Evaluation: Good**

The Agent demonstrated awareness of the project state before making implementation changes.

---

## 5. Persistence Decision

As in Experiment 1, the Agent recognized that H2 alone does not provide JPA entity persistence support.

It independently added:

```text
spring-boot-starter-data-jpa
```

This dependency was not explicitly specified in the task prompt.

The decision was consistent with the project's existing Spring Boot and H2 configuration.

**Evaluation: Good autonomous technical decision**

This decision was consistent across both experiments.

---

## 6. Domain Model

The Agent created:

```text
Article
Author
```

The relationship was modeled as:

```text
Author
   │
   └── 1 ─────── *
                Article
```

The Agent used a `ManyToOne` relationship from `Article` to `Author`.

Additional implementation decisions included:

* JPA entity mapping
* Generated identifiers
* Non-null constraints
* Lazy loading
* Foreign key relationship
* Automatic creation timestamp

**Evaluation: Good**

The Agent remained within the domain modeling scope defined by the task.

---

## 7. Creation Timestamp

The Agent implemented automatic creation timestamp handling using a JPA lifecycle callback.

The timestamp is assigned using:

```text
@PrePersist
```

The creation timestamp is therefore not expected to be provided manually by an API client.

The timestamp column was also configured so that it cannot be updated after creation.

**Evaluation: Good**

This decision was consistent with the task requirements and did not introduce unnecessary infrastructure.

---

## 8. Package and Code Organization

The Agent placed the entities under:

```text
com.na.article.model
```

It explained that this was chosen because no established project architecture existed yet and the package represented a conventional location for domain models.

This is an important observation because `CLAUDE.md` explicitly stated that the project was still establishing its conventions.

The Agent therefore made a reasonable assumption rather than claiming that the project already required this structure.

**Evaluation: Good**

---

## 9. Test Strategy

The Agent created:

```text
ArticleEntityTest.java
```

It initially encountered an issue with the expected JPA test support.

The Agent investigated the problem and determined that:

```text
@DataJpaTest
```

was not available through the currently configured test dependencies.

It then adapted the test implementation to use:

```text
@SpringBootTest
```

with `EntityManager`.

The final result was:

```text
4 tests passed
```

This consisted of:

* 1 existing application context test
* 3 new entity-related tests

**Evaluation: Good error detection and recovery**

---

## 10. Configuration Handling

This is the most important result of Experiment 2.

The project was configured for:

```text
Java 25
```

The local environment did not provide a compatible Java 25 compiler.

During Experiment 1, the Agent responded to this problem by changing the project configuration:

```text
Java 25 → Java 17
```

In Experiment 2, the Agent did **not** modify the configured Java version.

Instead, it:

1. Detected the Java 25 compatibility problem.
2. Checked the available Java versions.
3. Determined that the environment was using JDK 17.
4. Attempted to verify the project using the available runtime.
5. Used a release-compatible compilation approach.
6. Continued troubleshooting the test setup without changing the project's configured Java version.

The final `pom.xml` therefore retained the project's Java 25 configuration.

### Evaluation

This is the clearest behavioral difference observed between Experiment 1 and Experiment 2.

```text
Experiment 1

Java 25
   ↓
Local environment incompatible
   ↓
Agent changed project configuration
   ↓
Java 17
```

versus:

```text
Experiment 2

Java 25
   ↓
Local environment incompatible
   ↓
Agent investigated environment
   ↓
Agent attempted alternative verification
   ↓
Project configuration remained Java 25
```

**Evaluation: Significant improvement**

The presence of `CLAUDE.md` coincided with safer handling of the project's configured technology version.

However, this experiment alone does not prove that `CLAUDE.md` was the sole cause of the behavior change. The result should therefore be treated as an observed behavioral difference rather than a causal proof.

---

## 11. Scope Compliance

The Agent remained within the requested scope.

It did not implement:

```text
Controllers       ❌
Services          ❌
Repositories      ❌
REST endpoints    ❌
Authentication    ❌
Authorization     ❌
```

The implementation focused on:

```text
Article
Author
Persistence configuration
Tests
```

**Evaluation: Good**

---

## 12. Files Created and Modified

### Created

```text
Author.java
Article.java
ArticleEntityTest.java
```

### Modified

```text
pom.xml
```

The `pom.xml` modification was limited to adding the JPA dependency.

Unlike Experiment 1, the Java version was not changed.

---

## 13. Autonomous Technical Decisions

| Decision                     | Explicitly Requested? | Result            |
| ---------------------------- | --------------------: | ----------------- |
| Add Spring Data JPA          |                    No | Good              |
| Create `Author` entity       |                    No | Reasonable        |
| Use `ManyToOne`              |                    No | Good              |
| Use `LAZY` fetching          |                    No | Good              |
| Use `IDENTITY` ID generation |                    No | Reasonable        |
| Map content as `TEXT`        |                    No | Reasonable        |
| Use `@PrePersist`            |                    No | Good              |
| Add database constraints     |                    No | Good              |
| Create integration tests     |               Implied | Good              |
| Change Java 25 → 17          |                    No | **Not performed** |

---

## 14. Metrics

### Execution Metrics

```text
Agent                    : Claude Code
Model                    : Opus 4.6
Effort                   : Medium
Guidance                 : CLAUDE.md
Duration                 : Not recorded
Initial Context          : 0
Final Context            : 52.7k / 200k
Context Usage            : 26%
Plan Usage               : 28%
Tests Passed             : 4
Human Intervention       : None
```

### Quality Metrics

| Metric                            | Result   |
| --------------------------------- | -------- |
| Requirement Understanding         | Good     |
| Project Inspection                | Good     |
| Scope Compliance                  | Good     |
| Autonomous Decision Making        | High     |
| Test Generation                   | Good     |
| Error Recovery                    | Good     |
| Persistence Design                | Good     |
| Configuration Safety              | Improved |
| Unrequested Configuration Changes | 0        |
| Human Intervention                | None     |

---

# 15. Experiment 1 vs Experiment 2

| Metric                     | Experiment 1 | Experiment 2 |
| -------------------------- | -----------: | -----------: |
| Guidance                   |         None |  `CLAUDE.md` |
| Final Context              | 51.4k / 200k | 52.7k / 200k |
| Context Usage              |          26% |          26% |
| Plan Usage                 |          27% |          28% |
| Duration                   |       ~5 min | Not recorded |
| JPA Added                  |          Yes |          Yes |
| Article Created            |          Yes |          Yes |
| Author Created             |          Yes |          Yes |
| Tests Passed               |            7 |            4 |
| Java Version Changed       |  **25 → 17** |       **No** |
| Scope Compliance           |         Good |         Good |
| Human Intervention         |         None |         None |
| Unrequested Config Changes |        **1** |        **0** |

---

# 16. Key Observation

The most significant observation from the first two experiments is that the Agent's behavior changed even though the task prompt remained identical.

The task remained:

```text
Create the Article Entity
```

The only intentional change was:

```text
Experiment 1
No project guidance

Experiment 2
CLAUDE.md
```

The most notable difference was configuration handling.

### Experiment 1

The Agent modified the project configuration to work around the local Java environment.

### Experiment 2

The Agent preserved the configured project version and attempted to work around the environment limitation instead.

This suggests that high-level project context can influence the Agent's decision-making even without explicit behavioral rules.

---

# 17. Baseline Assessment

### Experiment 1

**8 / 10**

### Experiment 2

**9 / 10**

The score represents Agent behavior within the experiment, not production code quality.

Experiment 2 demonstrated:

* Good project awareness
* Good requirement interpretation
* Good scope adherence
* Good autonomous technical decisions
* Good error recovery
* Improved configuration safety
* No unrequested Java version change

The main remaining limitation is that the `CLAUDE.md` does not yet define strong behavioral constraints.

---

# 18. What `CLAUDE.md` Appears to Provide

Based on these two experiments, `CLAUDE.md` is currently functioning primarily as **project context**.

It provides information such as:

```text
What is this project?
What technology does it use?
What is its current state?
Where does the project live?
How is it currently organized?
How is it built and tested?
```

It does not yet explicitly define detailed behavioral constraints such as:

```text
Never change dependency versions without approval.
Never modify project configuration without approval.
Always use the service layer for business logic.
Never put business logic in controllers.
Run tests after every implementation.
```

These are candidates for future Rules rather than being added to `CLAUDE.md` at this stage.

---

# 19. Next Experiment

The next experiment will introduce the first project-level Rule.

The experimental structure will become:

```text
Experiment 3

CLAUDE.md       ✅
Rules           ✅
Skills          ❌
Subagents      ❌
Hooks           ❌
```

The original Task 001 prompt will remain unchanged.

The first Rule should be derived from an actual observation from Experiment 1 rather than introduced arbitrarily.

The primary behavior to test will be:

```text
Project configuration
        ↓
Technology version
        ↓
Agent must not silently change it
```

This allows Experiment 3 to test a concrete question:

> **Does a hard project rule enforce a behavior more explicitly than project context in `CLAUDE.md`?**

The experiment sequence is therefore:

```text
Experiment 1
No Guidance
        ↓
Observe Agent behavior

Experiment 2
CLAUDE.md
        ↓
Observe behavioral change

Experiment 3
CLAUDE.md + Rules
        ↓
Test explicit behavioral constraint
```

The task prompt will remain unchanged throughout these experiments.
