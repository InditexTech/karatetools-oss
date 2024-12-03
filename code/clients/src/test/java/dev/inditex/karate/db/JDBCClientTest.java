package dev.inditex.karate.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractClientTest;
import dev.inditex.karate.logging.KarateClientLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;

public class JDBCClientTest extends AbstractClientTest {
  static final String JDBC_URL = "jdbc-url";

  static final String DRIVER_CLASS_NAME = "driver-class-name";

  static final String USERNAME = "username";

  static final String PASSWORD = "password";

  static final String HEALTH_QUERY = "health-query";

  @Nested
  class Constructor {
    @Test
    void when_valid_config_expect_fields_informed() {
      final Map<Object, Object> config = getValidConfig();

      final JDBCClient client = instantiateClient(config);

      assertThat(client.getJdbcURL()).isNotNull().isEqualTo(config.get(JDBC_URL));
      assertThat(client.getDriverClassName()).isNotNull().isEqualTo(config.get(DRIVER_CLASS_NAME));
      assertThat(client.getUser()).isNotNull().isEqualTo(config.get(USERNAME));
      assertThat(client.getPwd()).isNotNull().isEqualTo(config.get(PASSWORD));
      assertThat(client.getHealthQuery()).isNotNull().isEqualTo(config.get(HEALTH_QUERY));
    }
  }

  @Nested
  class Available {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void when_first_call_expect_driver_test(final Boolean available) throws SQLException {
      final Boolean rsNext = available;
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(client.getHealthQuery())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(rsNext);

        final var result = client.available();

        assertThat(result).isEqualTo(available);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("available(jdbc:driver://host:9999/instance)=" + available));
      }
    }

    @Test
    void when_driver_test_exception_expect_not_available() throws SQLException {
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(client.getHealthQuery())).thenReturn(resultSet);
        when(resultSet.next()).thenThrow(new RuntimeException("SQL"));

        final var result = client.available();

        assertThat(result).isFalse();
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("available(jdbc:driver://host:9999/instance) Exception=SQL"));
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("available(jdbc:driver://host:9999/instance)=false"));
      }
    }

    @Test
    void when_already_available_expect_no_driver_test() throws SQLException {
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(client.getHealthQuery())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        final var resultFirst = client.available();
        final var result = client.available();

        assertThat(resultFirst).isTrue();
        assertThat(result).isTrue();
        verify(resultSet, times(1)).next();
        assertThat(logWatcher.list).allMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("available(jdbc:driver://host:9999/instance)=true"));
      }
    }
  }

  @Nested
  class ExecuteQuery {
    @Test
    void when_query_no_columns_expect_empty() throws SQLException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String query = "SELECT * FROM TABLE";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(query)).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(0);

        final var result = client.executeQuery(query);

        assertThat(result).isNotNull().isEmpty();
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("executeQuery(SELECT * FROM TABLE)=#[0]"));
        assertThat(logWatcher.list).noneMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage().contains("executeQuery(SELECT * FROM TABLE)=#[0]"));
      }
    }

    @Test
    void when_query_no_results_expect_empty() throws SQLException {
      final String query = "SELECT * FROM TABLE";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(query)).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(2);
        when(resultSet.next()).thenReturn(false);

        final var result = client.executeQuery(query);

        assertThat(result).isNotNull().isEmpty();
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("executeQuery(SELECT * FROM TABLE)=#[0]"));
      }
    }

    @Test
    void when_query_has_results_expect_informed() throws SQLException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String query = "SELECT COLUMN_1, COLUMN_2 FROM TABLE ORDER BY COLUMN_1";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(query)).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(2);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        // Query Results
        when(metadata.getColumnLabel(1)).thenReturn("COLUMN_1");
        when(metadata.getColumnLabel(2)).thenReturn("COLUMN_2");
        when(resultSet.getObject(1)).thenReturn("value-1-1").thenReturn("value-2-1");
        when(resultSet.getObject(2)).thenReturn("value-1-2").thenReturn("value-2-2");
        final List<Map<String, Object>> expected = List.of(
            Map.of("COLUMN_1", "value-1-1", "COLUMN_2", "value-1-2"),
            Map.of("COLUMN_1", "value-2-1", "COLUMN_2", "value-2-2"));

        final var result = client.executeQuery(query);

        assertThat(result).isNotNull().isEqualTo(expected);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("executeQuery(SELECT COLUMN_1, COLUMN_2 FROM TABLE ORD...)=#[2]"));
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage().contains("executeQuery(SELECT COLUMN_1, COLUMN_2 FROM TABLE ORDER BY COLUMN_1)=#[2]"));
      }
    }

    @Test
    void when_query_exception_expect_exception() throws SQLException {
      final String query = "SELECT * FROM TABLE";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        doThrow(new SQLException("SQL")).when(statement).executeQuery(query);

        assertThatThrownBy(() -> {
          client.executeQuery(query);
        }).isInstanceOf(SQLException.class).hasMessageContaining("SQL");

        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("executeQuery(SELECT * FROM TABLE) SQLException=SQL"));
      }
    }
  }

  @Nested
  class ExecuteUpdate {
    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void when_update_rows_expect_row_count(final int recordsUpdated) throws SQLException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String query = "UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2'";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeUpdate(query)).thenReturn(recordsUpdated);

        final var result = client.executeUpdate(query);

        assertThat(result).isEqualTo(recordsUpdated);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("executeUpdate(UPDATE TABLE SET COLUMN1 = 'VALUE1', COL...)=#[" + recordsUpdated + "]"));
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage()
                .contains("executeUpdate(UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2')=#[" + recordsUpdated + "]"));
      }
    }

    @Test
    void when_update_exception_expect_exception() throws SQLException {
      final String query = "UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2'";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        doThrow(new SQLException("SQL")).when(statement).executeUpdate(query);

        assertThatThrownBy(() -> {
          client.executeUpdate(query);
        }).isInstanceOf(SQLException.class).hasMessageContaining("SQL");

        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage()
                .contains("executeUpdate(UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2') SQLException=SQL"));
      }
    }
  }

  @Nested
  class ExecuteUpdateScript {
    @Test
    void when_update_script_single_line_without_line_separator_expect_no_jdbc_call() throws SQLException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String query = "UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2'";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        final var result = client.executeUpdateScript(query);

        assertThat(result).isZero();
        verify(statement, never()).executeUpdate(query);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage()
                .contains(
                    "executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2') SKIPPING LINE (not ending with ;)"));
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1', COL...)=#[0]"));
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage().contains("executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2')=#[0]"));
      }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void when_update_script_single_line_without_line_separator_expect_row_count(final int recordsUpdated) throws SQLException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String query = "UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2';";
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeUpdate(query)).thenReturn(recordsUpdated);

        final var result = client.executeUpdateScript(query);

        assertThat(result).isEqualTo(recordsUpdated);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1', COL...)=#[" + recordsUpdated + "]"));
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage()
                .contains("executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1', COLUMN2 = 'VALUE2';)=#[" + recordsUpdated + "]"));
      }
    }

    @Test
    void when_update_script_multiline_expect_aggregated_row_count() throws SQLException {
      final String query1 = "UPDATE TABLE SET COLUMN1 = 'VALUE1'";
      final int recordsUpdated1 = 0;
      final String query2 = "UPDATE TABLE SET COLUMN1 = 'VALUE2';";
      final int recordsUpdated2 = 1;
      final String query3 = "UPDATE TABLE SET COLUMN1 = 'VALUE3'";
      final int recordsUpdated3 = 0;
      final String query4 = "UPDATE TABLE SET COLUMN1 = 'VALUE4';";
      final int recordsUpdated4 = 2;
      final String query = query1 + "\r\n" + " \r\n" + query2 + "\r\n" + query3 + "\r\n" + query4;
      final int expectedRecordsUpdated = recordsUpdated1 + recordsUpdated2 + recordsUpdated3 + recordsUpdated4;
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeUpdate(query2)).thenReturn(recordsUpdated2);
        when(statement.executeUpdate(query4)).thenReturn(recordsUpdated4);

        final var result = client.executeUpdateScript(query);

        assertThat(result).isEqualTo(expectedRecordsUpdated);
        verify(statement, never()).executeUpdate(query1);
        verify(statement, never()).executeUpdate(query3);
        verify(statement, times(1)).executeUpdate(query2);
        verify(statement, times(1)).executeUpdate(query4);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1'...)=#[" + expectedRecordsUpdated + "]"));
      }
    }

    @Test
    void when_update_script_multiline_ignoreSeparator_expect_aggregated_row_count() throws SQLException {
      final String query1 = "UPDATE TABLE SET COLUMN1 = 'VALUE1'";
      final int recordsUpdated1 = 0;
      final String query2 = "UPDATE TABLE SET COLUMN1 = 'VALUE2';";
      final int recordsUpdated2 = 1;
      final String query3 = "UPDATE TABLE SET COLUMN1 = 'VALUE3'";
      final int recordsUpdated3 = 0;
      final String query4 = "UPDATE TABLE SET COLUMN1 = 'VALUE4';";
      final int recordsUpdated4 = 2;
      final String query = query1 + "\r\n" + " \r\n" + query2 + "\r\n" + query3 + "\r\n" + query4;
      final int expectedRecordsUpdated = recordsUpdated1 + recordsUpdated2 + recordsUpdated3 + recordsUpdated4;
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeUpdate(query2.substring(0, query2.length() - 1))).thenReturn(recordsUpdated2);
        when(statement.executeUpdate(query4.substring(0, query4.length() - 1))).thenReturn(recordsUpdated4);

        final var result = client.executeUpdateScript(query, true);

        assertThat(result).isEqualTo(expectedRecordsUpdated);
        verify(statement, never()).executeUpdate(query1);
        verify(statement, never()).executeUpdate(query3);
        verify(statement, times(1)).executeUpdate(query2.substring(0, query2.length() - 1));
        verify(statement, times(1)).executeUpdate(query4.substring(0, query2.length() - 1));
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1'...)=#[" + expectedRecordsUpdated + "]"));
      }
    }

    @Test
    void when_update_script_exception_expect_exception() throws SQLException {
      final String query1 = "UPDATE TABLE SET COLUMN1 = 'VALUE1';";
      final String query2 = "UPDATE TABLE SET COLUMN1 = 'VALUE2';";
      final String query = query1 + "\r\n" + " \r\n" + query2;
      final Map<Object, Object> config = getValidConfig();
      final JDBCClient client = instantiateClient(config);
      try (final MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class)) {
        final Connection connection = mock(Connection.class);
        final Statement statement = mock(Statement.class);
        driverManager.when(() -> DriverManager.getConnection(
            client.getJdbcURL(), client.getUser(), client.getPwd())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        doThrow(new SQLException("SQL")).when(statement).executeUpdate(any());

        assertThatThrownBy(() -> {
          client.executeUpdateScript(query);
        }).isInstanceOf(SQLException.class).hasMessageContaining("SQL");

        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("executeUpdateScript(UPDATE TABLE SET COLUMN1 = 'VALUE1';...) SQLException=SQL"));
      }
    }
  }

  protected static Map<Object, Object> getValidConfig() {
    return Map.of(
        JDBC_URL, "jdbc:driver://host:9999/instance",
        DRIVER_CLASS_NAME, "java.sql.Driver",
        USERNAME, "user",
        PASSWORD, "pwd",
        HEALTH_QUERY, "SELECT 1");
  }

  protected JDBCClient instantiateClient(final Map<Object, Object> config) {
    return new JDBCClient(config);
  }

}
