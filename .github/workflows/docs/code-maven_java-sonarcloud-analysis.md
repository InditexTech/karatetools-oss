# `code-maven_java-sonarcloud-analysis`

[`code-maven_java-sonarcloud-analysis.yml`](../code-maven_java-sonarcloud-analysis.yml) workflow allows to **run** the Sonarcloud scanner.

## Trigger

- Any `closed` and `merged` pull request to `main` branch with changes on `code` path if using Trunk-based development, AND IS_INDITEXTECH_REPO repository variable has `true` value.
- Any `closed` and `merged` pull request to `develop` branch with changes on `code` path if using Gitflow, AND IS_INDITEXTECH_REPO repository variable has `true` value.
- A manual dispatch (`workflow_dispatch`) invoked from the GitHub UI AND IS_INDITEXTECH_REPO repository variable has `true` value.
- When publishing a GitHub release AND IS_INDITEXTECH_REPO repository variable has `true` value.

## Where does it run?

`ubuntu-20.04` GitHub infrastructure.

## Versions used

`asdf` and any `Java`, `Maven` and `Node`.

## How does it work?

This workflow relies on asdf to automatically load any tool version defined on the project's `code/.tool-versions` file.

## Jobs

- ### `unit-tests`

  - **Steps**
    - Checkout the repository.
    - Setup Maven and Asdf caches.
    - Configure asdf environment with the added tools in the `.tool-versions` file.
    - Setup JAVA_HOME env var.
    - if (event_name == `release`):
      - Run verify command skipping enforcing snapshots (if using enforcer Maven plugin)
    - if (event_name != `release`):
      - Run verify command
    - Store project name and version.
    - Setup asdf tools for sonarcloud usage.
    - Set JAVA_HOME env var.
    - if (event_name == `release`):
      - Run Maven Sonar goal in a `release/*` branch in SonarCloud.
    - if (event_name != `release`):
      - Run Maven Sonar goal.
