package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import dev.inditex.karate.openapi.ExampleBasicApiMother;
import dev.inditex.karate.openapi.data.KarateSmokeFeature.SmokeTestResponse;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KarateSmokeFeatureTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateSmokeFeature::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class ProcessTemplate {
    @ParameterizedTest(name = "ProcessTemplate_Smoke_{0}")
    @MethodSource
    void when_process_smoke_template_expect_karate_feature(final String operationId, final Operation operation,
        final String operationFeatureClassPath, final List<SmokeTestResponse> responses, final String expected) {

      final var result = KarateSmokeFeature.processTemplate(operation, operationFeatureClassPath, responses);

      assertThat(result).isEqualToNormalizingNewlines(getResourceAsString("/openapi/unit/smoke/" + expected));
    }

    public static Stream<Arguments> when_process_smoke_template_expect_karate_feature() {
      return Stream.of(
          Arguments.of(
              ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation().operation(),
              "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
              List.of(new SmokeTestResponse("201", ""), new SmokeTestResponse("400", ""), new SmokeTestResponse("default", "")),
              "BasicApi/createItems/createItems.feature"),
          Arguments.of(
              ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
              List.of(new SmokeTestResponse("200", ""), new SmokeTestResponse("404", ""), new SmokeTestResponse("default", "")),
              "BasicApi/showItemById/showItemById.feature"),
          Arguments.of(
              ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation().operation(),
              "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
              List.of(new SmokeTestResponse("200", ""), new SmokeTestResponse("400", ""), new SmokeTestResponse("default", "")),
              "BasicApi/listItems/listItems.feature"));
    }
  }

  @Nested
  class Save {
    @ParameterizedTest(name = "Save_Smoke_{0}")
    @MethodSource
    void when_save_smoke_test_expect_feature_and_data(final String operationId, final Operation operation,
        final String operationFeatureClassPath, final List<SmokeTestResponse> responses, final List<String> expectedFiles) {
      final OpenAPI openApi = OpenApiParser.parseOpenApi(getPathFromResources(ExampleBasicApiMother.getApiPath()));

      final var result = KarateSmokeFeature.save(Paths.get(targetFolder), operation, operationFeatureClassPath, responses, openApi);

      assertThat(result).hasSameSizeAs(expectedFiles);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i)).isEqualTo(Paths.get(targetFolder, expectedFiles.get(i)));
      }
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i).toFile()).exists()
            .hasContent(getResourceAsString("/openapi/unit/smoke/" + expectedFiles.get(i)));
      }
    }

    public static Stream<Arguments> when_save_smoke_test_expect_feature_and_data() {

      return Stream.of(
          Arguments.of(
              ExampleBasicApiMother.getCreateItemsOperationId(), ExampleBasicApiMother.getCreateItemsOperation().operation(),
              "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
              List.of(
                  new SmokeTestResponse("201",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
                  new SmokeTestResponse("400",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_400.schema.yml"),
                  new SmokeTestResponse("default",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_default.schema.yml")),
              List.of(
                  "BasicApi/createItems/createItems.feature",
                  "BasicApi/createItems/test-data/createItems_201.yml",
                  "BasicApi/createItems/test-data/createItems_400.yml",
                  "BasicApi/createItems/test-data/createItems_default.yml")),
          Arguments.of(
              ExampleBasicApiMother.getShowItemByIdOperationId(), ExampleBasicApiMother.getShowItemByIdOperation().operation(),
              "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
              List.of(
                  new SmokeTestResponse("200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml"),
                  new SmokeTestResponse("404",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_404.schema.yml"),
                  new SmokeTestResponse("default",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_default.schema.yml")),
              List.of(
                  "BasicApi/showItemById/showItemById.feature",
                  "BasicApi/showItemById/test-data/showItemById_200.yml",
                  "BasicApi/showItemById/test-data/showItemById_404.yml",
                  "BasicApi/showItemById/test-data/showItemById_default.yml")),
          Arguments.of(
              ExampleBasicApiMother.getListItemsOperationId(), ExampleBasicApiMother.getListItemsOperation().operation(),
              "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
              List.of(
                  new SmokeTestResponse("200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml"),
                  new SmokeTestResponse("400",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_400.schema.yml"),
                  new SmokeTestResponse("default",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_default.schema.yml")),
              List.of(
                  "BasicApi/listItems/listItems.feature",
                  "BasicApi/listItems/test-data/listItems_200.yml",
                  "BasicApi/listItems/test-data/listItems_400.yml",
                  "BasicApi/listItems/test-data/listItems_default.yml")));
    }
  }
}
