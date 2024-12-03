package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import io.swagger.oas.inflector.examples.ExampleBuilder;
import io.swagger.oas.inflector.examples.models.Example;
import io.swagger.oas.inflector.processors.JsonNodeExampleSerializer;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class KarateSchemaTest extends KarateTest {

  @Nested
  class Build {

    @SuppressWarnings("rawtypes")
    @ParameterizedTest(name = "KarateSchema-Build-{0}")
    @MethodSource
    void when_open_api_schema_is_created_expect_to_validate(final String testId, final String folder)
        throws Exception {
      final String openApiFile = "/openapi/unit/schema/" + folder + "/openapi-test.yml";
      final OpenAPI openAPI = OpenApiParser.parseOpenApi(getPathFromResources(openApiFile));
      final Schema schema = findFirstResponseSchema(openAPI);

      final KarateSchema sut = new OpenApiSchemaParser().build(schema);

      final String jsonExample = generateExample(openAPI, schema);
      final Path path = writeKarateTestToFile(jsonExample, sut);
      runKarateTest(path);
      assertEquals(path, "/openapi/unit/schema/" + folder + "/expected.feature");
    }

    private static Stream<Arguments> when_open_api_schema_is_created_expect_to_validate() {
      return Stream.of(
          Arguments.of("SimpleOpenApi", "SimpleOpenApi"),
          Arguments.of("ObjectsRequired", "ObjectsRequired"),
          Arguments.of("ObjectsOptional", "ObjectsOptional"),
          Arguments.of("ObjectsEdgeCases", "ObjectsEdgeCases"),
          Arguments.of("Arrays", "Arrays"),
          Arguments.of("SpecificTypes", "SpecificTypes"),
          Arguments.of("Composed-AllOf", "Composed/AllOf"),
          Arguments.of("Composed-AnyOf", "Composed/AnyOf"), // Currently only generates #object
          Arguments.of("Composed-OneOf", "Composed/OneOf"), // Currently only generates #object
          Arguments.of("Circular", "Circular") // Currently only generates #object
      );
    }

    private void assertEquals(final Path path, final String feature) throws Exception {
      try (final InputStream resourceAsStream = getClass().getResourceAsStream(feature)) {
        if (resourceAsStream == null) {
          log.warn("Resource not found: {}", feature);
        } else {
          final String expectedContent = new String(resourceAsStream.readAllBytes());
          final String actualContent = Files.readString(path);
          assertThat(actualContent).isEqualToIgnoringWhitespace(expectedContent);
        }
      }
    }

    private static void runKarateTest(final Path path) {
      final Results results = Runner.path(path.toAbsolutePath().toString())
          .outputCucumberJson(false)
          .outputHtmlReport(false)
          .outputJunitXml(false)
          .reportDir(karateReportDir)
          .parallel(1);
      assertThat(results.getFailCount()).isZero();
    }

    private Path writeKarateTestToFile(final String jsonExample, final KarateSchema schemaResult) throws IOException {
      final String baseFeature = """
          Feature:
          Scenario: HelloTest
          * def result =
          ""\"
          %s
          ""\"
          %s
          """;

      final Path targetPath = Paths.get(targetFolder);
      final Path test = Files.createTempFile(targetPath, "test", ".feature");
      return Files.writeString(test, baseFeature.formatted(jsonExample, schemaResult.toString()));
    }

    @SuppressWarnings("rawtypes")
    private static Schema findFirstResponseSchema(final OpenAPI openAPI) {
      return openAPI.getPaths().entrySet().iterator().next().getValue().readOperations().iterator().next().getResponses().entrySet()
          .iterator().next().getValue().getContent().entrySet().iterator().next().getValue().getSchema();
    }

    @SuppressWarnings("rawtypes")
    private static String generateExample(final OpenAPI openAPI, final Schema schema) {
      final Example example = ExampleBuilder.fromSchema(schema, openAPI.getComponents().getSchemas());
      final SimpleModule simpleModule = new SimpleModule().addSerializer(new JsonNodeExampleSerializer());
      Json.mapper().registerModule(simpleModule);
      return Json.pretty(example);
    }
  }
}
