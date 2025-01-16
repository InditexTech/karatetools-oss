package dev.inditex.karate.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import de.codeshelf.consoleui.elements.ConfirmChoice;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.ConfirmResult;
import de.codeshelf.consoleui.prompt.ExpandableChoiceResult;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConsoleCLITest {

  protected String title = "ConsoleCLITest";

  protected String description = "Console CLI Test";

  @BeforeEach
  void beforeEach() {
    ConsoleCLI.withoutRealTerminal();
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(ConsoleCLI::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class InitializeConsole {

    @Test
    void when_real_terminal_expect_terminal() {
      ConsoleCLI.withRealTerminal();
      final var result = ConsoleCLI.initializeConsole();

      assertThat(result).isNotNull();
    }

    @Test
    void when_not_real_terminal_expect_null() {
      final var result = ConsoleCLI.initializeConsole();

      assertThat(result).isNull();
    }
  }

  @Nested
  class Main {
    @Test
    void when_no_args_expect_no_exception() {
      final String[] args = {};

      final ThrowingCallable result = () -> ConsoleCLI.main(args);

      assertThatCode(result).doesNotThrowAnyException();
    }
  }

  @Nested
  class PromptInput {
    @Test
    void when_input_provided_expect_value() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(name, new InputResult(value));
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptInput(console, name, name, defaultValue);

      assertThat(result).isEqualTo(value);
    }

    @Test
    void when_input_not_provided_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptInput(console, name, name, defaultValue);

      assertThat(result).isEqualTo(defaultValue);
    }

    @Test
    void when_input_exception_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, true);

      final var result = ConsoleCLI.promptInput(console, name, name, defaultValue);

      assertThat(result).isEqualTo(defaultValue);
    }

  }

  @Nested
  class PromptList {
    @Test
    void when_list_provided_expect_value() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value),
          new ConsoleItem(defaultValue, defaultValue));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(name, new ListResult(value));
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptList(console, name, name, values, new ConsoleItem(defaultValue, defaultValue));

      assertThat(result).isEqualTo(value);
    }

    @Test
    void when_list_not_provided_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value, value.charAt(0), false));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptList(console, name, name, values, new ConsoleItem(defaultValue, defaultValue));

      assertThat(result).isEqualTo(defaultValue);
    }

    @Test
    void when_list_exception_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value),
          new ConsoleItem(defaultValue, defaultValue));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, true);

      final var result = ConsoleCLI.promptList(console, name, name, values, new ConsoleItem(defaultValue, defaultValue));

      assertThat(result).isEqualTo(defaultValue);
    }
  }

  @Nested
  class PromptCheckbox {
    @Test
    void when_checkbox_provided_expect_value() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final HashSet<String> valueSet = new HashSet<>();
      valueSet.add(value);
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value, value.charAt(0), false),
          new ConsoleItem(defaultValue, defaultValue, defaultValue.charAt(0), true));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(name, new CheckboxResult(valueSet));
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptCheckbox(console, name, name, values, new ConsoleItem(defaultValue, defaultValue));

      assertThat(result).isEqualTo(valueSet);
    }

    @Test
    void when_checkbox_not_provided_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value, value.charAt(0), false));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptCheckbox(console, name, name, values, new ConsoleItem(defaultValue, defaultValue));

      assertThat(result).isEqualTo(Set.of(defaultValue));
    }

    @Test
    void when_checkbox_exception_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value, value.charAt(0), false),
          new ConsoleItem(defaultValue, defaultValue, defaultValue.charAt(0), true));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, true);

      final var result = ConsoleCLI.promptCheckbox(console, name, name, values, new ConsoleItem(defaultValue, defaultValue));

      assertThat(result).isEqualTo(Set.of(defaultValue));
    }
  }

  @Nested
  class PromptConfirm {
    @Test
    void when_confirm_provided_expect_value() {
      final String name = "name";
      final Boolean defaultValue = true;
      final Boolean value = true;
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(name, new ConfirmResult(ConfirmChoice.ConfirmationValue.YES));
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptConfirm(console, name, name, defaultValue);

      assertThat(result).isEqualTo(value);
    }

    @Test
    void when_confirm_not_provided_expect_default() {
      final String name = "name";
      final Boolean defaultValue = false;
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptConfirm(console, name, name, defaultValue);

      assertThat(result).isEqualTo(defaultValue);
    }

    @Test
    void when_confirm_exception_expect_default() {
      final String name = "name";
      final Boolean defaultValue = false;
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, true);

      final var result = ConsoleCLI.promptConfirm(console, name, name, defaultValue);

      assertThat(result).isEqualTo(defaultValue);
    }

  }

  @Nested
  class PromptChoice {
    @Test
    void when_choice_provided_expect_value() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value, value.charAt(0), false),
          new ConsoleItem(defaultValue, defaultValue, defaultValue.charAt(0), true));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      mockPrompts.put(name, new ExpandableChoiceResult(value));
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptChoice(console, name, name, values);

      assertThat(result).isEqualTo(value);
    }

    @Test
    void when_choice_not_provided_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value, value.charAt(0), false),
          new ConsoleItem(defaultValue, defaultValue, defaultValue.charAt(0), true));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, false);

      final var result = ConsoleCLI.promptChoice(console, name, name, values);

      assertThat(result).isEqualTo(defaultValue);
    }

    @Test
    void when_choice_exception_expect_default() {
      final String name = "name";
      final String defaultValue = "default";
      final String value = "value";
      final List<ConsoleItem> values = List.of(
          new ConsoleItem(value, value, value.charAt(0), false),
          new ConsoleItem(defaultValue, defaultValue, defaultValue.charAt(0), true));
      final HashMap<String, PromtResultItemIF> mockPrompts = new HashMap<>();
      final DumbConsolePrompt console = new DumbConsolePrompt(mockPrompts, true);

      final var result = ConsoleCLI.promptChoice(console, name, name, values);

      assertThat(result).isEqualTo(defaultValue);
    }
  }

  @Nested
  class AddDefaultFirst {
    @ParameterizedTest
    @MethodSource
    void when_console_items_expect_default_added_first(final List<ConsoleItem> values, final ConsoleItem value,
        final List<String> expected) {

      final var result = ConsoleCLI.addDefaultFirst(values, value);

      assertThat(result.stream().map(ConsoleItem::getName).toList()).isEqualTo(expected);
    }

    private static Stream<Arguments> when_console_items_expect_default_added_first() {
      return Stream.of(
          Arguments.of(
              List.of(new ConsoleItem("001", null), new ConsoleItem("002", null)),
              new ConsoleItem("all", null),
              List.of("all", "001", "002")),
          Arguments.of(
              List.of(new ConsoleItem("all", null), new ConsoleItem("001", null), new ConsoleItem("002", null)),
              new ConsoleItem("all", null),
              List.of("all", "001", "002")),
          Arguments.of(
              List.of(new ConsoleItem("001", null), new ConsoleItem("all", null), new ConsoleItem("002", null)),
              new ConsoleItem("all", null),
              List.of("all", "001", "002")),
          Arguments.of(
              List.of(new ConsoleItem("001", null), new ConsoleItem("002", null), new ConsoleItem("all", null)),
              new ConsoleItem("all", null),
              List.of("all", "001", "002")));
    }

  }
}
