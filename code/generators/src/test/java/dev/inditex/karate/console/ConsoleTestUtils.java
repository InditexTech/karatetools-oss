package dev.inditex.karate.console;

import java.lang.reflect.Field;
import java.util.HashMap;

import dev.inditex.karate.openapi.OpenApiGeneratorConsole;

import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import org.springframework.util.ReflectionUtils;

public class ConsoleTestUtils {

  public static void initConsoleWithMockPrompts(final HashMap<String, PromtResultItemIF> mockPrompts, final boolean exception)
      throws NoSuchFieldException, SecurityException {
    final Field consolePromptField = OpenApiGeneratorConsole.class.getDeclaredField("consolePrompt");
    ReflectionUtils.makeAccessible(consolePromptField);
    ReflectionUtils.setField(consolePromptField, null, new DumbConsolePrompt(mockPrompts, exception));
  }

}
