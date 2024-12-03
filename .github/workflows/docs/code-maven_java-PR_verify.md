# `code-maven-PR_verify`

[`code-maven_java-PR_verify.yml`](../code-maven_java-PR_verify.yml) workflow allows to **run** different types of tests.

## Trigger

Any pull request `opened` with changes about `code` folder.

## Where does it run?

`ubuntu-20.04` GitHub infrastructure.

## Versions used

`asdf` and any `Java`, `Maven` and `Node`.

## How does it work?

This workflow relies on asdf to automatically load any tool version defined on the project's `code/.tool-versions` file.

## Jobs

- ### `unit-tests`

  - **Steps**
    - Checkout the repository in the specific pull request.
    - Setup Maven and Asdf caches.
    - Configure asdf environment with the added tools in the `.tool-versions` file.
    - Setup JAVA_HOME env var.
    - Store project version and name.
    - Verify artifact with coverage.
    -  Process Surefire report and annotate PR
    - if (repository variable IS_INDITEXTECH_REPO has true value):
      - Setup asdf tools for sonarcloud usage.
      - Set JAVA_HOME env var.
      - Run Maven Sonar goal.
