package dev.inditex.karate.openapi.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import dev.inditex.karate.openapi.OpenApiGeneratorRuntimeException;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;

/**
 * The Class KarateMockData.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class KarateMockData extends KarateWriter {

  /** The operation id. */
  private String operationId;

  /** The method. */
  private String method;

  /** The path. */
  private String path;

  /** The params. */
  private String params;

  /** The request. */
  private JsonNode request;

  /** The response status. */
  private Integer responseStatus;

  /** The response headers. */
  private Map<String, String> responseHeaders;

  /** The response. */
  private JsonNode response;

  /**
   * Save.
   *
   * @param root the root
   * @param operationPath the operation path
   * @param responseStatus the response status
   * @param openApi the open api
   * @return the path
   */
  public static Path save(final Path root, final OperationPath operationPath, final String responseStatus, final OpenAPI openApi) {
    final KarateMockData karateMockData = KarateMockData.build(operationPath, responseStatus, openApi);

    Path output = null;
    try {
      final ObjectMapper yamlWriter = new ObjectMapper(new YAMLFactory());
      yamlWriter.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
      final String tag = operationPath.operation().getTags() != null ? operationPath.operation().getTags().get(0) : "NoTag";
      output =
          root.resolve(tag).resolve("XXXX_" + operationPath.operation().getOperationId() + "_" + responseStatus + Constants.YML_EXTENSION);
      uncheckedDirExists(output.getParent());
      try (final OutputStream outputStream = Files.newOutputStream(output)) {
        yamlWriter.writerWithDefaultPrettyPrinter().writeValue(outputStream, karateMockData);
      }
    } catch (final IOException e) {
      log.error("KarateMockData.save() Exception {}", e);
      throw new OpenApiGeneratorRuntimeException(e);
    }
    return output;
  }

  /**
   * Builds the.
   *
   * @param operationPath the operation path
   * @param responseStatus the response status
   * @param openApi the open api
   * @return the karate mock data
   */
  @SuppressWarnings("rawtypes")
  protected static KarateMockData build(final OperationPath operationPath, final String responseStatus, final OpenAPI openApi) {
    final Operation operation = operationPath.operation();
    final KarateMockData mockData = new KarateMockData();
    mockData.setOperationId(operation.getOperationId());
    mockData.setMethod(operationPath.method());
    if (NumberUtils.isCreatable(responseStatus)) {
      mockData.setResponseStatus(Integer.valueOf(responseStatus));
    }
    mockData.setResponseHeaders(Map.of(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()));
    mockData.setPath(operationPath.path());
    // Set Params Example if defined
    if (operation.getParameters() != null) {
      final StringBuilder queryParams = new StringBuilder();
      operation.getParameters().forEach(p -> {
        final String paramName = p.getName();
        String paramValue = null;
        if (p.getSchema() != null) {
          paramValue = OpenApiExampleGenerator.generateExampleAsStr(openApi, p.getSchema());
        }
        if (p.getIn().equals("query")) {
          queryParams.append(paramName).append("=").append(paramValue).append("&");
        } else if (p.getIn().equals("in")) {
          mockData.setPath(mockData.getPath().replace(paramName, paramValue));
        }
      });
      if (queryParams.length() > 1) {
        mockData.setParams(queryParams.deleteCharAt(queryParams.length() - 1).toString());
      }
    }
    // Set Body Example if Schema defined
    final Schema requestSchema = OpenApiGenerators.getRequestSchema(operation);
    if (requestSchema != null) {
      mockData.setRequest(OpenApiExampleGenerator.generateExample(openApi, requestSchema));
    }
    // Set Response if Schema defined
    final Schema responseSchema = OpenApiGenerators.getResponseSchema(operation, responseStatus);
    if (responseSchema != null) {
      mockData.setResponse(OpenApiExampleGenerator.generateExample(openApi, responseSchema));
    }
    return mockData;
  }
}
