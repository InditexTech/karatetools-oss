# code-maven_java-QA_e2e_karate

[`code-maven_java-QA_e2e_karate.yml`](../code-maven_java-QA_e2e_karate.yml) workflow to **execute karate tests**.

* The tests must be located in the `e2e/karate` folder.

## Trigger

* On `workflow_dispatch`
  * inputs:
    * `KARATE_ENV` : Karate Target environment.
    * `KARATE_OPTIONS` : Karate options to run the tests.
    * `KARATE_APP_ENABLED` : Flag to enable the build and launch of the Java app.
    * `KARATE_COVERAGE_THRESHOLD` : The minimum coverage threshold for the karate tests
* On `pull_request` which is not a `draft`
  * inputs are read from the `github_config.yml` file

## Configuration

`github_config.yml` : Configuration file for the GitHub Workflows

```yaml
# Karate test configuration
karate:
  # Environment: The environment to run the karate tests
  env: local
  # Karate options: The options to run the karate tests
  options: "-t ~@mock.templates.inline -t ~@ignore --threads 1"
  # Default test report configuration
  report:
    # Karate report folder, used to upload the report
    folder: e2e/karate/target/karate-reports
  # App configuration
  app:
    # Enabled: The flag to enable the build and launch of the app
    enabled : true
    # Jar file: The jar file to launch the app - relative path from 'code'
    jar: boot/target/karatetools-boot
    # Health probe: The health probe to check the app status
    health_probe: karatetools/health/docker
  # JaCoCo configuration
  jacoco:
    # JaCoCo includes: The classes to include in the coverage
    includes:
      - dev.inditex.*
    # JaCoCo excludes: The classes to exclude from the coverage
    excludes:
      - "**.*DTO"
    # JaCoCo source files: The source files to include in the coverage
    sourcefiles:
      - clients/src/main/java
      - generators/src/main/java
      - runner/src/main/java
      - boot/src/main/java
    # JaCoCo report folder, used to verify the coverage and upload the report
    report:
      folder: code/target/jacoco-e2e
  # Coverage configuration
  coverage:
    # Coverage threshold: The minimum coverage threshold to pass the karate tests
    threshold: 80
```

## Where does it run?

GitHub-hosted runner `ubuntu-20.04`:
* 4 cores / 16 Gb RAM / 14 Gb Storage
* [Ubuntu2004-Readme.md](https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2004-Readme.md)

## Jobs

* ### `karate-tests`
  * `checkout` : Checkout the repository
  * `config` : Read the `github_config.yml` file merged with inputs
  * `setup-maven-cache` : Download the Github Maven cache if available and save it for next usages
  * `setup-asdf-cache` : Download the Github asdf cache if available and save it for next usages
  * `save-tool-versions-content` : Save the .tool-versions file content to be installed in the `asdf-install` step
  * `asdf-install` : Install tools (java, maven, ...) using asdf
  * `set-java-home` : Set the JAVA_HOME environment variable
  * if `karate.app.enabled = true`
    * `app-config` : Set the Java application configuration
    * `mvn-clean-install` : Run maven clean install to build the application
    * `start-java-app` : Start the Java application
  * `mvn-clean-verify` : Run maven clean verify with karate tests
  * `karate-results-verification` : Display the Karate report summary and verify its result (success rate = 100%)
  * `karate-report-upload` : Upload the Karate report
  * if `karate.app.enabled = true`
    * `stop-java-app` : Stop the Java application
    * `generate-jacoco-report` : Generate the JaCoCo report
    * `jacoco-results-verification` : Display the JaCoCo report summary and verify its result (configured minimum coverage %)
    * `jacoco-report-upload` : Upload the Jacoco report
  * `execution-logs-upload` : Upload the execution logs	(`code/**/*.log` and `e2e/karate/**/*.log`)
