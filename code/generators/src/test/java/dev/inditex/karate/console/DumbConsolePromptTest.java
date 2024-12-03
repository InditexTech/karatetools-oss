package dev.inditex.karate.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.codeshelf.consoleui.elements.ConfirmChoice;
import de.codeshelf.consoleui.elements.ConfirmChoice.ConfirmationValue;
import de.codeshelf.consoleui.elements.InputValue;
import de.codeshelf.consoleui.elements.PromptableElementIF;
import de.codeshelf.consoleui.prompt.ConfirmResult;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DumbConsolePromptTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(() -> new DumbConsolePrompt(Map.of(), false)).doesNotThrowAnyException();
    }
  }

  @Nested
  class Prompt {

    @Test
    void when_informed_expect_prompts() throws IOException {
      final List<PromptableElementIF> prompts = List.of(
          new InputValue("input", "input-message"),
          new ConfirmChoice("confirm", "confirm-message"));
      final Map<String, PromtResultItemIF> results = Map.of(
          "input", new InputResult("input-result"),
          "confirm", new ConfirmResult(ConfirmationValue.YES));
      final DumbConsolePrompt consolePrompt = new DumbConsolePrompt(results, false);

      final var result = consolePrompt.prompt(prompts);

      assertThat(result).isEqualTo(results);
    }

    @Test
    void when_exception_expect_exception() {
      final DumbConsolePrompt consolePrompt = new DumbConsolePrompt(Map.of(), true);

      assertThatThrownBy(() -> consolePrompt.prompt(List.of()))
          .isInstanceOf(IOException.class)
          .hasMessage("PROMPT");
    }
  }
}
