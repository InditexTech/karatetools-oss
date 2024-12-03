package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Paths;
import java.util.stream.Stream;

import dev.inditex.karate.openapi.ExampleBasicApiMother;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KarateMockDataTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateMockData::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Save {
    @ParameterizedTest(name = "Save_MockData_{0}_{1}_{3}")
    @MethodSource
    void when_save_mock_data_expect_test_data_files(final String tag, final String operationId, final OperationPath operationPath,
        final String responseStatus, final String expected) {
      final OpenAPI openApi = OpenApiParser.parseOpenApi(getPathFromResources(ExampleBasicApiMother.getApiPath()));

      final var result = KarateMockData.save(Paths.get(targetFolder), operationPath, responseStatus, openApi);

      assertThat(result).isEqualTo(Paths.get(targetFolder, tag, expected));
      assertThat(result.toFile()).exists().hasContent(getResourceAsString("/openapi/unit/mocks/" + tag + "/" + expected));
    }

    public static Stream<Arguments> when_save_mock_data_expect_test_data_files() {
      return Stream.of(
          Arguments.of("BasicApi", ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation(),
              "201", "XXXX_createItems_201.yml"),
          Arguments.of("BasicApi", ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation(),
              "default", "XXXX_createItems_default.yml"),
          Arguments.of("BasicApi", ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(),
              "200", "XXXX_showItemById_200.yml"),
          Arguments.of("BasicApi", ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(),
              "404", "XXXX_showItemById_404.yml"),
          Arguments.of("BasicApi", ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation(),
              "default", "XXXX_showItemById_default.yml"),
          Arguments.of("BasicApi", ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation(),
              "200", "XXXX_listItems_200.yml"),
          Arguments.of("BasicApi", ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation(),
              "default", "XXXX_listItems_default.yml"));
    }

  }

}
