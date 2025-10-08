package dev.inditex.karate.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class HttpLogMaskingTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(HttpLogMasking::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class EnableForUri {
    @Test
    void when_any_uri_expect_true() {
      final HttpLogMasking masking = new HttpLogMasking();

      final boolean enabled = masking.enableForUri("/any/uri");

      assertThat(enabled).isTrue();
    }
  }

  @Nested
  class Uri {
    @ParameterizedTest
    @ValueSource(strings = {"/test/uri", "http://example.com", ""})
    void when_any_uri_expect_unchanged(final String uri) {
      final HttpLogMasking masking = new HttpLogMasking();

      final String result = masking.uri(uri);

      assertThat(result).isEqualTo(uri);
    }
  }

  @Nested
  class Header {
    static Stream<Arguments> getTestHeadersArguments() {
      return Stream.of(
          // Values
          Arguments.of("Auth", "Bearer abc.def.ghi", "Bearer *****"),
          Arguments.of("auth", "Bearer abc.def.ghi", "Bearer *****"),
          Arguments.of("Auth", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", "Basic *****"),
          // Exact Headers
          Arguments.of("Authorization", "secret-value", "*****"),
          Arguments.of("authorization", "secret-value", "*****"),
          Arguments.of("Token", "secret-value", "*****"),
          Arguments.of("token", "secret-value", "*****"),
          Arguments.of("Secret", "secret-value", "*****"),
          Arguments.of("secret", "secret-value", "*****"),
          Arguments.of("Key", "secret-value", "*****"),
          Arguments.of("key", "secret-value", "*****"),
          Arguments.of("Username", "secret-value", "*****"),
          Arguments.of("username", "secret-value", "*****"),
          Arguments.of("Password", "secret-value", "*****"),
          Arguments.of("password", "secret-value", "*****"),
          // Partial Headers
          Arguments.of("X-Authorization", "secret-value", "*****"),
          Arguments.of("X-Token", "secret-value", "*****"),
          Arguments.of("X-Secret", "secret-value", "*****"),
          Arguments.of("X-Key", "secret-value", "*****"),
          Arguments.of("X-OpenAM-Username", "secret-value", "*****"),
          Arguments.of("X-OpenAM-Password", "secret-value", "*****"));
    }

    @ParameterizedTest
    @MethodSource("getTestHeadersArguments")
    void when_sensitive_header_expect_masked(final String header, final String value, final String expected) {
      final HttpLogMasking masking = new HttpLogMasking();

      final String result = masking.header(header, value);

      assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("getTestHeadersArguments")
    void when_disabled_sensitive_header_expect_original(final String header, final String value, final String expected) {
      final HttpLogMasking masking = new HttpLogMasking();
      masking.setEnabled(false);

      final String result = masking.header(header, value);

      assertThat(result).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({
        "X-NotSensitive,value,value",
        "null,null,null",
        "X-Api-Key,null,null",
        "null,secret-value,secret-value",
        "Security,secret-value,secret-value"
    })
    void when_null_or_non_sensitive_expect_unchanged(final String header, final String value, final String expected) {
      final HttpLogMasking masking = new HttpLogMasking();
      final String h = "null".equals(header) ? null : header;
      final String v = "null".equals(value) ? null : value;
      final String e = "null".equals(expected) ? null : expected;

      final String result = masking.header(h, v);

      assertThat(result).isEqualTo(e);
    }
  }

  @Nested
  class Request {
    @Test
    void when_any_request_expect_unchanged() {
      final HttpLogMasking masking = new HttpLogMasking();

      final String result = masking.request("/any/uri", "any request body");

      assertThat(result).isEqualTo("any request body");
    }
  }

  @Nested
  class Response {
    @Test
    void when_any_response_expect_unchanged() {
      final HttpLogMasking masking = new HttpLogMasking();

      final String result = masking.response("/any/uri", "any response body");

      assertThat(result).isEqualTo("any response body");
    }
  }

  @Nested
  class SetEnabled {
    @Test
    void when_set_enabled_expect_no_exception() {
      final HttpLogMasking masking = new HttpLogMasking();

      assertThatCode(() -> masking.setEnabled(false)).doesNotThrowAnyException();
      assertThatCode(() -> masking.setEnabled(true)).doesNotThrowAnyException();
    }
  }

  @Nested
  class IsEnabled {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void when_set_enabled_expect_reflect_value(final boolean value) {
      final HttpLogMasking masking = new HttpLogMasking();
      masking.setEnabled(value);

      final boolean result = masking.isEnabled();
      assertThat(result).isEqualTo(value);
    }
  }
}
