package dev.inditex.karate.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import dev.inditex.karate.AbstractKarateTest;

import ch.qos.logback.classic.Level;
import io.karatelabs.core.KarateOptionsHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class KarateRunnerTest extends AbstractKarateTest {

  @Nested
  class Execute {
    @ParameterizedTest
    @MethodSource("executeArguments")
    void when_execute_expect_result(final String env, final String opts,
        final String expectedEnv, final int expectedThreads, final List<String> expectedPaths, final List<String> expectedTags) {
      if (env != null && !env.isEmpty()) {
        System.setProperty(KarateOptionsHandler.PROP_ENV, env);
      } else {
        System.clearProperty(KarateOptionsHandler.PROP_ENV);
      }
      if (opts != null && !opts.isEmpty()) {
        System.setProperty(KarateOptionsHandler.PROP_OPTIONS, opts);
      } else {
        System.clearProperty(KarateOptionsHandler.PROP_OPTIONS);
      }
      final KarateRunner runner = instantiateRunner();

      final var result = runner.execute();
      final var options = runner.options;

      assertThat(result).isNotNull();
      assertThat(result.getScenarioFailedCount()).isZero();
      assertThat(options).isNotNull();
      assertThat(options.env()).isEqualTo(expectedEnv);
      assertThat(options.paths()).isEqualTo(expectedPaths);
      assertThat(options.tags()).isEqualTo(expectedTags);
      assertThat(options.threads()).isEqualTo(expectedThreads);
    }

    static Stream<Arguments> executeArguments() {
      final int cores = Runtime.getRuntime().availableProcessors();
      // "-t ~@mock.templates.standalone" needed to avoid mock server start and port collision for multiple runners
      return Stream.of(
          Arguments.of(null, "-t ~@mock.templates.standalone classpath:dev/inditex/",
              "local", 1, List.of("classpath:dev/inditex/"), List.of("~@mock.templates.standalone")),
          Arguments.of("", "-t ~@mock.templates.standalone classpath:dev/inditex/",
              "local", 1, List.of("classpath:dev/inditex/"), List.of("~@mock.templates.standalone")),
          Arguments.of("pre", "-t ~@mock.templates.standalone classpath:dev/inditex/",
              "pre", 1, List.of("classpath:dev/inditex/"), List.of("~@mock.templates.standalone")),
          Arguments.of(null, "-t ~@mock.templates.standalone -t @TAG1 -t ~@TAG2",
              "local", 1, List.of("classpath:"), List.of("~@mock.templates.standalone", "@TAG1", "~@TAG2")),
          Arguments.of(null, "-t ~@mock.templates.standalone classpath:dev/inditex/",
              "local", 1, List.of("classpath:dev/inditex/"), List.of("~@mock.templates.standalone")),
          Arguments.of("", "-t ~@mock.templates.standalone classpath:dev/inditex/ --threads 2",
              "local", Math.min(2, Math.max(1, cores / 2)), List.of("classpath:dev/inditex/"), List.of("~@mock.templates.standalone")),
          Arguments.of("", "--threads 3 -t @TAG1 -t ~@mock.templates.standalone",
              "local", Math.min(3, Math.max(1, cores / 2)), List.of("classpath:"), List.of("@TAG1", "~@mock.templates.standalone")),
          Arguments.of("", "-t @TAG2 --threads 4 -t ~@mock.templates.standalone",
              "local", Math.min(4, Math.max(1, cores / 2)), List.of("classpath:"), List.of("@TAG2", "~@mock.templates.standalone")),
          Arguments.of("", "-t @TAG2 --threads 5 -t ~@mock.templates.standalone",
              "local", Math.min(5, Math.max(1, cores / 2)), List.of("classpath:"), List.of("@TAG2", "~@mock.templates.standalone")));
    }
  }

  @Nested
  class ParseKarateOptions {

    @Test
    void when_env_in_options_and_sysprop_expect_options_wins() {
      System.setProperty(KarateOptionsHandler.PROP_ENV, "dev");
      System.setProperty(KarateOptionsHandler.PROP_OPTIONS, "-e qa -t @smoke");
      final KarateRunner runner = instantiateRunner();

      runner.parseKarateOptions();

      // - If `karate.options` sets `--env qa` **and** `-Dkarate.env=dev` is also set, `karate.options` wins (it's applied last).
      assertThat(runner.options.env()).isEqualTo("qa");
    }

    @Test
    void when_env_in_options_without_sysprop_expect_options_value() {
      System.clearProperty(KarateOptionsHandler.PROP_ENV);
      System.setProperty(KarateOptionsHandler.PROP_OPTIONS, "-e qa -t @smoke");
      final KarateRunner runner = instantiateRunner();

      runner.parseKarateOptions();

      assertThat(runner.options.env()).isEqualTo("qa");
    }

    @ParameterizedTest
    @MethodSource("parseKarateOptionsArguments")
    void when_parse_expect_options(final String opts, final List<String> expectedTags, final int expectedThreads, final String warning) {
      if (opts != null && !opts.isEmpty()) {
        System.setProperty(KarateOptionsHandler.PROP_OPTIONS, opts);
      } else {
        System.clearProperty(KarateOptionsHandler.PROP_OPTIONS);
      }
      final KarateRunner runner = instantiateRunner();

      runner.parseKarateOptions();

      assertThat(runner.options.tags()).isEqualTo(expectedTags);
      assertThat(runner.options.threads()).isEqualTo(expectedThreads);
      if (warning != null) {
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.WARN)
            && log.getFormattedMessage().contains(warning));
      } else {
        assertThat(logWatcher.list).extracting("level").doesNotContain(Level.WARN);
      }
    }

    static Stream<Arguments> parseKarateOptionsArguments() {
      return Stream.of(
          // --- TAGS: default behavior ---
          // Empty options -> default tag ~@ignore
          Arguments.of("",
              List.of("~@ignore"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          // Only paths, no tags -> default tag ~@ignore
          Arguments.of("classpath:dev/inditex/",
              List.of("~@ignore"), getExpectedThreads(1), getExpectedThreadsWarning(1)),

          // --- TAGS: explicit values ---
          // Multiple tags (AND logic)
          Arguments.of("-t @TAG1 -t @TAG2 -t ~@TAG3",
              List.of("@TAG1", "@TAG2", "~@TAG3"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          // Comma-separated tags (OR logic within a single -t)
          Arguments.of("-t @TAG1,@TAG2",
              List.of("@TAG1,@TAG2"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          // Combined AND + OR
          Arguments.of("-t @TAG1,@TAG2 -t @TAG3 -t ~@TAG4",
              List.of("@TAG1,@TAG2", "@TAG3", "~@TAG4"), getExpectedThreads(1), getExpectedThreadsWarning(1)),

          // --- THREADS: default behavior ---
          // --threads present without tags -> default tag still added
          Arguments.of("--threads 1",
              List.of("~@ignore"), getExpectedThreads(1), getExpectedThreadsWarning(1)),

          // --- THREADS + TAGS: position independence ---
          // Tags before --threads
          Arguments.of("-t @TAG1 --threads 1",
              List.of("@TAG1"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          // Tags after --threads
          Arguments.of("--threads 1 -t @TAG2",
              List.of("@TAG2"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          // Tags on both sides of --threads
          Arguments.of("-t @TAG1 --threads 1 -t @TAG2",
              List.of("@TAG1", "@TAG2"), getExpectedThreads(1), getExpectedThreadsWarning(1)),

          // --- THREADS: capping ---
          // Threads within cap
          Arguments.of("--threads 2",
              List.of("~@ignore"), getExpectedThreads(2), getExpectedThreadsWarning(2)),
          // Threads at boundary
          Arguments.of("--threads 5",
              List.of("~@ignore"), getExpectedThreads(5), getExpectedThreadsWarning(5)),
          // Threads over cap -> capped with WARN
          Arguments.of("--threads 10",
              List.of("~@ignore"), getExpectedThreads(10), getExpectedThreadsWarning(10)),

          // --- THREADS: short form -T ---
          // -T short form (RunCommand supports both -T and --threads)
          Arguments.of("-T 2",
              List.of("~@ignore"), getExpectedThreads(2), getExpectedThreadsWarning(2)),
          // -T with tags
          Arguments.of("-T 3 -t @TAG1",
              List.of("@TAG1"), getExpectedThreads(3), getExpectedThreadsWarning(3)),

          // --- MALFORMED OPTIONS: graceful fallback ---
          // Invalid --threads value -> ParameterException, WARN, defaults
          Arguments.of("--threads abc",
              List.of("~@ignore"), 1, "invalid karate.options ignored"),
          // Unknown flag -> ParameterException, WARN, defaults
          Arguments.of("--unknown-flag xyz",
              List.of("~@ignore"), 1, "invalid karate.options ignored"));
    }

    private static int getExpectedThreads(final int threads) {
      final int cores = Runtime.getRuntime().availableProcessors();
      return Math.min(threads, Math.max(1, cores / 2));
    }

    private static String getExpectedThreadsWarning(final int threads) {
      final int cores = Runtime.getRuntime().availableProcessors();
      return threads > cores / 2
          ? "parseKarateOptions() threads capped from [" + threads + "] to [" + getExpectedThreads(threads) + "]"
          : null;
    }
  }

  @Override
  protected KarateRunner instantiateRunner() {
    return new KarateRunner();
  }
}
