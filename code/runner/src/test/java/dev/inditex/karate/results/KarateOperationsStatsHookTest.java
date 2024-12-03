package dev.inditex.karate.results;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import dev.inditex.karate.AbstractKarateTest;
import dev.inditex.karate.results.KarateOperationsStatsHook.Stats;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.karate.Runner;
import com.intuit.karate.Suite;
import com.intuit.karate.core.FeatureRuntime;
import com.intuit.karate.core.Scenario;
import com.intuit.karate.core.Step;
import com.intuit.karate.core.StepResult;
import com.intuit.karate.core.Tag;
import com.intuit.karate.http.HttpClientFactory;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

public class KarateOperationsStatsHookTest extends AbstractKarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateOperationsStatsHook::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class AfterScenario {
    @ParameterizedTest(name = "StatsHook_AfterScenario_Single_{0}")
    @MethodSource("dev.inditex.karate.results.KarateOperationsStatsHookTest#getSingleScenarioArguments")
    void when_single_scenario_expect_stats(final String label, final String tag, final List<String> steps,
        final Map<String, Stats> expected, final List<String> expectedLogs) {
      final var featureRuntime = FeatureRuntime.forTempUse(HttpClientFactory.DEFAULT);
      final var scenarioRuntime = featureRuntime.scenarios.next();

      if (tag != null) {
        scenarioRuntime.scenario.setTags(List.of(new Tag(1, tag)));
      }
      steps.forEach(s -> scenarioRuntime.result.addFakeStepResult(s, null));
      final var hook = new KarateOperationsStatsHook();

      hook.afterScenario(scenarioRuntime);

      assertThat(hook.operationStats).isEqualTo(expected);
      expectedLogs.forEach(log -> assertThat(logWatcher.list).anyMatch(e -> e.getFormattedMessage().contains(log)));
    }

    @ParameterizedTest(name = "StatsHook_AfterScenario_Multiple_{0}")
    @MethodSource("dev.inditex.karate.results.KarateOperationsStatsHookTest#getMultipleScenarioArguments")
    void when_multiple_scenario_expect_stats(final String label, final String tag, final List<String> steps,
        final Map<String, Stats> expected, final List<String> expectedLogs) {
      final var featureRuntime = FeatureRuntime.forTempUse(HttpClientFactory.DEFAULT);
      final var scenarioRuntime = featureRuntime.scenarios.next();
      if (tag != null) {
        scenarioRuntime.scenario.setTags(List.of(new Tag(1, tag)));
      }
      steps.forEach(s -> scenarioRuntime.result.addFakeStepResult(s, null));
      final KarateOperationsStatsHook hook = new KarateOperationsStatsHook();

      hook.afterScenario(scenarioRuntime);
      hook.afterScenario(scenarioRuntime);

      assertThat(hook.operationStats).isEqualTo(expected);
      expectedLogs.forEach(log -> assertThat(logWatcher.list).anyMatch(e -> e.getFormattedMessage().contains(log)));
    }
  }

  @Nested
  class AfterSuite {
    @ParameterizedTest(name = "StatsHook_AfterSuite_Single_{0}")
    @MethodSource("dev.inditex.karate.results.KarateOperationsStatsHookTest#getSingleScenarioArguments")
    void when_single_scenario_expect_stats(final String label, final String tag, final List<String> steps,
        final Map<String, Stats> expected, final List<String> expectedLogs) throws IOException {
      final var featureRuntime = FeatureRuntime.forTempUse(HttpClientFactory.DEFAULT);
      final var scenarioRuntime = featureRuntime.scenarios.next();
      if (tag != null) {
        scenarioRuntime.scenario.setTags(List.of(new Tag(1, tag)));
      }
      steps.forEach(s -> scenarioRuntime.result.addFakeStepResult(s, null));
      final var suite = getSuite();
      final KarateOperationsStatsHook hook = new KarateOperationsStatsHook();
      hook.afterScenario(scenarioRuntime);

      hook.afterSuite(suite);

      assertThat(hook.operationStats).isEqualTo(expected);
      final var savedStats = getSavedStats();
      assertThat(savedStats).isEqualTo(expected);
      expectedLogs.forEach(log -> assertThat(logWatcher.list).anyMatch(e -> e.getFormattedMessage().contains(log)));
    }

    @ParameterizedTest(name = "StatsHook_AfterSuite_Multiple_{0}")
    @MethodSource("dev.inditex.karate.results.KarateOperationsStatsHookTest#getMultipleScenarioArguments")
    void when_multiple_scenario_expect_stats(final String label, final String tag, final List<String> steps,
        final Map<String, Stats> expected, final List<String> expectedLogs) throws IOException {
      final var featureRuntime = FeatureRuntime.forTempUse(HttpClientFactory.DEFAULT);
      final var scenarioRuntime = featureRuntime.scenarios.next();
      if (tag != null) {
        scenarioRuntime.scenario.setTags(List.of(new Tag(1, tag)));
      }
      steps.forEach(s -> scenarioRuntime.result.addFakeStepResult(s, null));
      final var suite = getSuite();
      final KarateOperationsStatsHook hook = new KarateOperationsStatsHook();
      hook.afterScenario(scenarioRuntime);
      hook.afterScenario(scenarioRuntime);

      hook.afterSuite(suite);

      assertThat(hook.operationStats).isEqualTo(expected);
      final var savedStats = getSavedStats();
      assertThat(savedStats).isEqualTo(expected);
      expectedLogs.forEach(log -> assertThat(logWatcher.list).anyMatch(e -> e.getFormattedMessage().contains(log)));
    }

    @Test
    void when_write_exception_expect_logged() {
      try (final MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
        fileUtils.when(() -> FileUtils.writeStringToFile(any(File.class), anyString(), any(Charset.class)))
            .thenThrow(new IOException("FileUtils.writeStringToFile"));

        final var featureRuntime = FeatureRuntime.forTempUse(HttpClientFactory.DEFAULT);
        final var scenarioRuntime = featureRuntime.scenarios.next();
        scenarioRuntime.scenario.setTags(List.of(new Tag(1, "@smoke")));

        final List<String> steps = List.of("def req = call utils.readTestData <testDataFile>",
            "def result = call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') req",
            "match result.responseStatus == <status>");
        steps.forEach(s -> scenarioRuntime.result.addFakeStepResult(s, null));
        final var suite = getSuite();
        final KarateOperationsStatsHook hook = new KarateOperationsStatsHook();
        hook.afterScenario(scenarioRuntime);
        hook.afterScenario(scenarioRuntime);

        hook.afterSuite(suite);

        assertThat(logWatcher.list).anyMatch(e -> e.getLevel().equals(Level.ERROR)
            && e.getFormattedMessage().contains(
                "KarateRunner.statsReport() saving Exception for [karate-operations-json.txt] => "
                    + "[java.io.IOException:FileUtils.writeStringToFile]"));
      }
    }
  }

  @Nested
  class GetOperation {
    @ParameterizedTest(name = "StatsHook_GetOperation_{0}")
    @MethodSource
    void when_text_expect_operation(final String label, final String text, final String expected) {
      final Step step = new Step(new Scenario(null, null, 0), -1);
      step.setText(text);

      final var operation = KarateOperationsStatsHook.getOperation(new StepResult(step, null));

      assertThat(operation).isEqualTo(expected);
    }

    private static Stream<Arguments> when_text_expect_operation() {
      return Stream.of(
          Arguments.of("null", null, null),
          Arguments.of("blank", "", null),
          Arguments.of("auth", "def authHeader = call read('classpath:karate-auth.js') req.auth'", null),
          Arguments.of("smoke_data", "def req = call utils.readTestData 'test-data/listItems_200.yml'", null),
          Arguments.of("smoke", "def result = "
              + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') req",
              "dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems"),
          Arguments.of("func_data", "def listItemsRequest = read('test-data/listItems_200.yml')", null),
          Arguments.of("func", "def listItemsResponse = "
              + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') listItemsRequest",
              "dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems"));
    }
  }

  static Stream<Arguments> getSingleScenarioArguments() {
    return Stream.of(
        Arguments.of("no-tags",
            null,
            List.of("def req = call utils.readTestData <testDataFile>",
                "def result = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') req",
                "match result.responseStatus == <status>"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(0, 1, 1)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=1, calls=1)]")),
        Arguments.of("smoke",
            "@smoke",
            List.of("def req = call utils.readTestData <testDataFile>",
                "def result = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') req",
                "match result.responseStatus == <status>"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(1, 0, 1)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=1, functional=0, calls=1)]")),
        Arguments.of("functional_single",
            "@functional",
            List.of(
                "def createItemsRequest = read('test-data/createItems_201.yml')",
                "def createItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') "
                    + "createItemsRequest",
                "match createItemsResponse.responseStatus == 201",
                "def showItemByIdRequest = read('test-data/showItemById_200.yml')",
                "def showItemByIdResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/showItemById/showItemById.feature') "
                    + "showItemByIdRequest",
                "match showItemByIdResponse.responseStatus == 200",
                "def listItemsRequest = read('test-data/listItems_200.yml')",
                "def listItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') "
                    + "listItemsRequest",
                "match listItemsResponse.responseStatus == 200"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(0, 1, 1),
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems", new Stats(0, 1, 1),
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/showItemById/showItemById", new Stats(0, 1, 1)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=1, calls=1)]",
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=1, calls=1)]",
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/showItemById/showItemById]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=1, calls=1)]")),
        Arguments.of("functional_multiple",
            "@functional",
            List.of(
                "def createItemsRequest = read('test-data/createItems_201.yml')",
                "def createItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') "
                    + "createItemsRequest",
                "match createItemsResponse.responseStatus == 201",
                "def createItemsRequest = read('test-data/createItems_201.yml')",
                "def createItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') "
                    + "createItemsRequest",
                "match createItemsResponse.responseStatus == 201"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(0, 1, 2)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=1, calls=2)]")));
  }

  static Stream<Arguments> getMultipleScenarioArguments() {
    return Stream.of(
        Arguments.of("no-tags",
            null,
            List.of("def req = call utils.readTestData <testDataFile>",
                "def result = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') req",
                "match result.responseStatus == <status>"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(0, 2, 2)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=2, calls=2)]")),
        Arguments.of("smoke",
            "@smoke",
            List.of("def req = call utils.readTestData <testDataFile>",
                "def result = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') req",
                "match result.responseStatus == <status>"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(2, 0, 2)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=2, functional=0, calls=2)]")),
        Arguments.of("functional_single",
            "@functional",
            List.of(
                "def createItemsRequest = read('test-data/createItems_201.yml')",
                "def createItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') "
                    + "createItemsRequest",
                "match createItemsResponse.responseStatus == 201",
                "def showItemByIdRequest = read('test-data/showItemById_200.yml')",
                "def showItemByIdResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/showItemById/showItemById.feature') "
                    + "showItemByIdRequest",
                "match showItemByIdResponse.responseStatus == 200",
                "def listItemsRequest = read('test-data/listItems_200.yml')",
                "def listItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') "
                    + "listItemsRequest",
                "match listItemsResponse.responseStatus == 200"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(0, 2, 2),
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems", new Stats(0, 2, 2),
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/showItemById/showItemById", new Stats(0, 2, 2)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=2, calls=2)]",
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/listItems/listItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=2, calls=2)]",
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/showItemById/showItemById]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=2, calls=2)]")),
        Arguments.of("functional_multiple",
            "@functional",
            List.of(
                "def createItemsRequest = read('test-data/createItems_201.yml')",
                "def createItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') "
                    + "createItemsRequest",
                "match createItemsResponse.responseStatus == 201",
                "def createItemsRequest = read('test-data/createItems_201.yml')",
                "def createItemsResponse = "
                    + "call read('classpath:apis/dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') "
                    + "createItemsRequest",
                "match createItemsResponse.responseStatus == 201"),
            Map.of(
                "dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems", new Stats(0, 2, 4)),
            List.of(
                "KarateRunner.stats[][dev/inditex/api/xxx-api-rest-stable/BasicApi/createItems/createItems]="
                    + "[KarateOperationsStatsHook.Stats(smoke=0, functional=2, calls=4)]")));
  }

  protected Map<String, Stats> getSavedStats() throws IOException {
    final File file = new File(operationStatsFile);
    final ObjectMapper mapper = new ObjectMapper();
    final TypeReference<Map<String, Stats>> typeRef = new TypeReference<Map<String, Stats>>() {};
    return mapper.readValue(file, typeRef);
  }

  @SuppressWarnings("rawtypes")
  protected Suite getSuite() {
    final String path = "classpath:scenarios/karate-stats";
    final Runner.Builder runner =
        Runner.builder().path(path)
            .outputCucumberJson(true)
            .outputJunitXml(true)
            .outputHtmlReport(true);
    return new Suite(runner);
  }
}
