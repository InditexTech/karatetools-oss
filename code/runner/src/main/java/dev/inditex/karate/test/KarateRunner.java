package dev.inditex.karate.test;

import java.util.Arrays;
import java.util.Optional;

import dev.inditex.karate.results.KarateOperationsStatsHook;

import com.intuit.karate.Constants;
import com.intuit.karate.Main;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class KarateRunner.
 */
@Slf4j
public class KarateRunner {

  /** The options. */
  protected Main options;

  /** The threads. */
  protected int threads = 1;

  /** The classpath. */
  protected final String[] classpath = {"classpath:"};

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
  @SuppressWarnings("unchecked")
  public Results execute() {
    parseKarateOptions();
    options.setEnv(StringUtils.trimToEmpty(System.getProperty(Constants.KARATE_ENV, "local")).toLowerCase());
    options.setPaths(Optional.ofNullable(options.getPaths()).orElse(Arrays.asList(classpath)));
    log.info("KarateRunner.run() with env=    [{}]", options.getEnv());
    log.info("KarateRunner.run() with paths=  [{}]", options.getPaths());
    log.info("KarateRunner.run() with tags=   [{}]", options.getTags());
    log.info("KarateRunner.run() with threads=[{}]", threads);

    return Runner.path(options.getPaths())
        .hooks(options.createHooks())
        .tags(options.getTags())
        .configDir(options.getConfigDir())
        .karateEnv(options.getEnv())
        .outputHtmlReport(true)
        .outputCucumberJson(true)
        .outputJunitXml(true)
        .hook(new KarateOperationsStatsHook())
        .parallel(threads);
  }

  /**
   * Parses the karate options.
   */
  protected void parseKarateOptions() {
    final String karateSystemOptions = StringUtils.trimToEmpty(System.getProperty(Constants.KARATE_OPTIONS));

    int karateThreads = 1;
    try {
      final String threadsKey = "--threads ";
      if (karateSystemOptions.contains(threadsKey)) {
        final String threadsStart = karateSystemOptions.split(threadsKey)[1];
        final String threadsStr = threadsStart.split(" ")[0];
        karateThreads = Integer.parseInt(threadsStr);
        final int cores = Runtime.getRuntime().availableProcessors();
        if (karateThreads != 1 && karateThreads > cores / 2) {
          log.warn("parseKarateOptions() karateThreads Capped from [{}] to [{}]", karateThreads, Math.max(1, cores / 2));
          karateThreads = Math.max(1, cores / 2);
        }
      }
    } catch (final RuntimeException e) {
      log.warn("parseKarateOptions() karateThreads Exception [{}] [{}]", e.getMessage(), e);
    }
    threads = karateThreads;
    String karateOptions = StringUtils.trimToEmpty(karateSystemOptions);
    // Custom Options must be removed before Karate Parsing to avoid Error "UnmatchedArgumentException"
    karateOptions = karateOptions.replaceAll("--threads (\\d+)", "");
    // Add exclusion of @ignore if karate.options not defined
    if (StringUtils.trimToEmpty(karateOptions).isEmpty()) {
      karateOptions = "-t ~@ignore ";
    }
    System.setProperty(Constants.KARATE_OPTIONS, karateOptions);
    options = Main.parseKarateOptions(karateOptions);
  }
}
