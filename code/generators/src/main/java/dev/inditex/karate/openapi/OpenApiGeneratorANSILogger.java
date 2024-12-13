package dev.inditex.karate.openapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ANSILogger.
 */
public class OpenApiGeneratorANSILogger {

  /** The Constant OPEN_API_GENERATOR_STDOUT. */
  protected static final Logger OPEN_API_GENERATOR_STDOUT = LoggerFactory.getLogger("OpenApiGenerator");

  /** The Constant ANSI_RESET. */
  protected static final String ANSI_RESET = "\u001B[0m";

  /** The Constant ANSI_BLUE. */
  protected static final String ANSI_BLUE = "\u001B[34m";

  /** The Constant ANSI_YELLOW. */
  protected static final String ANSI_YELLOW = "\u001B[33m";

  /** The Constant ANSI_RED. */
  protected static final String ANSI_RED = "\u001B[31m";

  /** The Constant LOG_FORMAT. */
  protected static final String LOG_FORMAT = "\n{}{}{}{}\n";

  /**
   * Instantiates a new ANSI logger.
   */
  protected OpenApiGeneratorANSILogger() {
    // Empty
  }

  /**
   * Info.
   *
   * @param message the message
   */
  public static void info(final String message) {
    OPEN_API_GENERATOR_STDOUT.info(LOG_FORMAT, ANSI_BLUE, "INFO  - ", message, ANSI_RESET);
  }

  /**
   * Warn.
   *
   * @param message the message
   */
  public static void warn(final String message) {
    OPEN_API_GENERATOR_STDOUT.warn(LOG_FORMAT, ANSI_YELLOW, "WARN  - ", message, ANSI_RESET);
  }

  /**
   * Error.
   *
   * @param message the message
   */
  public static void error(final String message) {
    OPEN_API_GENERATOR_STDOUT.error(LOG_FORMAT, ANSI_RED, "ERROR - ", message, ANSI_RESET);
  }
}
