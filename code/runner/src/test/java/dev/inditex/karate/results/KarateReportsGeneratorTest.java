package dev.inditex.karate.results;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import dev.inditex.karate.AbstractKarateTest;

import ch.qos.logback.classic.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.Suite;
import com.intuit.karate.core.FeatureResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@Slf4j
public class KarateReportsGeneratorTest extends AbstractKarateTest {

  @Override
  @AfterEach
  protected void afterEach() {
    // Clean test surefire reports folder so they are not counted in general reports
    try {
      FileUtils.deleteDirectory(new File(surefireReportsFolder));
    } catch (final IOException e) {
      log.error("Error deleting surefire reports folder", e);
    }
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateReportsGenerator::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Generate {
    @Test
    void when_informed_results_expect_aggregated_report_and_files_copied() throws IOException {
      final Results results = getTestResults("");

      final var result = generateResults(results);

      final var cucumberReport = new File(cucumberResultsFile);
      final Collection<File> xmlFiles =
          FileUtils.listFiles(new File(surefireReportsFolder), new String[]{"xml"}, true);
      assertThat(result).isEqualTo(String.format(KarateReportsGenerator.SUMMARY_FORMAT, 2, 1, 1, 5, 4, 1));
      assertThat(cucumberReport).exists().isReadable();
      assertCucumberReport(cucumberReport, results);
      assertThat(xmlFiles).hasSizeGreaterThanOrEqualTo(2).extracting(File::getName)
          .containsOnlyOnce("TEST-scenarios.karate-reports.reports-example-1.xml")
          .containsOnlyOnce("TEST-scenarios.karate-reports.reports-example-2.xml");
    }

    @Test
    void when_null_results_expect_aggregated_report_and_files_not_copied() throws IOException {
      final Results results = null;

      final var result = generateResults(results);

      final var cucumberReport = new File(cucumberResultsFile);
      assertThat(result).isEqualTo(String.format(KarateReportsGenerator.SUMMARY_FORMAT, 0, 0, 0, 0, 0, 0));
      assertThat(cucumberReport).exists().isReadable();
      assertCucumberReport(cucumberReport, results);
      assertThat(new File(surefireReportsFolder)).doesNotExist();
    }
  }

  protected String generateResults(final Results results) {
    return KarateReportsGenerator.generate(results);
  }

  @Nested
  class GenerateAggregatedCucumberReport {
    @Test
    void when_informed_results_expect_aggregated_report() throws IOException {
      final Results results = getTestResults("");

      final var result = KarateReportsGenerator.generateAggregatedCucumberReport(results);
      final var cucumberReport = new File(cucumberResultsFile);

      assertThat(result).isEqualTo(String.format(KarateReportsGenerator.SUMMARY_FORMAT, 2, 1, 1, 5, 4, 1));
      assertThat(cucumberReport).exists().isReadable();
      assertCucumberReport(cucumberReport, results);
    }

    @Test
    void when_null_results_expect_aggregated_report() throws IOException {
      final Results results = null;

      final var result = KarateReportsGenerator.generateAggregatedCucumberReport(results);
      final var cucumberReport = new File(cucumberResultsFile);

      assertThat(result).isEqualTo(String.format(KarateReportsGenerator.SUMMARY_FORMAT, 0, 0, 0, 0, 0, 0));
      assertThat(cucumberReport).exists().isReadable();
      assertCucumberReport(cucumberReport, results);
    }

    @Test
    void when_read_exception_expect_logged() {
      try (final MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
        final Results results = getTestResults("reports-example-1.feature");
        fileUtils.when(() -> FileUtils.listFiles(new File(results.getReportDir()), new String[]{"json"}, true))
            .thenReturn(List.of(new File("dummy.json")));
        fileUtils.when(() -> FileUtils.readFileToString(any(File.class), any(Charset.class)))
            .thenThrow(new IOException("FileUtils.readFileToString"));

        KarateReportsGenerator.generateAggregatedCucumberReport(results);

        assertThat(logWatcher.list).anyMatch(e -> e.getLevel().equals(Level.ERROR)
            && e.getFormattedMessage().contains(
                "KarateRunner.cucumberReport() saving Exception for [dummy.json] => "
                    + "[class java.io.IOException:FileUtils.readFileToString]"));
      }
    }

    @Test
    void when_write_exception_expect_logged() {
      try (final MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
        final Results results = getTestResults("reports-example-1.feature");
        fileUtils.when(() -> FileUtils.listFiles(new File(results.getReportDir()), new String[]{"json"}, true))
            .thenReturn(List.of(new File("dummy.json")));
        fileUtils.when(() -> FileUtils.readFileToString(any(File.class), any(Charset.class)))
            .thenReturn("[ { dummy : 0} ]");
        fileUtils.when(() -> FileUtils.writeStringToFile(any(File.class), anyString(), any(Charset.class)))
            .thenThrow(new IOException("FileUtils.writeStringToFile"));

        KarateReportsGenerator.generateAggregatedCucumberReport(results);

        assertThat(logWatcher.list).anyMatch(e -> e.getLevel().equals(Level.ERROR)
            && e.getFormattedMessage().contains(
                "KarateRunner.cucumberReport() saving Exception for [cucumber_result.json] => "
                    + "[java.io.IOException:FileUtils.writeStringToFile]"));
      }
    }
  }

  @Nested
  class CopyJUnitFileToSurefire {
    @Test
    void when_informed_results_expect_files_copied() {
      final Results results = getTestResults("");

      KarateReportsGenerator.copyJUnitFileToSurefire(results);
      final Collection<File> xmlFiles =
          FileUtils.listFiles(new File(surefireReportsFolder), new String[]{"xml"}, true);

      assertThat(xmlFiles).hasSizeGreaterThanOrEqualTo(2).extracting(File::getName)
          .containsOnlyOnce("TEST-scenarios.karate-reports.reports-example-1.xml")
          .containsOnlyOnce("TEST-scenarios.karate-reports.reports-example-2.xml");
    }

    @Test
    void when_null_results_expect_files_not_copied() {
      final Results results = null;

      KarateReportsGenerator.copyJUnitFileToSurefire(results);

      assertThat(new File(surefireReportsFolder)).doesNotExist();
    }

    @Test
    void when_copy_exception_expect_logged() {
      try (final MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
        final Results results = getTestResults("reports-example-1.feature");
        fileUtils.when(() -> FileUtils.listFiles(new File(results.getReportDir()), new String[]{"xml"}, true))
            .thenReturn(List.of(new File("dummy.xml")));
        fileUtils.when(() -> FileUtils.copyFile(any(File.class), any(File.class))).thenThrow(new IOException("FileUtils.copyFile"));

        KarateReportsGenerator.copyJUnitFileToSurefire(results);

        assertThat(logWatcher.list).anyMatch(e -> e.getLevel().equals(Level.ERROR)
            && e.getFormattedMessage().contains(
                "KarateRunner.surefireXMLs() saving Exception for [dummy.xml] => [class java.io.IOException:FileUtils.copyFile]"));
      }
    }
  }

  @Nested
  class GenerateSummary {
    @Test
    void when_informed_results_expect_informed_summary() {
      final Results results = getTestResults("");

      final var result = KarateReportsGenerator.generateSummary(results);

      assertThat(result).isEqualTo(String.format(KarateReportsGenerator.SUMMARY_FORMAT, 2, 1, 1, 5, 4, 1));
    }

    @Test
    void when_null_results_expect_zeros_summary() {
      final Results results = null;

      final var result = KarateReportsGenerator.generateSummary(results);

      assertThat(result).isEqualTo(String.format(KarateReportsGenerator.SUMMARY_FORMAT, 0, 0, 0, 0, 0, 0));
    }
  }

  protected void assertCucumberReport(final File actual, final Results expected) throws IOException {
    final String jsonString = FileUtils.readFileToString(actual, StandardCharsets.UTF_8);
    final JsonArray cucumberResult = (JsonArray) JsonParser.parseString(jsonString);
    if (expected == null) {
      assertThat(cucumberResult).isEmpty();
    } else {
      assertThat(cucumberResult).hasSize(expected.getFeaturesTotal());
      final List<FeatureResult> features = expected.getFeatureResults().toList();
      final List<String> actualTestNames = IntStream.range(0,
          cucumberResult.size()).mapToObj(i -> ((JsonObject) cucumberResult.get(i)).get("name").getAsString()).sorted().toList();
      final List<String> expectedTestNames = IntStream.range(0,
          features.size()).mapToObj(i -> features.get(i).getDisplayName()).sorted().toList();
      assertThat(actualTestNames).isEqualTo(expectedTestNames);
    }
  }

  @SuppressWarnings("rawtypes")
  protected Results getTestResults(final String file) {
    String path = "classpath:scenarios/karate-reports";
    if (file != null && !file.isEmpty()) {
      path = path + File.separator + file;
    }
    final Runner.Builder runner =
        Runner.builder().path(path)
            .outputCucumberJson(true)
            .outputJunitXml(true)
            .outputHtmlReport(true);
    final Suite suite = new Suite(runner);
    suite.run();
    return suite.buildResults();
  }

}
