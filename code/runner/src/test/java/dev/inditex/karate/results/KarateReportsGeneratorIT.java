package dev.inditex.karate.results;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IT")
@Slf4j
public class KarateReportsGeneratorIT extends AbstractKarateTest {

  @Override
  @AfterEach
  protected void afterEach() {
    super.afterEach();
    // Clean test surefire reports folder so they are not counted in general reports
    try {
      FileUtils.deleteDirectory(new File(surefireReportsFolder));
    } catch (final IOException e) {
      log.error("Error deleting surefire reports folder", e);
    }
  }

  @Nested
  class KarateScenarios {
    @Test
    void when_reports_scenarios_expect_no_failures() throws IOException {
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
              "/TEST-scenarios.karate-reports.reports-example-1.xml"),
          // stats
          Map.of(),
          // logs
          List.of(
              "Running Feature[ reports-example-1 ] Scenario [ reports-scenario-1-1 ] ... ",
              "Running Feature[ reports-example-1 ] Scenario [ reports-scenario-1-2 ] ... "));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @generate-reports-success";
  }
}
