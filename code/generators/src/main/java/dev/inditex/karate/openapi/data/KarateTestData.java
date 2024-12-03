package dev.inditex.karate.openapi.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import dev.inditex.karate.openapi.OpenApiGeneratorRuntimeException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The Class KarateTestData.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class KarateTestData extends KarateWriter {

  /** The operation id. */
  private String operationId;

  /** The status code. */
  private Integer statusCode;

  /** The params. */
  private JsonNode params;

  /** The headers. */
  @JsonInclude(Include.NON_NULL)
  private JsonNode headers;

  /** The body. */
  private JsonNode body;

  /** The match response. */
  private Boolean matchResponse = true;

  /** The response matches. */
  private String responseMatches;

  /**
   * Save.
   *
   * @param target the target
   * @param operation the operation
   * @param statusCode the status code
   * @param responseSchemaClassPath the response schema class path
   * @param openApi the open api
   * @return the path
   */
  public static Path save(final Path target, final Operation operation, final String statusCode, final String responseSchemaClassPath,
      final OpenAPI openApi) {
    final Path output = target.resolve("test-data").resolve(operation.getOperationId() + "_" + statusCode + Constants.YML_EXTENSION);
    final KarateTestData karateTestData = build(operation, statusCode, responseSchemaClassPath, openApi);
    final ObjectMapper yamlWriter = new ObjectMapper(new YAMLFactory());
    yamlWriter.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    uncheckedDirExists(output.getParent());
    try (final OutputStream outputStream = Files.newOutputStream(output)) {
      yamlWriter.writerWithDefaultPrettyPrinter().writeValue(outputStream, karateTestData);
    } catch (final IOException e) {
      log.error("KarateTestData.save() Exception {}", e);
      throw new OpenApiGeneratorRuntimeException(e);
    }
    return output;
  }

  /**
   * Builds the.
   *
   * @param operation the operation
   * @param statusCode the status code
   * @param responseSchemaClassPath the response schema class path
   * @param openApi the open api
   * @return the karate test data
   */
  @SuppressWarnings("rawtypes")
  protected static KarateTestData build(final Operation operation, final String statusCode, final String responseSchemaClassPath,
      final OpenAPI openApi) {
    final KarateTestData testData = new KarateTestData();
    testData.setOperationId(operation.getOperationId());
    if (NumberUtils.isCreatable(statusCode)) {
      testData.setStatusCode(Integer.valueOf(statusCode));
    }
    testData.setMatchResponse(true);
    // Set Params Example if defined
    setParams(operation, openApi, testData);
    // Set Headers Example if defined
    setHeaders(operation, openApi, testData);
    // Set Body Example if Schema defined
    final Schema requestSchema = OpenApiGenerators.getRequestSchema(operation);
    if (requestSchema != null) {
      testData.setBody(OpenApiExampleGenerator.generateExample(openApi, requestSchema));
    }
    // Set Response Matches if Schema defined
    final Schema responseSchema = OpenApiGenerators.getResponseSchema(operation, statusCode);
    if (responseSchema != null) {
      testData.setResponseMatches("#(read('classpath:" + responseSchemaClassPath + "'))");
    }
    return testData;
  }

  /**
   * Sets the params.
   *
   * @param operation the operation
   * @param openApi the open api
   * @param testData the test data
   */
  protected static void setParams(final Operation operation, final OpenAPI openApi, final KarateTestData testData) {
    if (operation.getParameters() != null) {
      final ObjectMapper mapper = new ObjectMapper();
      final ObjectNode params = mapper.createObjectNode();
      final AtomicInteger counter = new AtomicInteger(0);
      operation.getParameters().forEach(p -> {
        if (ParameterIn.PATH.toString().equals(p.getIn()) || ParameterIn.QUERY.toString().equals(p.getIn())) {
          final String paramName = p.getName();
          JsonNode paramValue = null;
          if (p.getSchema() != null) {
            paramValue = OpenApiExampleGenerator.generateExample(openApi, p.getSchema());
          }
          params.set(paramName, paramValue);
          counter.getAndIncrement();
        }
      });
      if (counter.get() > 0) {
        testData.setParams(params);
      }
    }
  }

  /**
   * Sets the headers.
   *
   * @param operation the operation
   * @param openApi the open api
   * @param testData the test data
   */
  protected static void setHeaders(final Operation operation, final OpenAPI openApi, final KarateTestData testData) {
    if (operation.getParameters() != null) {
      final ObjectMapper mapper = new ObjectMapper();
      final ObjectNode headers = mapper.createObjectNode();
      final AtomicInteger counter = new AtomicInteger(0);
      operation.getParameters().forEach(p -> {
        if (ParameterIn.HEADER.toString().equals(p.getIn())) {
          final String paramName = p.getName();
          JsonNode paramValue = null;
          if (p.getSchema() != null) {
            paramValue = OpenApiExampleGenerator.generateExample(openApi, p.getSchema());
          }
          headers.set(paramName, paramValue);
          counter.getAndIncrement();
        }
      });
      if (counter.get() > 0) {
        testData.setHeaders(headers);
      }
    }
  }
}
