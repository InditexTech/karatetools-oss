package dev.inditex.karate.results;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.karate.RuntimeHook;
import com.intuit.karate.Suite;
import com.intuit.karate.core.Scenario;
import com.intuit.karate.core.ScenarioRuntime;
import com.intuit.karate.core.StepResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

/**
 * The Class KarateOperationsStatsHook.
 */
@Slf4j
public class KarateOperationsStatsHook implements RuntimeHook {

  /** The Constant OPERATION_START. */
  public static final String OPERATION_START = "call read('classpath:apis/";

  /** The Constant OPERATION_END. */
  public static final String OPERATION_END = ".feature')";

  /** The Constant KARATE_OPERATIONS_FILE. */
  public static final String KARATE_OPERATIONS_FILE = "karate-operations-json.txt";

  /** The operation stats. */
  protected Map<String, Stats> operationStats = new ConcurrentHashMap<>();

  /**
   * After scenario.
   *
   * @param runtime the runtime
   */
  @Override
  public void afterScenario(final ScenarioRuntime runtime) {
    log.debug("KarateRunner.stats[{}]afterScenario", runtime.scenario.getName());
    try {
      // Get unique operations to count smoke and functional
      final List<String> scenarioUniqueOperations = runtime.result.getStepResults().stream().map(KarateOperationsStatsHook::getOperation)
          .filter(Objects::nonNull).distinct().toList();
      final boolean isSmoke = isSmoke(runtime.scenario);
      scenarioUniqueOperations.forEach(operation -> operationStats.compute(operation, (k, v) -> {
        if (v == null) {
          if (isSmoke) {
            return new Stats(1, 0, 0);
          }
          return new Stats(0, 1, 0);
        }
        if (isSmoke) {
          v.setSmoke(v.getSmoke() + 1);
        } else {
          v.setFunctional(v.getFunctional() + 1);
        }
        return v;
      }));
      // Get all operations to count calls
      final List<String> operations = runtime.result.getStepResults().stream().map(KarateOperationsStatsHook::getOperation)
          .filter(Objects::nonNull).toList();
      operations.forEach(operation -> operationStats.compute(operation, (k, v) -> {
        v.setCalls(v.getCalls() + 1);
        log.info("KarateRunner.stats[{}][{}]=[{}]", runtime.scenario.getName(), operation, operationStats.get(operation));
        return v;
      }));
    } catch (final RuntimeException e) {
      log.warn("KarateRunner.stats afterScenario Exception [{}]", e);
    }
  }

  /**
   * After suite.
   *
   * @param suite the suite
   */
  @Override
  public void afterSuite(final Suite suite) {
    log.debug("KarateRunner.statsReport() saving ...");
    try {
      final ObjectMapper objectMapper = new ObjectMapper();
      final File file = new File(suite.reportDir + File.separator + KARATE_OPERATIONS_FILE);
      FileUtils.writeStringToFile(file, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(operationStats),
          Charset.defaultCharset());
      log.info("KarateRunner.statsReport() saved [{}]", file.getAbsolutePath());
    } catch (final IOException e) {
      log.error("KarateRunner.statsReport() saving Exception for [{}] => [{}:{}]",
          KARATE_OPERATIONS_FILE, e.getClass().getName(), e.getMessage());
    }
  }

  /**
   * Gets the operation.
   *
   * @param result the result
   * @return the operation
   */
  protected static String getOperation(final StepResult result) {
    String operation = null;
    final var step = result.getStep().getText();
    if (step != null && step.contains(OPERATION_START) && step.contains(OPERATION_END)) {
      // Log calls to apis features
      // Step example:
      // def xxxResponse = call read('classpath:apis/dev/inditex/api/xxxx-api-rest-stable/tag/op/op.feature') ...
      // Expected operation = dev/inditex/api/xxxx-api-rest-stable/tag/op/op
      operation = step.substring(step.indexOf(OPERATION_START), step.indexOf(OPERATION_END))
          .replace(OPERATION_START, "");
    }
    return operation;
  }

  /**
   * Checks if is smoke.
   *
   * @param scenario the scenario
   * @return true, if is smoke
   */
  private static boolean isSmoke(final Scenario scenario) {
    boolean isSmoke = false;
    if (scenario.getTagsEffective() != null) {
      isSmoke = scenario.getTagsEffective().getTags().stream().anyMatch(t -> t.equals("smoke"));
    }
    if (!isSmoke && scenario.getTags() != null) {
      isSmoke = scenario.getTags().stream().anyMatch(t -> t.getName().equals("smoke"));
    }
    return isSmoke;
  }

  /**
   * The Class Stats.
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Stats {

    /** The smoke. */
    private int smoke;

    /** The functional. */
    private int functional;

    /** The calls. */
    private int calls;
  }
}
