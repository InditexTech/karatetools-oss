package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import dev.inditex.karate.openapi.ExampleBasicApiMother;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KarateOperationTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateOperation::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class ProcessTemplate {
    @ParameterizedTest(name = "ProcessTemplate_Operation_{0}_{1}")
    @MethodSource
    void when_process_operation_template_expect_karate_feature(final String tag, final String operationId,
        final OperationPath operationPath) {
      final MavenArtifact artifact = new MavenArtifact("dev.inditex.karate.openapi", "test");

      final var result = KarateOperation.processTemplate(artifact, operationPath);

      assertThat(result).isEqualToIgnoringNewLines(
          getResourceAsString("/openapi/unit/operation/" + tag + "/" + operationId + "/" + operationId + ".feature"));
    }

    public static Stream<Arguments> when_process_operation_template_expect_karate_feature() {
      return Stream.of(
          Arguments.of("Sample", "get", new OperationPath("/get",
              new Operation().operationId("get").parameters(List.of(new Parameter().name("id").in("query"))),
              "GET")),
          Arguments.of("Sample", "post", new OperationPath("/post",
              new Operation().operationId("post").parameters(List.of(new Parameter().name("id").in("path"))).requestBody(new RequestBody()),
              "POST")),
          Arguments.of("BasicApi", ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation()),
          Arguments.of("BasicApi", ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation()),
          Arguments.of("BasicApi", ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation()));
    }
  }

  @Nested
  class Save {
    @SuppressWarnings({"rawtypes"})
    @ParameterizedTest(name = "Save_Operation_{0}_{1}")
    @MethodSource
    void when_save_functional_test_expect_feature_and_data(final String tag, final String operationId, final OperationPath operationPath,
        final Map<String, Schema> responseSchemas, final List<String> expectedFiles) {
      final MavenArtifact artifact = new MavenArtifact("dev.inditex.karate.openapi", "test");
      final Path root = Paths.get(targetFolder);
      final Path target = root.resolve("apis").resolve(artifact.toPath()).resolve(tag).resolve(operationId);

      final var result = KarateOperation.save(root, target, artifact, operationPath, responseSchemas);

      assertThat(result).hasSameSizeAs(expectedFiles);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i))
            .isEqualTo(Paths.get(targetFolder, "apis", artifact.toPath(), tag, operationId, expectedFiles.get(i)));
      }
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i).toFile()).exists().hasSameTextualContentAs(
            getFileFromResources("/openapi/unit/operation/" + tag + "/" + operationId + "/" + expectedFiles.get(i)));
      }
    }

    public static Stream<Arguments> when_save_functional_test_expect_feature_and_data() {
      return Stream.of(
          Arguments.of("BasicApi", ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation(),
              ExampleBasicApiMother.getCreateItemsResponseSchemas(),
              List.of(
                  "createItems.feature",
                  "schema/createItems_201.schema.yml",
                  "schema/createItems_400.schema.yml",
                  "schema/createItems_default.schema.yml")),
          Arguments.of("BasicApi", ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation(),
              ExampleBasicApiMother.getListItemsResponseSchemas(),
              List.of(
                  "listItems.feature",
                  "schema/Items_200.schema.yml",
                  "schema/listItems_200.schema.yml",
                  "schema/listItems_400.schema.yml",
                  "schema/listItems_default.schema.yml")),
          Arguments.of("BasicApi", ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(),
              ExampleBasicApiMother.getShowItemByIdResponseSchemas(),
              List.of(
                  "showItemById.feature",
                  "schema/showItemById_200.schema.yml",
                  "schema/showItemById_404.schema.yml",
                  "schema/showItemById_default.schema.yml")));
    }
  }

  @Nested
  class PathToParams {
    @ParameterizedTest(name = "PathToParams_{0}")
    @MethodSource
    void when_path_expect_params(final String type, final String path, final String expected) {

      final var result = KarateOperation.pathToParams(path);

      assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> when_path_expect_params() {
      return Stream.of(
          Arguments.of("WithoutParams", "/path/", "'/path/'"),
          Arguments.of("SingleParams", "/path/{itemId}", "'/path/', req.params.itemId"),
          Arguments.of("MultipleParams", "/path/{item1}/{item2}", "'/path/', req.params.item1, '/', req.params.item2"),
          Arguments.of("MultipleParamsAndText", "/path/{item1}/value/{item2}", "'/path/', req.params.item1, '/value/', req.params.item2"));
    }
  }
}
