package dev.inditex.karate.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Set;
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

    @ParameterizedTest
    @MethodSource
    void when_instance_with_params_expect_no_exception(final String maskValue, final Set<String> sensitiveHeaders) {
      assertThatCode(() -> new HttpLogMasking(maskValue, sensitiveHeaders)).doesNotThrowAnyException();
    }

    static Stream<Arguments> when_instance_with_params_expect_no_exception() {
      return Stream.of(
          Arguments.of("MASKED", Set.of("custom-header")),
          Arguments.of("", Set.of("custom-header")),
          Arguments.of(null, Set.of("custom-header")),
          Arguments.of("MASKED", Set.of()),
          Arguments.of("MASKED", null),
          Arguments.of(null, null),
          Arguments.of("", null),
          Arguments.of(null, Set.of()));
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
    @ParameterizedTest
    @CsvSource({
        // Values
        "Auth,Bearer abc.def.ghi,Bearer *****",
        "auth,Bearer abc.def.ghi,Bearer *****",
        "Auth,Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==,Basic *****",
        // Exact Headers
        "Authorization,secret-value,*****",
        "authorization,secret-value,*****",
        "Token,secret-value,*****",
        "token,secret-value,*****",
        "Secret,secret-value,*****",
        "secret,secret-value,*****",
        "Key,secret-value,*****",
        "key,secret-value,*****",
        "Username,secret-value,*****",
        "username,secret-value,*****",
        "Password,secret-value,*****",
        "password,secret-value,*****",
        // Partial Headers
        "X-Authorization,secret-value,*****",
        "X-Token,secret-value,*****",
        "X-Secret,secret-value,*****",
        "X-Key,secret-value,*****",
        "X-OpenAM-Username,secret-value,*****",
        "X-OpenAM-Password,secret-value,*****"
    })
    void when_sensitive_header_expect_masked(final String header, final String value, final String expected) {
      final HttpLogMasking masking = new HttpLogMasking();

      final String result = masking.header(header, value);

      assertThat(result).isEqualTo(expected);
    }

    @Test
    void when_custom_mask_value_expect_custom_mask() {
      final HttpLogMasking masking = new HttpLogMasking("MASKED", Set.of("custom-header"));

      final String result = masking.header("custom-header", "secret-value");

      assertThat(result).isEqualTo("MASKED");
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
  class request {
    @Test
    void when_any_request_expect_unchanged() {
      final HttpLogMasking masking = new HttpLogMasking();

      final String result = masking.request("/any/uri", "any request body");

      assertThat(result).isEqualTo("any request body");
    }
  }

  @Nested
  class response {
    @Test
    void when_any_response_expect_unchanged() {
      final HttpLogMasking masking = new HttpLogMasking();

      final String result = masking.response("/any/uri", "any response body");

      assertThat(result).isEqualTo("any response body");
    }
  }
}
