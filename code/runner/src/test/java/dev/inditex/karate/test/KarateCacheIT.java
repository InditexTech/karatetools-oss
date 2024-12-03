package dev.inditex.karate.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IT")
public class KarateCacheIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_cache_scenarios_expect_no_failures() throws IOException {
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
              "/TEST-scenarios.cache.cache.xml"),
          // stats
          Map.of(),
          // logs
          List.of(
              "Running Feature[ karate-cache ] Scenario [ karate-cache-static ] ... ",
              "Running Feature[ karate-cache ] Scenario [ karate-cache-variable ] ... "));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @karate-cache";
  }
}
