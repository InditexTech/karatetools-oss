package dev.inditex.karate.openapi;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dev.inditex.karate.openapi.data.KarateConfig;
import dev.inditex.karate.openapi.data.OpenApiGenerators;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OpenApiGenerator.
 */
@Getter
@Setter
@Slf4j
public class OpenApiGenerator {

  /** The Constant KARATE_TARGET_FOLDER_DEFAULT_VALUE. */
  public static final String KARATE_TARGET_FOLDER_DEFAULT_VALUE = FilenameUtils.separatorsToSystem("src/test/resources");

  /** The target folder. */
  private final String targetFolder;

  /** The options. */
  private OpenApiGeneratorOptions options;

  /**
   * Instantiates a new open api generator.
   */
  public OpenApiGenerator() {
    this(KARATE_TARGET_FOLDER_DEFAULT_VALUE);
  }

  /**
   * Instantiates a new open api generator.
   *
   * @param targetFolder the target folder
   */
  public OpenApiGenerator(final String targetFolder) {
    this.targetFolder = targetFolder;
  }

  /**
   * Execute.
   *
   * @return the list
   */
  public List<Path> execute() {
    log.debug("OpenApiGenerator.execute() ...");
    final Path rootPath = Paths.get(this.targetFolder);
    final List<Path> outputs = new ArrayList<>();
    try {
      // Configure options
      this.configureOptions();
      // Execute based on Mode
      if (this.options.getMode() == OpenApiGeneratorModes.OPERATIONS) {
        outputs.addAll(this.executeOperations());
      } else if (this.options.getMode() == OpenApiGeneratorModes.SMOKE_TESTS) {
        outputs.addAll(this.executeSmokeTests());
      } else if (this.options.getMode() == OpenApiGeneratorModes.FUNCTIONAL_TEST) {
        outputs.addAll(this.executeFunctionalTest());
      } else if (this.options.getMode() == OpenApiGeneratorModes.MOCK_DATA) {
        outputs.addAll(this.executeMockData());
      }
    } catch (final IllegalArgumentException e) {
      ANSILogger.error(e.getMessage());
    }
    outputs.forEach(p -> ANSILogger.info("Generated " + rootPath.relativize(p)));
    return outputs;
  }

  /**
   * Configure options.
   */
  public void configureOptions() {
    this.options = new OpenApiGeneratorOptions();
    this.options.configure();
  }

  /**
   * Execute operations.
   *
   * @return the list
   */
  protected List<Path> executeOperations() {
    final Path rootPath = Paths.get(this.targetFolder);
    final List<Path> outputs = new ArrayList<>();
    ANSILogger.info("Generating Operations ...");
    outputs.addAll(OpenApiGenerators.generateOperations(rootPath, this.options.getArtifact(), this.options.getOperations()));
    KarateConfig.updateKarateUrls(this.targetFolder, this.options.getArtifact());
    return outputs;
  }

  /**
   * Execute smoke tests.
   *
   * @return the list
   */
  protected List<Path> executeSmokeTests() {
    final Path rootPath = Paths.get(this.targetFolder);
    final List<Path> outputs = new ArrayList<>();
    ANSILogger.info("Generating Smoke Tests ...");
    outputs.addAll(OpenApiGenerators.generateSmokeTests(rootPath, this.options.getArtifact(), this.options.getOperations(),
        this.options.getOpenApi()));
    return outputs;
  }

  /**
   * Execute functional test.
   *
   * @return the list
   */
  protected List<Path> executeFunctionalTest() {
    final Path rootPath = Paths.get(this.targetFolder);
    final List<Path> outputs = new ArrayList<>();
    ANSILogger.info("Generating Functional Tests ...");
    ANSILogger.info("Generating Functional Test Case [" + this.options.getTestName() + "] ...");
    outputs.addAll(OpenApiGenerators.generateFunctionalTest(rootPath,
        this.options.getArtifact(), this.options.getTestName(), this.options.getInlineMocks(), this.options.getOperationsResponses(),
        this.options.getOpenApi()));
    return outputs;
  }

  /**
   * Execute mock data.
   *
   * @return the list
   */
  protected List<Path> executeMockData() {
    final Path rootPath = Paths.get(this.targetFolder);
    final List<Path> outputs = new ArrayList<>();
    ANSILogger.info("Generating Mock Data ...");
    ANSILogger.info("Generating Mock Data ...");
    outputs.addAll(OpenApiGenerators.generateMockData(rootPath,
        this.options.getArtifact(), this.options.getInlineMocks(), this.options.getInlineMocksFunctionalArtifact(),
        this.options.getTestName(), this.options.getOperationsResponses(), this.options.getOpenApi()));
    return outputs;
  }

  /**
   * The Class ANSILogger.
   */
  protected static class ANSILogger {

    /** The Constant OPEN_API_GENERATOR_STDOUT. */
    protected static final Logger OPEN_API_GENERATOR_STDOUT = LoggerFactory.getLogger("OpenApiGenerator");

    /** The Constant ANSI_RESET. */
    protected static final String ANSI_RESET = "\u001B[0m";

    /** The Constant ANSI_BLUE. */
    protected static final String ANSI_BLUE = "\u001B[34m";

    /** The Constant ANSI_YELLOW. */
    protected static final String ANSI_YELLOW = "\u001B[33m";

    /** The Constant ANSI_RED. */
    protected static final String ANSI_RED = "\u001B[31m";

    /** The Constant LOG_FORMAT. */
    protected static final String LOG_FORMAT = "\n{}{}{}{}\n";

    /**
     * Instantiates a new ANSI logger.
     */
    protected ANSILogger() {
      // Empty
    }

    /**
     * Info.
     *
     * @param message the message
     */
    public static void info(final String message) {
      OPEN_API_GENERATOR_STDOUT.info(LOG_FORMAT, ANSI_BLUE, "INFO  - ", message, ANSI_RESET);
    }

    /**
     * Warn.
     *
     * @param message the message
     */
    public static void warn(final String message) {
      OPEN_API_GENERATOR_STDOUT.debug(LOG_FORMAT, ANSI_YELLOW, "WARN  - ", message, ANSI_RESET);
    }

    /**
     * Error.
     *
     * @param message the message
     */
    public static void error(final String message) {
      OPEN_API_GENERATOR_STDOUT.error(LOG_FORMAT, ANSI_RED, "ERROR - ", message, ANSI_RESET);
    }
  }
}
