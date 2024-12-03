package dev.inditex.karate.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.intuit.karate.core.FeatureRuntime;
import com.intuit.karate.core.ScenarioEngine;
import com.intuit.karate.core.ScenarioFileReader;
import com.intuit.karate.graal.JsValue;
import com.intuit.karate.http.HttpClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@Tag("IT")
class SystemPropertiesParserIT {

  protected ListAppender<ILoggingEvent> logWatcher;

  @BeforeEach
  protected void beforeEach() {
    logWatcher = new ListAppender<>();
    logWatcher.start();
    ((Logger) LoggerFactory.getLogger(SystemPropertiesParser.class)).addAppender(logWatcher);
    ((Logger) LoggerFactory.getLogger(SystemPropertiesParser.class)).setLevel(Level.DEBUG);
  }

  @Nested
  class ParseConfiguration {

    @SuppressWarnings("unchecked")
    @Test
    void when_config_no_system_properties_expect_parsed_and_logged() {
      // Read the file in the same way as Karate does
      final ScenarioEngine scenarioEngine = ScenarioEngine.forTempUse(HttpClientFactory.DEFAULT);
      final var featureRuntime = FeatureRuntime.forTempUse(HttpClientFactory.DEFAULT);
      final ScenarioFileReader karateFileReader = new ScenarioFileReader(scenarioEngine, featureRuntime);
      final var configFile = karateFileReader.readFile("classpath:config/config-sys-properties-test.yml");
      final var config = JsValue.fromJava(configFile);

      final var result = SystemPropertiesParser.parseConfiguration((Map<Object, Object>) config);

      assertThat(result).isNotEmpty()
          .containsEntry("number-fixed", 100)
          .containsEntry("number-system", "100")
          .containsEntry("boolean-fixed", true)
          .containsEntry("boolean-system", "true")
          .containsEntry("string-fixed", "fixed")
          .containsEntry("string-system", "fixed")
          .containsEntry("mask-password-fixed", "pass01")
          .containsEntry("mask-password-system", "pass01")
          .containsEntry("mask-username-fixed", "user01")
          .containsEntry("mask-username-system", "user01")
          .containsEntry("basic.auth.user.info", "user01:pass01")
          .containsEntry("auth-fixed", "username=user01 password=pass01")
          .containsEntry("auth-system", "username=user01 password=pass01")
          .containsKeys("map");
      assertThat((Map<Object, Object>) result.get("map")).isNotEmpty()
          .containsEntry("number-fixed", 100)
          .containsEntry("number-system", "100")
          .containsEntry("boolean-fixed", true)
          .containsEntry("boolean-system", "true")
          .containsEntry("string-fixed", "fixed")
          .containsEntry("string-system", "fixed")
          .containsEntry("mask-password-fixed", "pass01")
          .containsEntry("mask-password-system", "pass01")
          .containsEntry("mask-username-fixed", "user01")
          .containsEntry("mask-username-system", "user01")
          .containsEntry("basic.auth.user.info", "user01:pass01")
          .containsEntry("auth-fixed", "username=user01 password=pass01")
          .containsEntry("auth-system", "username=user01 password=pass01");
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
          && log.getFormattedMessage().contains("number-fixed=100")
          && log.getFormattedMessage().contains("number-system=100")
          && log.getFormattedMessage().contains("boolean-fixed=true")
          && log.getFormattedMessage().contains("boolean-system=true")
          && log.getFormattedMessage().contains("string-fixed=fixed")
          && log.getFormattedMessage().contains("string-system=fixed")
          && log.getFormattedMessage().contains("mask-password-fixed=********")
          && log.getFormattedMessage().contains("mask-password-system=********")
          && log.getFormattedMessage().contains("mask-username-fixed=********")
          && log.getFormattedMessage().contains("mask-username-system=********")
          && log.getFormattedMessage().contains("basic.auth.user.info=********:********")
          && log.getFormattedMessage().contains("auth-fixed=username=******** password=********")
          && log.getFormattedMessage().contains("auth-system=username=******** password=********")
          && !log.getFormattedMessage().contains("user01")
          && !log.getFormattedMessage().contains("pass01")
          && !log.getFormattedMessage().contains("user02")
          && !log.getFormattedMessage().contains("pass02"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_config_with_system_properties_expect_parsed_and_logged() {
      // Read the file in the same way as Karate does
      final ScenarioEngine scenarioEngine = ScenarioEngine.forTempUse(HttpClientFactory.DEFAULT);
      final var featureRuntime = FeatureRuntime.forTempUse(HttpClientFactory.DEFAULT);
      final ScenarioFileReader karateFileReader = new ScenarioFileReader(scenarioEngine, featureRuntime);
      final var configFile = karateFileReader.readFile("classpath:config/config-sys-properties-test.yml");
      final var config = JsValue.fromJava(configFile);
      // setSystemProperties
      System.setProperty("number-var", "200");
      System.setProperty("boolean-var", "false");
      System.setProperty("string-var", "var");
      System.setProperty("username-var", "user02");
      System.setProperty("password-var", "pass02");

      final var result = SystemPropertiesParser.parseConfiguration((Map<Object, Object>) config);

      assertThat(result).isNotEmpty()
          .containsEntry("number-fixed", 100)
          .containsEntry("number-system", "200")
          .containsEntry("boolean-fixed", true)
          .containsEntry("boolean-system", "false")
          .containsEntry("string-fixed", "fixed")
          .containsEntry("string-system", "var")
          .containsEntry("mask-password-fixed", "pass01")
          .containsEntry("mask-password-system", "pass02")
          .containsEntry("mask-username-fixed", "user01")
          .containsEntry("mask-username-system", "user02")
          .containsEntry("basic.auth.user.info", "user01:pass01")
          .containsEntry("auth-fixed", "username=user01 password=pass01")
          .containsEntry("auth-system", "username=user02 password=pass02")
          .containsKeys("map");
      assertThat((Map<Object, Object>) result.get("map")).isNotEmpty()
          .containsEntry("number-fixed", 100)
          .containsEntry("number-system", "200")
          .containsEntry("boolean-fixed", true)
          .containsEntry("boolean-system", "false")
          .containsEntry("string-fixed", "fixed")
          .containsEntry("string-system", "var")
          .containsEntry("mask-password-fixed", "pass01")
          .containsEntry("mask-password-system", "pass02")
          .containsEntry("mask-username-fixed", "user01")
          .containsEntry("mask-username-system", "user02")
          .containsEntry("basic.auth.user.info", "user02:pass02")
          .containsEntry("auth-fixed", "username=user01 password=pass01")
          .containsEntry("auth-system", "username=user02 password=pass02");
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
          && log.getFormattedMessage().contains("number-fixed=100")
          && log.getFormattedMessage().contains("number-system=200")
          && log.getFormattedMessage().contains("boolean-fixed=true")
          && log.getFormattedMessage().contains("boolean-system=false")
          && log.getFormattedMessage().contains("string-fixed=fixed")
          && log.getFormattedMessage().contains("string-system=var")
          && log.getFormattedMessage().contains("mask-password-fixed=********")
          && log.getFormattedMessage().contains("mask-password-system=********")
          && log.getFormattedMessage().contains("mask-username-fixed=********")
          && log.getFormattedMessage().contains("mask-username-system=********")
          && log.getFormattedMessage().contains("basic.auth.user.info=********:********")
          && log.getFormattedMessage().contains("auth-fixed=username=******** password=********")
          && !log.getFormattedMessage().contains("user01")
          && !log.getFormattedMessage().contains("pass01")
          && !log.getFormattedMessage().contains("user02")
          && !log.getFormattedMessage().contains("pass02"));
    }
  }
}
