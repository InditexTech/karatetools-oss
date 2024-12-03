package dev.inditex.karate.openapi;

/**
 * The Class OpenApiGeneratorRuntimeException.
 */
public class OpenApiGeneratorRuntimeException extends RuntimeException {

  /** The Constant serialVersionUID. */
  protected static final long serialVersionUID = 1L;

  /**
   * Instantiates a new open api generator runtime exception.
   */
  public OpenApiGeneratorRuntimeException() {
    super();
  }

  /**
   * Instantiates a new open api generator runtime exception.
   *
   * @param e the e
   */
  public OpenApiGeneratorRuntimeException(final Exception e) {
    super(e);
  }

  /**
   * Instantiates a new open api generator runtime exception.
   *
   * @param message the message
   */
  public OpenApiGeneratorRuntimeException(final String message) {
    super(message);
  }
}
