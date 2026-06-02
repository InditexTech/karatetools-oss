package dev.inditex.karate.test;

import java.util.List;

import dev.inditex.karate.results.KarateOperationsStatsHook;

import io.karatelabs.cli.RunCommand;
import io.karatelabs.common.StringUtils;
import io.karatelabs.core.KarateOptionsHandler;
import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import io.karatelabs.process.ProcessBuilder;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

/**
 * The Class KarateRunner.
 */
@Slf4j
public class KarateRunner {

  /** The options. */
  protected KarateRunOptions options;

  /**
   * Instantiates a new karate runner.
   */
  public KarateRunner() {
    // Settings from system properties
  }

  /**
   * Execute.
   *
   * @return the results
   */
  public SuiteResult execute() {
    parseKarateOptions();

    final Runner.Builder builder = Runner.builder()
        .path(options.paths())
        .karateEnv(options.env())
        .tags(options.tags().toArray(new String[0]))
        .outputHtmlReport(true)
        .outputCucumberJson(true)
        .outputJunitXml(true)
        .listener(new KarateOperationsStatsHook());

    // Apply system property or env overrides to the given Builder.
    final int effectiveThreads = KarateOptionsHandler.apply(builder, options.threads());

    log.info("KarateRunner.run() with env=    [{}]", options.env());
    log.info("KarateRunner.run() with paths=  [{}]", String.join(", ", options.paths()));
    log.info("KarateRunner.run() with tags=   [{}]", String.join(", ", options.tags()));
    log.info("KarateRunner.run() with threads=[{}]", effectiveThreads);

    return builder.parallel(effectiveThreads);
  }

  /**
   * Parses the karate options from system properties using RunCommand (same as Karate internally).
   * 
   * <pre>
   * Precedence (highest => lowest):
   * We need to rewrite karate.options with any changes, for KarateOptionsHandler.apply()
   * Precedence (highest => lowest)
   * karate.options (sysprop)
   * KARATE_OPTIONS (env) - only if sysprop absent
   * individual KARATE_ENV / KARATE_CONFIG_DIR (env) - fallback
   * karate.output.dir
   * Runner.Builder values (programmatic)
   * karate-pom.json
   * defaults
   * - If **both** `karate.options` sysprop and `KARATE_OPTIONS` env var are set, the sysprop wins.
   * - If `karate.options` sets `--env qa` **and** `-Dkarate.env=dev` is also set, `karate.options` wins (it's applied last).
   * - Paths in `karate.options` **replace** Builder paths entirely (v1 parity) - not additive.
   * - A malformed `karate.options` string is logged at WARN and ignored; the run proceeds with Builder defaults rather than crashing.
   * </pre>
   */
  protected void parseKarateOptions() {
    final String karateSystemOptions = StringUtils.trimToEmpty(System.getProperty(KarateRunOptions.CONFIG_OPTIONS));
    String karateOptions = StringUtils.trimToEmpty(karateSystemOptions);

    // Parse using RunCommand (Karate 2.0 equivalent of Main.parseKarateOptions)
    final List<String> tokens = ProcessBuilder.tokenize(karateSystemOptions);
    final RunCommand command = new RunCommand();
    try {
      new CommandLine(command).parseArgs(tokens.toArray(new String[0]));
    } catch (final CommandLine.ParameterException e) {
      log.warn("invalid karate.options ignored: {}", e.getMessage());
    }

    // Cap threads to half available cores
    final int threads = capThreads(command.getThreads());
    // Rewrite only if --threads/-T was present; no-op otherwise (default passed via apply() parameter)
    karateOptions = karateOptions.replaceAll("(-T|--threads)\\s+\\d+", "--threads " + threads);

    // If no tags specified, add default to exclude @ignore
    if (command.getTags() == null || command.getTags().isEmpty()) {
      // Rewrite karate.options so KarateOptionsHandler.apply() sees the default tag
      karateOptions = karateOptions + " -t ~@ignore";
    }
    System.setProperty(KarateRunOptions.CONFIG_OPTIONS, StringUtils.trimToEmpty(karateOptions));

    options = new KarateRunOptions(
        command.getEnv() != null ? command.getEnv() : System.getProperty(KarateRunOptions.CONFIG_ENV),
        command.getPaths(),
        command.getTags(),
        threads);
  }

  /**
   * Caps threads to half available cores.
   *
   * @param threads the requested thread count (null treated as default)
   * @return the capped thread count
   */
  private int capThreads(final Integer threads) {
    final int t = threads == null || threads < 1 ? KarateRunOptions.DEFAULT_THREADS : threads;
    final int cores = Runtime.getRuntime().availableProcessors();
    if (t != 1 && t > cores / 2) {
      final int capped = Math.max(1, cores / 2);
      log.warn("parseKarateOptions() threads capped from [{}] to [{}]", t, capped);
      return capped;
    }
    return t;
  }

  /**
   * Karate run options (SPOT for all defaults and runtime values).
   *
   * @param env the karate environment
   * @param paths feature files or directories to run
   * @param tags tag expressions (each -t value; commas inside a value mean OR, multiple values mean AND)
   * @param threads parallel thread count
   */
  public record KarateRunOptions(
      String env,
      List<String> paths,
      List<String> tags,
      int threads) {

    public static final String CONFIG_OPTIONS = "karate.options";

    public static final String CONFIG_ENV = "karate.env";

    public static final String DEFAULT_ENV = "local";

    public static final List<String> DEFAULT_PATHS = List.of("classpath:");

    public static final List<String> DEFAULT_TAGS = List.of("~@ignore");

    public static final int DEFAULT_THREADS = 1;

    /** Compact constructor with defaults. */
    public KarateRunOptions {
      env = env == null || env.isBlank() ? DEFAULT_ENV : env.toLowerCase();
      paths = paths == null || paths.isEmpty() ? DEFAULT_PATHS : paths;
      tags = tags == null || tags.isEmpty() ? DEFAULT_TAGS : tags;
    }
  }
}
