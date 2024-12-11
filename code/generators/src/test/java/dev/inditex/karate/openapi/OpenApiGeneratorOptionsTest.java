package dev.inditex.karate.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dev.inditex.karate.console.ConsoleCLI;
import dev.inditex.karate.console.ConsoleTestUtils;
import dev.inditex.karate.openapi.data.MavenUtils;

import de.codeshelf.consoleui.elements.ConfirmChoice.ConfirmationValue;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.ConfirmResult;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OpenApiGeneratorOptionsTest {

  @BeforeEach
  void beforeEach() {
    ConsoleCLI.withoutRealTerminal();
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiGeneratorOptions::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Configure {

    @Test
    void when_configure_operations_expect_result() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.OPERATIONS;
      final String artifactId = "dev.inditex.karate.openapi:test";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(ExampleBasicApiMother.getCreateItemsOperationDisplayName()))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.configure();

      assertThat(options.getMode()).isEqualTo(mode);
      assertThat(options.getOpenApi()).isNotNull();
      assertThat(options.getOperations()).isNotEmpty().hasSize(1)
          .allMatch(o -> o.operation().getOperationId().equals("createItems"));
      assertThat(options.getArtifact()).isNotNull();
      assertThat(options.getArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getArtifact().getArtifactId()).isEqualTo("test");
      assertThat(options.getOperationsResponses()).isNull();
      assertThat(options.getTestName()).isNull();
      assertThat(options.getInlineMocks()).isNull();
      assertThat(options.getInlineMocksFunctionalArtifact()).isNull();
    }

    @Test
    void when_configure_smoke_tests_expect_result() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.SMOKE_TESTS;
      final String artifactId = "dev.inditex.karate.openapi:test";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(ExampleBasicApiMother.getCreateItemsOperationDisplayName()))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.configure();

      assertThat(options.getMode()).isEqualTo(mode);
      assertThat(options.getOpenApi()).isNotNull();
      assertThat(options.getOperations()).isNotEmpty().hasSize(1)
          .allMatch(o -> o.operation().getOperationId().equals("createItems"));
      assertThat(options.getArtifact()).isNotNull();
      assertThat(options.getArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getArtifact().getArtifactId()).isEqualTo("test");
      assertThat(options.getOperationsResponses()).isNull();
      assertThat(options.getTestName()).isNull();
      assertThat(options.getInlineMocks()).isNull();
      assertThat(options.getInlineMocksFunctionalArtifact()).isNull();
    }

    @Test
    void when_configure_functional_test_expect_result() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.FUNCTIONAL_TEST;
      final String artifactId = "dev.inditex.karate.openapi:test";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(ExampleBasicApiMother.getCreateItemsOperationDisplayName()))));
      // Response Code only applicable for Functional, Mock Data
      mockPrompts.put(
          OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + ExampleBasicApiMother.getCreateItemsOperationId(),
          new CheckboxResult(new HashSet<>(Set.of("201"))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      // Inline Mocks only applicable for Functional and Mock Data
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(ConfirmationValue.YES));
      // Test Name only applicable for Functional and Mock Data (Inline Mocks)
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID, new InputResult("testName"));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.configure();

      assertThat(options.getMode()).isEqualTo(mode);
      assertThat(options.getOpenApi()).isNotNull();
      assertThat(options.getOperations()).isNotEmpty().hasSize(1)
          .allMatch(o -> o.operation().getOperationId().equals("createItems"));
      assertThat(options.getArtifact()).isNotNull();
      assertThat(options.getArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getArtifact().getArtifactId()).isEqualTo("test");
      assertThat(options.getOperationsResponses()).isNotEmpty().hasSize(1).containsKey(options.getOperations().get(0))
          .containsValue(Set.of("201"));
      assertThat(options.getTestName()).isEqualTo("testName");
      assertThat(options.getInlineMocks()).isTrue();
      assertThat(options.getInlineMocksFunctionalArtifact()).isNull();
    }

    @Test
    void when_configure_mock_data_expect_result() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.MOCK_DATA;
      final String artifactId = "dev.inditex.karate.openapi:external";
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(ExampleBasicApiMother.getCreateItemsOperationDisplayName()))));
      // Response Code only applicable for Functional, Mock Data
      mockPrompts.put(
          OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + ExampleBasicApiMother.getCreateItemsOperationId(),
          new CheckboxResult(new HashSet<>(Set.of("201"))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      // Inline Mocks only applicable for Functional and Mock Data
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(ConfirmationValue.YES));
      // Test Name only applicable for Functional and Mock Data (Inline Mocks)
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID,
          new InputResult("testName"));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID,
          new ListResult("dev.inditex.karate.openapi:test"));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.configure();

      assertThat(options.getMode()).isEqualTo(mode);
      assertThat(options.getOpenApi()).isNotNull();
      assertThat(options.getOperations()).isNotEmpty().hasSize(1)
          .allMatch(o -> o.operation().getOperationId().equals("createItems"));
      assertThat(options.getArtifact()).isNotNull();
      assertThat(options.getArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getArtifact().getArtifactId()).isEqualTo("external");
      assertThat(options.getOperationsResponses()).isNotEmpty().hasSize(1).containsKey(options.getOperations().get(0))
          .containsValue(Set.of("201"));
      assertThat(options.getTestName()).isEqualTo("testName");
      assertThat(options.getInlineMocks()).isTrue();
      assertThat(options.getInlineMocksFunctionalArtifact()).isNotNull();
      assertThat(options.getInlineMocksFunctionalArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getInlineMocksFunctionalArtifact().getArtifactId()).isEqualTo("test");
    }

  }

  @Nested
  class SelectMode {

    @Test
    void when_informed_expect_result() throws NoSuchFieldException, SecurityException {
      final OpenApiGeneratorMode mode = OpenApiGeneratorModes.OPERATIONS;
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID, new ListResult(mode.name()));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectMode();

      assertThat(options.getMode()).isEqualTo(mode);
    }

    @Test
    void when_not_informed_expect_default() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectMode();

      assertThat(options.getMode()).isEqualTo(OpenApiGeneratorModes.OPERATIONS);
    }

  }

  @Nested
  class SelectOpenApi {

    @Test
    void when_openapi_file_from_list_expect_result() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectOpenApi();

      assertThat(options.getOpenApi()).isNotNull();
      assertThat(options.getOpenApi().getInfo().getTitle()).isEqualTo("KarateTools Open Api - Basic");
    }

    @Test
    void when_manual_openapi_file_expect_result() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(openApiFile));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectOpenApi();

      assertThat(options.getOpenApi()).isNotNull();
      assertThat(options.getOpenApi().getInfo().getTitle()).isEqualTo("KarateTools Open Api - Basic");
    }

    @Test
    void when_no_openapi_file_expect_exception() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final String expectedError = "Open Api file must be provided";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      final ThrowingCallable result = options::selectOpenApi;

      assertThatThrownBy(result).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

    @Test
    void when_invalid_openapi_file_expect_exception() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/invalid/invalid-openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      final String expectedError = "Unable to parse Open Api file";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      final ThrowingCallable result = options::selectOpenApi;

      assertThatThrownBy(result).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

  }

  @Nested
  class SelectOperations {

    @Test
    void when_informed_expect_result() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(ExampleBasicApiMother.getCreateItemsOperationDisplayName()))));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.selectOpenApi();

      options.selectOperations();

      assertThat(options.getOperations()).isNotEmpty().hasSize(1)
          .allMatch(o -> o.operation().getOperationId().equals("createItems"));
    }

    @Test
    void when_not_informed_expect_exception() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of())));
      final String expectedError = "Operations to generate for must be selected";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.selectOpenApi();

      final ThrowingCallable result = options::selectOperations;

      assertThatThrownBy(result).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

    @Test
    void when_invalid_openapi_file_expect_exception() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/invalid/invalid-openapi-rest.yml";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      final String expectedError = "Unable to parse Open Api file";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      final ThrowingCallable result = options::selectOperations;

      assertThatThrownBy(result).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

  }

  @Nested
  class SelectOperationsResponses {

    @Test
    void when_informed_expect_result() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final String operationId = ExampleBasicApiMother.getCreateItemsOperationId();
      final String operationDisplayName = ExampleBasicApiMother.getCreateItemsOperationDisplayName();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(Set.of("200"))));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.selectOpenApi();
      options.selectOperations();

      options.selectOperationsResponses();

      assertThat(options.getOperationsResponses()).isNotEmpty().hasSize(1).containsKey(options.getOperations().get(0));
    }

    @Test
    void when_not_informed_expect_exception() throws NoSuchFieldException, SecurityException {
      final String openApiFile = "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml";
      final String operationId = ExampleBasicApiMother.getCreateItemsOperationId();
      final String operationDisplayName = ExampleBasicApiMother.getCreateItemsOperationDisplayName();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(openApiFile));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID,
          new CheckboxResult(new HashSet<>(Set.of(operationDisplayName))));
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operationId,
          new CheckboxResult(new HashSet<>(Set.of())));
      final String expectedError = "Responses to generate for must be selected for all Operations";
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.selectOpenApi();
      options.selectOperations();

      final ThrowingCallable result = options::selectOperationsResponses;

      assertThatThrownBy(result).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

  }

  @Nested
  class SelectArtifact {

    @Test
    void when_informed_expect_result() throws NoSuchFieldException, SecurityException {
      final String artifactId = "dev.inditex.karate.openapi:test";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(artifactId));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.setArtifacts(MavenUtils.getPomArtifacts("src/test/resources/maven-model/pom-default-artifact.xml"));

      options.selectArtifact();

      assertThat(options.getArtifact()).isNotNull();
      assertThat(options.getArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getArtifact().getArtifactId()).isEqualTo("test");
    }

    @Test
    void when_not_informed_expect_first_alphabetically() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.setArtifacts(MavenUtils.getPomArtifacts("src/test/resources/maven-model/pom-default-artifact.xml"));

      options.selectArtifact();

      assertThat(options.getArtifact()).isNotNull();
      assertThat(options.getArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getArtifact().getArtifactId()).isEqualTo("openapi-unit-tests");
    }

  }

  @Nested
  class SelectTestName {

    @Test
    void when_informed_expect_result() throws NoSuchFieldException, SecurityException {
      final String testName = "testName";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID,
          new InputResult(testName));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectTestName();

      assertThat(options.getTestName()).isEqualTo(testName);
    }

    @Test
    void when_not_informed_expect_exception() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      final ThrowingCallable result = options::selectTestName;

      assertThatThrownBy(result).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Test Name must be provided");
    }

  }

  @Nested
  class SelectFunctionalWithInlineMocks {

    @Test
    void when_informed_yes_expect_result() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(ConfirmationValue.YES));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectFunctionalWithInlineMocks();

      assertThat(options.getInlineMocks()).isTrue();
    }

    @Test
    void when_informed_no_expect_result() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(ConfirmationValue.NO));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectFunctionalWithInlineMocks();

      assertThat(options.getInlineMocks()).isFalse();
    }

    @Test
    void when_not_informed_expect_default() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectFunctionalWithInlineMocks();

      assertThat(options.getInlineMocks()).isFalse();
    }

  }

  @Nested
  class SelectInlineMocks {

    @Test
    void when_informed_yes_expect_result() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(ConfirmationValue.YES));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectInlineMocks();

      assertThat(options.getInlineMocks()).isTrue();
    }

    @Test
    void when_informed_no_expect_result() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(ConfirmationValue.NO));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectInlineMocks();

      assertThat(options.getInlineMocks()).isFalse();
    }

    @Test
    void when_not_informed_expect_default() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectInlineMocks();

      assertThat(options.getInlineMocks()).isFalse();
    }

  }

  @Nested
  class SelectInlineMocksFunctionalArtifact {

    @Test
    void when_informed_expect_result() throws NoSuchFieldException, SecurityException {
      final String artifactId = "dev.inditex.karate.openapi:other";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID, new ListResult(artifactId));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.setArtifacts(MavenUtils.getPomArtifacts("src/test/resources/maven-model/pom-default-artifact.xml"));

      options.selectInlineMocksFunctionalArtifact();

      assertThat(options.getInlineMocksFunctionalArtifact()).isNotNull();
      assertThat(options.getInlineMocksFunctionalArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getInlineMocksFunctionalArtifact().getArtifactId()).isEqualTo("other");
    }

    @Test
    void when_not_informed_expect_first_alphabetically() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      options.setArtifacts(MavenUtils.getPomArtifacts("src/test/resources/maven-model/pom-default-artifact.xml"));

      options.selectInlineMocksFunctionalArtifact();

      assertThat(options.getInlineMocksFunctionalArtifact()).isNotNull();
      assertThat(options.getInlineMocksFunctionalArtifact().getGroupId()).isEqualTo("dev.inditex.karate.openapi");
      assertThat(options.getInlineMocksFunctionalArtifact().getArtifactId()).isEqualTo("openapi-unit-tests");
    }

  }

  @Nested
  class SelectInlineMocksFunctionalTestName {

    @Test
    void when_informed_expect_result() throws NoSuchFieldException, SecurityException {
      final String testName = "testWithInlineMocks";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID,
          new InputResult(testName));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      options.selectInlineMocksFunctionalTestName();

      assertThat(options.getTestName()).isEqualTo(testName);
    }

    @Test
    void when_not_informed_expect_exception() throws NoSuchFieldException, SecurityException {
      final String expectedError = "Test Name must be provided for inline mocks";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      final ThrowingCallable result = options::selectInlineMocksFunctionalTestName;

      assertThatThrownBy(result).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

  }

  @Nested
  class FindOpenApis {

    @Test
    void when_find_expect_result() {
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      final var result = options.findOpenApis();

      assertThat(result).isNotEmpty().allMatch(f -> {
        final File file = new File(f);
        final String fileNane = file.getName();
        return fileNane.startsWith("openapi") && fileNane.endsWith(".yml");
      });
    }

  }

  @Nested
  class IsOpenApi {

    @Test
    void when_valid_expect_true() {
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      final File dir = new File("src/test/resources/openapi/karatetools/rest/basic/");
      final String name = "openapi-rest.yml";

      final var result = options.isOpenApi(dir, name);

      assertThat(result).isTrue();
    }

    @Test
    void when_invalid_expect_false() {
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      final File dir = new File("src/test/resources/openapi/karatetools/rest/invalid/");
      final String name = "invalid-openapi-rest.yml";

      final var result = options.isOpenApi(dir, name);

      assertThat(result).isFalse();
    }

  }

  @Nested
  class ValidateApiFile {

    @Test
    void when_null_api_file_expect_exception() {
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      final String expectedError = "Open Api file must be provided";

      assertThatThrownBy(() -> {
        options.validateApiFile(null);
      }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

    @Test
    void when_blank_api_expect_exception() {
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();
      final String expectedError = "Open Api file must be valid";

      assertThatThrownBy(() -> {
        options.validateApiFile("");
      }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

    @ParameterizedTest
    @CsvSource({
        ",Open Api file must be provided",
        "src/test/resources/openapi/karatetools/rest/basic/openapi-rest2.yml,Open Api file must be valid"
    })
    void when_invalid_api_file_expect_exception(final String file, final String expectedError) {
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      assertThatThrownBy(() -> {
        options.validateApiFile(file);
      }).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(expectedError);
    }

    @ParameterizedTest
    @CsvSource({
        "src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml,true"
    })
    void when_valid_api_file_expect_validation(final String file, final boolean expected) {
      final OpenApiGeneratorOptions options = new OpenApiGeneratorOptions();

      final var result = options.validateApiFile(file);

      assertThat(result).isEqualTo(expected);
    }

  }
}
