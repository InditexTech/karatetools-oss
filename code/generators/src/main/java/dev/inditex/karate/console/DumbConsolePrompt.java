package dev.inditex.karate.console;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.codeshelf.consoleui.elements.PromptableElementIF;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import lombok.Getter;

/**
 * The Class DumbConsolePrompt.
 */
@Getter
public class DumbConsolePrompt extends ConsolePrompt {

  /** The mock propmts. */
  private final Map<String, PromtResultItemIF> mockPropmts;

  /** The exception. */
  private final boolean exception;

  /**
   * Instantiates a new dumb console prompt.
   *
   * @param mockPropmts the mock propmts
   * @param exception the exception
   */
  public DumbConsolePrompt(final Map<String, PromtResultItemIF> mockPropmts, final boolean exception) {
    super();
    this.mockPropmts = mockPropmts;
    this.exception = exception;
  }

  /**
   * Prompt.
   *
   * @param prompts the prompts
   * @return the hash map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public HashMap<String, PromtResultItemIF> prompt(final List<PromptableElementIF> prompts) throws IOException {
    if (exception) {
      throw new IOException("PROMPT");
    }
    return new HashMap<>(mockPropmts);
  }
}
