package dev.inditex.karate.openapi.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.inditex.karate.openapi.OpenApiGeneratorRuntimeException;

/**
 * The Class KarateWriter.
 */
public class KarateWriter {

  /**
   * Instantiates a new karate writer.
   */
  protected KarateWriter() {
  }

  /**
   * Unchecked dir exists.
   *
   * @param path the path
   */
  public static void uncheckedDirExists(final Path path) {
    try {
      if (!Files.exists(path)) {
        Files.createDirectories(path);
      }
    } catch (final IOException e) {
      throw new OpenApiGeneratorRuntimeException(e);
    }
  }

}
