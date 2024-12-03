package dev.inditex.karate.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.KarateTestUtils;

public class AbstractJDBCClientIT {

  protected List<Map<String, Object>> testInstance(final String jdbcConfig, final String script, final boolean ignoreScriptSeparator,
      final int expectedScriptResult, final String update, final int expectedUpdateResult, final String query)
      throws SQLException, IOException {
    final Map<Object, Object> config = KarateTestUtils.readYaml(jdbcConfig);
    final JDBCClient client = instantiateClient(config);

    // available
    final var available = client.available();
    // executeUpdateScript
    final var scriptResult = client.executeUpdateScript(script, ignoreScriptSeparator);
    // executeUpdate
    final var updateResult = client.executeUpdate(update);
    // executeQuery
    final var queryResult = client.executeQuery(query);

    assertThat(available).isTrue();
    assertThat(scriptResult).isEqualTo(expectedScriptResult);
    assertThat(updateResult).isEqualTo(expectedUpdateResult);
    return queryResult;
  }

  protected List<Map<String, Object>> testInstance(final String jdbcConfig, final String script,
      final int expectedScriptResult, final String update, final int expectedUpdateResult, final String query)
      throws SQLException, IOException {
    return testInstance(jdbcConfig, script, false, expectedScriptResult, update, expectedUpdateResult, query);
  }

  protected JDBCClient instantiateClient(final Map<Object, Object> config) {
    return new JDBCClient(config);
  }
}
