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
      OpenApiGeneratorANSILogger.error(e.getMessage());
    }
    outputs.forEach(p -> OpenApiGeneratorANSILogger.info("Generated " + rootPath.relativize(p)));
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
    OpenApiGeneratorANSILogger.info("Generating Operations ...");
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
    OpenApiGeneratorANSILogger.info("Generating Smoke Tests ...");
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
    OpenApiGeneratorANSILogger.info("Generating Functional Tests ...");
    OpenApiGeneratorANSILogger.info("Generating Functional Test Case [" + this.options.getTestName() + "] ...");
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
    OpenApiGeneratorANSILogger.info("Generating Mock Data ...");
    OpenApiGeneratorANSILogger.info("Generating Mock Data ...");
    outputs.addAll(OpenApiGenerators.generateMockData(rootPath,
        this.options.getArtifact(), this.options.getInlineMocks(), this.options.getInlineMocksFunctionalArtifact(),
        this.options.getTestName(), this.options.getOperationsResponses(), this.options.getOpenApi()));
    return outputs;
  }
}
