package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenApiExampleGeneratorTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiExampleGenerator::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GenerateExample {
    @Test
    void when_generate_example_expect_result() {
      final OpenAPI openAPI = OpenApiParser.parseOpenApi(getPathFromResources("/openapi/unit/parser/openapi-test.yml"));
      final String expected = """
          {"id":0,"name":"doggie","photoUrls":["string"],"tags":["string"],"status":"available"}\
          """;

      final var result = OpenApiExampleGenerator.generateExample(openAPI, openAPI.getComponents().getSchemas().get("Pet"));

      assertThat(result).hasToString(expected);
    }
  }

  @Nested
  class GenerateExampleAsStr {
    @Test
    void when_generate_example_str_expect_result() {
      final OpenAPI openAPI = OpenApiParser.parseOpenApi(getPathFromResources("/openapi/unit/parser/openapi-test.yml"));
      final String expected = "0";

      final var result = OpenApiExampleGenerator.generateExample(openAPI,
          openAPI.getPaths().get("/pet/{petId}").getGet().getParameters().get(0).getSchema());

      assertThat(result).hasToString(expected);
    }
  }
}
