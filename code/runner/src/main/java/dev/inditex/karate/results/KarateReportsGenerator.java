package dev.inditex.karate.results;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.intuit.karate.Results;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 * The Class KarateReportsGenerator.
 */
@Slf4j
public class KarateReportsGenerator {

  /** The Constant SUMMARY_FORMAT. */
  public static final String SUMMARY_FORMAT = """
      Summary :
          Total features: %s
              Features passed: %s
              Features failed: %s
          Total scenarios: %s
              Scenarios passed: %s
              Scenarios failed: %s
      """.replace("\n", "%n");

  /** The surefire report folder. */
  protected static String surefireReportFolder = "target/surefire-reports";

  /** The cucumber results file. */
  protected static String cucumberResultsFile = "target/cucumber_result.json";

  /**
   * Instantiates a new karate reports generator.
   */
  protected KarateReportsGenerator() {
  }

  /**
   * Sets the folders.
   *
   * @param surefire the surefire
   * @param cucumber the cucumber
   */
  public static void setFolders(final String surefire, final String cucumber) {
    surefireReportFolder = surefire;
    cucumberResultsFile = cucumber;
  }

  /**
   * Generate.
   *
   * @param results the results
   * @return the string
   */
  public static String generate(final Results results) {
    final String summary = generateAggregatedCucumberReport(results);
    copyJUnitFileToSurefire(results);
    return summary;
  }

  /**
   * Generate aggregated cucumber report.
   *
   * @param results the results
   * @return the string
   */
  public static String generateAggregatedCucumberReport(final Results results) {
    log.debug("KarateRunner.cucumberReport() saving ...");
    final JsonArray jsonResults = new JsonArray();
    Collection<File> jsonFiles = List.of();
    if (results != null) {
      jsonFiles = FileUtils.listFiles(new File(results.getReportDir()), new String[]{"json"}, true);
    }
    jsonFiles.forEach(jsonFile -> {
      try {
        final String jsonString = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
        jsonResults.addAll((JsonArray) JsonParser.parseString(jsonString));
        log.debug("KarateRunner.cucumberReport() saving [{}]", jsonFile);
      } catch (final IOException e) {
        log.error("KarateRunner.cucumberReport() saving Exception for [{}] => [{}:{}]", jsonFile, e.getClass(), e.getMessage());
      }
    });
    try {
      FileUtils.writeStringToFile(new File(cucumberResultsFile), jsonResults.toString(), Charset.defaultCharset());
      log.info("KarateRunner.cucumberReport() saved [{}]", cucumberResultsFile);
    } catch (final IOException e) {
      log.error("KarateRunner.cucumberReport() saving Exception for [{}] => [{}:{}]",
          "cucumber_result.json", e.getClass().getName(), e.getMessage());
    }
    return generateSummary(results);
  }

  /**
   * Copy J unit file to surefire.
   *
   * @param results the results
   */
  public static void copyJUnitFileToSurefire(final Results results) {
    if (results != null) {
      log.debug("KarateRunner.surefireXMLs() saving ...");
      final Collection<File> xmlFiles = FileUtils.listFiles(new File(results.getReportDir()), new String[]{"xml"}, true);
      xmlFiles.forEach(xmlFile -> {
        try {
          FileUtils.copyFile(xmlFile, new File(surefireReportFolder + "/TEST-" + xmlFile.getName()));
          log.debug("KarateRunner.surefireXMLs() saving [{}]", xmlFile);
        } catch (final IOException e) {
          log.error("KarateRunner.surefireXMLs() saving Exception for [{}] => [{}:{}]",
              xmlFile, e.getClass(), e.getMessage());
        }
      });
      log.info("KarateRunner.surefireXMLs() saved [{}] files to [{}]", xmlFiles.size(), surefireReportFolder);
    }
  }

  /**
   * Generate summary.
   *
   * @param results the results
   * @return the string
   */
  public static String generateSummary(final Results results) {
    if (results != null) {
      return String.format(SUMMARY_FORMAT, results.getFeaturesTotal(), results.getFeaturesPassed(), results.getFeaturesFailed(),
          results.getScenariosTotal(), results.getScenariosPassed(), results.getScenariosFailed());
    }
    return String.format(SUMMARY_FORMAT, 0, 0, 0, 0, 0, 0);
  }
}
