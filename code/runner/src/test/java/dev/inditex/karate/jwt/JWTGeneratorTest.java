package dev.inditex.karate.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import dev.inditex.karate.AbstractKarateTest;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class JWTGeneratorTest extends AbstractKarateTest {
  public static final String HEADERS = "headers";

  public static final String PAYLOADS = "payloads";

  public static final String SECRET = "secret";

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(JWTGenerator::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GenerateToken {
    @Test
    void when_jwt_valid_algorithm_expect_token() {
      final TreeMap<String, Object> headers = new TreeMap<>();
      headers.put("alg", "HS256");
      headers.put("kid", "test");
      headers.put("typ", "JWT");
      final TreeMap<String, Object> payloads = new TreeMap<>();
      payloads.put("id", "1234");
      payloads.put("sub", "username100");
      payloads.put("iss", "https://www.inditex.com/jwt-token");
      payloads.put("exp", 2147483647);
      payloads.put("iat", 1704067200);
      final String secret = "aaaa1111-bb22-cc33-dd44-eeeeee555555";
      final Map<String, Object> jwtData = new HashMap<>();
      jwtData.put(SECRET, secret);
      jwtData.put(HEADERS, headers);
      jwtData.put(PAYLOADS, payloads);
      final String expected =
          "eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1"
              + "QifQ.eyJleHAiOjIxNDc0ODM2NDcsImlhdCI6MTcwNDA2NzIwM"
              + "CwiaWQiOiIxMjM0IiwiaXNzIjoiaHR0cHM6Ly93d3cuaW5kaXR"
              + "leC5jb20vand0LXRva2VuIiwic3ViIjoidXNlcm5hbWUxMDAif"
              + "Q.qXP_iGhZSgFTVZtup9dYFont8QjLH8x2q2UDlQM0tIY";

      final var actual = generateToken(jwtData);

      assertThat(actual).isEqualTo(expected);
    }

    @Test
    void when_jwt_invalid_algorithm_expect_exception() {
      final TreeMap<String, Object> headers = new TreeMap<>();
      headers.put("alg", "HS256B");
      headers.put("kid", "test");
      headers.put("typ", "JWT");
      final TreeMap<String, Object> payloads = new TreeMap<>();
      payloads.put("id", "1234");
      payloads.put("sub", "username");
      payloads.put("iss", "https://www.inditex.com/jwt-token");
      payloads.put("exp", 1672570800);
      payloads.put("iat", 1988190000);
      final String secret = "aaaa1111-bb22-cc33-dd44-eeeeee555555";
      final Map<String, Object> jwtData = new HashMap<>();
      jwtData.put(SECRET, secret);
      jwtData.put(HEADERS, headers);
      jwtData.put(PAYLOADS, payloads);
      assertThatThrownBy(() -> {
        JWTGenerator.generateToken(jwtData);
      }).isInstanceOf(IllegalArgumentException.class).hasMessage("Unrecognized JWS Digital Signature or MAC id: HS256B");
    }

    @Test
    void when_jwt_not_mac_algorithm_expect_exception() {
      final TreeMap<String, Object> headers = new TreeMap<>();
      headers.put("alg", "RS256");
      headers.put("kid", "test");
      headers.put("typ", "JWT");
      final TreeMap<String, Object> payloads = new TreeMap<>();
      payloads.put("id", "1234");
      payloads.put("sub", "username");
      payloads.put("iss", "https://www.inditex.com/jwt-token");
      payloads.put("exp", 1672570800);
      payloads.put("iat", 1988190000);
      final String secret = "aaaa1111-bb22-cc33-dd44-eeeeee555555";
      final Map<String, Object> jwtData = new HashMap<>();
      jwtData.put(SECRET, secret);
      jwtData.put(HEADERS, headers);
      jwtData.put(PAYLOADS, payloads);
      assertThatThrownBy(() -> {
        JWTGenerator.generateToken(jwtData);
      }).isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid algorithm for HMAC key generation: RS256");
    }
  }

  protected String generateToken(final Map<String, Object> jwtData) {
    return JWTGenerator.generateToken(jwtData);
  }
}
