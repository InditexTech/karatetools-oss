package dev.inditex.karate.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;
import dev.inditex.karate.results.KarateOperationsStatsHook.Stats;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class KarateAuthTest extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_auth_scenarios_expect_no_failures() throws IOException {
      final KarateExecutionTest test = getKarateExecutionTest();

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected KarateExecutionTest getKarateExecutionTest() {
    return new KarateExecutionTest(
        // karate environment
        "local",
        // karate options
        getKarateOptions(),
        // featuresCount
        2,
        // scenariosCount
        13,
        // surefireFiles
        List.of(
            "/TEST-scenarios.auth.auth-basic.xml",
            "/TEST-scenarios.auth.auth-jwt.xml"),
        // stats
        Map.of("package/tag/ops/op-with-auth", new Stats(0, 2, 2)),
        // logs
        List.of(
            "Running Feature[ karate-auth-basic ] Scenario [ karate-auth-basic-invalid-mode ] ... ",
            "Running Feature[ karate-auth-basic ] Scenario [ karate-auth-basic-invalid-username ] ... ",
            "Running Feature[ karate-auth-basic ] Scenario [ karate-auth-basic-invalid-password ] ... ",
            "Running Feature[ karate-auth-basic ] Scenario [ karate-auth-basic-local ] ... ",
            "Running Feature[ karate-auth-basic ] Scenario [ karate-auth-basic-local-injected-password ] ... ",
            "Running Feature[ karate-auth-basic ] Scenario [ karate-auth-basic-local-cache ] ... ",
            "Running Feature[ karate-auth-basic ] Scenario [ karate-auth-basic-local-operation ] ... ",

            "Running Feature[ karate-auth-jwt ] Scenario [ karate-auth-jwt-invalid-mode ] ... ",
            "Running Feature[ karate-auth-jwt ] Scenario [ karate-auth-jwt-invalid-username ] ... ",
            "Running Feature[ karate-auth-jwt ] Scenario [ karate-auth-jwt-local ] ... ",
            "Running Feature[ karate-auth-jwt ] Scenario [ karate-auth-jwt-local-invalid-jwt-algorithm ] ... ",
            "Running Feature[ karate-auth-jwt ] Scenario [ karate-auth-jwt-local-cache ] ... ",
            "Running Feature[ karate-auth-jwt ] Scenario [ karate-auth-jwt-local-operation ] ... "));
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @karate-auth";
  }
}
