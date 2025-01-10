# code-maven_java-QA_unit

[`code-maven_java-QA_unit.yml`](../code-maven_java-QA_unit.yml) workflow to **execute unit tests**.

## Trigger

* On `workflow_dispatch`
  * inputs:
    * `UNIT_COVERAGE_THRESHOLD` : The minimum coverage threshold for the unit tests
* On `pull_request` which is not a `draft`
  * inputs are read from the `github_config.yml` file

## Configuration

`github_config.yml` : Configuration file for the GitHub Workflows

```yaml
# Unit test configuration
unit:
  # Default test report configuration
  report:
    # Surefire report folder, used to upload the report
    folder: code/target/reports
  # JaCoCo configuration
  jacoco:
    report:
      # JaCoCo report folder, used to verify the coverage and upload the report
      folder: code/jacoco-report-aggregate/target/site/jacoco-aggregate
  # Coverage configuration
  coverage:
    # Coverage threshold: The minimum coverage threshold to pass the unit tests
    threshold: 90
```

## Where does it run?

GitHub-hosted runner `ubuntu-20.04`:
* 4 cores / 16 Gb RAM / 14 Gb Storage
* [Ubuntu2004-Readme.md](https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2004-Readme.md)

## Jobs

* ### `unit-tests`
  * `checkout` : Checkout the repository
  * `config` : Read the `github_config.yml` file merged with inputs
  * `setup-maven-cache` : Download the Github Maven cache if available and save it for next usages
  * `setup-asdf-cache` : Download the Github asdf cache if available and save it for next usages
  * `save-tool-versions-content` : Save the .tool-versions file content to be installed in the `asdf-install` step
  * `asdf-install` : Install tools (java, maven, ...) using asdf
  * `set-java-home` : Set the JAVA_HOME environment variable
  * `mvn-clean-verify` : Run maven clean verify skipping Integration tests
  * `mvn-surefire-report` : Generate the Surefire report
  * `surefire-report-verification` : Display the SureFire report summary and verify its result (success rate = 100%)
  * `surefire-report-upload` : Upload the Surefire report
  * `jaccoco-report-verification` : Display the JaCoCo report summary and verify its result (configured minimum coverage %)
  * `jaccoco-report-upload` : Upload the Jacoco report
  * `execution-logs-upload` : Upload the execution logs	(`code/**/*.log`)
