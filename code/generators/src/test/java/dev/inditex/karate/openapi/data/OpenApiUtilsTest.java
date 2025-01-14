package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import dev.inditex.karate.openapi.ExampleBasicApiMother;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenApiUtilsTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiUtils::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GetOperationClasspath {

    @ParameterizedTest(name = "GetOperationClasspath_{0}")
    @MethodSource
    void when_get_classpath_expect_result(final String operationId, final OperationPath operation, final String root,
        final String expected) {
      final Path rootPath = Paths.get(targetFolder, root);
      final MavenArtifact artifact = new MavenArtifact("dev.inditex.karate.openapi", "test");

      final var result = OpenApiUtils.getOperationClasspath(rootPath, artifact, operation);

      assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> when_get_classpath_expect_result() {
      final String root = FilenameUtils.separatorsToSystem("src/test/resources");
      return Stream.of(
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(), root,
              "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature"),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation(), root,
              "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature"),
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation(), root,
              "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature"));
    }
  }

  @Nested
  class GetResponseSchemaClasspath {

    @ParameterizedTest(name = "GetResponseSchemaClasspath_{0}")
    @MethodSource
    void when_get_response_schema_classpath_expect_result(final String operationId, final OperationPath operation, final String root,
        final String code, final String expected) {
      final Path rootPath = Paths.get(targetFolder, root);
      final MavenArtifact artifact = new MavenArtifact("dev.inditex.karate.openapi", "test");

      final var result = OpenApiUtils.getResponseSchemaClasspath(rootPath, artifact, operation, code);

      assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> when_get_response_schema_classpath_expect_result() {
      final String root = FilenameUtils.separatorsToSystem("src/test/resources");
      return Stream.of(
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(), root,
              "200", "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml"),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(), root,
              "404", "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_404.schema.yml"),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(), root,
              "default", "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_default.schema.yml"),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation(), root,
              "200", "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml"),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation(), root,
              "default", "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_default.schema.yml"),
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation(), root,
              "201", "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation(), root,
              "default", "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_default.schema.yml")

      );
    }
  }

  @Nested
  class GetResponseSchema {

    @SuppressWarnings("rawtypes")
    @ParameterizedTest(name = "GetResponseSchema_{0}")
    @MethodSource
    void when_get_response_schema_expect_result(final String operationId, final Operation operation, final String code,
        final Schema expected) {

      final var result = OpenApiUtils.getResponseSchema(operation, code);

      assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> when_get_response_schema_expect_result() {
      return Stream.of(
          Arguments.of("noResponse", new Operation().responses(null), "200", null),
          Arguments.of("noResponseCode", new Operation().responses(new ApiResponses()), "200", null),
          Arguments.of("nullResponseCode", new Operation().responses(new ApiResponses().addApiResponse("200", null)), "200", null),
          Arguments.of("noResponseCodeContent", new Operation().responses(new ApiResponses().addApiResponse("200",
              new ApiResponse().content(null))), "200", null),
          Arguments.of("noResponseCodeContentMediaType", new Operation().responses(
              new ApiResponses().addApiResponse("200", new ApiResponse().content(new Content()))), "200", null),
          Arguments.of("noResponseCodeContentMediaTypeSchema", new Operation().responses(
              new ApiResponses().addApiResponse("200",
                  new ApiResponse().content(new Content().addMediaType(
                      MediaType.APPLICATION_JSON, new io.swagger.v3.oas.models.media.MediaType().schema(null))))),
              "200", null),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "200", ExampleBasicApiMother.getShowItemByIdOperation().operation().getResponses().get("200").getContent()
                  .get(MediaType.APPLICATION_JSON).getSchema()),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "404", ExampleBasicApiMother.getShowItemByIdOperation().operation().getResponses().get("404").getContent()
                  .get(MediaType.APPLICATION_JSON).getSchema()),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "default", ExampleBasicApiMother.getShowItemByIdOperation().operation().getResponses().get("default").getContent()
                  .get(MediaType.APPLICATION_JSON).getSchema()),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation().operation(),
              "200", ExampleBasicApiMother.getListItemsOperation().operation().getResponses().get("200").getContent()
                  .get(MediaType.APPLICATION_JSON).getSchema()),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation().operation(),
              "default", ExampleBasicApiMother.getListItemsOperation().operation().getResponses().get("default").getContent()
                  .get(MediaType.APPLICATION_JSON).getSchema()),
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation().operation(),
              "201", ExampleBasicApiMother.getCreateItemsOperation().operation().getResponses().get("201").getContent()
                  .get(MediaType.APPLICATION_JSON).getSchema()),
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation().operation(),
              "default", ExampleBasicApiMother.getCreateItemsOperation().operation().getResponses().get("default").getContent()
                  .get(MediaType.APPLICATION_JSON).getSchema()));
    }
  }

  @Nested
  class GetRequestSchema {

    @SuppressWarnings("rawtypes")
    @ParameterizedTest(name = "GetRequestSchema_{0}")
    @MethodSource
    void when_get_request_schema_expect_result(final String operationId, final Operation operation, final Schema expected) {

      final var result = OpenApiUtils.getRequestSchema(operation);

      assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> when_get_request_schema_expect_result() {
      return Stream.of(
          Arguments.of("noRequestBody", new Operation().requestBody(null), null),
          Arguments.of("noRequestBodyContent", new Operation().requestBody(new RequestBody().content(null)), null),
          Arguments.of("noRequestBodyContentMediaType", new Operation().requestBody(new RequestBody().content(new Content())), null),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              null),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation().operation(),
              null),
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation().operation(),
              ExampleBasicApiMother.getCreateItemsOperation().operation().getRequestBody().getContent().get(MediaType.APPLICATION_JSON)
                  .getSchema()));
    }
  }

  @Nested
  class GetMockDataTargetPath {
    @ParameterizedTest(name = "GetMockDataTargetPath{0}")
    @MethodSource
    void when_get_request_schema_expect_result(final String testId, final MavenArtifact artifact,
        final Boolean inlineMocks, final MavenArtifact functionalArtifact, final String testName, final String expected) {

      final var result =
          OpenApiUtils.getMockDataTargetPath(Paths.get(targetFolder), artifact, inlineMocks, functionalArtifact, testName);

      assertThat(result).isEqualTo(Paths.get(targetFolder, expected));
    }

    public static Stream<Arguments> when_get_request_schema_expect_result() {
      return Stream.of(
          Arguments.of("Inline", MavenArtifact.fromId("dev.inditex.karate.openapi:external"), true,
              MavenArtifact.fromId("dev.inditex.karate.openapi:test"), "TestName",
              "dev/inditex/karate/openapi/test/functional/TestName/mocks/external"),
          Arguments.of("Standalone", MavenArtifact.fromId("dev.inditex.karate.openapi:external"), false,
              MavenArtifact.fromId("dev.inditex.karate.openapi:test"), "TestName",
              "mocks/templates/standalone/external"));
    }

  }
}
