package dev.inditex.karate.openapi;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The Class OpenApiGeneratorModes.
 */
public class OpenApiGeneratorModes {

  /** The Constant OPERATIONS. */
  public static final OpenApiGeneratorMode OPERATIONS = new OpenApiGeneratorMode("Operations", "Open Api Operations");

  /** The Constant SMOKE_TESTS. */
  public static final OpenApiGeneratorMode SMOKE_TESTS = new OpenApiGeneratorMode("Smoke Tests", "Open Api Smoke Tests");

  /** The Constant FUNCTIONAL_TEST. */
  public static final OpenApiGeneratorMode FUNCTIONAL_TEST = new OpenApiGeneratorMode("Functional Test", "Open Api Functional Test");

  /** The Constant MOCK_DATA. */
  public static final OpenApiGeneratorMode MOCK_DATA = new OpenApiGeneratorMode("Mock Data", "Open Api Mock Data");

  /** The Constant AVAILABLE. */
  protected static final LinkedHashSet<OpenApiGeneratorMode> AVAILABLE =
      new LinkedHashSet<>(Arrays.asList(OPERATIONS, SMOKE_TESTS, FUNCTIONAL_TEST, MOCK_DATA));

  /**
   * Instantiates a new open api generator modes.
   */
  protected OpenApiGeneratorModes() {
  }

  /**
   * Sets the available modes.
   *
   * @param modes the new available modes
   */
  public static void setAvailableModes(final Set<OpenApiGeneratorMode> modes) {
    AVAILABLE.clear();
    AVAILABLE.addAll(modes);
  }

  /**
   * Gets the available modes as stream.
   *
   * @return the available modes as stream
   */
  public static Stream<OpenApiGeneratorMode> getAvailableModesAsStream() {
    return AVAILABLE.stream();
  }

  /**
   * Gets the available modes as text.
   *
   * @return the available modes as text
   */
  public static String getAvailableModesAsText() {
    return String.format("(%s)", getAvailableModesAsStream().map(m -> m.name()).reduce((a, b) -> a + " / " + b).orElse(""));
  }

  /**
   * Value of.
   *
   * @param value the value
   * @return the open api generator mode
   */
  public static OpenApiGeneratorMode valueOf(final String value) {
    return getAvailableModesAsStream().filter(m -> m.name().equals(value)).findFirst().orElse(null);
  }

}
