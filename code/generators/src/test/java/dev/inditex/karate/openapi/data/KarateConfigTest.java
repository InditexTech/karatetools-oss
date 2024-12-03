package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KarateConfigTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(KarateConfig::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class UpdateKarateUrls {
    @ParameterizedTest
    @ValueSource(strings = {
        "config",
        "config-local",
        "config-local-already-defined",
        "config-local-empty-urls",
        "config-local-no-placeholder",
        "config-no-urls"})
    void when_update_config_urls_expect_result(final String file) throws IOException {
      // Copy Sample Files
      FileUtils.copyDirectory(getFileFromResources("/openapi/unit/config/"), new File(targetFolder));

      KarateConfig.updateKarateUrls(new File(FilenameUtils.separatorsToSystem(targetFolder + "/" + file + ".yml")),
          MavenArtifact.fromId("dev.inditex.karate.openapi:test"));

      assertThat(new File(FilenameUtils.separatorsToSystem(targetFolder + "/" + file + ".yml"))).exists()
          .hasContent(getResourceAsString("/openapi/unit/config/expected/" + file + ".yml"));
    }
  }
}
