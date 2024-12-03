package dev.inditex.karate.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import dev.inditex.karate.console.ConsoleCLI;
import dev.inditex.karate.console.ConsoleTestUtils;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import de.codeshelf.consoleui.elements.ConfirmChoice.ConfirmationValue;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.ConfirmResult;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;

class OpenApiGeneratorCLITest {

  @BeforeEach
  void beforeEach() {
    ConsoleCLI.withoutRealTerminal();
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiGeneratorCLI::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Main {
    @Test
    void when_main_expect_generation_start() {
      try (
          final MockedConstruction<OpenApiGenerator> mockedGenerator = mockConstruction(OpenApiGenerator.class,
              (mock, context) -> assertThat(context.getCount()).isEqualTo(1))) {

        OpenApiGeneratorCLI.main(null);

        assertThat(mockedGenerator.constructed()).hasSize(1);
        final OpenApiGenerator generator = mockedGenerator.constructed().get(0);
        verify(generator, times(1)).execute();
      }
    }
  }

  @Nested
  class PromptMode {
    @ValueSource(strings = {"OPERATIONS", "SMOKE_TESTS", "FUNCTIONAL_TEST", "MOCK_DATA"})
    void when_mode_arguments_expect_selected_value(final String mode) throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID,
          new ListResult(mode));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptMode();

      assertThat(result).isEqualTo(OpenApiGeneratorModes.valueOf(mode));
    }

    @Test
    void when_mode_exception_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_ID,
          new ListResult(OpenApiGeneratorModes.OPERATIONS.name()));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptMode();

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_MODE_DEFAULT_VALUE);
    }
  }

  @Nested
  class PromptArtifacts {
    @ParameterizedTest
    @ValueSource(
        strings = {"dev.inditex.karate.openapi:test", "dev.inditex.karate.openapi:test1", "dev.inditex.karate.openapi:test2"})
    void when_artifacts_arguments_expect_selected_value(final String selected) throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID,
          new ListResult(selected));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptArtifacts(
          List.of("dev.inditex.karate.openapi:test1", "dev.inditex.karate.openapi:test2"), "dev.inditex.karate.openapi:test");

      assertThat(result).isEqualTo(selected);
    }

    @Test
    void when_artifacts_empty_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptArtifacts(List.of(), null);

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE);
    }

    @Test
    void when_artifacts_null_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptArtifacts(null, null);

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE);
    }

    @Test
    void when_artifacts_exception_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_ID, new ListResult(""));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptArtifacts(
          List.of("dev.inditex.karate.openapi:test1", "dev.inditex.karate.openapi:test2"), "dev.inditex.karate.openapi:test");

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE);
    }
  }

  @Nested
  class PromptSecondaryArtifacts {
    @ParameterizedTest
    @ValueSource(strings = {"dev.inditex.karate.openapi:other", "dev.inditex.karate.openapi:other1",
        "dev.inditex.karate.openapi:other2"})
    void when_secondary_artifacts_arguments_expect_selected_value(final String selected) throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID,
          new ListResult(selected));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptSecondaryArtifacts("Other Artifact",
          List.of("dev.inditex.karate.openapi:other1", "dev.inditex.karate.openapi:other2"),
          "dev.inditex.karate.openapi:other");

      assertThat(result).isEqualTo(selected);
    }

    @Test
    void when_secondary_artifacts_empty_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptSecondaryArtifacts("Other Artifact", List.of(), null);

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE);
    }

    @Test
    void when_secondary_artifacts_null_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptSecondaryArtifacts("Other Artifact", null, null);

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE);
    }

    @Test
    void when_secondary_artifacts_exception_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID, new ListResult(""));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptSecondaryArtifacts("Other Artifact",
          List.of("dev.inditex.karate.openapi:other1", "dev.inditex.karate.openapi:other2"),
          "dev.inditex.karate.openapi:other");

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE);
    }
  }

  @Nested
  class PromptOpenApi {
    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml",
        "target/test-classes/openapi/karatetools/rest/basic/openapi-rest.yml"})
    void when_api_list_arguments_expect_selected_value(final String selected) throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
          new ListResult(selected));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOpenApi(
          List.of("src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml",
              "target/test-classes/openapi/karatetools/rest/basic/openapi-rest.yml"));

      assertThat(result).isEqualTo(selected);
    }

    @ParameterizedTest
    @MethodSource
    void when_api_list_arguments_select_manual_expect_null(final List<String> list) throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
          new ListResult(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOpenApi(list);

      assertThat(result).isNull();
    }

    protected static Stream<Arguments> when_api_list_arguments_select_manual_expect_null() {
      return Stream.of(Arguments.of(List.of("src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml",
          "target/test-classes/openapi/karatetools/rest/basic/openapi-rest.yml")),
          Arguments.of(List.of()),
          null);
    }

    @Test
    void when_api_list_exception_expect_null() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID, new ListResult(""));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptOpenApi(
          List.of("src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml",
              "target/test-classes/openapi/karatetools/rest/basic/openapi-rest.yml"));

      assertThat(result).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/openapi/karatetools/rest/basic/openapi-rest.yml",
        "target/test-classes/openapi/karatetools/rest/basic/openapi-rest.yml"})
    void when_api_input_argument_expect_selected_value(final String selected) throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID,
          new InputResult(selected));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOpenApi();

      assertThat(result).isEqualTo(selected);
    }

    @Test
    void when_api_input_blank_exception_expect_null() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(""));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptOpenApi();

      assertThat(result).isNull();
    }

    @Test
    void when_api_input_null_exception_expect_null() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID, new InputResult(null));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptOpenApi();

      assertThat(result).isNull();
    }
  }

  @Nested
  class PromptOperations {
    @Test
    void when_operations_arguments_expect_selected_value() throws NoSuchFieldException, SecurityException {
      final String operationId = ExampleBasicApiMother.getShowItemByIdOperationDisplayName();
      final OperationPath operation = ExampleBasicApiMother.getShowItemByIdOperation();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID, new CheckboxResult(new HashSet<>(Set.of(operationId))));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOperations(Map.of("BasicApi", List.of(
          ExampleBasicApiMother.getShowItemByIdOperation(),
          ExampleBasicApiMother.getListItemsOperation(),
          ExampleBasicApiMother.getCreateItemsOperation())));

      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(operation);
    }

    @Test
    void when_operations_empty_expect_empty() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID, new CheckboxResult(new HashSet<>(Set.of(""))));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOperations(Map.of("BasicApi", List.of()));

      assertThat(result).isEmpty();
    }

    @Test
    void when_operations_null_expect_empty() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID, new CheckboxResult(new HashSet<>(Set.of(""))));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOperations(null);

      assertThat(result).isEmpty();
    }

    @Test
    void when_operations_exception_expect_all() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATIONS_ID, new CheckboxResult(new HashSet<>(Set.of(""))));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptOperations(Map.of("BasicApi", List.of(
          ExampleBasicApiMother.getShowItemByIdOperation(),
          ExampleBasicApiMother.getListItemsOperation(),
          ExampleBasicApiMother.getCreateItemsOperation())));

      assertThat(result).isEqualTo(List.of(ExampleBasicApiMother.getShowItemByIdOperation(),
          ExampleBasicApiMother.getListItemsOperation(),
          ExampleBasicApiMother.getCreateItemsOperation()));
    }
  }

  @Nested
  class PromptOperationResponses {
    @Test
    void when_operation_responses_single_expect_selected_value() throws NoSuchFieldException, SecurityException {
      final OperationPath operation = ExampleBasicApiMother.getShowItemByIdOperation();
      final Set<String> responses = Set.of("200", "404", "default");
      final Set<String> selected = Set.of("200");
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operation.operation().getOperationId(),
          new CheckboxResult(new HashSet<>(selected)));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOperationResponses(operation, responses);

      assertThat(result).isEqualTo(selected);
    }

    @Test
    void when_operation_responses_multiple_expect_selected_values() throws NoSuchFieldException, SecurityException {
      final OperationPath operation = ExampleBasicApiMother.getShowItemByIdOperation();
      final Set<String> responses = Set.of("200", "404", "default");
      final Set<String> selected = Set.of("200", "404");
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operation.operation().getOperationId(),
          new CheckboxResult(new HashSet<>(selected)));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOperationResponses(operation, responses);

      assertThat(result).isEqualTo(selected);
    }

    @Test
    void when_operation_responses_empty_expect_empty() throws NoSuchFieldException, SecurityException {
      final OperationPath operation = ExampleBasicApiMother.getShowItemByIdOperation();
      final Set<String> responses = Set.of("200", "404", "default");
      final Set<String> selected = Set.of();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operation.operation().getOperationId(),
          new CheckboxResult(new HashSet<>(selected)));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOperationResponses(operation, responses);

      assertThat(result).isEmpty();
    }

    @Test
    void when_operation_responses_null_expect_empty() throws NoSuchFieldException, SecurityException {
      final OperationPath operation = ExampleBasicApiMother.getShowItemByIdOperation();
      final Set<String> selected = Set.of();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operation.operation().getOperationId(),
          new CheckboxResult(new HashSet<>(selected)));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptOperationResponses(operation, null);

      assertThat(result).isEmpty();
    }

    @Test
    void when_operation_responses_exception_expect_all() throws NoSuchFieldException, SecurityException {
      final OperationPath operation = ExampleBasicApiMother.getShowItemByIdOperation();
      final Set<String> responses = Set.of("200", "404", "default");
      final Set<String> selected = Set.of();
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operation.operation().getOperationId(),
          new CheckboxResult(new HashSet<>(selected)));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptOperationResponses(operation, responses);

      assertThat(result).isEqualTo(responses);
    }
  }

  @Nested
  class PromptTestName {
    @ParameterizedTest(name = "PromptTestName_{1}")
    @CsvSource({
        "MyTest,MyTest", "My_Test,My_Test", "MyTest001,MyTest001", "My Test 002,MyTest002", "My/Test/003,MyTest003",
        "My-Test-004,My-Test-004", "My.Test-005,My.Test-005", "My.New_Test-006,My.New_Test-006"
    })
    void when_test_name_input_argument_expect_parsed_selected_value(final String selected, final String expected)
        throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID,
          new InputResult(selected));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptTestName("Test Name");

      assertThat(result).isEqualTo(expected);
    }

    @Test
    void when_test_name_input_exception_expect_null() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_TEST_NAME_ID, new InputResult(""));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptTestName("Test Name");

      assertThat(result).isNull();
    }

  }

  @Nested
  class PromptInlineMocks {
    @ParameterizedTest(name = "PromptInlineMocks_{1}")
    @ValueSource(booleans = {true, false})
    void when_mode_arguments_expect_selected_value(final Boolean inlineMocks) throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(inlineMocks ? ConfirmationValue.YES : ConfirmationValue.NO));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, false);

      final var result = OpenApiGeneratorCLI.promptInlineMocks("Inline Mocks");

      assertThat(result).isEqualTo(inlineMocks);
    }

    @Test
    void when_mode_exception_expect_default_value() throws NoSuchFieldException, SecurityException {
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_ID,
          new ConfirmResult(ConfirmationValue.YES));
      ConsoleTestUtils.initConsoleWithMockPrompts(mockPrompts, true);

      final var result = OpenApiGeneratorCLI.promptInlineMocks("Inline Mocks");

      assertThat(result).isEqualTo(OpenApiGeneratorCLI.OPEN_API_GENERATOR_INLINE_MOCKS_DEFAULT_VALUE);
    }
  }

}
