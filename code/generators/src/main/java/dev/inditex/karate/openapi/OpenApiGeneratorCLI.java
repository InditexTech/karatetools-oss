package dev.inditex.karate.openapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import dev.inditex.karate.console.ConsoleCLI;
import dev.inditex.karate.console.ConsoleItem;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import de.codeshelf.consoleui.prompt.ConsolePrompt;

/**
 * The Class OpenApiGeneratorCLI.
 */
public class OpenApiGeneratorCLI {

  /** The Constant OPEN_API_GENERATOR_MODE_ID. */
  public static final String OPEN_API_GENERATOR_MODE_ID = "open-api-generator.mode";

  /** The Constant OPEN_API_GENERATOR_MODE_DEFAULT_VALUE. */
  public static final OpenApiGeneratorMode OPEN_API_GENERATOR_MODE_DEFAULT_VALUE = OpenApiGeneratorModes.OPERATIONS;

  /** The Constant OPEN_API_GENERATOR_ARTIFACT_ID. */
  public static final String OPEN_API_GENERATOR_ARTIFACT_ID = "open-api-generator.artifact-id";

  /** The Constant OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE. */
  public static final String OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE = "dev.inditex.karate.openapi:test";

  /** The Constant OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID. */
  public static final String OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID = "open-api-generator.secondary-artifact-id";

  /** The Constant OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE. */
  public static final String OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE = "dev.inditex.karate.openapi:other";

  /** The Constant OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID. */
  public static final String OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID = "open-api-generator.open-api-file-list";

  /** The Constant OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID. */
  public static final String OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID = "open-api-generator.open-api-file-manual";

  /** The Constant OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE. */
  public static final String OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE = "Enter Manually";

  /** The Constant OPEN_API_GENERATOR_OPERATIONS_ID. */
  public static final String OPEN_API_GENERATOR_OPERATIONS_ID = "open-api-generator.operations-list";

  /** The Constant OPEN_API_GENERATOR_OPERATIONS_DEFAULT_VALUE. */
  public static final String OPEN_API_GENERATOR_OPERATIONS_DEFAULT_VALUE = "all";

  /** The Constant OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX. */
  public static final String OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX = "open-api-generator.operation-response.";

  /** The Constant OPEN_API_GENERATOR_OPERATION_RESPONSE_DEFAULT_VALUE. */
  public static final String OPEN_API_GENERATOR_OPERATION_RESPONSE_DEFAULT_VALUE = "all";

  /** The Constant OPEN_API_GENERATOR_TEST_NAME_ID. */
  public static final String OPEN_API_GENERATOR_TEST_NAME_ID = "open-api-generator.test-name";

  /** The Constant OPEN_API_GENERATOR_TEST_NAME_DEFAULT_VALUE. */
  public static final String OPEN_API_GENERATOR_TEST_NAME_DEFAULT_VALUE = "TestName";

  /** The Constant OPEN_API_GENERATOR_INLINE_MOCKS_ID. */
  public static final String OPEN_API_GENERATOR_INLINE_MOCKS_ID = "open-api-generator.inline-mocks";

  /** The Constant OPEN_API_GENERATOR_INLINE_MOCKS_DEFAULT_VALUE. */
  public static final Boolean OPEN_API_GENERATOR_INLINE_MOCKS_DEFAULT_VALUE = false;

  /** The console prompt. */
  protected static ConsolePrompt consolePrompt = ConsoleCLI.initializeConsole();

  /** The Constant OPERATION_STRING_FORMAT. */
  protected static final String OPERATION_STRING_FORMAT = "s   %-";

  /** The Constant MAX_METHOD_LENGTH. */
  protected static final int MAX_METHOD_LENGTH = 7;

  /** The Constant MAX_PATH_LENGTH. */
  protected static final int MAX_PATH_LENGTH = 30;

  /** The Constant MAX_OP_ID_LENGTH. */
  protected static final int MAX_OP_ID_LENGTH = 30;

  /** The Constant DEFAULT_OPERATION_FORMAT. */
  protected static final String DEFAULT_OPERATION_FORMAT =
      "%-" + MAX_METHOD_LENGTH + OPERATION_STRING_FORMAT + MAX_PATH_LENGTH + OPERATION_STRING_FORMAT + MAX_OP_ID_LENGTH + "s";

  /** The operation format. */
  protected static String operationFormat = DEFAULT_OPERATION_FORMAT;

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    final OpenApiGenerator generator = new OpenApiGenerator();
    generator.execute();
  }

  /**
   * Prompt mode.
   *
   * @return the open api generator mode
   */
  public static OpenApiGeneratorMode promptMode() {
    return OpenApiGeneratorModes.valueOf(ConsoleCLI.promptList(consolePrompt,
        OPEN_API_GENERATOR_MODE_ID,
        "Enter Open Api Generator Mode " + OpenApiGeneratorModes.getAvailableModesAsText(),
        OpenApiGeneratorModes.getAvailableModesAsStream().map(m -> new ConsoleItem(m.name(), m.label())).toList(),
        new ConsoleItem(OPEN_API_GENERATOR_MODE_DEFAULT_VALUE.name(), OPEN_API_GENERATOR_MODE_DEFAULT_VALUE.label())));
  }

  /**
   * Prompt artifacts.
   *
   * @param values the values
   * @param defaultValue the default value
   * @return the string
   */
  public static String promptArtifacts(final List<String> values, final String defaultValue) {
    return promptArtifacts(OPEN_API_GENERATOR_ARTIFACT_ID, "Select which artifactId you want to generate for", values, defaultValue,
        OPEN_API_GENERATOR_ARTIFACT_DEFAULT_VALUE);
  }

  /**
   * Prompt artifacts.
   *
   * @param id the id
   * @param message the message
   * @param values the values
   * @param defaultValue the default value
   * @param baseDefaultValue the base default value
   * @return the string
   */
  protected static String promptArtifacts(final String id, final String message, final List<String> values, final String defaultValue,
      final String baseDefaultValue) {
    String value = baseDefaultValue;
    if (defaultValue != null) {
      value = defaultValue;
    }
    final ConsoleItem defaultItem = new ConsoleItem(value, value);
    final List<ConsoleItem> items = new ArrayList<>();
    if (values != null && !values.isEmpty()) {
      items.addAll(values.stream().map(a -> new ConsoleItem(a, a)).toList());
    }
    return ConsoleCLI.promptList(consolePrompt, id, message, items, defaultItem);
  }

  /**
   * Prompt secondary artifacts.
   *
   * @param message the message
   * @param values the values
   * @param defaultValue the default value
   * @return the string
   */
  public static String promptSecondaryArtifacts(final String message, final List<String> values, final String defaultValue) {
    return promptArtifacts(OPEN_API_GENERATOR_SECONDARY_ARTIFACT_ID, message, values, defaultValue,
        OPEN_API_GENERATOR_SECONDARY_ARTIFACT_DEFAULT_VALUE);
  }

  /**
   * Prompt open api.
   *
   * @param values the values
   * @return the string
   */
  public static String promptOpenApi(final List<String> values) {
    String value = OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE;
    final ConsoleItem defaultItem = new ConsoleItem(value, value);
    final List<ConsoleItem> items = new ArrayList<>();
    if (values != null && !values.isEmpty()) {
      items.addAll(values.stream().map(a -> new ConsoleItem(a, a)).toList());
    }
    value = ConsoleCLI.promptList(consolePrompt,
        OPEN_API_GENERATOR_OPEN_API_FILE_LIST_ID,
        "Enter the location of the Open Api definition",
        items, defaultItem);
    if (value.equals(OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE)) {
      value = null;
    }
    return value;
  }

  /**
   * Prompt open api.
   *
   * @return the string
   */
  public static String promptOpenApi() {
    String value = OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE;
    value = ConsoleCLI.promptInput(consolePrompt,
        OPEN_API_GENERATOR_OPEN_API_FILE_MANUAL_ID,
        "Enter the location of the Open Api definition",
        value);
    if (value != null && value.equals(OPEN_API_GENERATOR_OPEN_API_FILE_DEFAULT_VALUE)) {
      value = null;
    }
    return value;
  }

  /**
   * Prompt operations.
   *
   * @param values the values
   * @return the list
   */
  public static List<OperationPath> promptOperations(final Map<String, List<OperationPath>> values) {
    final Map<String, OperationPath> operations = formatOperations(values);
    List<OperationPath> selectedValues;
    final ConsoleItem defaultItem =
        new ConsoleItem(OPEN_API_GENERATOR_OPERATIONS_DEFAULT_VALUE, OPEN_API_GENERATOR_OPERATIONS_DEFAULT_VALUE);
    final List<ConsoleItem> items = new ArrayList<>();
    if (values != null && !values.isEmpty()) {
      items.addAll(operations.keySet().stream().map(k -> new ConsoleItem(k, k)).toList());
    }
    final Set<String> selected = ConsoleCLI.promptCheckbox(consolePrompt,
        OPEN_API_GENERATOR_OPERATIONS_ID,
        "Select which Operations you want to generate for",
        items, defaultItem);
    if (selected.size() == 1 && OPEN_API_GENERATOR_OPERATIONS_DEFAULT_VALUE.equals(selected.iterator().next())) {
      // Select all
      selectedValues = operations.values().stream().toList();
    } else {
      // Selected to Operation
      selectedValues = operations.entrySet().stream().filter(e -> selected.contains(e.getKey())).map(Entry::getValue).toList();
    }
    return selectedValues;
  }

  /**
   * Prompt operation responses.
   *
   * @param operation the operation
   * @param responses the responses
   * @return the sets the
   */
  public static Set<String> promptOperationResponses(final OperationPath operation, final Set<String> responses) {
    final String operationDisplayId =
        String.format(operationFormat, operation.method(), operation.path(), operation.operation().getOperationId());
    final ConsoleItem defaultItem =
        new ConsoleItem(OPEN_API_GENERATOR_OPERATION_RESPONSE_DEFAULT_VALUE, OPEN_API_GENERATOR_OPERATION_RESPONSE_DEFAULT_VALUE);
    final List<ConsoleItem> items = new ArrayList<>();
    if (responses != null && !responses.isEmpty()) {
      items.addAll(responses.stream().sorted().map(a -> new ConsoleItem(a, a)).toList());
    }
    final String promptId = OPEN_API_GENERATOR_OPERATION_RESPONSE_ID_PREFIX + operation.operation().getOperationId();
    final Set<String> selected = ConsoleCLI.promptCheckbox(consolePrompt,
        promptId,
        "Enter the responses to generate for: " + operationDisplayId,
        items, defaultItem);
    if (selected.size() == 1 && OPEN_API_GENERATOR_OPERATION_RESPONSE_DEFAULT_VALUE.equals(selected.iterator().next())) {
      return responses;
    }
    return selected;
  }

  /**
   * Prompt test name.
   *
   * @param message the message
   * @return the string
   */
  public static String promptTestName(final String message) {
    String value = OPEN_API_GENERATOR_TEST_NAME_DEFAULT_VALUE;
    value = ConsoleCLI.promptInput(consolePrompt,
        OPEN_API_GENERATOR_TEST_NAME_ID,
        message,
        value);
    if (value.equals(OPEN_API_GENERATOR_TEST_NAME_DEFAULT_VALUE)) {
      value = null;
    } else {
      // replace all special chars except "-" "." and "_"
      value = value.replaceAll("[^a-zA-Z0-9-\\._]+", "");
    }
    return value;
  }

  /**
   * Prompt inline mocks.
   *
   * @param message the message
   * @return the boolean
   */
  public static Boolean promptInlineMocks(final String message) {
    final Boolean value = OPEN_API_GENERATOR_INLINE_MOCKS_DEFAULT_VALUE;
    return ConsoleCLI.promptConfirm(consolePrompt,
        OPEN_API_GENERATOR_INLINE_MOCKS_ID,
        message,
        value);
  }

  /**
   * Format operations.
   *
   * @param values the values
   * @return the map
   */
  protected static Map<String, OperationPath> formatOperations(final Map<String, List<OperationPath>> values) {
    if (values != null) {
      final Integer maxMethod =
          values.values().stream().flatMap(Collection::stream).mapToInt(e -> e.method().length()).max().orElse(MAX_METHOD_LENGTH);
      final Integer maxPath =
          values.values().stream().flatMap(Collection::stream).mapToInt(e -> e.path().length()).max().orElse(MAX_PATH_LENGTH);
      final Integer maxOperationId = values.values().stream().flatMap(Collection::stream)
          .mapToInt(e -> e.operation().getOperationId() != null ? e.operation().getOperationId().length() : 0).max()
          .orElse(MAX_OP_ID_LENGTH);
      operationFormat = "%-" + maxMethod + OPERATION_STRING_FORMAT + maxPath + OPERATION_STRING_FORMAT + maxOperationId + "s";
      return values.values().stream().flatMap(Collection::stream)
          .collect(Collectors.toMap(v -> String.format(operationFormat, v.method(), v.path(), v.operation().getOperationId()), v -> v));
    }
    return Collections.emptyMap();
  }

}
