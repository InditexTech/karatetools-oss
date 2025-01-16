# code-maven_java-QA_compatibility

[`code-maven_java-QA_compatibility.yml`](../code-maven_java-QA_compatibility.yml) workflow to **execute compatibility tests**.

## Trigger

* On `workflow_dispatch`
  * inputs:
    * `RUNNERS` : Comma separated list of runners where the tests must be executed, for example: "ubuntu-latest,windows-latest,macos-latest".
* On `pull_request` which is not a `draft`
  * inputs are read from the `github_config.yml` file

## Configuration

NA

## Where does it run?

In the runners defined as inputs.

## Jobs

* ### `compatibility-tests`
  * `checkout` : Checkout the repository
  * `config` : Read the `github_config.yml` file merged with inputs
  * `setup-maven-cache` : Download the Github Maven cache if available and save it for next usages
  * `setup-asdf-cache` : Download the Github asdf cache if available and save it for next usages
  * `save-tool-versions-content` : Save the .tool-versions file content to be installed in the `asdf-install` step
  * `asdf-install` : Install tools (java, maven, ...) using asdf
  * `set-java-home` : Set the JAVA_HOME environment variable
  * `mvn-exec-java` : Run maven exec java
