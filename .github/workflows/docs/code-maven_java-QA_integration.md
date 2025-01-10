# code-maven_java-QA_integration

[`code-maven_java-QA_integration.yml`](../code-maven_java-QA_integration.yml) workflow to **execute integration tests**.

## Trigger

* On `workflow_dispatch`
  * inputs:
    * `INTEGRATION_COVERAGE_THRESHOLD` : The minimum coverage threshold for the integration tests
* On `pull_request` which is not a `draft`
  * inputs are read from the `github_config.yml` file

## Configuration

`github_config.yml` : Configuration file for the GitHub Workflows

```yaml
# Integration test configuration
integration:
  # Default test report configuration
  report:
    # Failsafe report folder, used to upload the report
    folder: code/target/reports
  # JaCoCo configuration
  jacoco:
    report:
      # JaCoCo report folder, used to verify the coverage and upload the report
      folder: code/jacoco-report-aggregate/target/site/jacoco-aggregate-it
  # Coverage configuration
  coverage:
    # Coverage threshold: The minimum coverage threshold to pass the integration tests
    threshold: 80
```

## Where does it run?

GitHub-hosted runner `ubuntu-20.04`:
* 4 cores / 16 Gb RAM / 14 Gb Storage
* [Ubuntu2004-Readme.md](https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2004-Readme.md)

## Jobs

* ### `integration-tests`
  * `checkout` : Checkout the repository
  * `config` : Read the `github_config.yml` file merged with inputs
  * `setup-maven-cache` : Download the Github Maven cache if available and save it for next usages
  * `setup-asdf-cache` : Download the Github asdf cache if available and save it for next usages
  * `save-tool-versions-content` : Save the .tool-versions file content to be installed in the `asdf-install` step
  * `asdf-install` : Install tools (java, maven, ...) using asdf
  * `set-java-home` : Set the JAVA_HOME environment variable
  * `mvn-clean-verify` : Run maven clean verify skipping Unit tests
  * `mvn-failsafe-report` : Generate the Failsafe report
  * `failsafe-report-verification` : Display the FailSafe report summary and verify its result (success rate = 100%)
  * `failsafe-report-upload` : Upload the Failsafe report
  * `jaccoco-report-verification` : Display the JaCoCo report summary and verify its result (configured minimum coverage %)
  * `jaccoco-report-upload` : Upload the Jacoco report
  * `execution-logs-upload` : Upload the execution logs	(`code/**/*.log`)
