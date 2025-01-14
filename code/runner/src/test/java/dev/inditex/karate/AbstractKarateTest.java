package dev.inditex.karate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.fileNamesIn;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.results.KarateOperationsStatsHook.Stats;
import dev.inditex.karate.results.KarateReportsGenerator;
import dev.inditex.karate.test.KarateRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.karate.Constants;
import com.intuit.karate.Results;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.LoggerFactory;

public abstract class AbstractKarateTest {

  protected String targetFolderName;

  protected File targetFolder;

  protected String cucumberResultsFile;

  protected String surefireReportsFolder;

  protected String operationStatsFile;

  protected ListAppender<ILoggingEvent> logWatcher;

  protected Level defaultLogLevelIntuitKarate;

  protected Level defaultLogLevelKarateTools;

  @BeforeEach
  protected void beforeEach(final TestInfo testInfo) throws IOException {
    defaultLogLevelIntuitKarate = ((Logger) LoggerFactory.getLogger("com.intuit.karate")).getLevel();
    defaultLogLevelKarateTools = ((Logger) LoggerFactory.getLogger("dev.inditex.karate")).getLevel();
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((Logger) LoggerFactory.getLogger("com.intuit.karate")).addAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("com.intuit.karate")).setLevel(Level.DEBUG);
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate")).addAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate")).setLevel(Level.DEBUG);
    clearKarateSystemProperties();
    prepareReportFolders(testInfo);
    setRamdomPortForKarateMockServer();
  }

  @AfterEach
  protected void afterEach() {
    ((Logger) LoggerFactory.getLogger("com.intuit.karate")).detachAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("com.intuit.karate")).setLevel(defaultLogLevelIntuitKarate);
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate")).detachAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate")).setLevel(defaultLogLevelKarateTools);
  }

  protected void clearKarateSystemProperties() {
    System.clearProperty(Constants.KARATE_ENV);
    System.clearProperty(Constants.KARATE_CONFIG_DIR);
    System.clearProperty(Constants.KARATE_CONFIG_INCL_RESULT_METHOD);
    System.clearProperty(Constants.KARATE_OUTPUT_DIR);
    System.clearProperty(Constants.KARATE_OPTIONS);
    System.clearProperty(Constants.KARATE_REPORTS);
    System.clearProperty(Constants.KARATE_JSON_SUFFIX);
  }

  protected void prepareReportFolders(final TestInfo testInfo) throws IOException {
    Files.createDirectories(Paths.get("target/karate-runner"));
    final String testName = testInfo.getTestMethod().get().getName();
    targetFolder = Files.createTempDirectory(Paths.get("target/karate-runner"), testName + "-").toFile();
    targetFolderName = targetFolder.getAbsolutePath();
    cucumberResultsFile = targetFolder.getAbsolutePath() + File.separator + "cucumber_result.json";
    surefireReportsFolder = targetFolder.getAbsolutePath() + File.separator + "surefire-reports";
    operationStatsFile = targetFolder.getAbsolutePath() + File.separator + "karate-reports" + File.separator + "karate-operations-json.txt";
    KarateReportsGenerator.setFolders(surefireReportsFolder, cucumberResultsFile);
    System.setProperty(Constants.KARATE_OUTPUT_DIR, targetFolderName);
  }

  public record KarateExecutionTest(
      String env,
      String options,
      int featuresCount,
      int scenariosCount,
      List<String> surefireFiles,
      Map<String, Stats> stats,
      List<String> logs) {
  }

  protected int executeScenarios(final KarateExecutionTest test) throws IOException {
    System.setProperty(Constants.KARATE_ENV, test.env);
    System.setProperty(Constants.KARATE_OPTIONS, test.options);
    System.setProperty("karate.report.options", "--showLog true");
    final KarateRunner runner = instantiateRunner();

    final Results results = runner.execute();
    final var result = KarateReportsGenerator.generate(results);

    assertResults(results);
    assertSummary(result, test.featuresCount, test.scenariosCount);
    assertCucumberResults();
    assertSurefireReports(test.surefireFiles);
    assertStats(test.stats);
    assertLogs(test.logs);
    return results.getFailCount();
  }

  protected KarateRunner instantiateRunner() {
    return new KarateRunner();
  }

  protected void setRamdomPortForKarateMockServer() {
    try (final ServerSocket serverSocket = new ServerSocket(0)) {
      System.setProperty("KARATE_MOCK_SERVER_PORT", String.valueOf(serverSocket.getLocalPort()));
    } catch (final IOException e) {
      throw new RuntimeException("Could not set random port", e);
    }
  }

  protected void assertResults(final Results results) {
    // No failures or errors
    assertThat(results).isNotNull();
    assertThat(results.getFailCount()).as("Karate Fail Count [%s]", results.getScenariosFailed()).isZero();
  }

  protected void assertSummary(final String summary, final int features, final int scenarios) {
    // Expected summary
    if (summary != null) {
      assertThat(summary).isEqualTo(String.format(KarateReportsGenerator.SUMMARY_FORMAT, features, features, 0, scenarios, scenarios, 0));
    }
  }

  protected void assertCucumberResults() {
    // Cucumber report present
    assertThat(new File(cucumberResultsFile)).exists().isReadable();
  }

  protected void assertSurefireReports(final List<String> expectedSurefireFiles) {
    // Surefire reports present
    assertThat(fileNamesIn(surefireReportsFolder, /* recurse */ true))
        .containsExactlyInAnyOrderElementsOf(expectedSurefireFiles.stream().map(this::getSurefireFile).toList());
  }

  protected void assertStats(final Map<String, Stats> expectedStats) throws IOException {
    // Stats file present
    final var statsFile = new File(operationStatsFile);
    assertThat(statsFile).exists().isReadable();
    // Expected Stats
    final ObjectMapper mapper = new ObjectMapper();
    final TypeReference<Map<String, Stats>> typeRef = new TypeReference<Map<String, Stats>>() {};
    final var stats = mapper.readValue(statsFile, typeRef);
    assertThat(stats).isNotNull().containsExactlyInAnyOrderEntriesOf(expectedStats);
  }

  protected void assertLogs(final List<String> expectedLogs) {
    // Expected logs
    expectedLogs.forEach(log -> assertThat(logWatcher.list).anyMatch(e -> e.getFormattedMessage().contains(log)));
  }

  protected String getSurefireFile(final String file) {
    return surefireReportsFolder + file.replace('/', File.separatorChar);
  }

}
