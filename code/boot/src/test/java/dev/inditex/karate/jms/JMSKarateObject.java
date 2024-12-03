package dev.inditex.karate.jms;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JMSKarateObject implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id;

  private final String name;

  private final int value;
}
