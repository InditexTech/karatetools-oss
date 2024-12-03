package dev.inditex.karate.scenarios;

import static org.assertj.core.api.Assertions.assertThat;

import dev.inditex.karate.docker.DockerComposeTestConfiguration;

import com.intuit.karate.Constants;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("IT")
@Tag("ITScenarios")
@ActiveProfiles({"test-docker"})
@SpringBootTest(classes = {
    DockerComposeTestConfiguration.class
})
public class KarateScenariosRunnerIT {
  @Nested
  class KarateClients {

    @Test
    void when_scenarios_executed_expect_no_failures() {
      System.setProperty(Constants.KARATE_OPTIONS, getKarateClientsOptions());
      final Results results = Runner.path("classpath:scenarios")
          .karateEnv("local")
          .outputHtmlReport(true)
          .outputCucumberJson(true)
          .outputJunitXml(true)
          .parallel(1);

      assertThat(results).isNotNull();
      assertThat(results.getFailCount()).isZero();
    }
  }

  protected String getKarateClientsOptions() {
    return "-t ~@ignore -t @inditex-oss-karate -t @karate-clients";
  }

}
