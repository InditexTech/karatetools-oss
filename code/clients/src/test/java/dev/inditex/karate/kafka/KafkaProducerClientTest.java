package dev.inditex.karate.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.inditex.karate.logging.KarateClientLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;

public class KafkaProducerClientTest extends KafkaAbstractClientTest {

  @Nested
  class Constructor {
    @Test
    void when_default_config_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfig();

      final KafkaProducerClient client = instantiateClient(config);

      // Common
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
      // Kafka Producer - Defaults
      assertThat(client.getConfiguration().getProperty(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo("true");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)).isEqualTo("5");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG)).isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      // Kafka Producer - Specific
      assertThat(client.getConfiguration().getProperty(ProducerConfig.CLIENT_ID_CONFIG))
          .isEqualTo("KARATETOOLS");
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS))
          .isEqualTo("false");
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.KEY_SUBJECT_NAME_STRATEGY))
          .isEqualTo("io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY))
          .isEqualTo("io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG))
          .isEqualTo(StringSerializer.class.getName());
      assertThat(client.getConfiguration().getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))
          .isEqualTo(StringSerializer.class.getName());
    }

    @Test
    void when_custom_serializer_config_expect_fields_informed() {
      final String serializerKeyClass = "org.apache.kafka.common.serialization.StringSerializer";
      final String serializerValueClass = "io.confluent.kafka.serializers.KafkaAvroSerializer";
      final Map<Object, Object> config = getConfig(serializerKeyClass, serializerValueClass);

      final KafkaProducerClient client = instantiateClient(config);

      // Common
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
      // Kafka Producer - Defaults
      assertThat(client.getConfiguration().getProperty(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo("true");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)).isEqualTo("5");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG)).isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      // Kafka Producer - Specific
      assertThat(client.getConfiguration().getProperty(ProducerConfig.CLIENT_ID_CONFIG))
          .isEqualTo("KARATETOOLS");
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS))
          .isEqualTo("false");
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.KEY_SUBJECT_NAME_STRATEGY))
          .isEqualTo("io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY))
          .isEqualTo("io.confluent.kafka.serializers.subject.TopicRecordNameStrategy");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG))
          .isEqualTo(serializerKeyClass);
      assertThat(client.getConfiguration().getProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))
          .isEqualTo(serializerValueClass);
    }

    @Test
    void when_config_defaults_overridden_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfigWithDefaultsOverriden();

      final KafkaProducerClient client = instantiateClient(config);

      // Common
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
      // Kafka Producer - Defaults
      assertThat(client.getConfiguration().getProperty(ProducerConfig.ACKS_CONFIG)).isEqualTo("1");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo("false");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)).isEqualTo("1");
      assertThat(client.getConfiguration().getProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG)).isEqualTo("5000");
    }
  }

  @Nested
  class Send {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void when_topic_value_expect_sent() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getDefaultConfig();
      final String topic = "dev.inditex.karate.kafka.public";
      final KarateEvent event = new KarateEvent("1", "karate-01", 1L);
      final KafkaProducerClient client = instantiateClient(config);
      try (final MockedConstruction mockedProducerConstruction = mockConstruction(KafkaProducer.class)) {
        final ArgumentCaptor<ProducerRecord> producerRecordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        final ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        client.send(topic, event);

        assertThat(mockedProducerConstruction.constructed()).hasSize(1);
        final KafkaProducer producer = (KafkaProducer) mockedProducerConstruction.constructed().get(0);
        verify(producer, times(1)).send(producerRecordCaptor.capture(), callbackCaptor.capture());
        assertThat(producerRecordCaptor.getValue()).isNotNull();
        assertThat(producerRecordCaptor.getValue().topic()).isEqualTo(topic);
        assertThat(producerRecordCaptor.getValue().partition()).isNull();
        assertThat(producerRecordCaptor.getValue().headers()).isEmpty();
        assertThat(producerRecordCaptor.getValue().key()).isNull();
        assertThat(producerRecordCaptor.getValue().value()).isEqualTo(event);
        assertThat(producerRecordCaptor.getValue().timestamp()).isNull();
        assertThat(callbackCaptor.getValue()).isNotNull();
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to topic [dev.inditex.karate.kafka.public] completed"));
        callbackCaptor.getValue().onCompletion(new RecordMetadata(new TopicPartition(topic, 1), 2, 3, 4L, 5, 6), null);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage().contains("producer.send() Record sent")
            && log.getFormattedMessage().contains("offset[5]"));
      }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void when_topic_value_headers_expect_result() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getDefaultConfig();
      final String topic = "dev.inditex.karate.kafka.public";
      final KarateEvent event = new KarateEvent("1", "karate-01", 1L);
      final Map<String, List<String>> headers = Map.of("contentType", List.of("application/*+avro"), "status", List.of("ok"));
      final KafkaProducerClient client = instantiateClient(config);
      try (final MockedConstruction mockedProducerConstruction = mockConstruction(KafkaProducer.class)) {
        final ArgumentCaptor<ProducerRecord> producerRecordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        final ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        client.send(topic, event, headers);

        assertThat(mockedProducerConstruction.constructed()).hasSize(1);
        final KafkaProducer producer = (KafkaProducer) mockedProducerConstruction.constructed().get(0);
        verify(producer, times(1)).send(producerRecordCaptor.capture(), callbackCaptor.capture());
        assertThat(producerRecordCaptor.getValue()).isNotNull();
        assertThat(producerRecordCaptor.getValue().topic()).isEqualTo(topic);
        assertThat(producerRecordCaptor.getValue().partition()).isNull();
        assertThat(producerRecordCaptor.getValue().headers()).isNotNull();
        assertHeaders(producerRecordCaptor.getValue().headers(), headers);
        assertThat(producerRecordCaptor.getValue().key()).isNull();
        assertThat(producerRecordCaptor.getValue().value()).isEqualTo(event);
        assertThat(producerRecordCaptor.getValue().timestamp()).isNull();
        assertThat(callbackCaptor.getValue()).isNotNull();
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to topic [dev.inditex.karate.kafka.public] completed"));
        callbackCaptor.getValue().onCompletion(new RecordMetadata(new TopicPartition(topic, 1), 2, 3, 4L, 5, 6), null);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.DEBUG)
            && log.getFormattedMessage().contains("producer.send() Record sent")
            && log.getFormattedMessage().contains("offset[5]"));
      }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void when_topic_value_callback_kafka_exception_expect_log_and_exception() {
      final Map<Object, Object> config = getDefaultConfig();
      final String topic = "dev.inditex.karate.kafka.public";
      final KarateEvent event = new KarateEvent("1", "karate-01", 1L);
      final KafkaProducerClient client = instantiateClient(config);
      try (final MockedConstruction mockedProducerConstruction = mockConstruction(KafkaProducer.class)) {
        final ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        client.send(topic, event);

        assertThat(mockedProducerConstruction.constructed()).hasSize(1);
        final KafkaProducer producer = (KafkaProducer) mockedProducerConstruction.constructed().get(0);
        verify(producer, times(1)).send(any(), callbackCaptor.capture());
        final var callback = callbackCaptor.getValue();
        final var exception = new KafkaException("KafkaProducerClient.send()");
        assertThatThrownBy(() -> {
          callback.onCompletion(null, exception);
        }).isInstanceOf(KafkaException.class).hasMessage("KafkaProducerClient.send()");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaProducerClient => producer.send() Exception"));
      }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void when_topic_value_callback_general_exception_expect_log_and_exception() {
      final Map<Object, Object> config = getDefaultConfig();
      final String topic = "dev.inditex.karate.kafka.public";
      final KarateEvent event = new KarateEvent("1", "karate-01", 1L);
      final KafkaProducerClient client = instantiateClient(config);
      try (final MockedConstruction mockedProducerConstruction = mockConstruction(KafkaProducer.class)) {
        final ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        client.send(topic, event);

        assertThat(mockedProducerConstruction.constructed()).hasSize(1);
        final KafkaProducer producer = (KafkaProducer) mockedProducerConstruction.constructed().get(0);
        verify(producer, times(1)).send(any(), callbackCaptor.capture());
        final var callback = callbackCaptor.getValue();
        final var exception = new RuntimeException("KafkaProducerClient.send()");
        assertThatThrownBy(() -> {
          callback.onCompletion(null, exception);
        }).isInstanceOf(RuntimeException.class).hasMessage("java.lang.RuntimeException: KafkaProducerClient.send()");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaProducerClient => producer.send() Exception"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_value_headers_kafka_exception_expect_log_and_exception() {
      final Map<Object, Object> config = getDefaultConfig();
      final String topic = "dev.inditex.karate.kafka.public";
      final KarateEvent event = new KarateEvent("1", "karate-01", 1L);
      final Map<String, List<String>> headers = Map.of("contentType", List.of("application/*+avro"), "status", List.of("ok"));
      final KafkaProducerClient client = instantiateClient(config);
      try (final MockedConstruction mockedProducerConstruction =
          mockConstruction(KafkaProducer.class, withSettings().defaultAnswer(getKafkaException()))) {

        assertThatThrownBy(() -> {
          client.send(topic, event, headers);
        }).isInstanceOf(KafkaException.class).hasMessage("KafkaProducerClient.send()");

        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaProducerClient => send() KafkaException"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_value_headers_general_exception_expect_log_and_exception() {
      final Map<Object, Object> config = getDefaultConfig();
      final String topic = "dev.inditex.karate.kafka.public";
      final KarateEvent event = new KarateEvent("1", "karate-01", 1L);
      final Map<String, List<String>> headers = Map.of("contentType", List.of("application/*+avro"), "status", List.of("ok"));
      final KafkaProducerClient client = instantiateClient(config);
      try (final MockedConstruction mockedProducerConstruction =
          mockConstruction(KafkaProducer.class, withSettings().defaultAnswer(getGeneralException()))) {

        assertThatThrownBy(() -> {
          client.send(topic, event, headers);
        }).isInstanceOf(RuntimeException.class).hasMessage("java.lang.RuntimeException: KafkaProducerClient.send()");

        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaProducerClient => send() Exception"));
      }
    }

    private Answer<?> getGeneralException() {
      return invocationOnMock -> {
        throw new RuntimeException("KafkaProducerClient.send()");
      };
    }

    private Answer<?> getKafkaException() {
      return invocationOnMock -> {
        throw new KafkaException("KafkaProducerClient.send()");
      };
    }

    private void assertHeaders(final Headers actual, final Map<String, List<String>> headers) {
      assertThat(actual.toArray()).hasSameSizeAs(headers.keySet());
      for (final Entry<String, List<String>> header : headers.entrySet()) {
        assertThat(new String(actual.lastHeader(header.getKey()).value(), StandardCharsets.UTF_8)).isEqualTo(header.getValue().get(0));
      }
    }
  }

  @Override
  protected Map<Object, Object> getDefaultConfig() {
    return getConfig(StringSerializer.class.getName(), StringSerializer.class.getName());
  }

  @Override
  protected Map<Object, Object> getDefaultConfigWithDefaultsOverriden() {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "producer.acks", "1",
        "producer.enable.idempotence", "false",
        "producer.max.in.flight.requests.per.connection", "1",
        "producer.max.block.ms", "5000");
  }

  protected Map<Object, Object> getConfig(final String serializerKeyClass, final String serializerValueClass) {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "producer.client.id", "KARATETOOLS",
        "producer.auto.register.schemas", "false",
        "producer.key.subject.name.strategy", "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy",
        "producer.value.subject.name.strategy", "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy",
        "producer.key.serializer", serializerKeyClass, // "org.apache.kafka.common.serialization.StringSerializer"
        "producer.value.serializer", serializerValueClass, // "io.confluent.kafka.serializers.KafkaAvroSerializer"
        "consumer.group.id", "KARATETOOLS-local" // to be ignored by getApplicableKey
    );
  }

  protected KafkaProducerClient instantiateClient(final Map<Object, Object> config) {
    return new KafkaProducerClient(config);
  }

}
