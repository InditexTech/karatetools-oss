package dev.inditex.karate.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import dev.inditex.karate.logging.KarateClientLogger;
import dev.inditex.karate.parser.SystemPropertiesParser;

import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.event.Level;

/**
 * The Class JDBCClient.
 */
@Getter(AccessLevel.PROTECTED)
public class JDBCClient {

  /** The Constant JDBC_URL. */
  public static final String JDBC_URL = "jdbc-url";

  /** The Constant DRIVER_CLASS_NAME. */
  public static final String DRIVER_CLASS_NAME = "driver-class-name";

  /** The Constant USERNAME. */
  public static final String USERNAME = "username";

  /** The Constant PASSWORD. */
  public static final String PASSWORD = "password";

  /** The Constant HEALTH_QUERY. */
  public static final String HEALTH_QUERY = "health-query";

  /** The Constant SQL_TRIM_LENGTH. */
  protected static final int SQL_TRIM_LENGTH = 40;

  /** The is DB available. */
  protected Boolean isDBAvailable;

  /** The jdbc URL. */
  protected final String jdbcURL;

  /** The driver class name. */
  protected final String driverClassName;

  /** The user. */
  protected final String user;

  /** The pwd. */
  protected final String pwd;

  /** The health query. */
  protected final String healthQuery;

  /** The log. */
  protected final KarateClientLogger log = new KarateClientLogger();

  /**
   * Instantiates a new JDBC client.
   *
   * @param configMap the config map
   */
  public JDBCClient(final Map<Object, Object> configMap) {
    super();
    final Map<Object, Object> config = SystemPropertiesParser.parseConfiguration(configMap);

    jdbcURL = String.valueOf(config.get(JDBC_URL));
    driverClassName = String.valueOf(config.get(DRIVER_CLASS_NAME));
    user = String.valueOf(config.get(USERNAME));
    pwd = String.valueOf(config.get(PASSWORD));
    healthQuery = String.valueOf(config.get(HEALTH_QUERY));
  }

  /**
   * Available.
   *
   * @return the boolean
   */
  public Boolean available() {
    log.debug("available() ... ");
    if (isDBAvailable == null) {
      try {
        Class.forName(driverClassName);
        try (final Connection con = DriverManager.getConnection(jdbcURL, user, pwd);
            final Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery(healthQuery)) {
          if (rs.next()) {
            isDBAvailable = true;
          } else {
            isDBAvailable = false;
          }
          log.debug("available({}) {}", jdbcURL, isDBAvailable);
        }
      } catch (final Exception e) {
        log.error("available({}) Exception={}", jdbcURL, e.getMessage());
        isDBAvailable = false;
      }
    }
    log.info("available({})={}", jdbcURL, isDBAvailable);
    return isDBAvailable;
  }

  /**
   * Execute query.
   *
   * @param sql the sql
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<Map<String, Object>> executeQuery(final String sql) throws SQLException {
    log.debug("executeQuery({})", sql);
    final List<Map<String, Object>> results = new ArrayList<>();
    try (final Connection con = DriverManager.getConnection(jdbcURL, user, pwd);
        final Statement stmt = con.createStatement();
        final ResultSet rs = stmt.executeQuery(sql)) {
      final ResultSetMetaData metadata = rs.getMetaData();
      log.debug("executeQuery({})=Metadata[{}]", sql, metadata);
      if (metadata.getColumnCount() != 0) {
        while (rs.next()) {
          final Map<String, Object> result = new HashMap<>();
          for (int i = 1; i <= metadata.getColumnCount(); i++) {
            result.put(metadata.getColumnLabel(i), rs.getObject(i));
          }
          results.add(result);
        }
      }
    } catch (final SQLException e) {
      log.error("executeQuery({}) SQLException={}", sql, e.getMessage());
      throw e;
    }

    log.info("executeQuery({})=#[{}]", trimmed(sql), results.size());
    if (needsTrim(sql) && log.isEnabledForLevel(Level.DEBUG)) {
      log.debug("executeQuery({})=#[{}]", sql, results.size());
    }
    return results;
  }

  /**
   * Execute update.
   *
   * @param sql the sql
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int executeUpdate(final String sql) throws SQLException {
    log.debug("executeUpdate({})", sql);
    int result = -1;
    try (final Connection con = DriverManager.getConnection(jdbcURL, user, pwd);
        final Statement stmt = con.createStatement()) {
      try {
        result = stmt.executeUpdate(sql);
      } catch (final SQLException e) {
        log.error("executeUpdate({}) SQLException={}", sql, e.getMessage());
        throw e;
      }
    }
    log.info("executeUpdate({})=#[{}]", trimmed(sql), result);
    if (needsTrim(sql) && log.isEnabledForLevel(Level.DEBUG)) {
      log.debug("executeUpdate({})=#[{}]", sql, result);
    }
    return result;
  }

  /**
   * Execute update script.
   *
   * @param script the script
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int executeUpdateScript(final String script) throws SQLException {
    log.debug("executeUpdateScript({})", script);
    return executeUpdateScript(script, false);
  }

  /**
   * Execute update script.
   *
   * @param script the script
   * @param ignoreSeparator the ignore separator
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int executeUpdateScript(final String script, final boolean ignoreSeparator) throws SQLException {
    log.debug("executeUpdateScript({},{})", script, ignoreSeparator);
    int result = 0;
    try (final Scanner scanner = new Scanner(script);
        final Connection con = DriverManager.getConnection(jdbcURL, user, pwd);
        final Statement stmt = con.createStatement()) {
      int sqlResult = 0;
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        final String sql = getSQL(line, ignoreSeparator);
        if (sql != null) {
          log.debug("executeUpdateScript()LINE[{}]", sql);
          sqlResult = stmt.executeUpdate(sql);
          log.debug("executeUpdateScript()LINE[{}]=#[{}]", sql, sqlResult);
          if (sqlResult > 0) {
            result += sqlResult;
          }
        } else {
          log.debug("executeUpdateScript({}) SKIPPING LINE (not ending with ;)", line);
        }
      }
    } catch (final SQLException e) {
      log.error("executeUpdateScript({}) SQLException={}", trimmed(script), e.getMessage());
      throw e;
    }
    log.info("executeUpdateScript({})=#[{}]", trimmed(script), result);
    if (needsTrim(script) && log.isEnabledForLevel(Level.DEBUG)) {
      log.debug("executeUpdateScript({})=#[{}]", script, result);
    }
    return result;
  }

  /**
   * Gets the sql.
   *
   * @param sql the sql
   * @param ignoreSeparator the ignore separator
   * @return the sql
   */
  protected static String getSQL(final String sql, final boolean ignoreSeparator) {
    if (sql.endsWith(";")) {
      if (ignoreSeparator) {
        return sql.substring(0, sql.length() - 1);
      }
      return sql;
    }
    return null;
  }

  /**
   * Trimmed.
   *
   * @param string the string
   * @return the string
   */
  protected String trimmed(final String string) {
    if (needsTrim(string)) {
      return string.substring(0, Math.min(string.length(), SQL_TRIM_LENGTH)).trim() + "...";
    }
    return string;
  }

  /**
   * Needs trim.
   *
   * @param string the string
   * @return true, if successful
   */
  protected boolean needsTrim(final String string) {
    return string.length() > SQL_TRIM_LENGTH;
  }
}
