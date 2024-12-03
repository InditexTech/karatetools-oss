package dev.inditex.karate.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import dev.inditex.karate.docker.DockerComposeTestConfiguration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("IT")
@ActiveProfiles({"test-docker"})
@SpringBootTest(classes = {
    DockerComposeTestConfiguration.class
})
public class KafkaClientsIT extends AbstractKafkaClientsIT {

  static final String STRING_SERIALIZATION_NO_AUTH_REGISTRY_NO_AUTH_CONFIG_FILE =
      "classpath:config/kafka/kafka-config-no-auth-registry-no-auth-string-serialization.yml";

  static final String AVRO_NO_AUTH_REGISTRY_BASIC_AUTH_CONFIG_FILE =
      "classpath:config/kafka/kafka-config-no-auth-registry-basic-auth.yml";

  static final String AVRO_NO_AUTH_REGISTRY_NO_AUTH_CONFIG_FILE =
      "classpath:config/kafka/kafka-config-no-auth-registry-no-auth.yml";

  static final String AVRO_SASL_PLAIN_REGISTRY_BASIC_AUTH_CONFIG_FILE =
      "classpath:config/kafka/kafka-config-sasl-plain-registry-basic-auth.yml";

  static final String AVRO_SASL_PLAIN_REGISTRY_NO_AUTH_CONFIG_FILE =
      "classpath:config/kafka/kafka-config-sasl-plain-registry-no-auth.yml";

  static final String AVRO_SASL_SCRAM_REGISTRY_BASIC_AUTH_CONFIG_FILE =
      "classpath:config/kafka/kafka-config-sasl-scram-sha-512-registry-basic-auth.yml";

  static final String AVRO_SASL_SCRAM_REGISTRY_NO_AUTH_CONFIG_FILE =
      "classpath:config/kafka/kafka-config-sasl-scram-sha-512-registry-no-auth.yml";

  @Nested
  class KafkaClients {

    @ParameterizedTest
    @MethodSource
    void when_kafka_clients_string_serialization_expect_results(final String type, final String file, final TestInfo testInfo)
        throws IOException {
      final var result = when_kafka_clients_string_expect_results(type, file, testInfo);

      assertThat(result).isTrue();
    }

    static Stream<Arguments> when_kafka_clients_string_serialization_expect_results() {
      return Stream.of(
          Arguments.of("kafka-noauth.reg-noauth", STRING_SERIALIZATION_NO_AUTH_REGISTRY_NO_AUTH_CONFIG_FILE));
    }

    @ParameterizedTest
    @MethodSource
    void when_kafka_clients_avro_serialization_expect_results(final String type, final String file, final TestInfo testInfo)
        throws IOException {
      final var result = when_kafka_clients_avro_expect_results(type, file, testInfo);

      assertThat(result).isTrue();
    }

    static Stream<Arguments> when_kafka_clients_avro_serialization_expect_results() {
      return Stream.of(
          Arguments.of("kafka-noauth.reg-noauth", AVRO_NO_AUTH_REGISTRY_NO_AUTH_CONFIG_FILE),
          Arguments.of("kafka-noauth.reg-basic", AVRO_NO_AUTH_REGISTRY_BASIC_AUTH_CONFIG_FILE),
          Arguments.of("kafka-sasl-plain.reg-noauth", AVRO_SASL_PLAIN_REGISTRY_NO_AUTH_CONFIG_FILE),
          Arguments.of("kafka-sasl-plain.reg-basic", AVRO_SASL_PLAIN_REGISTRY_BASIC_AUTH_CONFIG_FILE),
          Arguments.of("kafka-sasl-scram.reg-noauth", AVRO_SASL_SCRAM_REGISTRY_NO_AUTH_CONFIG_FILE),
          Arguments.of("kafka-sasl-scram.reg-basic", AVRO_SASL_SCRAM_REGISTRY_BASIC_AUTH_CONFIG_FILE));
    }
  }

  @Override
  protected KafkaProducerClient instantiateProducerClient(final Map<Object, Object> config) {
    return new KafkaProducerClient(config);
  }

  @Override
  protected KafkaConsumerClient instantiateConsumerClient(final Map<Object, Object> config) {
    return new KafkaConsumerClient(config);
  }
}
