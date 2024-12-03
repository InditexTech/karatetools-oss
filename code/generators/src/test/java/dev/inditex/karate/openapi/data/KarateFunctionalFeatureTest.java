package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import dev.inditex.karate.openapi.ExampleBasicApiMother;
import dev.inditex.karate.openapi.data.KarateFunctionalFeature.FunctionalTestStep;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KarateFunctionalFeatureTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateFunctionalFeature::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class ProcessTemplate {
    @ParameterizedTest(name = "ProcessTemplate_Functional_{0}")
    @MethodSource
    void when_process_functional_template_expect_karate_feature(final String testName, final Boolean inlineMocks,
        final List<FunctionalTestStep> steps) {

      final String unitFolder = inlineMocks ? "mocks-inline" : "functional";
      final var result = KarateFunctionalFeature.processTemplate(testName, inlineMocks, steps);

      assertThat(result).isEqualToIgnoringNewLines(
          getResourceAsString("/openapi/unit/" + unitFolder + "/" + testName + "/" + testName + ".feature"));
    }

    public static Stream<Arguments> when_process_functional_template_expect_karate_feature() {
      return Stream.of(
          Arguments.of("CreateItem", false,
              List.of(new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"))),
          Arguments.of(
              "CreateAndShowItem", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml"))),
          Arguments.of(
              "CreateShowAndListItems", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getListItemsOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml"))),

          // Inline Mocks
          Arguments.of("createItemsWithInlineMocks", true,
              List.of(new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"))),

          // Multiple Functional tests in the same feature file for different return codes
          Arguments.of("createItemsWithMultipleCodes", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "400",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_400.schema.yml"))),
          Arguments.of("showItemByIdWithMultipleCodes", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "404",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_404.schema.yml"))),
          Arguments.of("listItemsWithMultipleCodes", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getListItemsOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getListItemsOperation().operation(), "400",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_400.schema.yml")))

      );
    }
  }

  @Nested
  class Save {
    @ParameterizedTest(name = "Save_Functional_{0}")
    @MethodSource
    void when_save_functional_test_expect_feature_and_data(final String testName, final Boolean inlineMocks,
        final List<FunctionalTestStep> steps,
        final List<String> expectedFiles) {
      final OpenAPI openApi = OpenApiParser.parseOpenApi(getPathFromResources(ExampleBasicApiMother.getApiPath()));
      final String unitFolder = inlineMocks ? "mocks-inline" : "functional";

      final var result = KarateFunctionalFeature.save(Paths.get(targetFolder), testName, inlineMocks, steps, openApi);

      assertThat(result).hasSameSizeAs(expectedFiles);
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i)).isEqualTo(Paths.get(targetFolder, expectedFiles.get(i)));
      }
      for (int i = 0; i < result.size(); i++) {
        assertThat(result.get(i).toFile()).exists()
            .hasContent(getResourceAsString("/openapi/unit/" + unitFolder + "/" + expectedFiles.get(i)));
      }
    }

    public static Stream<Arguments> when_save_functional_test_expect_feature_and_data() {
      return Stream.of(
          Arguments.of(
              "CreateItem", false,
              List.of(new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml")),
              List.of(
                  "CreateItem/CreateItem.feature",
                  "CreateItem/test-data/createItems_201.yml")),
          Arguments.of(
              "CreateAndShowItem", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml")),
              List.of(
                  "CreateAndShowItem/CreateAndShowItem.feature",
                  "CreateAndShowItem/test-data/createItems_201.yml",
                  "CreateAndShowItem/test-data/showItemById_200.yml")),
          Arguments.of(
              "CreateShowAndListItems", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getListItemsOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml")),
              List.of(
                  "CreateShowAndListItems/CreateShowAndListItems.feature",
                  "CreateShowAndListItems/test-data/createItems_201.yml",
                  "CreateShowAndListItems/test-data/showItemById_200.yml",
                  "CreateShowAndListItems/test-data/listItems_200.yml")),
          // Inline Mocks
          Arguments.of(
              "createItemsWithInlineMocks", true,
              List.of(new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                  "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml")),
              List.of(
                  "createItemsWithInlineMocks/createItemsWithInlineMocks.feature",
                  "createItemsWithInlineMocks/test-data/createItems_201.yml")),

          // Multiple Functional tests in the same feature file for different return codes
          Arguments.of(
              "createItemsWithMultipleCodes", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "201",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_201.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getCreateItemsOperation().operation(), "400",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/createItems/schema/createItems_400.schema.yml")),
              List.of(
                  "createItemsWithMultipleCodes/createItemsWithMultipleCodes.feature",
                  "createItemsWithMultipleCodes/test-data/createItems_201.yml",
                  "createItemsWithMultipleCodes/test-data/createItems_400.yml")),
          Arguments.of(
              "showItemByIdWithMultipleCodes", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_200.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getShowItemByIdOperation().operation(), "404",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/schema/showItemById_404.schema.yml")),
              List.of(
                  "showItemByIdWithMultipleCodes/showItemByIdWithMultipleCodes.feature",
                  "showItemByIdWithMultipleCodes/test-data/showItemById_200.yml",
                  "showItemByIdWithMultipleCodes/test-data/showItemById_404.yml")),
          Arguments.of(
              "listItemsWithMultipleCodes", false,
              List.of(
                  new FunctionalTestStep(ExampleBasicApiMother.getListItemsOperation().operation(), "200",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_200.schema.yml"),
                  new FunctionalTestStep(ExampleBasicApiMother.getListItemsOperation().operation(), "400",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature",
                      "apis/dev/inditex/karate/openapi/test/BasicApi/listItems/schema/listItems_400.schema.yml")),
              List.of(
                  "listItemsWithMultipleCodes/listItemsWithMultipleCodes.feature",
                  "listItemsWithMultipleCodes/test-data/listItems_200.yml",
                  "listItemsWithMultipleCodes/test-data/listItems_400.yml")));

    }
  }
}
