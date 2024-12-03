package dev.inditex.karate.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Map;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import javax.crypto.spec.SecretKeySpec;

/**
 * The Class JWTGenerator.
 */
public class JWTGenerator {

  /** The Constant HEADERS. */
  private static final String HEADERS = "headers";

  /** The Constant PAYLOADS. */
  private static final String PAYLOADS = "payloads";

  /** The Constant SECRET. */
  private static final String SECRET = "secret";

  /** The Constant ALGORITHM. */
  private static final String ALGORITHM = "alg";

  /**
   * Instantiates a new JWT generator.
   */
  protected JWTGenerator() {
  }

  /**
   * Generate token.
   *
   * @param jwtData the jwt data
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public static String generateToken(final Map<String, Object> jwtData) {
    // Headers
    final Map<String, Object> headers = (Map<String, Object>) jwtData.get(HEADERS);
    // Algorithm
    final String algorithm = (String) headers.get(ALGORITHM);
    // Payloads
    final Map<String, Object> payloads = (Map<String, Object>) jwtData.get(PAYLOADS);
    // Secret
    final String secret = (String) jwtData.get(SECRET);

    // HMAC Key
    final var alg = Jwts.SIG.get().forKey(algorithm);
    if (!(alg instanceof MacAlgorithm)) {
      throw new IllegalArgumentException("Invalid algorithm for HMAC key generation: " + algorithm);
    }
    final String hmacAlgorithm = ((MacAlgorithm) alg).key().build().getAlgorithm();
    final Key hmacKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), hmacAlgorithm);

    // JWT Token
    final JwtBuilder tokenJWT = Jwts.builder();
    tokenJWT.claims(payloads);
    for (final Map.Entry<String, Object> entry : headers.entrySet()) {
      tokenJWT.header().add(entry.getKey(), entry.getValue());
    }

    // Sign JWT Token with HMAC Key
    return tokenJWT.signWith(hmacKey).compact();
  }
}
