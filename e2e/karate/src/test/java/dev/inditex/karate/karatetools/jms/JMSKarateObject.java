package dev.inditex.karate.karatetools.jms;

import java.io.Serializable;

public record JMSKarateObject(
    String id,
    String name,
    int value) implements Serializable {
  private static final long serialVersionUID = 1L;
}
