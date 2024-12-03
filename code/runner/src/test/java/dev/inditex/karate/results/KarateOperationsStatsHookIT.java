package dev.inditex.karate.results;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;
import dev.inditex.karate.results.KarateOperationsStatsHook.Stats;

import com.intuit.karate.Runner;
import com.intuit.karate.Suite;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IT")
public class KarateOperationsStatsHookIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_stats_scenarios_expect_no_failures() throws IOException {
      final KarateExecutionTest test = new KarateExecutionTest(
          // karate environment
          "local",
          // karate options
          getKarateOptions(),
          // featuresCount
          1,
          // scenariosCount
          1,
          // surefireFiles
          List.of(
              "/TEST-scenarios.karate-stats.stats.xml"),
          // stats
          Map.of("package/tag/ops/stats", new Stats(0, 1, 1)),
          // logs
          List.of(
              "Running Feature[ ops-stats ] Scenario [ stats ] ... "));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @karate-stats";
  }

  @SuppressWarnings("rawtypes")
  protected Suite getSuite() {
    final String path = "classpath:scenarios/karate-stats";
    final Runner.Builder runner =
        Runner.builder().path(path)
            .outputCucumberJson(true)
            .outputJunitXml(true)
            .outputHtmlReport(true);
    return new Suite(runner);
  }
}
