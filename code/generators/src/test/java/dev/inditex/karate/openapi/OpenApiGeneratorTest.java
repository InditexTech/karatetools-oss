package dev.inditex.karate.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import dev.inditex.karate.console.ConsoleCLI;
import dev.inditex.karate.console.ConsoleTestUtils;
import dev.inditex.karate.openapi.data.MavenArtifact;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.codeshelf.consoleui.elements.ConfirmChoice.ConfirmationValue;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.ConfirmResult;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

public class OpenApiGeneratorTest {

  protected static String targetFolder;

  protected OpenApiGenerator generator;

  protected ListAppender<ILoggingEvent> logWatcher;

  @BeforeEach
  protected void beforeEach(final TestInfo testInfo) throws IOException {
    ConsoleCLI.withoutRealTerminal();
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((Logger) LoggerFactory.getLogger("OpenApiGenerator")).addAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("OpenApiGenerator")).setLevel(Level.WARN);
    prepareGeneratorFolders(testInfo);
    generator = new OpenApiGenerator(targetFolder);
  }

  protected void prepareGeneratorFolders(final TestInfo testInfo) throws IOException {
    final String testName = testInfo.getDisplayName() + "_" + testInfo.getTestMethod().get().getName();
    Files.createDirectories(Paths.get("target/tests-karate-openapi-generator"));
    targetFolder = Files.createTempDirectory(
        Paths.get("target/tests-karate-openapi-generator"), testName + "-").toFile().getAbsolutePath();
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiGenerator::new).doesNotThrowAnyException();
    }

    @Test
    void when_instance_with_target_folder_expect_no_exception() {
      assertThatCode(() -> new OpenApiGenerator(targetFolder)).doesNotThrowAnyException();
    }
  }

  @Nested
  class Execute {

    @ParameterizedTest(name = "OpenApiGenerator-operations-{1}-Man-{0}")
    @MethodSource
    void when_execute_api_operations_expect_result(
        final boolean manualInput, final String operationId, final String operationDisplayName, final OperationPath operation,
        final List<String> expectedFiles, final List<String> expectedContent)
        throws NoSuchFieldException, SecurityException, IOException {
      // Copy config-local.yml to target folder so it can be updated by the generator
      final Path target = Paths.get(targetFolder);
      final InputStream sourceConfig = this.getClass().getResourceAsStream("/openapi/unit/config/config-local.yml");
      final Path targetConfig = target.resolve("config-local.yml");
      Files.copy(sourceConfig, targetConfig);
      // Initialize prompt values
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.OPERATIONS;
      final String artifactId = "dev.inditex.karate.openapi:test";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      if (manualInput) {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
            new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      } else {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      }
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(generator.getOptions().getMode()).isEqualTo(mode);
      assertThat(generator.getOptions().getArtifact()).isEqualTo(MavenArtifact.fromId(artifactId));
      assertThat(generator.getOptions().getOpenApi()).isNotNull();
      assertThat(generator.getOptions().getOperations()).isNotNull().hasSize(1);
      assertThat(generator.getOptions().getOperations().get(0).method()).isEqualTo(operation.method());
      assertThat(generator.getOptions().getOperations().get(0).path()).isEqualTo(operation.path());
      assertThat(generator.getOptions().getOperations().get(0).operation().getOperationId()).isEqualTo(operationId);
      assertThat(result).hasSameSizeAs(expectedFiles);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i)).isEqualTo(Paths.get(targetFolder, expectedFiles.get(i)));
      }
      assertThat(result).hasSameSizeAs(expectedContent);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i).toFile()).exists().hasContent(getResourceAsString(expectedContent.get(i)));
      }
      // config file contains xxxApiRestStableUrl
      final String configContent = Files.readString(targetConfig);
      assertThat(configContent)
          .contains("testUrl: \"#('http://localhost:' + (karate.properties['APP_PORT'] || 8080) + '/TO_BE_COMPLETED')\"");
    }

    public static Stream<Arguments> when_execute_api_operations_expect_result() {
      return Stream.of(
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Expected files
              List.of(
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_400.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_default.schema.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/operation/BasicApi/createItems/createItems.feature",
                  "/openapi/unit/operation/BasicApi/createItems/schema/createItems_201.schema.yml",
                  "/openapi/unit/operation/BasicApi/createItems/schema/createItems_400.schema.yml",
                  "/openapi/unit/operation/BasicApi/createItems/schema/createItems_default.schema.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getListItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getListItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getListItemsOperation(),
              // Expected files
              List.of(
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/Items_200.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_400.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_default.schema.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/operation/BasicApi/listItems/listItems.feature",
                  "/openapi/unit/operation/BasicApi/listItems/schema/Items_200.schema.yml",
                  "/openapi/unit/operation/BasicApi/listItems/schema/listItems_200.schema.yml",
                  "/openapi/unit/operation/BasicApi/listItems/schema/listItems_400.schema.yml",
                  "/openapi/unit/operation/BasicApi/listItems/schema/listItems_default.schema.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Expected files
              List.of(
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_404.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_default.schema.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/operation/BasicApi/showItemById/showItemById.feature",
                  "/openapi/unit/operation/BasicApi/showItemById/schema/showItemById_200.schema.yml",
                  "/openapi/unit/operation/BasicApi/showItemById/schema/showItemById_404.schema.yml",
                  "/openapi/unit/operation/BasicApi/showItemById/schema/showItemById_default.schema.yml")),
          Arguments.of(
              // Manual Input
              false,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Expected files
              List.of(
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_404.schema.yml",
                  "/apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_default.schema.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/operation/BasicApi/showItemById/showItemById.feature",
                  "/openapi/unit/operation/BasicApi/showItemById/schema/showItemById_200.schema.yml",
                  "/openapi/unit/operation/BasicApi/showItemById/schema/showItemById_404.schema.yml",
                  "/openapi/unit/operation/BasicApi/showItemById/schema/showItemById_default.schema.yml")));
    }

    @ParameterizedTest(name = "OpenApiGenerator-smoke-{1}-Man-{0}")
    @MethodSource
    void when_execute_api_smoke_expect_result(
        final boolean manualInput, final String operationId, final String operationDisplayName, final OperationPath operation,
        final List<String> expectedFiles, final List<String> expectedContent)
        throws NoSuchFieldException, SecurityException {
      // Initialize prompt values
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.SMOKE_TESTS;
      final String artifactId = "dev.inditex.karate.openapi:test";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      if (manualInput) {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
            new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      } else {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      }
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(generator.getOptions().getMode()).isEqualTo(mode);
      assertThat(generator.getOptions().getArtifact()).isEqualTo(MavenArtifact.fromId(artifactId));
      assertThat(generator.getOptions().getOpenApi()).isNotNull();
      assertThat(generator.getOptions().getOperations()).isNotNull().hasSize(1);
      assertThat(generator.getOptions().getOperations().get(0).method()).isEqualTo(operation.method());
      assertThat(generator.getOptions().getOperations().get(0).path()).isEqualTo(operation.path());
      assertThat(generator.getOptions().getOperations().get(0).operation().getOperationId()).isEqualTo(operationId);
      assertThat(result).hasSameSizeAs(expectedFiles);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i)).isEqualTo(Paths.get(targetFolder, expectedFiles.get(i)));
      }
      assertThat(result).hasSameSizeAs(expectedContent);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i).toFile()).exists().hasContent(getResourceAsString(expectedContent.get(i)));
      }
    }

    public static Stream<Arguments> when_execute_api_smoke_expect_result() {
      return Stream.of(

          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/createItems/createItems.feature",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/createItems/test-data/createItems_201.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/createItems/test-data/createItems_400.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/createItems/test-data/createItems_default.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/smoke/BasicApi/createItems/createItems.feature",
                  "/openapi/unit/smoke/BasicApi/createItems/test-data/createItems_201.yml",
                  "/openapi/unit/smoke/BasicApi/createItems/test-data/createItems_400.yml",
                  "/openapi/unit/smoke/BasicApi/createItems/test-data/createItems_default.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getListItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getListItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getListItemsOperation(),
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/listItems/listItems.feature",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/listItems/test-data/listItems_200.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/listItems/test-data/listItems_400.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/listItems/test-data/listItems_default.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/smoke/BasicApi/listItems/listItems.feature",
                  "/openapi/unit/smoke/BasicApi/listItems/test-data/listItems_200.yml",
                  "/openapi/unit/smoke/BasicApi/listItems/test-data/listItems_400.yml",
                  "/openapi/unit/smoke/BasicApi/listItems/test-data/listItems_default.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/showItemById.feature",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/test-data/showItemById_200.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/test-data/showItemById_404.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/test-data/showItemById_default.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/smoke/BasicApi/showItemById/showItemById.feature",
                  "/openapi/unit/smoke/BasicApi/showItemById/test-data/showItemById_200.yml",
                  "/openapi/unit/smoke/BasicApi/showItemById/test-data/showItemById_404.yml",
                  "/openapi/unit/smoke/BasicApi/showItemById/test-data/showItemById_default.yml")),

          Arguments.of(
              // Manual Input
              false,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/showItemById.feature",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/test-data/showItemById_200.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/test-data/showItemById_404.yml",
                  "dev/inditex/karate/openapi/test/smoke/BasicApi/showItemById/test-data/showItemById_default.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/smoke/BasicApi/showItemById/showItemById.feature",
                  "/openapi/unit/smoke/BasicApi/showItemById/test-data/showItemById_200.yml",
                  "/openapi/unit/smoke/BasicApi/showItemById/test-data/showItemById_404.yml",
                  "/openapi/unit/smoke/BasicApi/showItemById/test-data/showItemById_default.yml")));
    }

    @ParameterizedTest(name = "OpenApiGenerator-functional-{1}-Man-{0}-InMk-{5}-Codes-{4}")
    @MethodSource
    void when_execute_api_functional_expect_result(
        final boolean manualInput, final String operationId, final String operationDisplayName,
        final OperationPath operation, final Set<String> returnCodes, final Boolean inlineMocks,
        final List<String> expectedFiles, final List<String> expectedContent)
        throws NoSuchFieldException, SecurityException {
      // Initialize prompt values
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.FUNCTIONAL_TEST;
      final String artifactId = "dev.inditex.karate.openapi:test";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      if (manualInput) {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
            new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      } else {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      }
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      // Response Code only applicable for Functional, Mock Data
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(returnCodes)));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      // Inline Mocks only applicable for Functional and Mock Data
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(inlineMocks ? ConfirmationValue.YES : ConfirmationValue.NO));
      // Test Name only applicable for Functional and Mock Data (Inline Mocks)
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID,
          new InputResult(getTestName(inlineMocks, operationId, returnCodes)));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(generator.getOptions().getMode()).isEqualTo(mode);
      assertThat(generator.getOptions().getArtifact()).isEqualTo(MavenArtifact.fromId(artifactId));
      assertThat(generator.getOptions().getOpenApi()).isNotNull();
      assertThat(generator.getOptions().getOperations()).isNotNull().hasSize(1);
      assertThat(generator.getOptions().getOperations().get(0).method()).isEqualTo(operation.method());
      assertThat(generator.getOptions().getOperations().get(0).path()).isEqualTo(operation.path());
      assertThat(generator.getOptions().getOperations().get(0).operation().getOperationId()).isEqualTo(operationId);
      assertThat(result).hasSameSizeAs(expectedFiles);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i)).isEqualTo(Paths.get(targetFolder, expectedFiles.get(i)));
      }
      assertThat(result).hasSameSizeAs(expectedContent);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i).toFile()).exists().hasContent(getResourceAsString(expectedContent.get(i)));
      }
    }

    public static Stream<Arguments> when_execute_api_functional_expect_result() {
      return Stream.of(

          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Return Codes
              Set.of("201"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/createItems/createItems.feature",
                  "dev/inditex/karate/openapi/test/functional/createItems/test-data/createItems_201.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/functional/createItems/createItems.feature",
                  "/openapi/unit/functional/createItems/test-data/createItems_201.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getListItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getListItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getListItemsOperation(),
              // Return Codes
              Set.of("200"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/listItems/listItems.feature",
                  "dev/inditex/karate/openapi/test/functional/listItems/test-data/listItems_200.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/functional/listItems/listItems.feature",
                  "/openapi/unit/functional/listItems/test-data/listItems_200.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Return Codes
              Set.of("200"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/showItemById/showItemById.feature",
                  "dev/inditex/karate/openapi/test/functional/showItemById/test-data/showItemById_200.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/functional/showItemById/showItemById.feature",
                  "/openapi/unit/functional/showItemById/test-data/showItemById_200.yml")),
          Arguments.of(
              // Manual Input
              false,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Return Codes
              Set.of("200"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/showItemById/showItemById.feature",
                  "dev/inditex/karate/openapi/test/functional/showItemById/test-data/showItemById_200.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/functional/showItemById/showItemById.feature",
                  "/openapi/unit/functional/showItemById/test-data/showItemById_200.yml")),

          // Multiple Functional tests in the same feature file for different return codes
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Return Codes
              Set.of("201", "400"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/createItemsWithMultipleCodes/createItemsWithMultipleCodes.feature",
                  "dev/inditex/karate/openapi/test/functional/createItemsWithMultipleCodes/test-data/createItems_201.yml",
                  "dev/inditex/karate/openapi/test/functional/createItemsWithMultipleCodes/test-data/createItems_400.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/functional/createItemsWithMultipleCodes/createItemsWithMultipleCodes.feature",
                  "/openapi/unit/functional/createItemsWithMultipleCodes/test-data/createItems_201.yml",
                  "/openapi/unit/functional/createItemsWithMultipleCodes/test-data/createItems_400.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getListItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getListItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getListItemsOperation(),
              // Return Codes
              Set.of("200", "400"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/listItemsWithMultipleCodes/listItemsWithMultipleCodes.feature",
                  "dev/inditex/karate/openapi/test/functional/listItemsWithMultipleCodes/test-data/listItems_200.yml",
                  "dev/inditex/karate/openapi/test/functional/listItemsWithMultipleCodes/test-data/listItems_400.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/functional/listItemsWithMultipleCodes/listItemsWithMultipleCodes.feature",
                  "/openapi/unit/functional/listItemsWithMultipleCodes/test-data/listItems_200.yml",
                  "/openapi/unit/functional/listItemsWithMultipleCodes/test-data/listItems_400.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Return Codes
              Set.of("200", "404"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/showItemByIdWithMultipleCodes/showItemByIdWithMultipleCodes.feature",
                  "dev/inditex/karate/openapi/test/functional/showItemByIdWithMultipleCodes/test-data/showItemById_200.yml",
                  "dev/inditex/karate/openapi/test/functional/showItemByIdWithMultipleCodes/test-data/showItemById_404.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/functional/showItemByIdWithMultipleCodes/showItemByIdWithMultipleCodes.feature",
                  "/openapi/unit/functional/showItemByIdWithMultipleCodes/test-data/showItemById_200.yml",
                  "/openapi/unit/functional/showItemByIdWithMultipleCodes/test-data/showItemById_404.yml")),

          // Inline Mocks
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Return Codes
              Set.of("201"),
              // Inline Mocks
              true,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/createItemsWithInlineMocks/createItemsWithInlineMocks.feature",
                  "dev/inditex/karate/openapi/test/functional/createItemsWithInlineMocks/test-data/createItems_201.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/mocks-inline/createItemsWithInlineMocks/createItemsWithInlineMocks.feature",
                  "/openapi/unit/mocks-inline/createItemsWithInlineMocks/test-data/createItems_201.yml"))

      );
    }

    @ParameterizedTest(name = "OpenApiGenerator-mockdata-{1}-Man-{0}-InMk-{5}-Codes-{4}")
    @MethodSource
    void when_execute_api_mock_data_expect_result(
        final boolean manualInput, final String operationId, final String operationDisplayName,
        final OperationPath operation, final Set<String> returnCodes, final Boolean inlineMocks,
        final List<String> expectedFiles, final List<String> expectedContent)
        throws NoSuchFieldException, SecurityException {
      // Initialize prompt values
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.MOCK_DATA;
      final String artifactId = "dev.inditex.karate.openapi:external";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      if (manualInput) {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
            new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      } else {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      }
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      // Response Code only applicable for Functional, Mock Data
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(returnCodes)));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      // Inline Mocks only applicable for Functional and Mock Data
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(inlineMocks ? ConfirmationValue.YES : ConfirmationValue.NO));
      // Test Name only applicable for Functional and Mock Data (Inline Mocks)
      if (inlineMocks) {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID,
            new InputResult(getTestName(inlineMocks, operationId, returnCodes)));
      }
      // Secondary Artifact only applicable for Mock Data (Inline Mocks)
      if (inlineMocks) {
        mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID,
            new ListResult("dev.inditex.karate.openapi:test"));
      }
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(generator.getOptions().getMode()).isEqualTo(mode);
      assertThat(generator.getOptions().getArtifact()).isEqualTo(MavenArtifact.fromId(artifactId));
      assertThat(generator.getOptions().getOpenApi()).isNotNull();
      assertThat(generator.getOptions().getOperations()).isNotNull().hasSize(1);
      assertThat(generator.getOptions().getOperations().get(0).method()).isEqualTo(operation.method());
      assertThat(generator.getOptions().getOperations().get(0).path()).isEqualTo(operation.path());
      assertThat(generator.getOptions().getOperations().get(0).operation().getOperationId()).isEqualTo(operationId);
      assertThat(result).hasSameSizeAs(expectedFiles);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i)).isEqualTo(Paths.get(targetFolder, expectedFiles.get(i)));
      }
      assertThat(result).hasSameSizeAs(expectedContent);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i).toFile()).exists().hasContent(getResourceAsString(expectedContent.get(i)));
      }
    }

    public static Stream<Arguments> when_execute_api_mock_data_expect_result() {
      return Stream.of(

          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Return Codes
              Set.of("201"),
              // Inline Mocks
              false,
              // Expected files
              List.of("mocks/templates/standalone/external/BasicApi/XXXX_createItems_201.yml"),
              // Expected content
              List.of("/openapi/unit/mocks/BasicApi/XXXX_createItems_201.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getListItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getListItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getListItemsOperation(),
              // Return Codes
              Set.of("200"),
              // Inline Mocks
              false,
              // Expected files
              List.of("mocks/templates/standalone/external/BasicApi/XXXX_listItems_200.yml"),
              // Expected content
              List.of("/openapi/unit/mocks/BasicApi/XXXX_listItems_200.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Return Codes
              Set.of("200"),
              // Inline Mocks
              false,
              // Expected files
              List.of("mocks/templates/standalone/external/BasicApi/XXXX_showItemById_200.yml"),
              // Expected content
              List.of("/openapi/unit/mocks/BasicApi/XXXX_showItemById_200.yml")),

          // Multiple Mock Files for different return codes
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Return Codes
              Set.of("201", "400"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "mocks/templates/standalone/external/BasicApi/XXXX_createItems_201.yml",
                  "mocks/templates/standalone/external/BasicApi/XXXX_createItems_400.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/mocks/BasicApi/XXXX_createItems_201.yml",
                  "/openapi/unit/mocks/BasicApi/XXXX_createItems_400.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getListItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getListItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getListItemsOperation(),
              // Return Codes
              Set.of("200", "400"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "mocks/templates/standalone/external/BasicApi/XXXX_listItems_200.yml",
                  "mocks/templates/standalone/external/BasicApi/XXXX_listItems_400.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/mocks/BasicApi/XXXX_listItems_200.yml",
                  "/openapi/unit/mocks/BasicApi/XXXX_listItems_400.yml")),
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Return Codes
              Set.of("200", "404"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "mocks/templates/standalone/external/BasicApi/XXXX_showItemById_200.yml",
                  "mocks/templates/standalone/external/BasicApi/XXXX_showItemById_404.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/mocks/BasicApi/XXXX_showItemById_200.yml",
                  "/openapi/unit/mocks/BasicApi/XXXX_showItemById_404.yml")),
          Arguments.of(
              // Manual Input
              false,
              // Operation Id
              ExampleBasicApiMother.getShowItemByIdOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getShowItemByIdOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getShowItemByIdOperation(),
              // Return Codes
              Set.of("200", "404"),
              // Inline Mocks
              false,
              // Expected files
              List.of(
                  "mocks/templates/standalone/external/BasicApi/XXXX_showItemById_200.yml",
                  "mocks/templates/standalone/external/BasicApi/XXXX_showItemById_404.yml"),
              // Expected content
              List.of(
                  "/openapi/unit/mocks/BasicApi/XXXX_showItemById_200.yml",
                  "/openapi/unit/mocks/BasicApi/XXXX_showItemById_404.yml")),
          // Inline Mocks
          Arguments.of(
              // Manual Input
              true,
              // Operation Id
              ExampleBasicApiMother.getCreateItemsOperationId(),
              // Operation Display Name
              ExampleBasicApiMother.getCreateItemsOperationDisplayName(),
              // Operation
              ExampleBasicApiMother.getCreateItemsOperation(),
              // Return Codes
              Set.of("201"),
              // Inline Mocks
              true,
              // Expected files
              List.of(
                  "dev/inditex/karate/openapi/test/functional/createItemsWithInlineMocks/mocks/external/BasicApi/"
                      + "XXXX_createItems_201.yml"),
              // Expected content
              List.of("/openapi/unit/mocks-inline/createItemsWithInlineMocks/mocks/BasicApi/XXXX_createItems_201.yml"))

      );
    }

    private String getTestName(final Boolean inlineMocks, final String operationId, final Set<String> returnCodes) {
      String testName = operationId;
      if (returnCodes.size() > 1) {
        testName = testName + "WithMultipleCodes";
      }
      if (inlineMocks) {
        testName = testName + "WithInlineMocks";
      }
      return testName;
    }
  }

  @Nested
  class ValidationErrors {
    @Test
    void when_common_with_null_api_expect_error() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.OPERATIONS;
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(null));
      final String expectedError = "Open Api file must be provided";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(result).isEmpty();
      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expectedError);
    }

    @Test
    void when_common_with_blank_api_expect_error() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.OPERATIONS;
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(""));
      final String expectedError = "Open Api file must be valid";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(result).isEmpty();
      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expectedError);
    }

    @Test
    void when_common_with_no_operations_expect_error() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.OPERATIONS;
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of())));
      final String expectedError = "Operations to generate for must be selected";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(result).isEmpty();
      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expectedError);
    }

    @Test
    void when_functional_test_with_no_return_codes_expect_error() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.FUNCTIONAL_TEST;
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final String operationId = ExampleBasicApiMother.getCreateItemsOperationId();
      final String operationDisplayName = ExampleBasicApiMother.getCreateItemsOperationDisplayName();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(Set.of())));
      final String expectedError = "Responses to generate for must be selected for all Operations";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(result).isEmpty();
      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expectedError);
    }

    @Test
    void when_mock_data_with_no_return_codes_expect_error() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.MOCK_DATA;
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final String operationId = ExampleBasicApiMother.getCreateItemsOperationId();
      final String operationDisplayName = ExampleBasicApiMother.getCreateItemsOperationDisplayName();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(Set.of())));
      final String expectedError = "Responses to generate for must be selected for all Operations";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(result).isEmpty();
      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expectedError);
    }

    @Test
    void when_functional_test_with_no_test_name_expect_error() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.FUNCTIONAL_TEST;
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final String operationId = ExampleBasicApiMother.getCreateItemsOperationId();
      final String operationDisplayName = ExampleBasicApiMother.getCreateItemsOperationDisplayName();
      final Set<String> returnCodes = Set.of("201");
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(returnCodes)));
      final String expectedError = "Test Name must be provided for functional tests";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(result).isEmpty();
      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expectedError);
    }

    @Test
    void when_mock_data_with_no_inline_mocks_test_name_expect_error() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.MOCK_DATA;
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final String operationId = ExampleBasicApiMother.getCreateItemsOperationId();
      final String operationDisplayName = ExampleBasicApiMother.getCreateItemsOperationDisplayName();
      final Set<String> returnCodes = Set.of("201");
      final String artifactId = "dev.inditex.karate.openapi:test";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(returnCodes)));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID, new ConfirmResult(ConfirmationValue.YES));
      final String expectedError = "Test Name must be provided for inline mocks";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = generator.execute();

      assertThat(result).isEmpty();
      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).contains(expectedError);
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
