package dev.inditex.karate.logging;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

/**
 * The Class KarateClientLogger.
 */
@Slf4j
public class KarateClientLogger {

  /** The Constant LOG_PREFIX. */
  protected static final String LOG_PREFIX = ">>>>>> %s => %s";

  /** The Constant WALKER. */
  protected static final StackWalker WALKER = StackWalker.getInstance(RETAIN_CLASS_REFERENCE);

  /**
   * Trace.
   *
   * @param format the format
   * @param arguments the arguments
   */
  public void trace(final String format, final Object... arguments) {
    log.trace(String.format(LOG_PREFIX, WALKER.getCallerClass().getSimpleName(), format), arguments);
  }

  /**
   * Debug.
   *
   * @param format the format
   * @param arguments the arguments
   */
  public void debug(final String format, final Object... arguments) {
    log.debug(String.format(LOG_PREFIX, WALKER.getCallerClass().getSimpleName(), format), arguments);
  }

  /**
   * Info.
   *
   * @param format the format
   * @param arguments the arguments
   */
  public void info(final String format, final Object... arguments) {
    log.info(String.format(LOG_PREFIX, WALKER.getCallerClass().getSimpleName(), format), arguments);
  }

  /**
   * Warn.
   *
   * @param format the format
   * @param arguments the arguments
   */
  public void warn(final String format, final Object... arguments) {
    log.warn(String.format(LOG_PREFIX, WALKER.getCallerClass().getSimpleName(), format), arguments);
  }

  /**
   * Error.
   *
   * @param format the format
   * @param arguments the arguments
   */
  public void error(final String format, final Object... arguments) {
    log.error(String.format(LOG_PREFIX, WALKER.getCallerClass().getSimpleName(), format), arguments);
  }

  /**
   * Checks if is enabled for level.
   *
   * @param level the level
   * @return true, if is enabled for level
   */
  public boolean isEnabledForLevel(final Level level) {
    return log.isEnabledForLevel(level);
  }
}
