package dev.inditex.karate.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.codeshelf.consoleui.elements.ConfirmChoice;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.ConfirmResult;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.ExpandableChoiceResult;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import de.codeshelf.consoleui.prompt.builder.CheckboxPromptBuilder;
import de.codeshelf.consoleui.prompt.builder.ConfirmPromptBuilder;
import de.codeshelf.consoleui.prompt.builder.ExpandableChoicePromptBuilder;
import de.codeshelf.consoleui.prompt.builder.InputValueBuilder;
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import jline.TerminalFactory;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.AnsiConsole;

/**
 * The Class ConsoleCLI.
 */
@Slf4j
public class ConsoleCLI {

  /** The Constant PAGE_SIZE. */
  protected static final int PAGE_SIZE = 20;

  /** The with real terminal. */
  protected static boolean withRealTerminal = true;

  /**
   * Instantiates a new console CLI.
   */
  protected ConsoleCLI() {
  }

  /**
   * Main.
   *
   * @param args the arguments
   */
  public static void main(final String[] args) {
    ConsoleCLI.withRealTerminal();
    final var console = ConsoleCLI.initializeConsole();
    log.info("ConsoleCLI.main Console {}", console);
    if (console == null) {
      log.error("ConsoleCLI.main Console is null");
      System.exit(1);
    }
  }

  /**
   * Without real terminal.
   */
  public static void withoutRealTerminal() {
    withRealTerminal = false;
  }

  /**
   * With real terminal.
   */
  public static void withRealTerminal() {
    withRealTerminal = true;
  }

  /**
   * Initialize console.
   *
   * @return the console prompt
   */
  public static ConsolePrompt initializeConsole() {
    ConsolePrompt consolePrompt = null;
    if (withRealTerminal) {
      try {
        AnsiConsole.systemInstall();
        consolePrompt = new ConsolePrompt();
      } catch (final Exception e) {
        log.warn("initializeConsole Exception {}", e.getMessage());
      }
    }
    return consolePrompt;
  }

  /**
   * Restore console.
   */
  public static void restoreConsole() {
    try {
      TerminalFactory.get().restore();
      AnsiConsole.systemUninstall();
    } catch (final Exception e) {
      log.warn("restoreConsole Exception {}", e.getMessage());
    }
  }

  /**
   * Prompt input.
   *
   * @param consolePrompt the console prompt
   * @param name the name
   * @param text the text
   * @param defaultValue the default value
   * @return the string
   */
  public static String promptInput(final ConsolePrompt consolePrompt, final String name, final String text, final String defaultValue) {
    final PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();
    final InputValueBuilder builder = promptBuilder.createInputPrompt()
        .name(name)
        .message(text)
        .defaultValue(defaultValue);
    builder.addPrompt();
    try {
      final PromtResultItemIF result = prompt(consolePrompt, promptBuilder, name);
      return result != null ? ((InputResult) result).getInput() : defaultValue;
    } catch (final IOException e) {
      log.error("ConsoleCLI.promptInput({}) Exception {}", name, e.getMessage());
      log.warn("ConsoleCLI.promptInput({}) Invalid Parameter, using defaults: {}", name, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Prompt list.
   *
   * @param consolePrompt the console prompt
   * @param name the name
   * @param text the text
   * @param values the values
   * @param defaultValue the default value
   * @return the string
   */
  public static String promptList(final ConsolePrompt consolePrompt, final String name, final String text,
      final List<ConsoleItem> values, final ConsoleItem defaultValue) {
    final PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();
    final ListPromptBuilder builder = promptBuilder.createListPrompt().pageSize(PAGE_SIZE)
        .name(name)
        .message(text);
    final List<ConsoleItem> items = addDefaultFirst(values, defaultValue);
    for (final ConsoleItem item : items) {
      builder.newItem(item.getName()).text(item.getText()).add();
    }
    builder.addPrompt();
    try {
      final PromtResultItemIF result = prompt(consolePrompt, promptBuilder, name);
      return result != null ? ((ListResult) result).getSelectedId() : defaultValue.getName();
    } catch (final IOException e) {
      log.error("ConsoleCLI.promptList({}) Exception {}", name, e.getMessage());
      log.warn("ConsoleCLI.promptList({}) Invalid Parameter, using defaults: {}", name, defaultValue);
    }
    return defaultValue.getName();
  }

  /**
   * Prompt checkbox.
   *
   * @param consolePrompt the console prompt
   * @param name the name
   * @param text the text
   * @param values the values
   * @param defaultValue the default value
   * @return the sets the
   */
  public static Set<String> promptCheckbox(final ConsolePrompt consolePrompt, final String name, final String text,
      final List<ConsoleItem> values, final ConsoleItem defaultValue) {
    final PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();
    final CheckboxPromptBuilder builder = promptBuilder.createCheckboxPrompt().pageSize(PAGE_SIZE)
        .name(name)
        .message(text)
        .pageSize(10);
    final List<ConsoleItem> items = addDefaultFirst(values, defaultValue);
    for (final ConsoleItem item : items) {
      builder.newItem(item.getName()).text(item.getText()).add();
    }
    builder.addPrompt();
    try {
      final PromtResultItemIF result = prompt(consolePrompt, promptBuilder, name);
      return result != null ? ((CheckboxResult) result).getSelectedIds() : Set.of(defaultValue.getName());
    } catch (final IOException e) {
      log.error("ConsoleCLI.promptCheckBox({}) Exception {}", name, e.getMessage());
      log.warn("ConsoleCLI.promptCheckBox({}) Invalid Parameter, using defaults: {}", name, defaultValue.getName());
    }
    return Set.of(defaultValue.getName());
  }

  /**
   * Prompt confirm.
   *
   * @param consolePrompt the console prompt
   * @param name the name
   * @param text the text
   * @param defaultValue the default value
   * @return true, if successful
   */
  public static boolean promptConfirm(final ConsolePrompt consolePrompt, final String name, final String text, final Boolean defaultValue) {
    final PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();
    final ConfirmPromptBuilder builder = promptBuilder.createConfirmPromp()
        .name(name)
        .message(text)
        .defaultValue(Boolean.TRUE.equals(defaultValue) ? ConfirmChoice.ConfirmationValue.YES : ConfirmChoice.ConfirmationValue.NO);
    builder.addPrompt();
    try {
      final PromtResultItemIF result = prompt(consolePrompt, promptBuilder, name);
      return result != null ? ((ConfirmResult) result).getConfirmed() == ConfirmChoice.ConfirmationValue.YES : defaultValue;
    } catch (final IOException e) {
      log.error("ConsoleCLI.promptConfim({}) Exception {}", name, e.getMessage());
      log.warn("ConsoleCLI.promptConfim({}) Invalid Parameter, using defaults: {}", name, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Prompt choice.
   *
   * @param consolePrompt the console prompt
   * @param name the name
   * @param message the message
   * @param values the values
   * @return the string
   */
  public static String promptChoice(final ConsolePrompt consolePrompt, final String name, final String message,
      final List<ConsoleItem> values) {
    String defaultValue = null;
    ConsoleItem defaultItem = null;
    final PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();
    final ExpandableChoicePromptBuilder builder = promptBuilder.createChoicePrompt()
        .name(name)
        .message(message);
    for (final ConsoleItem value : values) {
      if (value.isAsDefault()) {
        defaultItem = value;
        defaultValue = defaultItem.getName();
      }
    }
    final List<ConsoleItem> items = addDefaultFirst(values, defaultItem);
    for (final ConsoleItem item : items) {
      if (item.isAsDefault()) {
        builder.newItem(item.getName()).message(item.getText()).key(item.getKey()).asDefault().add();
      } else {
        builder.newItem(item.getName()).message(item.getText()).key(item.getKey()).add();
      }
    }
    builder.addPrompt();
    try {
      final PromtResultItemIF result = prompt(consolePrompt, promptBuilder, name);
      return result != null ? ((ExpandableChoiceResult) result).getSelectedId() : defaultValue;
    } catch (final IOException e) {
      log.error("ConsoleCLI.promptChoice({}) Exception {}", name, e.getMessage());
      log.warn("ConsoleCLI.promptChoice({}) Invalid Parameter, using defaults: {}", name, defaultValue);
    }
    return defaultValue;
  }

  /**
   * Adds the default first.
   *
   * @param values the values
   * @param value the value
   * @return the list
   */
  protected static List<ConsoleItem> addDefaultFirst(final List<ConsoleItem> values, final ConsoleItem value) {
    final List<ConsoleItem> items = new ArrayList<>(values);
    final ConsoleItem item = items.stream().filter(ci -> ci.getName().equals(value.getName())).findAny().orElseGet(() -> {
      items.add(value);
      return value;
    });
    items.remove(item);
    items.add(0, item);
    return items;
  }

  /**
   * Prompt.
   *
   * @param <T> the generic type
   * @param consolePrompt the console prompt
   * @param promptBuilder the prompt builder
   * @param name the name
   * @return the t
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("unchecked")
  protected static <T extends PromtResultItemIF> T prompt(final ConsolePrompt consolePrompt,
      final PromptBuilder promptBuilder, final String name) throws IOException {
    return (T) consolePrompt.prompt(promptBuilder.build()).get(name);
  }
}
