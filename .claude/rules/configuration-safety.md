# Configuration Safety

## Rule

Do not change project-level configuration values without explicit user approval.

This includes, but is not limited to:

- Java version (`java.version` in `pom.xml`)
- Spring Boot version
- Dependency versions
- Build tool configuration
- Packaging or plugin configuration

## Required behavior on conflict

If the current environment cannot build or run the project with the
configured values (for example, the configured Java version is not available
locally), do **not** silently change the configuration to make it work.

Instead:

1. Stop.
2. Report the exact conflict (what is configured vs. what the environment supports).
3. Ask the user whether the project configuration should be changed.
4. Wait for explicit approval before modifying any configuration file.

## Rationale

The project's configured technology versions are intentional decisions. An
Agent lowering or altering them to fit the local environment changes a
project-level decision that belongs to the user, not the Agent. Reporting the
conflict and asking preserves the user's control over project configuration.
