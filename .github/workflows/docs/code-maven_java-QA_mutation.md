# code-maven_java-QA_mutation

[`code-maven_java-QA_mutation.yml`](../code-maven_java-QA_mutation.yml) workflow to **execute mutation tests**.

## Trigger

* On `workflow_dispatch`
  * inputs:
    * `MUTATION_COVERAGE_THRESHOLD` : The minimum coverage threshold for the mutation tests
* On `pull_request` which is not a `draft`
  * inputs are read from the `github_config.yml` file

## Configuration

`github_config.yml` : Configuration file for the GitHub Workflows

```yaml
# Mutation test configuration
mutation:
  # Default test report configuration
  report:
    # Pitest report folder, used to verify the coverage and upload the report
    folder: code/target/pit-reports
  # Coverage configuration
  coverage:
    # Coverage threshold: The minimum coverage threshold to pass the mutation tests
    threshold: 90
```

## Where does it run?

GitHub-hosted runner `ubuntu-20.04`:
* 4 cores / 16 Gb RAM / 14 Gb Storage
* [Ubuntu2004-Readme.md](https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2004-Readme.md)

## Jobs

* ### `mutation-tests`
  * `checkout` : Checkout the repository
  * `config` : Read the `github_config.yml` file merged with inputs
  * `setup-maven-cache` : Download the Github Maven cache if available and save it for next usages
  * `setup-asdf-cache` : Download the Github asdf cache if available and save it for next usages
  * `save-tool-versions-content` : Save the .tool-versions file content to be installed in the `asdf-install` step
  * `asdf-install` : Install tools (java, maven, ...) using asdf
  * `set-java-home` : Set the JAVA_HOME environment variable
  * `mvn-clean-verify` : Run maven clean verify with mutation skipping Integration tests
  * `pitest-report-verification` : Display the PITest report summary and verify its result (configured minimum coverage %)
  * `pitest-report-upload` : Upload the PITest report
  * `execution-logs-upload` : Upload the execution logs	(`code/**/*.log`)
