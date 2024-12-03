package dev.inditex.karate.openapi;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;

public class ExampleBasicApiMother {

  public static String getApiPath() {
    return "/openapi/karatetools/rest/basic/openapi-rest.yml";
  }

  public static OpenAPI getOpenApi() {
    final ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setResolveFully(true);
    try {
      return new OpenAPIParser().readLocation(ExampleBasicApiMother.class.getResource(getApiPath()).toURI().toString(), null, parseOptions)
          .getOpenAPI();
    } catch (final URISyntaxException e) {
      return null;
    }
  }

  public static String getShowItemByIdOperationId() {
    return "showItemById";
  }

  public static String getListItemsOperationId() {
    return "listItems";
  }

  public static String getCreateItemsOperationId() {
    return "createItems";
  }

  public static String getShowItemByIdOperationDisplayName() {
    return "GET    /items/{itemId}   showItemById";
  }

  public static String getListItemsOperationDisplayName() {
    return "GET    /items            listItems   ";
  }

  public static String getCreateItemsOperationDisplayName() {
    return "POST   /items            createItems ";
  }

  public static OperationPath getShowItemByIdOperation() {
    return new OperationPath("/items/{itemId}", getOpenApi().getPaths().get("/items/{itemId}").getGet(), "GET");
  }

  public static OperationPath getListItemsOperation() {
    return new OperationPath("/items", getOpenApi().getPaths().get("/items").getGet(), "GET");
  }

  public static OperationPath getCreateItemsOperation() {
    return new OperationPath("/items", getOpenApi().getPaths().get("/items").getPost(), "POST");
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, Schema> getShowItemByIdResponseSchemas() {
    return getResponseSchemas(getShowItemByIdOperation().operation());
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, Schema> getListItemsResponseSchemas() {
    return getResponseSchemas(getListItemsOperation().operation());
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, Schema> getCreateItemsResponseSchemas() {
    return getResponseSchemas(getCreateItemsOperation().operation());
  }

  @SuppressWarnings("rawtypes")
  private static Map<String, Schema> getResponseSchemas(final Operation operation) {
    final Map<String, Schema> schemas = new TreeMap<>();
    operation.getResponses().entrySet().forEach(e -> {
      final String returnCode = e.getKey();
      if (e.getValue().getContent() != null) {
        final Optional<MediaType> mediaType = e.getValue().getContent().values().stream().findFirst();
        if (mediaType.isPresent() && mediaType.get().getSchema() != null) {
          schemas.put(returnCode, mediaType.get().getSchema());
        }
      }
    });
    return schemas;
  }
}
