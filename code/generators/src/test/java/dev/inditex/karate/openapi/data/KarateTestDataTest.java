package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Paths;
import java.util.stream.Stream;

import dev.inditex.karate.openapi.ExampleBasicApiMother;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KarateTestDataTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateTestData::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Save {
    @ParameterizedTest(name = "Save_TestData_{0}_{2}")
    @MethodSource
    void when_save_smoke_test_expect_feature_and_data(final String operationId, final Operation operation, final String statusCode,
        final String responseSchemaClassPath, final String expected) {
      final OpenAPI openApi = OpenApiParser.parseOpenApi(getPathFromResources(ExampleBasicApiMother.getApiPath()));

      final var result = KarateTestData.save(Paths.get(targetFolder), operation, statusCode, responseSchemaClassPath, openApi);

      assertThat(result).isEqualTo(Paths.get(targetFolder, expected));
      assertThat(result.toFile()).exists().hasContent(getResourceAsString("/openapi/unit/data/" + expected));
    }

    public static Stream<Arguments> when_save_smoke_test_expect_feature_and_data() {
      return Stream.of(
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation().operation(),
              "201", "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml",
              "test-data/createItems_201.yml"),
          Arguments.of(ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation().operation(),
              "default", "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_default.schema.yml",
              "test-data/createItems_default.yml"),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "200", "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml",
              "test-data/showItemById_200.yml"),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "404", "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_404.schema.yml",
              "test-data/showItemById_404.yml"),
          Arguments.of(ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "default", "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_default.schema.yml",
              "test-data/showItemById_default.yml"),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation().operation(),
              "200", "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml",
              "test-data/listItems_200.yml"),
          Arguments.of(ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation().operation(),
              "default", "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_default.schema.yml",
              "test-data/listItems_default.yml"));
    }
  }
}
