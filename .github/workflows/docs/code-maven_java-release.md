# `code-maven-release`

[`code-maven_java-release.yml`](../code-maven_java-release.yml) workflow releases a new version to Maven Central and also generates the associated release in GitHub.

## Triggers

- Any `closed` pull request to `main` branch on `code` path, if no `skip-release` label AND if a `release-type/*` label is added if using Gitflow development flow.
- A manual dispatch (`workflow_dispatch`) invoked from the GitHub UI.

## Where does it run?

`ubuntu-20.04` GitHub infrastructure.

## Versions used

`asdf` and any `Java`, `Maven` and `Node`.

## How does it work?

This workflow relies on asdf to automatically load any tool version defined on the project's `code/.tool-versions` file.

## Jobs

- ### `release`

  - **Steps**

    - Get the release labels and the baseline branch.
    - Checkout the specific merge commit or the baseline branch if using workflow_dispatch trigger.
    - Throw an error if CHANGELOG.md changes are not added.
    - Setup Maven and Asdf caches.
    - Configure asdf environment with the added tools in the `.tool-versions` file.
    - Setup JAVA_HOME env var.
    - Update the version based on the input release type.
    - Setup the version increment depending on the introduced release labels.
    - Prepare committer information and set GPG key, that needs to be configured in the repository.
    - Update CHANGELOG.md and calculate next version using `release-flow/keep-a-changelog-action` action
    - CHANGELOG.md is restored
    - Goal Maven with `mvn release`
      - `mvn deploy` artifacts into Maven Central
      - `git tag` on GitHub with final version setted in previous steps
      - Prepare the possible next version in the `pom.xml` files
    - Create Sync Branch PR into Develop if using Gitflow development flow.
    - Publish release on GitHub
    - Comment in PR if sync PR failed
    - Comment in PR if the release creation has failed
