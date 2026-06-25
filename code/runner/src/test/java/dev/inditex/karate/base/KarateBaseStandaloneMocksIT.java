package dev.inditex.karate.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class KarateBaseStandaloneMocksIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_standalone_mocks_enabled_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest(
          getKarateOptionsForEnabled(),
          "mocks-bootstrap-enabled-with-standalone-tag",
          List.of(
              "karate.options['@mock.templates.standalone'] >> true",
              "karate.callSingle(\"classpath:mocks/mock-templates-standalone.feature\") DONE"));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }

    @Test
    void when_standalone_mocks_disabled_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest(
          getKarateOptionsForDisabled(),
          "mocks-bootstrap-disabled-with-inline-tag",
          List.of("karate.options['@mock.templates.standalone'] >> false"));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected KarateExecutionTest getKarateExecutionTest(final String options, final String scenarioName,
      final List<String> additionalLogs) {
    final var expectedLogs = new java.util.ArrayList<String>();
    expectedLogs.add("Running Feature[ karate-base-mocks-standalone ] Scenario [ " + scenarioName + " ] ... ");
    expectedLogs.addAll(additionalLogs);
    return new KarateExecutionTest(
        "local",
        options,
        1,
        1,
        List.of("/TEST-scenarios.base.mocks-standalone.xml"),
        Map.of(),
        expectedLogs);
  }

  protected String getKarateOptionsForEnabled() {
    return "-t ~@ignore -t @inditex-oss-karate -t @karate-base-mocks -t @mock.templates.standalone";
  }

  protected String getKarateOptionsForDisabled() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @karate-base-mocks -t @mock.templates.inline";
  }
}
