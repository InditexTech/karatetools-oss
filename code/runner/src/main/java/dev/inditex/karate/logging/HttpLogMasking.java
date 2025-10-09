package dev.inditex.karate.logging;

import java.util.Set;

import com.intuit.karate.http.HttpLogModifier;

public class HttpLogMasking implements HttpLogModifier {

  public static final HttpLogModifier INSTANCE = new HttpLogMasking();

  private boolean enabled;

  private final String mask;

  private final Set<String> sensitiveHeadersKeys;

  private final Set<String> sensitiveHeaderValues;

  public HttpLogMasking() {
    // Default constructor
    enabled = true;
    mask = "*****";
    sensitiveHeadersKeys = Set.of(
        "authorization",
        "token",
        "secret",
        "key",
        "username",
        "password");
    sensitiveHeaderValues = Set.of(
        // Pattern-based detection for JWT tokens
        "^Bearer\\s+[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]*$",
        // Basic auth detection
        "^Basic\\s+[A-Za-z0-9+/]+=*$");
  }

  @Override
  public boolean enableForUri(final String uri) {
    return true; // Enable for all URIs
  }

  @Override
  public String uri(final String uri) {
    return uri; // No modification to the URI
  }

  @Override
  public String header(final String header, final String value) {
    if (!enabled) {
      return value;
    }
    if (value == null) {
      return value;
    }

    for (final String pattern : sensitiveHeaderValues) {
      if (value.matches(pattern)) {
        // Return first word of the value followed by mask
        return value.split("\\s+")[0] + " " + mask;
      }
    }

    if (header == null) {
      return value;
    }

    final String lowerHeader = header.toLowerCase();
    // if the header is in the sensitive list even with partial match
    if (sensitiveHeadersKeys.stream().anyMatch(lowerHeader::contains)) {
      return mask;
    }

    return value;
  }

  @Override
  public String request(final String uri, final String request) {
    return request; // No modification to the request body
  }

  @Override
  public String response(final String uri, final String response) {
    return response; // No modification to the response body
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
