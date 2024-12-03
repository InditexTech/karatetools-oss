package dev.inditex.karate.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.docker.DockerComposeTestConfiguration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("IT")
@ActiveProfiles({"test-docker"})
@SpringBootTest(classes = {
    DockerComposeTestConfiguration.class
})
public class JDBCClientPostgreSQLIT extends AbstractJDBCClientIT {

  static final String CONFIG_FILE = "classpath:config/db/postgresql-config.yml";

  @Nested
  class PostgreSQL {
    @Test
    void when_jdbc_expect_results() throws SQLException, IOException {
      final String script =
          """
              TRUNCATE TABLE DATA;
              INSERT INTO DATA (NAME, VALUE) VALUES ('karate-01', 1);
              INSERT INTO DATA (NAME, VALUE) VALUES ('karate-02', 2);
              INSERT INTO DATA (NAME, VALUE) VALUES ('karate-03', 3);
              """;
      final int expectedScriptResult = 3;
      final String update = "DELETE FROM DATA WHERE NAME = 'karate-03';";
      final int expectedUpdateResult = 1;
      final String query = "SELECT ID, NAME, VALUE FROM DATA ORDER BY VALUE ASC;";
      final List<Map<String, Object>> expected = List.of(
          Map.of("id", "<internal>", "name", "karate-01", "value", 1),
          Map.of("id", "<internal>", "name", "karate-02", "value", 2));

      final var result = testInstance(CONFIG_FILE, script, expectedScriptResult, update, expectedUpdateResult, query);

      assertThat(result).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id").isEqualTo(expected);
    }
  }
}
