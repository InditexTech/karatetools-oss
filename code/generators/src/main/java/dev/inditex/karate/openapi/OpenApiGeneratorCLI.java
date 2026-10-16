package dev.inditex.karate.openapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class OpenApiGeneratorCLI.
 */
public class OpenApiGeneratorCLI {

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    // Show the implementation version of the generator actually loaded at runtime.
    final String version = getImplementationVersion();
    OpenApiGeneratorANSILogger.info("KarateTools OpenAPI Generator version: " + (version == null ? "unknown" : version));
    final OpenApiGenerator generator = new OpenApiGenerator();
    generator.execute();
  }

  private static String getImplementationVersion() {
    final String packageVersion = OpenApiGeneratorCLI.class.getPackage().getImplementationVersion();
    if (packageVersion != null) {
      return packageVersion;
    }
    final String metadataPath = "/META-INF/maven/dev.inditex.karate/karatetools-generators/pom.properties";
    try (InputStream metadata = OpenApiGeneratorCLI.class.getResourceAsStream(metadataPath)) {
      if (metadata != null) {
        final Properties properties = new Properties();
        properties.load(metadata);
        return properties.getProperty("version");
      }
    } catch (final IOException e) {
      return null;
    }
    return null;
  }

}
