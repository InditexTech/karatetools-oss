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
    final Path rootPath = Paths.get(targetFolder);
    final List<Path> outputs = new ArrayList<>();
    try {
      // Configure options
      configureOptions();
      // Execute based on Mode
      if (options.getMode() == OpenApiGeneratorModes.OPERATIONS) {
        outputs.addAll(executeOperations());
      } else if (options.getMode() == OpenApiGeneratorModes.SMOKE_TESTS) {
        outputs.addAll(executeSmokeTests());
      } else if (options.getMode() == OpenApiGeneratorModes.FUNCTIONAL_TEST) {
        outputs.addAll(executeFunctionalTest());
      } else if (options.getMode() == OpenApiGeneratorModes.MOCK_DATA) {
        outputs.addAll(executeMockData());
      }
    } catch (final IllegalArgumentException e) {
      OpenApiGeneratorANSILogger.error(e.getMessage());
    }
    outputs.forEach(p -> OpenApiGeneratorANSILogger.info("Generated " + rootPath.relativize(p)));
    return outputs;
  }

  /**
   * Configure options.
   */
  public void configureOptions() {
    options = new OpenApiGeneratorOptions();
    options.configure();
  }

  /**
   * Execute operations.
   *
   * @return the list
   */
  protected List<Path> executeOperations() {
    final Path rootPath = Paths.get(targetFolder);
    final List<Path> outputs = new ArrayList<>();
    OpenApiGeneratorANSILogger.info("Generating Operations ...");
    outputs.addAll(
        OpenApiGenerators.generateOperations(
            rootPath,
            options.getArtifact(),
            options.getOperations()));
    KarateConfig.updateKarateUrls(targetFolder, options.getArtifact());
    return outputs;
  }

  /**
   * Execute smoke tests.
   *
   * @return the list
   */
  protected List<Path> executeSmokeTests() {
    final Path rootPath = Paths.get(targetFolder);
    final List<Path> outputs = new ArrayList<>();
    OpenApiGeneratorANSILogger.info("Generating Smoke Tests ...");
    outputs.addAll(
        OpenApiGenerators.generateSmokeTests(
            rootPath,
            options.getArtifact(),
            options.getOperations(),
            options.getOpenApi()));
    return outputs;
  }

  /**
   * Execute functional test.
   *
   * @return the list
   */
  protected List<Path> executeFunctionalTest() {
    final Path rootPath = Paths.get(targetFolder);
    final List<Path> outputs = new ArrayList<>();
    OpenApiGeneratorANSILogger.info("Generating Functional Tests ...");
    OpenApiGeneratorANSILogger.info("Generating Functional Test Case [" + options.getTestName() + "] ...");
    outputs.addAll(
        OpenApiGenerators.generateFunctionalTest(
            rootPath,
            options.getArtifact(),
            options.getTestName(),
            options.getInlineMocks(),
            options.getOperationsResponses(),
            options.getOpenApi()));
    return outputs;
  }

  /**
   * Execute mock data.
   *
   * @return the list
   */
  protected List<Path> executeMockData() {
    final Path rootPath = Paths.get(targetFolder);
    final List<Path> outputs = new ArrayList<>();
    OpenApiGeneratorANSILogger.info("Generating Mock Data ...");
    OpenApiGeneratorANSILogger.info("Generating Mock Data ...");
    outputs.addAll(OpenApiGenerators.generateMockData(rootPath,
        options.getArtifact(),
        options.getInlineMocks(),
        options.getInlineMocksFunctionalArtifact(),
        options.getTestName(),
        options.getOperationsResponses(),
        options.getOpenApi()));
    return outputs;
  }
}
