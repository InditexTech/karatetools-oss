package dev.inditex.karate.console;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The Class ConsoleItem.
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class ConsoleItem {

  /** The name. */
  private final String name;

  /** The text. */
  private final String text;

  /** The key. */
  private char key;

  /** The as default. */
  private boolean asDefault;
}
