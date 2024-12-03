package dev.inditex.karate.it.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import dev.inditex.karate.results.KarateReportsGenerator;
import dev.inditex.karate.test.KarateRunner;

import com.intuit.karate.Results;
import org.junit.jupiter.api.Test;

class KarateRunnerTest extends KarateRunner {

  @Test
  void run() {
    final Results results = super.execute();

    KarateReportsGenerator.generate(results);

    assertThat(results).isNotNull();
    assertThat(results.getFailCount()).as("Karate Fail Count [%s]", results.getScenariosFailed()).isZero();
  }
}
