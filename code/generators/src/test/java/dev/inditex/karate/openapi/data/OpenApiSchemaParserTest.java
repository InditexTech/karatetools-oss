package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.media.BinarySchema;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenApiSchemaParserTest {

  @Nested
  class Build {

    @Test
    void when_binary_schema_is_built_expect_string_schema() {
      final KarateSchema result = new OpenApiSchemaParser().build(new BinarySchema());

      assertThat(result.toString()).contains("#string");
    }
  }
}
