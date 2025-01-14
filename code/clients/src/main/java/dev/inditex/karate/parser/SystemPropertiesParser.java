package dev.inditex.karate.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class SystemPropertiesParser.
 */
@Slf4j
public class SystemPropertiesParser {

  /** The Constant DEFAULT_SEPARATOR. */
  protected static final String DEFAULT_SEPARATOR = ":";

  /** The Constant VARIABLE_PATTERN. */
  protected static final String VARIABLE_PATTERN = "\\$\\{(.*?)}";

  /**
   * Instantiates a new system properties parser.
   */
  protected SystemPropertiesParser() {
  }

  /**
   * Parses the configuration.
   *
   * @param config the config
   * @return the map
   */
  public static Map<Object, Object> parseConfiguration(final Map<Object, Object> config) {
    logMaskedConfiguration("configFile   >>", config);
    final var parsedConfig = parseMap(config);
    logMaskedConfiguration("configParsed >>", parsedConfig);
    return parsedConfig;
  }

  /**
   * Parses the map.
   *
   * @param map the map
   * @return the map
   */
  protected static Map<Object, Object> parseMap(final Map<?, ?> map) {
    final Map<Object, Object> parsedMap = new HashMap<>();
    if (map != null) {
      for (final Entry<?, ?> entry : map.entrySet()) {
        final Object parsedValue = parseValue(entry.getValue());
        parsedMap.put(entry.getKey(), parsedValue);
      }
    }
    return parsedMap;
  }

  /**
   * Parses the value.
   *
   * @param value the value
   * @return the object
   */
  protected static Object parseValue(final Object value) {
    log.trace("parseValue-> {}", value);
    return switch (value) {
      case final Map<?, ?> map -> parseMap(map);
      case final Number num -> num;
      case final Boolean bool -> bool;
      case final String str -> {
        final StringBuilder sb = new StringBuilder();
        final Matcher matcher = Pattern.compile(VARIABLE_PATTERN).matcher(str);
        while (matcher.find()) {
          final String match = matcher.group(1);
          log.trace("          -> {}", match);
          String systemKey = match;
          String defaultValue = match;
          if (match.contains(DEFAULT_SEPARATOR)) {
            systemKey = match.substring(0, match.indexOf(DEFAULT_SEPARATOR));
            defaultValue = match.substring(match.indexOf(DEFAULT_SEPARATOR) + 1);
          }
          log.trace("               -> [{},{}]", systemKey, defaultValue);
          final String systemValue = System.getProperty(systemKey, defaultValue);
          log.trace("                    -> [{}]", systemValue);
          matcher.appendReplacement(sb, systemValue);
        }
        matcher.appendTail(sb);
        final String parsedValue = sb.toString();
        log.trace("parseValue({})\n        => {}", value, parsedValue);
        yield parsedValue;
      }
      default -> throw new IllegalArgumentException(String.format("Unable to parse type [%s]", value.getClass()));
    };
  }

  /**
   * Log masked configuration.
   *
   * @param prefix the prefix
   * @param config the config
   */
  protected static void logMaskedConfiguration(final String prefix, final Map<Object, Object> config) {
    final var maskedConfig = maskMap(config);
    log.debug("{}[{}] ", prefix, maskedConfig);
  }

  /**
   * Mask map.
   *
   * @param map the map
   * @return the map
   */
  protected static Map<Object, Object> maskMap(final Map<?, ?> map) {
    final Map<Object, Object> maskedMap = new HashMap<>();
    if (map != null) {
      for (final Entry<?, ?> entry : map.entrySet()) {
        final Object maskedValue = mask(entry.getKey(), entry.getValue());
        maskedMap.put(entry.getKey(), maskedValue);
      }
    }
    return maskedMap;
  }

  /**
   * Mask.
   *
   * @param key the key
   * @param value the value
   * @return the object
   */
  protected static Object mask(final Object key, final Object value) {
    return switch (value) {
      case final Map<?, ?> map -> maskMap(map);
      case final String str -> maskValue(key, str);
      default -> value;
    };
  }

  /**
   * Mask value.
   *
   * @param key the key
   * @param str the str
   * @return the string
   */
  protected static String maskValue(final Object key, final String str) {
    String maskedValue = str;
    if (key.toString().toLowerCase().contains("password") || key.toString().toLowerCase().contains("username")) {
      maskedValue = "********";
    } else if (key.toString().toLowerCase().contains("basic.auth.user.info")) {
      if (str.contains(":")) {
        maskedValue = "********:********";
      }
    } else {
      if (str.toLowerCase().contains("username=") || str.toLowerCase().contains("password=")) {
        maskedValue = maskedValue.replaceAll("password=([^\\s]+)", "password=********");
        maskedValue = maskedValue.replaceAll("username=([^\\s]+)", "username=********");
      }
    }
    return maskedValue;
  }
}
