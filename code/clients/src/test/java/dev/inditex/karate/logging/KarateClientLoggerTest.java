package dev.inditex.karate.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

class KarateClientLoggerTest {

  protected static final String LOG_FORMAT = "Message [{}] [{}]";

  protected static final String ARG1 = "arg1";

  protected static final Object ARG2 = "arg2";

  protected static final String STRING_FORMAT = ">>>>>> %s => Message [%s] [%s]";

  protected ListAppender<ILoggingEvent> logWatcher;

  @BeforeEach
  void setup() {
    logWatcher = new ListAppender<>();
    logWatcher.start();
    final Logger logger = (Logger) LoggerFactory.getLogger(KarateClientLogger.class);
    logger.setLevel(Level.TRACE);
    logger.addAppender(logWatcher);
  }

  @Nested
  class Trace {
    @Test
    void when_log_expect_formatted() {
      final KarateClientLogger client = new KarateClientLogger();
      final String expected = String.format(STRING_FORMAT, getClass().getSimpleName(), ARG1, ARG2);

      client.trace(LOG_FORMAT, ARG1, ARG2);

      assertThat(logWatcher.list).hasSize(1);
      assertThat(logWatcher.list.get(0).getLevel()).isEqualTo(Level.TRACE);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).isEqualTo(expected);
    }
  }

  @Nested
  class Debug {
    @Test
    void when_log_expect_formatted() {
      final KarateClientLogger client = new KarateClientLogger();
      final String expected = String.format(STRING_FORMAT, getClass().getSimpleName(), ARG1, ARG2);

      client.debug(LOG_FORMAT, ARG1, ARG2);

      assertThat(logWatcher.list).hasSize(1);
      assertThat(logWatcher.list.get(0).getLevel()).isEqualTo(Level.DEBUG);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).isEqualTo(expected);
    }
  }

  @Nested
  class Info {
    @Test
    void when_log_expect_formatted() {
      final KarateClientLogger client = new KarateClientLogger();
      final String expected = String.format(STRING_FORMAT, getClass().getSimpleName(), ARG1, ARG2);

      client.info(LOG_FORMAT, ARG1, ARG2);

      assertThat(logWatcher.list).hasSize(1);
      assertThat(logWatcher.list.get(0).getLevel()).isEqualTo(Level.INFO);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).isEqualTo(expected);
    }
  }

  @Nested
  class Warn {
    @Test
    void when_log_expect_formatted() {
      final KarateClientLogger client = new KarateClientLogger();
      final String expected = String.format(STRING_FORMAT, getClass().getSimpleName(), ARG1, ARG2);

      client.warn(LOG_FORMAT, ARG1, ARG2);

      assertThat(logWatcher.list).hasSize(1);
      assertThat(logWatcher.list.get(0).getLevel()).isEqualTo(Level.WARN);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).isEqualTo(expected);
    }
  }

  @Nested
  class Error {
    @Test
    void when_log_expect_formatted() {
      final KarateClientLogger client = new KarateClientLogger();
      final String expected = String.format(STRING_FORMAT, getClass().getSimpleName(), ARG1, ARG2);

      client.error(LOG_FORMAT, ARG1, ARG2);

      assertThat(logWatcher.list).hasSize(1);
      assertThat(logWatcher.list.get(0).getLevel()).isEqualTo(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage()).isEqualTo(expected);
    }
  }

  @Nested
  class IsEnabledForLevel {
    @ParameterizedTest
    @MethodSource
    void when_log_level_expect_delegate(final org.slf4j.event.Level level, final boolean expected) {
      final Logger logger = (Logger) LoggerFactory.getLogger(KarateClientLogger.class);
      logger.setLevel(Level.INFO);
      final KarateClientLogger client = new KarateClientLogger();

      final boolean result = client.isEnabledForLevel(level);

      assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> when_log_level_expect_delegate() {
      return Stream.of(
          Arguments.of(org.slf4j.event.Level.ERROR, true),
          Arguments.of(org.slf4j.event.Level.WARN, true),
          Arguments.of(org.slf4j.event.Level.INFO, true),
          Arguments.of(org.slf4j.event.Level.DEBUG, false),
          Arguments.of(org.slf4j.event.Level.TRACE, false));
    }
  }
}
