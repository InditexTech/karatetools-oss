package dev.inditex.karate.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractKarateTest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IT")
public class JWTGeneratorIT extends AbstractKarateTest {

  @Nested
  class KarateScenarios {
    @Test
    void when_jwt_scenarios_expect_no_failures() throws IOException {
      final KarateExecutionTest test = new KarateExecutionTest(
          // karate environment
          "local",
          // karate options
          getKarateOptions(),
          // featuresCount
          1,
          // scenariosCount
          2,
          // surefireFiles
          List.of(
              "/TEST-scenarios.jwt.generate-jwt.xml"),
          // stats
          Map.of(),
          // logs
          List.of(
              "Running Feature[ generate-jwt ] Scenario [ generate-jwt ] ... ",
              "Running Feature[ generate-jwt ] Scenario [ generate-jwt from default jwt file ] ... "));

      final var result = executeScenarios(test);

      assertThat(result).isZero();
    }
  }

  protected String getKarateOptions() {
    return "-t ~@ignore -t ~@mock.templates.standalone -t @inditex-oss-karate -t @generate-jwt";
  }

}
