package dev.inditex.karate.openapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;

@Tag("IT")
public class OpenApiGeneratorIT extends OpenApiGeneratorTest {

  @Override
  protected void prepareGeneratorFolders(final TestInfo testInfo) throws IOException {
    final String testName = testInfo.getDisplayName() + "_" + testInfo.getTestMethod().get().getName();
    Files.createDirectories(Paths.get("target/it-karate-openapi-generator"));
    targetFolder = Files.createTempDirectory(
        Paths.get("target/it-karate-openapi-generator"), testName + "-").toFile().getAbsolutePath();
  }

}
