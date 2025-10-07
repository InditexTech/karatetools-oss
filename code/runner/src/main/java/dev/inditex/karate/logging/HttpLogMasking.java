package dev.inditex.karate.logging;

import java.util.Set;

import com.intuit.karate.http.HttpLogModifier;

public class HttpLogMasking implements HttpLogModifier {

  private String maskValue = "*****";

  private Set<String> sensitiveHeaders = Set.of(
      "authorization",
      "token",
      "secret",
      "key",
      "username",
      "password");

  public HttpLogMasking() {
    // Default constructor
  }

  public HttpLogMasking(final String maskValue, final Set<String> sensitiveHeaders) {
    if (maskValue != null && !maskValue.isEmpty()) {
      this.maskValue = maskValue;
    }
    if (sensitiveHeaders != null && !sensitiveHeaders.isEmpty()) {
      this.sensitiveHeaders = sensitiveHeaders;
    }
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
    if (value == null) {
      return value;
    }

    // Pattern-based detection for JWT tokens
    if (value.matches("^Bearer\\s+[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]*$")) {
      return "Bearer " + maskValue;
    }

    // Basic auth detection
    if (value.matches("^Basic\\s+[A-Za-z0-9+/]+=*$")) {
      return "Basic " + maskValue;
    }

    if (header == null) {
      return value;
    }

    final String lowerHeader = header.toLowerCase();
    // if the header is in the sensitive list even with partial match
    if (sensitiveHeaders.stream().anyMatch(lowerHeader::contains)) {
      return maskValue;
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
}
