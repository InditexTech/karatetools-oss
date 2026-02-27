package dev.inditex.karate.openapi.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.ParseOptions;

/**
 * The Class OpenApiParser.
 */
public class OpenApiParser {

  // Runtime configuration property key for operationId sanitize mode
  private static final String OPERATION_ID_SANITIZE_MODE_PROPERTY = "open-api-operation-id-sanitize.mode";

  // Supported operationId sanitize modes
  private static final String OPERATION_ID_SANITIZE_MODE_LETTERS_ONLY = "letters-only";

  private static final String OPERATION_ID_SANITIZE_MODE_ALPHANUMERIC = "alphanumeric";

  // Regular expression patterns for each operationId sanitize mode
  private static final String OPERATION_ID_SANITIZE_PATTERN_LETTERS_ONLY = "[^a-zA-Z]";

  private static final String OPERATION_ID_SANITIZE_PATTERN_ALPHANUMERIC = "(?:^[^A-Za-z_]+|\\W)";

  /**
   * The Record OperationPath.
   *
   * @param path the path
   * @param operation the operation
   * @param method the method
   */
  public record OperationPath(String path, Operation operation, String method) {
    // Record
  }

  /**
   * Gets the operations by tag.
   *
   * @param openApiFilePath the open api file path
   * @return the operations by tag
   */
  public static Map<String, List<OperationPath>> getOperationsByTag(final String openApiFilePath) {
    return getOperationsByTag(parseOpenApi(openApiFilePath));
  }

  /**
   * Gets the operations by tag.
   *
   * @param openAPI the open API
   * @return the operations by tag
   */
  public static Map<String, List<OperationPath>> getOperationsByTag(final OpenAPI openAPI) {
    if (openAPI != null) {
      // get operations from openAPI grouped by tag
      return openAPI.getPaths().entrySet().stream()
          .flatMap(OpenApiParser::pathsToOperationPaths)
          .flatMap(OpenApiParser::indexOperationByTags)
          .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
    return Collections.emptyMap();
  }

  /**
   * Parses the open api.
   *
   * @param openApiFilePath the open api file path
   * @return the open API
   */
  public static OpenAPI parseOpenApi(final String openApiFilePath) {
    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(true);
    final var openAPI = new OpenAPIParser().readLocation(openApiFilePath, null, parseOptions).getOpenAPI();
    if (openAPI != null) {
      sanitizeOperationIds(openAPI);
    }
    return openAPI;
  }

  /**
   * Paths to operation paths.
   *
   * @param paths the paths
   * @return the stream
   */
  protected static Stream<OperationPath> pathsToOperationPaths(final Map.Entry<String, PathItem> paths) {
    return paths.getValue().readOperationsMap().entrySet()
        .stream()
        .map(op -> new OperationPath(paths.getKey(), op.getValue(), op.getKey().name()));
  }

  /**
   * Index operation by tags.
   *
   * @param operationPath the operation path
   * @return the stream
   */
  protected static Stream<Map.Entry<String, OperationPath>> indexOperationByTags(final OperationPath operationPath) {
    return Optional.ofNullable(operationPath.operation().getTags())
        .map(Collection::stream)
        .orElseGet(() -> Stream.of("NoTag"))
        .map(tag -> Map.entry(tag, operationPath));
  }

  /**
   * Sanitize operation ids.
   *
   * @param openAPI the open API
   */
  protected static void sanitizeOperationIds(final OpenAPI openAPI) {

    final String mode = System.getProperty(OPERATION_ID_SANITIZE_MODE_PROPERTY, "");

    // Default pattern is alphanumeric if mode is not recognized or not set
    final String pattern = switch (mode) {
      case OPERATION_ID_SANITIZE_MODE_LETTERS_ONLY -> OPERATION_ID_SANITIZE_PATTERN_LETTERS_ONLY;
      case OPERATION_ID_SANITIZE_MODE_ALPHANUMERIC -> OPERATION_ID_SANITIZE_PATTERN_ALPHANUMERIC;
      default -> OPERATION_ID_SANITIZE_PATTERN_ALPHANUMERIC;
    };

    openAPI.getPaths().entrySet().stream().forEach(
        path -> path.getValue().readOperations().forEach(
            op -> op.setOperationId(op.getOperationId().replaceAll(pattern, ""))));
  }
}
