package dev.inditex.karate.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dev.inditex.karate.console.ConsoleCLI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class OpenApiGeneratorCLITest {

  @BeforeEach
  void beforeEach() {
    ConsoleCLI.withoutRealTerminal();
  }

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiGeneratorCLI::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class Main {
    @Test
    void when_main_expect_generation_start() {
      try (
          final MockedConstruction<OpenApiGenerator> mockedGenerator = mockConstruction(OpenApiGenerator.class,
              (mock, context) -> assertThat(context.getCount()).isEqualTo(1))) {

        OpenApiGeneratorCLI.main(null);

        assertThat(mockedGenerator.constructed()).hasSize(1);
        final OpenApiGenerator generator = mockedGenerator.constructed().get(0);
        verify(generator, times(1)).execute();
      }
    }
  }
}
