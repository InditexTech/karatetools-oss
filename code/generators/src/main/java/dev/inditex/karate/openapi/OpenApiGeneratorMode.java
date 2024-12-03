package dev.inditex.karate.openapi;

/**
 * The Class OpenApiGeneratorMode.
 */
public class OpenApiGeneratorMode {

  /** The name value. */
  public final String nameValue;

  /** The label value. */
  public final String labelValue;

  /**
   * Instantiates a new open api generator mode.
   *
   * @param name the name
   * @param label the label
   */
  public OpenApiGeneratorMode(final String name, final String label) {
    this.nameValue = name;
    this.labelValue = label;
  }

  /**
   * Name.
   *
   * @return the string
   */
  public String name() {
    return nameValue;
  }

  /**
   * Label.
   *
   * @return the string
   */
  public String label() {
    return labelValue;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return name();
  }
}
