package dev.inditex.karate.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;

import com.intuit.karate.Http;
import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.client.encoding.DecodingClient;
import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.encoding.StreamDecoderFactory;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.encoding.EncodingService;
import org.junit.jupiter.api.Test;

class BrotliCompressionTest {

  @Test
  void when_brotli_response_expect_native_compression_and_decompression() {
    final String body = "Karate Tools Brotli compression regression test. ".repeat(128);
    try (final Server server = Server.builder()
        .http(new InetSocketAddress("127.0.0.1", 0))
        .service("/", (ctx, req) -> HttpResponse.of(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, body))
        .decorator(EncodingService.newDecorator())
        .build();
        final ClientFactory factory = ClientFactory.builder().build()) {
      server.start().join();
      final String url = "http://127.0.0.1:" + server.activeLocalPort();
      final WebClient client = WebClient.builder(url).factory(factory).build();

      final var compressed = client.prepare().get("/")
          .header(HttpHeaderNames.ACCEPT_ENCODING, "br")
          .execute().aggregate().join();

      assertThat(compressed.status()).isEqualTo(HttpStatus.OK);
      assertThat(compressed.headers().get(HttpHeaderNames.CONTENT_ENCODING)).isEqualTo("br");
      assertThat(compressed.content().length()).isLessThan(body.length());

      final WebClient decodingClient = WebClient.builder(url).factory(factory)
          .decorator(DecodingClient.builder().decoderFactories(StreamDecoderFactory.brotli()).newDecorator())
          .build();
      final var decoded = decodingClient.get("/").aggregate().join();

      assertThat(decoded.status()).isEqualTo(HttpStatus.OK);
      assertThat(decoded.contentUtf8()).isEqualTo(body);

      final var karateResponse = Http.to(url).get();

      assertThat(karateResponse.getStatus()).isEqualTo(200);
      assertThat(karateResponse.getBodyAsString()).isEqualTo(body);
    }
  }
}
