package dev.inditex.karate.logging;

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
public class HttpLogMaskingIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_http_log_masking_scenarios_expect_no_failures() throws IOException {
      final KarateExecutionTest test = new KarateExecutionTest(
          // karate environment
          "local",
          // karate options
          getKarateOptions(),
          // featuresCount
          1,
          // scenariosCount
          2,
          // surefireFiles
          List.of(
              "/TEST-scenarios.logging.http-log-masking.xml"),
          // stats
          Map.of(
              "package/tag/ops/op-with-auth", new Stats(0, 2, 2)),
          // logs
          List.of(
              "Running Feature[ karate-http-log-masking ] Scenario [ karate-http-log-masking-basic ] ... ",
              "Running Feature[ karate-http-log-masking ] Scenario [ karate-http-log-masking-jwt ] ... "));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @http-log-masking";
  }

}
