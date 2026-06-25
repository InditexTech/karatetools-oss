package dev.inditex.karate.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;
import dev.inditex.karate.results.KarateReportsGenerator;
import dev.inditex.karate.test.KarateRunner;

import com.intuit.karate.Constants;
import com.intuit.karate.Results;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class KarateBaseReportOptionsIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {

    @Test
    void when_report_showlog_true_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest(
          "@report-showlog-true",
          "report-option-showlog-true-parsing",
          "showLog[ true ] showAllSteps[ true ]");

      final var result = executeScenariosWithReportOptions(test, "--showLog true --showAllStepsOption true");

      assertThat(result).isZero();
    }

    @Test
    void when_report_showallsteps_false_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest(
          "@report-showallsteps-false",
          "report-option-showallsteps-false-parsing",
          "showLog[ true ] showAllSteps[ false ]");

      final var result = executeScenariosWithReportOptions(test, "--showLog true --showAllStepsOption false");

      assertThat(result).isZero();
    }
  }

  protected KarateExecutionTest getKarateExecutionTest(final String scenarioTag, final String scenarioName,
      final String verbosityLog) {
    return new KarateExecutionTest(
        "local",
        getKarateOptions(scenarioTag),
        1,
        1,
        List.of("/TEST-scenarios.base.report-options.xml"),
        Map.of(),
        List.of(
            "Running Feature[ karate-base-report-options ] Scenario [ " + scenarioName + " ] ... ",
            "Report Verbosity >> " + verbosityLog));
  }

  protected String getKarateOptions(final String scenarioTag) {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @karate-base-report -t "
        + scenarioTag;
  }

  protected int executeScenariosWithReportOptions(final KarateExecutionTest test, final String reportOptions)
      throws IOException {
    System.setProperty(Constants.KARATE_ENV, test.env());
    System.setProperty(Constants.KARATE_OPTIONS, test.options());
    System.setProperty("karate.report.options", reportOptions);
    final KarateRunner runner = instantiateRunner();

    final Results results = runner.execute();
    final var result = KarateReportsGenerator.generate(results);

    assertResults(results);
    assertSummary(result, test.featuresCount(), test.scenariosCount());
    assertCucumberResults();
    assertSurefireReports(test.surefireFiles());
    assertStats(test.stats());
    assertLogs(test.logs());
    return results.getFailCount();
  }
}
