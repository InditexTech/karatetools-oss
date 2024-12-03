package dev.inditex.karate.openapi.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * The Class KarateConfig.
 */
@Slf4j
public class KarateConfig {

  /**
   * The Record ConfigYaml.
   *
   * @param urls the urls
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonDeserialize(using = ConfigUrlsDeserializer.class)
  record ConfigYaml(Map<String, String> urls) {
    // Record
  }

  /** The Constant KARATE_CONFIG_URL_LOCATOR_PATTERN. */
  protected static final String KARATE_CONFIG_URL_LOCATOR_PATTERN = "#karate-utils-new-karate-url-marker(.*)";

  /** The Constant KARATE_CONFIG_URL_LOCATOR_FORMAT. */
  protected static final String KARATE_CONFIG_URL_LOCATOR_FORMAT =
      "%s: \"#('http://localhost:' + (karate.properties['APP_PORT'] || 8080) + '/TO_BE_COMPLETED')\"%n"
          + "  #karate-utils-new-karate-url-marker (do not remove) - new generated apis urls will be placed here automatically";

  /**
   * Instantiates a new karate config.
   */
  protected KarateConfig() {
  }

  /**
   * Update karate urls.
   *
   * @param targetFolder the target folder
   * @param artifact the artifact
   */
  public static void updateKarateUrls(final String targetFolder, final MavenArtifact artifact) {
    final Collection<File> files = findConfigFiles(targetFolder);
    for (final File file : files) {
      updateKarateUrls(file, artifact);
    }
  }

  /**
   * Update karate urls.
   *
   * @param file the file
   * @param artifact the artifact
   */
  protected static void updateKarateUrls(final File file, final MavenArtifact artifact) {
    final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    final String apiUrl = artifact.toApiUrl();
    try {
      log.info("updateKarateUrls() checking [{}]", file);
      boolean isApiUrlDefined = false;
      // Load file as yaml
      final ConfigYaml configYaml = mapper.readValue(file, ConfigYaml.class);
      // Check if apiUrl is defined
      if (configYaml.urls != null) {
        isApiUrlDefined = configYaml.urls.get(apiUrl) != null;
        // Add if apiUrl is not defined
        if (!isApiUrlDefined) {
          final String text = String.format(KARATE_CONFIG_URL_LOCATOR_FORMAT, apiUrl);
          String content = "";
          try (final FileInputStream is = new FileInputStream(file)) {
            content = IOUtils.toString(is, StandardCharsets.UTF_8);
          }
          content = content.replaceFirst(KARATE_CONFIG_URL_LOCATOR_PATTERN, text);
          try (final FileOutputStream os = new FileOutputStream(file)) {
            IOUtils.write(content, os, StandardCharsets.UTF_8);
          }
          log.info("updateKarateUrls() for [{}] in [{}]", apiUrl, file);
        }
      } else {
        log.warn("updateKarateUrls() skipping file [{}] without urls ", file, file);
      }

    } catch (final IOException e) {
      log.error("updateKarateUrls() for [{}] EXCEPTION [{}]", apiUrl, e.getMessage());
    }
  }

  /**
   * Find config files.
   *
   * @param targetFolder the target folder
   * @return the collection
   */
  protected static Collection<File> findConfigFiles(final String targetFolder) {
    final File root = new File(targetFolder);
    return FileUtils.listFiles(root, new IOFileFilter() {
      @Override
      public boolean accept(final File dir, final String name) {
        return name.startsWith("config") && name.endsWith(".yml");
      }

      @Override
      public boolean accept(final File file) {
        return file.isFile() && file.getName().startsWith("config") && file.getName().endsWith(".yml");
      }
    }, null);
  }

  /**
   * The Class ConfigUrlsDeserializer.
   */
  protected static class ConfigUrlsDeserializer extends JsonDeserializer<ConfigYaml> {

    /**
     * Deserialize.
     *
     * @param jp the jp
     * @param ctxt the ctxt
     * @return the config yaml
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("unchecked")
    @Override
    public ConfigYaml deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
      Map<String, String> urls = null;
      final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
      final ObjectNode root = mapper.readTree(jp);
      final var urlsNode = root.path("urls");
      if (urlsNode.isNull()) {
        // Urls defined empty - empty Map
        urls = new HashMap<>();
      } else if (!urlsNode.isMissingNode()) {
        // Urls defined not empty - use default mapping
        urls = mapper.readValue(mapper.writeValueAsString(urlsNode), Map.class);
      }
      // Urls not defined - null
      return new ConfigYaml(urls);
    }
  }
}
