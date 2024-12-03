package dev.inditex.karate.openapi;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenApiGeneratorRuntimeExceptionTest {

  @Nested
  class Constructor {
    @Test
    void when_no_args_expect_no_exception() {
      assertThatCode(OpenApiGeneratorRuntimeException::new).doesNotThrowAnyException();
    }

    @Test
    void when_exception_expect_no_exception() {
      assertThatCode(() -> {
        new OpenApiGeneratorRuntimeException(new Exception());
      }).doesNotThrowAnyException();
    }

    @Test
    void when_message_expect_no_exception() {
      assertThatCode(() -> {
        new OpenApiGeneratorRuntimeException("message");
      }).doesNotThrowAnyException();
    }
  }

}
