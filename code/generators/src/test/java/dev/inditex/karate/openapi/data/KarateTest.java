package dev.inditex.karate.openapi.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.LoggerFactory;

public abstract class KarateTest {

  public static String targetFolder;

  public static String karateReportDir;

  protected ListAppender<ILoggingEvent> logWatcher;

  protected Level defaultLogLevel;

  @BeforeEach
  void beforeEach(final TestInfo testInfo) throws IOException {
    defaultLogLevel = ((Logger) LoggerFactory.getLogger("dev.inditex.karate.openapi.data")).getLevel();
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate.openapi.data")).addAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate.openapi.data")).setLevel(Level.WARN);
    prepareGeneratorFolders(testInfo);
  }

  @AfterEach
  void afterEach() {
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate.openapi.data")).detachAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("dev.inditex.karate.openapi.data")).setLevel(defaultLogLevel);
  }

  private void prepareGeneratorFolders(final TestInfo testInfo) throws IOException {
    final String testName = testInfo.getDisplayName() + "_" + testInfo.getTestMethod().get().getName();
    Files.createDirectories(Paths.get("target/tests-karate-openapi-generator"));
    targetFolder =
        Files.createTempDirectory(Paths.get("target/tests-karate-openapi-generator"), testName + "-").toFile().getAbsolutePath();
    karateReportDir = targetFolder + File.separator + "karate-reports";
  }

  protected File getFileFromResources(final String name) {
    try {
      return Paths.get(this.getClass().getResource(name).toURI()).toFile();
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getPathFromResources(final String name) {
    try {
      return this.getClass().getResource(name).toURI().toString();
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getResourceAsString(final String file) {
    try (final InputStream is = this.getClass().getResourceAsStream(file)) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
