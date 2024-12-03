package dev.inditex.karate.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SystemPropertiesParserTest {

  @Nested
  class ParseConfiguration {

    @Test
    void when_null_expect_empty() {
      final Map<Object, Object> config = null;

      final var result = SystemPropertiesParser.parseConfiguration(config);

      assertThat(result).isEmpty();
    }

    @Test
    void when_empty_expect_empty() {
      final Map<Object, Object> config = Collections.emptyMap();

      final var result = SystemPropertiesParser.parseConfiguration(config);

      assertThat(result).isEmpty();
    }

    @Test
    void when_invalid_value_expect_IllegalArgumentException() {
      final Map<Object, Object> config = new HashMap<>();
      config.put("key-1", new ArrayList<>());

      assertThatThrownBy(() -> SystemPropertiesParser.parseConfiguration(config))
          .isInstanceOf(IllegalArgumentException.class).hasMessage("Unable to parse type [class java.util.ArrayList]");
    }

    @Test
    void when_simple_map_expect_parsed() {
      final Map<Object, Object> config = getActualMap();
      final Map<Object, Object> expected = getExpectedMap();
      setSystemProperties();

      final var result = SystemPropertiesParser.parseConfiguration(config);

      assertThat(result).isNotEmpty().isEqualTo(expected);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void when_map_of_maps_expect_parsed() {
      final Map<Object, Object> configLevel1 = getActualMap();
      final Map<Object, Object> configLevel2 = getActualMap();
      final Map<Object, Object> config = new HashMap<>();
      config.put("level-1", configLevel1);
      config.put("level-2", new HashMap<>());
      ((Map) config.get("level-2")).put("level-2", configLevel2);
      final Map<Object, Object> expectedLevel1 = getExpectedMap();
      final Map<Object, Object> expectedLevel2 = getExpectedMap();
      final Map<Object, Object> expected = new HashMap<>();
      expected.put("level-1", expectedLevel1);
      expected.put("level-2", new HashMap<>());
      ((Map) expected.get("level-2")).put("level-2", expectedLevel2);
      setSystemProperties();

      final var result = SystemPropertiesParser.parseConfiguration(config);

      assertThat(result).isNotEmpty().isEqualTo(expected);
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_map_from_json_expect_parsed() throws JsonProcessingException {
      final ObjectMapper mapper = new ObjectMapper();
      final String configJSON = """
          {
            "jdbc-url": "jdbc:driver://${system.host:localhost}:${system.port:9999}/instance",
            "driver-class-name": "org.driver.Driver",
            "username": "${system.user:username}",
            "password": "${system.password:pwd}",
            "health-query": "SELECT 1"
          }
          """;
      final Map<Object, Object> config = mapper.readValue(configJSON, HashMap.class);
      final Map<Object, Object> expected = Map.of(
          "jdbc-url", "jdbc:driver://system-host:8888/instance",
          "driver-class-name", "org.driver.Driver",
          "username", "user",
          "password", "password",
          "health-query", "SELECT 1");
      System.setProperty("system.host", "system-host");
      System.setProperty("system.port", "8888");
      System.setProperty("system.user", "user");
      System.setProperty("system.password", "password");

      final var result = SystemPropertiesParser.parseConfiguration(config);

      assertThat(result).isNotEmpty().isEqualTo(expected);
    }

    private Map<Object, Object> getActualMap() {
      return Map.of(
          "key-1", "value-1", // Static String
          "key-2", "${system-var-2}", // System not informed
          "key-3", "${system-var-3}", // System informed
          "key-4", "${system-var-4:value-4}", // System not informed with default
          "key-5", "${system-var-5:value-5}", // System informed with default
          "key-6", "${system-var-2}:${system-var-3}:${system-var-5:value-5}", // Concatenated
          "key-7", 0, // Static Number
          "key-8", "0", // Static Number as String
          "key-9", Boolean.TRUE, // Static Boolean
          "key-11", "true" // Static Boolean as String
      );
    }

    private Map<Object, Object> getExpectedMap() {
      return Map.of(
          "key-1", "value-1", // Static
          "key-2", "system-var-2", // System not informed
          "key-3", "system-value-3", // System informed
          "key-4", "value-4", // System not informed with default
          "key-5", "system-value-5", // System informed with default
          "key-6", "system-var-2:system-value-3:system-value-5", // Concatenated
          "key-7", 0, // Static Number
          "key-8", "0", // Static Number as String
          "key-9", Boolean.TRUE, // Static Boolean
          "key-11", "true" // Static Boolean as String
      );
    }

    private void setSystemProperties() {
      System.setProperty("system-var-3", "system-value-3");
      System.setProperty("system-var-5", "system-value-5");
    }
  }

  @Nested
  class MaskMap {
    @Test
    void when_null_expect_empty() {
      final Map<Object, Object> config = null;

      final var result = SystemPropertiesParser.maskMap(config);

      assertThat(result).isEmpty();
    }

    @Test
    void when_empty_expect_empty() {
      final Map<Object, Object> config = Collections.emptyMap();

      final var result = SystemPropertiesParser.maskMap(config);

      assertThat(result).isEmpty();
    }

    @Test
    void when_simple_map_expect_masked() {
      final Map<Object, Object> config = getActualMap();
      final Map<Object, Object> expected = getExpectedMap();
      final var result = SystemPropertiesParser.maskMap(config);

      assertThat(result).isNotEmpty().isEqualTo(expected);
    }

    @Test
    void when_map_of_maps_expect_masked() {
      final Map<Object, Object> config = Map.of("level-1", getActualMap(), "level-2", getActualMap());
      final Map<Object, Object> expected = Map.of("level-1", getExpectedMap(), "level-2", getExpectedMap());

      final var result = SystemPropertiesParser.maskMap(config);

      assertThat(result).isNotEmpty().isEqualTo(expected);
    }
  }

  @Nested
  class Mask {
    @ParameterizedTest
    @MethodSource
    void when_pair_expect_masked(final Object key, final Object value, final Object expected) {
      final var result = SystemPropertiesParser.mask(key, value);
      assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> when_pair_expect_masked() {
      return Stream.of(
          Arguments.of("username", "user00", "********"),
          Arguments.of("USERNAME", "user00", "********"),
          Arguments.of("password", "123456", "********"),
          Arguments.of("PASSWORD", "123456", "********"),
          Arguments.of("Any", "username=user", "username=********"),
          Arguments.of("Any", "password=123456", "password=********"),
          Arguments.of("Any", "password=123456 username=user", "password=******** username=********"),
          Arguments.of("Any", "username=user password=123456", "username=******** password=********"),
          Arguments.of("basic.auth.user.info", "user:123456", "********:********"),
          Arguments.of("key", "value", "value"),
          Arguments.of(getActualMap(), getActualMap(), getExpectedMap()));
    }
  }

  private static Map<Object, Object> getExpectedMap() {
    return Map.of(
        "username", "********",
        "USERNAME", "********",
        "password", "********",
        "PASSWORD", "********",
        "Any1", "username=******** password=********",
        "Any2", "password=********",
        "Any3", "password=******** username=********",
        "Any4", "username=******** password=********",
        "basic.auth.user.info", "********:********",
        "key", "value");
  }

  private static Map<Object, Object> getActualMap() {
    return Map.of(
        "username", "user",
        "USERNAME", "user",
        "password", "123456",
        "PASSWORD", "123456",
        "Any1", "username=user password=123456",
        "Any2", "password=123456",
        "Any3", "password=123456 username=user",
        "Any4", "username=user password=123456",
        "basic.auth.user.info", "user:123456",
        "key", "value");
  }
}
