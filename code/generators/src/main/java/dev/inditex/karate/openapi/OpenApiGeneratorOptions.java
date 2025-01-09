package dev.inditex.karate.openapi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dev.inditex.karate.openapi.data.MavenArtifact;
import dev.inditex.karate.openapi.data.MavenUtils;
import dev.inditex.karate.openapi.data.OpenApiParser;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * The Class OpenApiGeneratorOptions.
 */
@Getter
@Setter
@Slf4j
public class OpenApiGeneratorOptions {

  /** The Constant POM_FILE. */
  protected static final String POM_FILE = "pom.xml";

  /** The mode. */
  // Common Options
  private OpenApiGeneratorMode mode = OpenApiGeneratorConsole.OPEN_API_GENERATOR_MODE_DEFAULT_VALUE;

  /** The open api. */
  private OpenAPI openApi;

  /** The operations. */
  private List<OperationPath> operations;

  /** The artifacts. */
  private Map<String, MavenArtifact> artifacts = MavenUtils.getPomArtifacts(POM_FILE);

  /** The artifact. */
  private MavenArtifact artifact;

  /** The operations responses. */
  // Specific Options based on Mode(s)
  private Map<OperationPath, Set<String>> operationsResponses;

  /** The test name. */
  private String testName;

  /** The inline mocks. */
  private Boolean inlineMocks;

  /** The inline mocks functional artifact. */
  private MavenArtifact inlineMocksFunctionalArtifact;

  /**
   * Configure.
   */
  public void configure() {
    // Select Execution Mode
    selectMode();

    // Select Open Api File
    selectOpenApi();

    // Select Operations
    selectOperations();

    // Select Operations Responses
    if (needsOperationsResponses()) {
      selectOperationsResponses();
    }

    // Select Artifact
    selectArtifact();

    // Select Test Name
    if (needsTestName()) {
      selectTestName();
    }

    // Select Functional with Inline Mocks
    if (needsFunctionalWithInlineMocks()) {
      selectFunctionalWithInlineMocks();
    }

    // Select Inline Mocks
    if (needsInlineMocks()) {
      selectInlineMocks();
      // Select Inline Mocks Functional Artifact and Test Name
      if (inlineMocks != null && inlineMocks) {
        selectInlineMocksFunctionalArtifact();
        selectInlineMocksFunctionalTestName();
      }
    }
  }

  /**
   * Select mode.
   */
  public void selectMode() {
    // Prompt for Execution Mode
    mode = OpenApiGeneratorConsole.promptMode();
    log.debug("OpenApiGeneratorOptions.mode: {}", mode);
  }

  /**
   * Select open api.
   */
  protected void selectOpenApi() {
    // Get Open Api Files
    final List<String> openApis = findOpenApis();
    // Prompt for Open Api File from List
    String openApiFileName = OpenApiGeneratorConsole.promptOpenApi(openApis);
    // If none provided prompt for manually entered
    if (openApiFileName == null) {
      openApiFileName = OpenApiGeneratorConsole.promptOpenApi();
      validateApiFile(openApiFileName);
    }
    // Parse Selected Open Api
    final OpenAPI api = OpenApiParser.parseOpenApi(openApiFileName);
    if (api == null) {
      throw new IllegalArgumentException("Unable to parse Open Api file");
    }
    log.debug("OpenApiGeneratorOptions.openApi: {}", openApiFileName);
    openApi = api;
  }

  /**
   * Select operations.
   */
  protected void selectOperations() {
    // Find Operations in Open Api
    final var operationsByTag = OpenApiParser.getOperationsByTag(openApi);
    if (operationsByTag == null || operationsByTag.isEmpty()) {
      throw new IllegalArgumentException("Unable to parse Open Api file Operations");
    }
    // Prompt for Operations to generate
    final List<OperationPath> ops = OpenApiGeneratorConsole.promptOperations(operationsByTag);
    if (ops == null || ops.isEmpty()) {
      throw new IllegalArgumentException("Operations to generate for must be selected");
    }
    operations = ops;
  }

  /**
   * Needs operations responses.
   *
   * @return true, if successful
   */
  protected boolean needsOperationsResponses() {
    return mode == OpenApiGeneratorModes.FUNCTIONAL_TEST || mode == OpenApiGeneratorModes.MOCK_DATA;
  }

  /**
   * Select operations responses.
   */
  protected void selectOperationsResponses() {
    final Map<OperationPath, Set<String>> opsResponses = operations.stream().collect(Collectors.toMap(
        o -> o, o -> OpenApiGeneratorConsole.promptOperationResponses(o, o.operation().getResponses().keySet())));
    if (opsResponses.entrySet().stream().filter(e -> e.getValue() == null || e.getValue().isEmpty())
        .findAny()
        .orElse(null) != null) {
      throw new IllegalArgumentException("Responses to generate for must be selected for all Operations");
    }
    operationsResponses = opsResponses;
  }

  /**
   * Select artifact.
   */
  protected void selectArtifact() {
    // Prompt for Artifact from List
    final String artifactId = OpenApiGeneratorConsole.promptArtifacts(
        artifacts.keySet().stream().toList(), artifacts.keySet().iterator().next());
    artifacts.putIfAbsent(artifactId,
        MavenArtifact.builder().groupId(artifactId.split(":")[0]).artifactId(artifactId.split(":")[1]).build());
    final MavenArtifact selectedArtifact = artifacts.get(artifactId);
    log.debug("OpenApiGeneratorOptions.artifact: {}", artifactId);
    artifact = selectedArtifact;
  }

  /**
   * Needs test name.
   *
   * @return true, if successful
   */
  protected boolean needsTestName() {
    return mode == OpenApiGeneratorModes.FUNCTIONAL_TEST;
  }

  /**
   * Select test name.
   */
  protected void selectTestName() {
    testName = selectTestName("Enter the name of the test to generate",
        "Test Name must be provided for functional tests");
    log.debug("OpenApiGeneratorOptions.testName: {}", testName);
  }

  /**
   * Select test name.
   *
   * @param message the message
   * @param error the error
   * @return the string
   */
  protected String selectTestName(final String message, final String error) {
    final String test = OpenApiGeneratorConsole.promptTestName(message);
    // If none provided break
    if (test == null) {
      throw new IllegalArgumentException(error);
    }
    return test;
  }

  /**
   * Needs functional with inline mocks.
   *
   * @return true, if successful
   */
  protected boolean needsFunctionalWithInlineMocks() {
    return mode == OpenApiGeneratorModes.FUNCTIONAL_TEST;
  }

  /**
   * Select functional with inline mocks.
   */
  protected void selectFunctionalWithInlineMocks() {
    inlineMocks = OpenApiGeneratorConsole.promptInlineMocks("Select if the test will include inline mocks");
  }

  /**
   * Needs inline mocks.
   *
   * @return true, if successful
   */
  protected boolean needsInlineMocks() {
    return mode == OpenApiGeneratorModes.MOCK_DATA;
  }

  /**
   * Select inline mocks.
   */
  protected void selectInlineMocks() {
    inlineMocks = OpenApiGeneratorConsole.promptInlineMocks("Select if the mocks are to be included in a functional test (Inline)");
  }

  /**
   * Select inline mocks functional artifact.
   */
  protected void selectInlineMocksFunctionalArtifact() {
    // Functional Test Artifact Id needed for Inline Mocks
    final String functionalArtifactId =
        OpenApiGeneratorConsole.promptSecondaryArtifacts("Select the artifact of the functional test for these inline mocks",
            artifacts.keySet().stream().toList(), artifacts.keySet().iterator().next());
    artifacts.putIfAbsent(functionalArtifactId,
        MavenArtifact.builder().groupId(functionalArtifactId.split(":")[0]).artifactId(functionalArtifactId.split(":")[1]).build());
    inlineMocksFunctionalArtifact = artifacts.get(functionalArtifactId);
    log.debug("OpenApiGeneratorOptions.inlineMocksFunctionalArtifact: {}", functionalArtifactId);
  }

  /**
   * Select inline mocks functional test name.
   */
  protected void selectInlineMocksFunctionalTestName() {
    testName = selectTestName("Enter the name of the functional test for these inline mocks",
        "Test Name must be provided for inline mocks");
    log.debug("OpenApiGeneratorOptions.testName: {}", testName);
  }

  /**
   * Find open apis.
   *
   * @return the list
   */
  protected List<String> findOpenApis() {
    final File root = new File(".");
    return FileUtils.listFiles(root, new IOFileFilter() {
      @Override
      public boolean accept(final File dir, final String name) {
        return isOpenApi(dir, name);
      }

      @Override
      public boolean accept(final File file) {
        if (file.isFile()) {
          return isOpenApi(file.getParentFile(), file.getName());
        }
        return false;
      }
    }, DirectoryFileFilter.DIRECTORY).stream().map(File::getPath).toList();
  }

  /**
   * Checks if is open api.
   *
   * @param dir the dir
   * @param name the name
   * @return true, if is open api
   */
  protected boolean isOpenApi(final File dir, final String name) {
    final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    if (name.endsWith(".yml")) {
      final File file = new File(dir.getAbsolutePath() + File.separator + name);
      try {
        final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        final Map<String, Object> yaml = yamlMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        return yaml.containsKey("openapi");
      } catch (final IOException e) {
        OpenApiGeneratorANSILogger.warn(
            String.format("Not an Open Api file %s in folder %s %n    Exception [%s]", name, dir, e.getMessage()));
      }
    }
    return false;
  }

  /**
   * Validate api file.
   *
   * @param openApiFileName the open api file name
   * @return true, if successful
   */
  protected boolean validateApiFile(final String openApiFileName) {
    // If none provided break
    if (openApiFileName == null) {
      throw new IllegalArgumentException("Open Api file must be provided");
    }
    // If file does not exist break
    final File openApiFile = new File(openApiFileName);
    if (!openApiFile.exists() || openApiFile.isDirectory()) {
      throw new IllegalArgumentException("Open Api file must be valid");
    }
    return true;
  }

}
