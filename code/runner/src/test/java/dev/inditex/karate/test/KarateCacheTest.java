package dev.inditex.karate.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import dev.inditex.karate.AbstractKarateTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class KarateCacheTest extends AbstractKarateTest {

  @BeforeEach
  protected void beforeEach() {
    KarateCache.clear();
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateCache::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Clear {
    @Test
    void when_cache_clear_expect_empty() {
      final String key = "key";
      final String value = "value";
      KarateCache.get().put(key, value);

      KarateCache.clear();

      assertThat(KarateCache.get()).isEmpty();
    }
  }

  @Nested
  class Get {
    @Test
    void when_cache_get_value_found_expect_present() {
      final String key = "key";
      final String value = "value";
      KarateCache.get().put(key, value);

      final var result = KarateCache.get(key);

      assertThat(result).isEqualTo(value);
    }

    @Test
    void when_cache_get_value_not_found_expect_not_present() {
      final String key = "key";

      final var result = KarateCache.get(key);

      assertThat(result).isNull();
    }

    @Test
    void when_cache_get_expect_present() {
      final String key = "key";
      final String value = "value";
      KarateCache.put(key, value);

      final var result = KarateCache.get();

      assertThat(result).containsEntry(key, value);
    }
  }

  @Nested
  class Put {
    @Test
    void when_cache_put_expect_present() {
      final String key = "key";
      final String value = "value";

      KarateCache.put(key, value);

      assertThat(KarateCache.get()).containsEntry(key, value);
    }
  }

  @Nested
  class Remove {

    @Test
    void when_cache_remove_found_expect_not_present() {
      final String key = "key";
      final String value = "value";
      KarateCache.get().put(key, value);

      KarateCache.remove(key);

      assertThat(KarateCache.get(key)).isNull();
    }

    @Test
    void when_cache_remove_not_found_expect_not_present() {
      final String key = "key";

      KarateCache.remove(key);

      assertThat(KarateCache.get(key)).isNull();
    }
  }
}
