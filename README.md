# Article Management System

This project is being developed as a practical learning project for understanding **AI Agent development workflows**.

The goal is not only to build an article management system, but also to understand how AI Agents can be guided, constrained, and extended within a real software project.

Throughout the project, AI Agent mechanisms such as:

* `CLAUDE.md`
* `rules`
* `skills`
* `subagents`
* `hooks`
* `output styles`
* `system prompts`

will be introduced step by step and evaluated in practice.

---

## 1. Project Initialization

The project was created using [Spring Initializr](https://start.spring.io/).

### Project

* **Build Tool:** Maven
* **Language:** Java
* **Packaging:** Jar

### Spring Boot

* **Spring Boot:** 4.1.0

### Java

* **Java:** 25

### Project Metadata

| Field        | Value                       |
| ------------ | --------------------------- |
| Group        | `com.na`                    |
| Artifact     | `article-management-system` |
| Package Name | `com.na.article`            |

### Configuration

* **Configuration:** Properties

---

## 2. Initial Dependencies

The following dependencies were selected during project initialization.

### Spring Web

Spring Web will be used to build the REST API.

It will provide the foundation for:

* REST controllers
* HTTP endpoints
* Request/response handling
* REST API development

### H2 Database

H2 will be used as the initial relational database during development.

The purpose is to avoid introducing an external database at the beginning of the project while developing and learning the data access layer.

### Lombok

Lombok will be used to reduce repetitive Java boilerplate code.

For example:

* Getters and setters
* Constructors
* Builders
* `equals` / `hashCode`
* `toString`

---

## 3. Purpose of the First Stage

At this stage, no AI Agent-specific rules or guidance mechanisms are being introduced.

The purpose is to establish a **baseline development environment** before introducing AI Agent customization.

The initial development flow is intentionally simple:

```text
Developer
    ↓
AI Agent
    ↓
Project Code
```

In later stages, this workflow will gradually become more structured:

```text
Developer
    ↓
CLAUDE.md
    ↓
Rules
    ↓
Skills
    ↓
Subagents
    ↓
Hooks
    ↓
Project Code
```

The two approaches will be compared throughout the project to understand how each mechanism affects the Agent's behavior.

---

## 4. Learning Approach

This project is not only about implementing features.

At each stage, the following questions will be investigated:

1. How does the AI Agent understand the project?
2. Which files does the Agent inspect?
3. What problem does `CLAUDE.md` solve?
4. What is the difference between `CLAUDE.md` and `rules`?
5. When should `skills` be used?
6. Why and when should `subagents` be used?
7. What problems can `hooks` solve?
8. How is the Agent's final context constructed?
9. How are the user's instructions combined with project-level instructions?
10. How can we ensure that generated code follows project standards?

The project will therefore follow this learning cycle:

```text
Feature
   ↓
AI Agent Mechanism
   ↓
Observation
   ↓
Comparison
   ↓
Improvement
```

The goal is to understand not only **what** each mechanism is, but also **why, when, and how** it should be used.

---

## 5. Initial Project Structure

At the end of this stage, the project should contain only the basic Spring Boot structure:

```text
article-management-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── na/
│   │   │           └── article/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

At this stage, the following AI Agent configuration files and directories **will not be created yet**:

```text
CLAUDE.md
rules/
skills/
subagents/
hooks/
```

They will be introduced incrementally in later stages.

---

## 6. Next Stage

The next stage will focus on defining the basic domain of the application.

The initial domain will contain concepts such as:

```text
Person
Article
Favorite
```

The system will allow users to:

* Create articles
* Publish articles
* Read articles
* Favorite articles
* Manage their own articles

The application will initially be developed **without additional AI Agent configuration**.

After establishing the basic application, AI Agent mechanisms will be introduced one by one.

This will allow the project to demonstrate the difference between:

```text
AI Agent without project guidance
```

and:

```text
AI Agent with structured project guidance
```

The objective is to learn these mechanisms through a real development workflow rather than studying them only as isolated concepts.
