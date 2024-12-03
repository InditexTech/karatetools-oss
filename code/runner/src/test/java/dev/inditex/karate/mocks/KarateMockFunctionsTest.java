package dev.inditex.karate.mocks;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.Suite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;

public class KarateMockFunctionsTest extends AbstractKarateTest {
  @Override
  @BeforeEach
  protected void beforeEach(final TestInfo testInfo) throws IOException {
    super.beforeEach(testInfo);
    ((Logger) LoggerFactory.getLogger("com.intuit.karate")).setLevel(Level.DEBUG);
  }

  @Nested
  class KarateMocks {
    @ParameterizedTest(name = "when_{0}_expect_no_error")
    @CsvSource({
        "list-templates-no-folder,>> karate.tools >> mock-templates >> listTemplates >> "
            + "ERROR >> Cannot read property \"getPath\" from null",
        "list-templates-empty-folder,>> karate.tools >> mock-templates >> listTemplates >> "
            + "# files >> 0",
        "list-templates-folder-with-mocks,>> karate.tools >> mock-templates >> listTemplates >> "
            + "# files >> 3",
        "read-templates-no-folder,>> karate.tools >> mock-templates >> readTemplates >> "
            + "# templates >> 0",
        "read-templates-empty-folder,>> karate.tools >> mock-templates >> readTemplates >> "
            + "# templates >> 0",
        "read-templates-folder-with-mocks,>> karate.tools >> mock-templates >> readTemplates >> "
            + "# templates >> 3",
        "find-template-not-found,>> karate.tools >> mock-templates >> findTemplate >> "
            + "NOT FOUND !!!",
        "find-template-found,>> karate.tools >> mock-templates >> findTemplate >> "
            + "FOUND !!![ 0 ][ classpath:mocks/templates/standalone/xxx-api/XXXX_get_200.yml ]",
        "find-template-not-found-path-param,>> karate.tools >> mock-templates >> findTemplate >> "
            + "NOT FOUND !!!",
        "find-template-found-path-param,>> karate.tools >> mock-templates >> findTemplate >> "
            + "FOUND !!![ 1 ][ classpath:mocks/templates/standalone/xxx-api/XXXX_get_204.yml ]"
    })
    void when_templates_functions_expect_no_error(final String tag, final String expectedLog) {
      final List<String> tagsList =
          new ArrayList<>(Arrays.asList(getKarateOptions().split("-t ")).stream().filter(s -> !s.isEmpty()).toList());
      tagsList.add("@" + tag);

      final Results results = getTestResults(tagsList);

      assertThat(results.getScenariosTotal()).isPositive();
      assertThat(results.getFailCount()).isZero();
      if (expectedLog != null && !expectedLog.isEmpty()) {
        assertThat(logWatcher.list).anyMatch(e -> e.getFormattedMessage().contains(expectedLog));
      }
    }
  }

  @Nested
  class KarateScenarios {
    @Test
    void when_mocks_scenarios_expect_no_failures() throws IOException {
      final KarateExecutionTest test = new KarateExecutionTest(
          // karate environment
          "local",
          // karate options
          getKarateOptions(),
          // featuresCount
          2,
          // scenariosCount
          10,
          // surefireFiles
          List.of(
              "/TEST-scenarios.karate-mocks.mock-functions-no-server.xml",
              "/TEST-scenarios.karate-mocks.mock-functions-with-server.xml"),
          // stats
          Map.of(),
          // logs
          List.of(
              "Running Feature[ Mocks ] Scenario [ list-templates-no-folder ] ... ",
              "Running Feature[ Mocks ] Scenario [ list-templates-empty-folder ] ... ",
              "Running Feature[ Mocks ] Scenario [ list-templates-folder-with-mocks ] ... ",
              "Running Feature[ Mocks ] Scenario [ read-templates-no-folder ] ... ",
              "Running Feature[ Mocks ] Scenario [ read-templates-empty-folder ] ... ",
              "Running Feature[ Mocks ] Scenario [ read-templates-folder-with-mocks ] ... ",
              "Running Feature[ Mocks ] Scenario [ find-template-not-found ] ... ",
              "Running Feature[ Mocks ] Scenario [ find-template-found ] ... ",
              "Running Feature[ Mocks ] Scenario [ find-template-not-found-path-param ] ... ",
              "Running Feature[ Mocks ] Scenario [ find-template-found-path-param ] ... "));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t @inditex-oss-karate -t @karate-mock-functions";
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected Results getTestResults(final List<String> tags) {
    final String path = "classpath:scenarios/karate-mocks";
    final Runner.Builder runner =
        Runner.builder()
            .path(path)
            .tags(tags)
            .outputCucumberJson(true)
            .outputJunitXml(true)
            .outputHtmlReport(true);
    final Suite suite = new Suite(runner);
    suite.run();
    return suite.buildResults();
  }
}
