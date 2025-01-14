package dev.inditex.karate.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class OpenApiGeneratorANSILoggerTest {

  protected ListAppender<ILoggingEvent> logWatcher;

  protected Level defaultLogLevel;

  @BeforeEach
  void beforeEach() {
    defaultLogLevel = ((Logger) LoggerFactory.getLogger("OpenApiGenerator")).getLevel();
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((Logger) LoggerFactory.getLogger("OpenApiGenerator")).addAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("OpenApiGenerator")).setLevel(Level.DEBUG);
  }

  @AfterEach
  void afterEach() {
    ((Logger) LoggerFactory.getLogger("OpenApiGenerator")).detachAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger("OpenApiGenerator")).setLevel(defaultLogLevel);
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiGeneratorANSILogger::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Info {
    @Test
    void when_message_expect_delegate_to_logger_with_color() {
      final String message = "message";
      OpenApiGeneratorANSILogger.info(message);

      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.INFO);
      assertThat(logWatcher.list.get(0).getFormattedMessage())
          .contains(OpenApiGeneratorANSILogger.ANSI_BLUE + "INFO  - " + message + OpenApiGeneratorANSILogger.ANSI_DEFAULT);

    }
  }

  @Nested
  class Warn {
    @Test
    void when_message_expect_delegate_to_logger_with_color() {
      final String message = "message";
      OpenApiGeneratorANSILogger.warn(message);

      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.WARN);
      assertThat(logWatcher.list.get(0).getFormattedMessage())
          .contains(OpenApiGeneratorANSILogger.ANSI_YELLOW + "WARN  - " + message + OpenApiGeneratorANSILogger.ANSI_DEFAULT);
    }
  }

  @Nested
  class Error {
    @Test
    void when_message_expect_delegate_to_logger_with_color() {
      final String message = "message";
      OpenApiGeneratorANSILogger.error(message);

      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.ERROR);
      assertThat(logWatcher.list.get(0).getFormattedMessage())
          .contains(OpenApiGeneratorANSILogger.ANSI_RED + "ERROR - " + message + OpenApiGeneratorANSILogger.ANSI_DEFAULT);
    }
  }

  @Nested
  class DEbug {
    @Test
    void when_message_expect_delegate_to_logger_with_color() {
      final String message = "message";
      OpenApiGeneratorANSILogger.debug(message);

      assertThat(logWatcher.list).hasSize(1).extracting("level").containsOnly(Level.DEBUG);
      assertThat(logWatcher.list.get(0).getFormattedMessage())
          .contains(OpenApiGeneratorANSILogger.ANSI_DEFAULT + "DEBUG - " + message + OpenApiGeneratorANSILogger.ANSI_DEFAULT);
    }
  }
}
