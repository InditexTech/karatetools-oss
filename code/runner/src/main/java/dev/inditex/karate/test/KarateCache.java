package dev.inditex.karate.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class KarateCache.
 */
public class KarateCache {

  /** The Constant KARATE_CACHE. */
  protected static final Map<String, Object> KARATE_CACHE = new ConcurrentHashMap<>();

  /**
   * Instantiates a new karate cache.
   */
  protected KarateCache() {
  }

  /**
   * Clear.
   */
  public static void clear() {
    KARATE_CACHE.clear();
  }

  /**
   * Gets the.
   *
   * @return the map
   */
  public static Map<String, Object> get() {
    return KARATE_CACHE;
  }

  /**
   * Gets the.
   *
   * @param key the key
   * @return the object
   */
  public static Object get(final String key) {
    return KARATE_CACHE.get(key);
  }

  /**
   * Put.
   *
   * @param key the key
   * @param value the value
   */
  public static void put(final String key, final Object value) {
    KARATE_CACHE.put(key, value);
  }

  /**
   * Removes the.
   *
   * @param key the key
   */
  public static void remove(final String key) {
    KARATE_CACHE.remove(key);
  }

}
