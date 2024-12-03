package dev.inditex.karate;

import dev.inditex.karate.logging.KarateClientLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

public abstract class AbstractClientTest {

  protected ListAppender<ILoggingEvent> logWatcher;

  @BeforeEach
  void beforeEach() {
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).addAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.INFO);
  }

}
