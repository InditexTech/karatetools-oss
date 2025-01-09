package dev.inditex.karate.openapi;

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
    final OpenApiGenerator generator = new OpenApiGenerator();
    generator.execute();
  }

}
