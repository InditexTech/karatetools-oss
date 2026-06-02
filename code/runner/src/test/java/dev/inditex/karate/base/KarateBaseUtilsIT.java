package dev.inditex.karate.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class KarateBaseUtilsIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_base_utils_scenarios_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest();

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected KarateExecutionTest getKarateExecutionTest() {
    return new KarateExecutionTest(
        "local",
        getKarateOptions(),
        1,
      6,
        List.of("/TEST-scenarios.base.utils.xml"),
        Map.of(),
        List.of(
        "Running Feature[ karate-base-utils ] Scenario [ utils-read-test-data-merges-hierarchy ] ... ",
        "Running Feature[ karate-base-utils ] Scenario [ utils-is-array-detects-array-values ] ... ",
            "Running Feature[ karate-base-utils ] Scenario [ utils-merge-combines-objects ] ... ",
        "Running Feature[ karate-base-utils ] Scenario [ utils-deep-merge-mutates-target ] ... ",
        "Running Feature[ karate-base-utils ] Scenario [ utils-replace-expressions-resolves-known-vars ] ... ",
        "Running Feature[ karate-base-utils ] Scenario [ utils-manipulate-leaf-values-applies-callback ] ... "));
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @karate-base-utils";
  }
}
