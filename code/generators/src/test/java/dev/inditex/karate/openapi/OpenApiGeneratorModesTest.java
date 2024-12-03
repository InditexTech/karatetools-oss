package dev.inditex.karate.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class OpenApiGeneratorModesTest {

  protected String defaultModesAsText = "(Operations / Smoke Tests / Functional Test / Mock Data)";

  protected List<OpenApiGeneratorMode> defaultModes = Arrays.asList(
      OpenApiGeneratorModes.OPERATIONS,
      OpenApiGeneratorModes.SMOKE_TESTS,
      OpenApiGeneratorModes.FUNCTIONAL_TEST,
      OpenApiGeneratorModes.MOCK_DATA);

  @BeforeEach
  protected void beforeEach() {
    final LinkedHashSet<OpenApiGeneratorMode> modes = new LinkedHashSet<>(Arrays.asList(
        OpenApiGeneratorModes.OPERATIONS,
        OpenApiGeneratorModes.SMOKE_TESTS,
        OpenApiGeneratorModes.FUNCTIONAL_TEST,
        OpenApiGeneratorModes.MOCK_DATA));
    OpenApiGeneratorModes.setAvailableModes(modes);
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiGeneratorModes::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GetAvailableModesAsStream {
    @Test
    void when_invoked_expect_default_modes() {

      final var result = OpenApiGeneratorModes.getAvailableModesAsStream();

      assertThat(result).containsExactly(defaultModes.toArray(OpenApiGeneratorMode[]::new));
    }

    @Test
    void when_added_mode_expect_mode_added() {
      final OpenApiGeneratorMode newMode = new OpenApiGeneratorMode("New Mode", "Open Api New Mode");
      final LinkedHashSet<OpenApiGeneratorMode> modes = new LinkedHashSet<>(Arrays.asList(
          OpenApiGeneratorModes.OPERATIONS,
          OpenApiGeneratorModes.SMOKE_TESTS,
          OpenApiGeneratorModes.FUNCTIONAL_TEST,
          OpenApiGeneratorModes.MOCK_DATA, newMode));
      OpenApiGeneratorModes.setAvailableModes(modes);

      final var result = OpenApiGeneratorModes.getAvailableModesAsStream();

      assertThat(result).containsExactly(
          OpenApiGeneratorModes.OPERATIONS,
          OpenApiGeneratorModes.SMOKE_TESTS,
          OpenApiGeneratorModes.FUNCTIONAL_TEST,
          OpenApiGeneratorModes.MOCK_DATA, newMode);
    }
  }

  @Nested
  class GetAvailableModesAsText {
    @Test
    void when_invoked_expect_default_values() {

      final var result = OpenApiGeneratorModes.getAvailableModesAsText();

      assertThat(result).isEqualTo(defaultModesAsText);
    }

    @Test
    void when_added_mode_expect_mode_added() {
      final OpenApiGeneratorMode newMode = new OpenApiGeneratorMode("New Mode", "Open Api New Mode");
      final LinkedHashSet<OpenApiGeneratorMode> modes = new LinkedHashSet<>(Arrays.asList(
          OpenApiGeneratorModes.OPERATIONS,
          OpenApiGeneratorModes.SMOKE_TESTS,
          OpenApiGeneratorModes.FUNCTIONAL_TEST,
          OpenApiGeneratorModes.MOCK_DATA, newMode));
      OpenApiGeneratorModes.setAvailableModes(modes);

      final var result = OpenApiGeneratorModes.getAvailableModesAsText();

      assertThat(result).isEqualTo("(Operations / Smoke Tests / Functional Test / Mock Data / New Mode)");
    }
  }

  @Nested
  class ValueOf {
    @ParameterizedTest
    @ValueSource(strings = {"Operations", "Smoke Tests", "Functional Test", "Mock Data"})
    void when_valid_expect_mode(final String value) {

      final var result = OpenApiGeneratorModes.valueOf(value);

      assertThat(result).isNotNull();
      assertThat(result.name()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Invalid"})
    @NullSource
    @EmptySource
    void when_invalid_expect_null(final String value) {

      final var result = OpenApiGeneratorModes.valueOf(value);

      assertThat(result).isNull();
    }

    @Test
    void when_added_mode_expect_mode_added() {
      final OpenApiGeneratorMode newMode = new OpenApiGeneratorMode("New Mode", "Open Api New Mode");
      OpenApiGeneratorModes.setAvailableModes(new LinkedHashSet<>(Set.of(
          OpenApiGeneratorModes.OPERATIONS,
          OpenApiGeneratorModes.SMOKE_TESTS,
          OpenApiGeneratorModes.FUNCTIONAL_TEST,
          OpenApiGeneratorModes.MOCK_DATA, newMode)));

      final var result = OpenApiGeneratorModes.valueOf("New Mode");

      assertThat(result).isNotNull();
      assertThat(result.name()).isEqualTo("New Mode");
    }
  }
}
