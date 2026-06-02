package dev.inditex.karate.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class KarateBaseConfigLoadingIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_default_env_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest(
          "local",
          "config-default-env");

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }

    @Test
    void when_alternative_env_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest(
          "dev",
          "config-alternative-env");

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected KarateExecutionTest getKarateExecutionTest(final String env, final String scenarioName) {
    return new KarateExecutionTest(
        // karate environment
        env,
        // karate options
        getKarateOptions(),
        // featuresCount
        1,
        // scenariosCount
        1,
        // surefireFiles
        List.of(
            "/TEST-scenarios.base.config.xml"),
        // stats
        Map.of(),
        // logs
        List.of(
            "Running Feature[ karate-base-config ] Scenario [ " + scenarioName + " ] ... "));
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @karate-base-config-default";
  }
}
