package dev.inditex.karate.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;
import dev.inditex.karate.results.KarateOperationsStatsHook.Stats;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IT")
public class KarateRunnerIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_runner_scenarios_expect_no_failures() throws IOException {
      final KarateExecutionTest test = new KarateExecutionTest(
          // karate environment
          "local",
          // karate options
          getKarateOptions(),
          // featuresCount
          3,
          // scenariosCount
          8,
          // surefireFiles
          List.of(
              "/TEST-scenarios.openapi.functional.functional.xml",
              "/TEST-scenarios.openapi.smoke.smoke.xml",
              "/TEST-scenarios.openapi.with-mocks.with-mocks.xml"),
          // stats
          Map.of(
              "package/tag/ops/get", new Stats(0, 2, 2),
              "package/tag/ops/op", new Stats(2, 2, 4),
              "package/tag/ops/post", new Stats(0, 2, 2)),
          // logs
          List.of(
              "Running Feature[ ops-functional ] Scenario [ op1 ] ... ",
              "Running Feature[ ops-functional ] Scenario [ op2 ] ... ",

              "Running Feature[ ops-smoke ] Scenario [ op1 200 ] ... ",
              "Running Feature[ ops-smoke ] Scenario [ op2 201 ] ... ",

              "Running Feature[ with-mocks ] Scenario [ get-standalone ] ... ",
              "Running Feature[ with-mocks ] Scenario [ post-standalone ] ... ",
              "Running Feature[ with-mocks ] Scenario [ get-inline ] ... ",
              "Running Feature[ with-mocks ] Scenario [ post-inline ]"));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t @inditex-oss-karate -t @karate-openapi";
  }

}
