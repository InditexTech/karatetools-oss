package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenApiSchemaParserTest {

  @Nested
  class Build {

    @Test
    void when_schema_is_self_referencing_required_and_non_nullable_expect_no_stack_overflow_and_link() {
      // reproduces the real-world case: a required, non-nullable schema (e.g. a tree/parent-child DTO) referencing itself
      final ObjectSchema schema = new ObjectSchema();
      schema.setTitle("Node");
      schema.setRequired(List.of("child"));
      schema.setProperties(Map.of("name", new StringSchema(), "child", schema));

      final KarateSchema result = assertDoesNotThrow(() -> new OpenApiSchemaParser().build(schema));

      assertThat(result.toString())
          .contains("def Node")
          .contains("\"child\" : \"#(Node)\"");
    }

    @Test
    void when_nullable_object_schema_is_built_expect_no_stack_overflow() {
      final ObjectSchema error = new ObjectSchema();
      error.setNullable(true);
      error.addProperties("code", new StringSchema());
      error.addProperties("description", new StringSchema());
      final ObjectSchema response = new ObjectSchema();
      response.addProperties("error", error);

      assertDoesNotThrow(() -> new OpenApiSchemaParser().build(response));
    }
  }
}
