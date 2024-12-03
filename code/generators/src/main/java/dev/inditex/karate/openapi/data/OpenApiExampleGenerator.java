package dev.inditex.karate.openapi.data;

import dev.inditex.karate.openapi.OpenApiGeneratorRuntimeException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

/**
 * The Class OpenApiExampleGenerator.
 */
public class OpenApiExampleGenerator {

  /**
   * Instantiates a new open api example generator.
   */
  protected OpenApiExampleGenerator() {
  }

  /**
   * Generate example.
   *
   * @param openAPI the open API
   * @param schema the schema
   * @return the json node
   */
  @SuppressWarnings("rawtypes")
  public static JsonNode generateExample(final OpenAPI openAPI, final Schema schema) {
    final Example example = ExampleBuilder.fromSchema(schema, openAPI.getComponents().getSchemas());
    final SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
    Json.mapper().registerModule(simpleModule);
    final String pretty = Json.pretty(example);
    final ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readTree(pretty);
    } catch (final JsonProcessingException e) {
      throw new OpenApiGeneratorRuntimeException(e);
    }
  }

  /**
   * Generate example as str.
   *
   * @param openAPI the open API
   * @param schema the schema
   * @return the string
   */
  @SuppressWarnings("rawtypes")
  public static String generateExampleAsStr(final OpenAPI openAPI, final Schema schema) {
    final Example example = ExampleBuilder.fromSchema(schema, openAPI.getComponents().getSchemas());
    return example.asString();
  }
}
