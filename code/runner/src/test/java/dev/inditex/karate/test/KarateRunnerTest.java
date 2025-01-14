package dev.inditex.karate.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import dev.inditex.karate.AbstractKarateTest;

import ch.qos.logback.classic.Level;
import com.intuit.karate.Constants;
import org.junit.jupiter.api.Nested;
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
        System.setProperty(Constants.KARATE_ENV, env);
      } else {
        System.clearProperty(Constants.KARATE_ENV);
      }
      if (opts != null && !opts.isEmpty()) {
        System.setProperty(Constants.KARATE_OPTIONS, opts);
      } else {
        System.clearProperty(Constants.KARATE_OPTIONS);
      }
      final KarateRunner runner = instantiateRunner();

      final var result = runner.execute();
      final var options = runner.options;

      assertThat(result).isNotNull();
      assertThat(result.getFailCount()).isZero();
      assertThat(options).isNotNull();
      assertThat(options.getEnv()).isEqualTo(expectedEnv);
      assertThat(options.getPaths()).isEqualTo(expectedPaths);
      assertThat(options.getTags()).isEqualTo(expectedTags);
      assertThat(runner.threads).isEqualTo(expectedThreads);
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
    @ParameterizedTest
    @MethodSource("parseKarateOptionsArguments")
    void when_parse_expect_options(final String opts, final List<String> expectedTags, final int expectedThreads, final String warning) {
      if (opts != null && !opts.isEmpty()) {
        System.setProperty(Constants.KARATE_OPTIONS, opts);
      } else {
        System.clearProperty(Constants.KARATE_OPTIONS);
      }
      final KarateRunner runner = instantiateRunner();

      runner.parseKarateOptions();

      assertThat(runner.options.getTags()).isEqualTo(expectedTags);
      assertThat(runner.threads).isEqualTo(expectedThreads);
      if (warning != null) {
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.WARN)
            && log.getFormattedMessage().contains(warning));
      } else {
        assertThat(logWatcher.list).extracting("level").doesNotContain(Level.WARN);
      }
    }

    static Stream<Arguments> parseKarateOptionsArguments() {
      return Stream.of(
          Arguments.of("",
              List.of("~@ignore"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("classpath:dev/inditex/",
              null, getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("-t @TAG1 -t @TAG2 -t @~TAG3",
              List.of("@TAG1", "@TAG2", "@~TAG3"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("-t @TAG1,@TAG2",
              List.of("@TAG1,@TAG2"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("-t @TAG1,@TAG2 -t @TAG3 -t ~@TAG4",
              List.of("@TAG1,@TAG2", "@TAG3", "~@TAG4"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("--threads 1",
              List.of("~@ignore"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("-t @TAG1 --threads 1",
              List.of("@TAG1"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("--threads 1 -t @TAG2",
              List.of("@TAG2"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("-t @TAG1 --threads 1 -t @TAG2",
              List.of("@TAG1", "@TAG2"), getExpectedThreads(1), getExpectedThreadsWarning(1)),
          Arguments.of("--threads 2",
              List.of("~@ignore"), getExpectedThreads(2), getExpectedThreadsWarning(2)),
          Arguments.of("--threads 5",
              List.of("~@ignore"), getExpectedThreads(5), getExpectedThreadsWarning(5)),
          Arguments.of("--threads 10",
              List.of("~@ignore"), getExpectedThreads(10), getExpectedThreadsWarning(10))

      );
    }

    private static int getExpectedThreads(final int threads) {
      final int cores = Runtime.getRuntime().availableProcessors();
      return Math.min(threads, Math.max(1, cores / 2));
    }

    private static String getExpectedThreadsWarning(final int threads) {
      final int cores = Runtime.getRuntime().availableProcessors();
      return threads > cores / 2
          ? "parseKarateOptions() karateThreads Capped from [" + threads + "] to [" + getExpectedThreads(threads) + "]"
          : null;
    }
  }

  @Override
  protected KarateRunner instantiateRunner() {
    return new KarateRunner();
  }
}
