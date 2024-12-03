package dev.inditex.karate.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;
import org.mockito.stubbing.Answer;

public class KafkaConsumerClientTest extends KafkaAbstractClientTest {

  final String group = "karate-kafka-test-consumer-group";

  final long timeout = 5L;

  final String offset = "1";

  final int partition = 0;

  final String key = "key";

  final String topicA = "dev.inditex.karate.kafka.public.A";

  final String topicB = "dev.inditex.karate.kafka.public.B";

  final String[] topics = {topicA, topicB};

  final String topic = "dev.inditex.karate.kafka.public";

  final KarateEvent event1 = new KarateEvent("1", "karate-01", 1L);

  final KarateEvent event2 = new KarateEvent("2", "karate-02", 2L);

  final KarateEvent event3 = new KarateEvent("3", "karate-03", 3L);

  @Nested
  class Constructor {
    @Test
    void when_default_config_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfig();

      final KafkaConsumerClient client = instantiateClient(config);

      // Common
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
      // Kafka consumer - Specific
      assertThat(client.getDefaultGroup()).isEqualTo(
          client.getConfiguration().getProperty(ConsumerConfig.GROUP_ID_CONFIG));
      assertThat(client.getConfiguration().getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG))
          .isEqualTo(StringDeserializer.class.getName());
      assertThat(client.getConfiguration().getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
          .isEqualTo(StringDeserializer.class.getName());
    }

    @Test
    void when_custom_deserializer_config_expect_fields_informed() {
      final String deserializerKeyClass = "org.apache.kafka.common.serialization.StringDeserializer";
      final String deserializerValueClass = "io.confluent.kafka.serializers.KafkaAvroDeserializer";
      final Map<Object, Object> config = getConfig(deserializerKeyClass, deserializerValueClass);

      final KafkaConsumerClient client = instantiateClient(config);

      // Common
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
      // Kafka consumer - Specific
      assertThat(client.getConfiguration().getProperty(ConsumerConfig.GROUP_ID_CONFIG)).isNull();
      assertThat(client.getDefaultGroup()).isEqualTo("karate-kafka-default-consumer-group");
      assertThat(client.getConfiguration().getProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG))
          .isEqualTo(deserializerKeyClass);
      assertThat(client.getConfiguration().getProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
          .isEqualTo(deserializerValueClass);
    }

    @ParameterizedTest
    @CsvSource({
        "consumer.group.id,consumer-group-id,consumer-group-id", // Valid consumer key "consumer.group.id"
        "group.id,group-id,group-id", // Valid general key "group.id"
        "my.group.id,my-group-id,karate-kafka-default-consumer-group" // Invalid key
    })
    void when_consumer_group_config_expect_correct_group(final String key, final String value, final String expected) {
      final Map<Object, Object> config = new HashMap<>();
      config.put(key, value);

      final KafkaConsumerClient client = instantiateClient(config);

      assertThat(client.getDefaultGroup()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "consumer.auto.offset.reset,latest,latest", // Valid consumer key "consumer.auto.offset.reset"
        "auto.offset.reset,none,none", // Valid general key "auto.offset.reset"
        "my.auto.offset.reset,latest,earliest" // Invalid key
    })
    void when_consumer_offset_config_expect_correct_offset(final String key, final String value, final String expected) {
      final Map<Object, Object> config = new HashMap<>();
      config.put(key, value);

      final KafkaConsumerClient client = instantiateClient(config);

      assertThat(client.getDefaultOffset()).isEqualTo(expected);
    }
  }

  @Nested
  class Consume {

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsSingleTopic();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topic);

        assertThat(result).hasSize(2);
        assertEvent(result.get(0), event1);
        assertEvent(result.get(1), event2);
        verifySubscriptionToTopic(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("consume() from topics [[dev.inditex.karate.kafka.public]] => #2"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsSingleTopic();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topic, timeout);

        assertThat(result).hasSize(2);
        assertEvent(result.get(0), event1);
        assertEvent(result.get(1), event2);
        verifySubscriptionToTopic(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("consume() from topics [[dev.inditex.karate.kafka.public]] => #2"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_group_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsSingleTopic();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topic, group);

        assertThat(result).hasSize(2);
        assertEvent(result.get(0), event1);
        assertEvent(result.get(1), event2);
        verifySubscriptionToTopic(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("consume() from topics [[dev.inditex.karate.kafka.public]] => #2"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_group_offset_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsSingleTopic();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topic, group, offset);

        assertThat(result).hasSize(2);
        assertEvent(result.get(0), event1);
        assertEvent(result.get(1), event2);
        verifySubscriptionToTopic(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("consume() from topics [[dev.inditex.karate.kafka.public]] => #2"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_group_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsSingleTopic();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topic, group, timeout);

        assertThat(result).hasSize(2);
        assertEvent(result.get(0), event1);
        assertEvent(result.get(1), event2);
        verifySubscriptionToTopic(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("consume() from topics [[dev.inditex.karate.kafka.public]] => #2"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topic_group_offset_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsSingleTopic();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topic, group, offset, timeout);

        assertThat(result).hasSize(2);
        assertEvent(result.get(0), event1);
        assertEvent(result.get(1), event2);
        verifySubscriptionToTopic(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("consume() from topics [[dev.inditex.karate.kafka.public]] => #2"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topics_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsMultipleTopics();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).hasSize(2);
        assertEvent(result.get(topicA).get(0), event1);
        assertEvent(result.get(topicA).get(1), event2);
        assertThat(result.get(topicB)).hasSize(1);
        assertEvent(result.get(topicB).get(0), event3);
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #3"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topics_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsMultipleTopics();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, timeout);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).hasSize(2);
        assertEvent(result.get(topicA).get(0), event1);
        assertEvent(result.get(topicA).get(1), event2);
        assertThat(result.get(topicB)).hasSize(1);
        assertEvent(result.get(topicB).get(0), event3);
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #3"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topics_group_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsMultipleTopics();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, group);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).hasSize(2);
        assertEvent(result.get(topicA).get(0), event1);
        assertEvent(result.get(topicA).get(1), event2);
        assertThat(result.get(topicB)).hasSize(1);
        assertEvent(result.get(topicB).get(0), event3);
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #3"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topics_group_offset_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsMultipleTopics();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, group, offset);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).hasSize(2);
        assertEvent(result.get(topicA).get(0), event1);
        assertEvent(result.get(topicA).get(1), event2);
        assertThat(result.get(topicB)).hasSize(1);
        assertEvent(result.get(topicB).get(0), event3);
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #3"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topics_group_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsMultipleTopics();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, group, timeout);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).hasSize(2);
        assertEvent(result.get(topicA).get(0), event1);
        assertEvent(result.get(topicA).get(1), event2);
        assertThat(result.get(topicB)).hasSize(1);
        assertEvent(result.get(topicB).get(0), event3);
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #3"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topics_group_offset_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = getRecordsMultipleTopics();
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, group, offset, timeout);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).hasSize(2);
        assertEvent(result.get(topicA).get(0), event1);
        assertEvent(result.get(topicA).get(1), event2);
        assertThat(result.get(topicB)).hasSize(1);
        assertEvent(result.get(topicB).get(0), event3);
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #3"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_topics_group_offset_timeout_exception_expect_empty_and_log() {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getKafkaException()))) {

        assertThatThrownBy(() -> {
          client.consume(topics, group, offset, timeout);
        }).isInstanceOf(KafkaException.class).hasMessage("KafkaConsumerClient.poll()");

        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaConsumerClient => consume() Exception"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_single_event_for_topics_offset_group_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = new ConsumerRecords<>(Map.of(
          new TopicPartition(topicA, partition),
          List.of(new ConsumerRecord<>(topicA, partition, Integer.parseInt(offset), key, event1))));
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, offset, group, timeout);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).hasSize(1);
        assertEvent(result.get(topicA).get(0), event1);
        assertThat(result.get(topicB)).isEmpty();
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #1"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_empty_value_event_for_topics_offset_group_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecord<?, ?> mockConsumerRecord = mock(ConsumerRecord.class);
      when(mockConsumerRecord.topic()).thenReturn(topicA);
      when(mockConsumerRecord.partition()).thenReturn(partition);
      when(mockConsumerRecord.offset()).thenReturn(Long.parseLong(offset));
      when(mockConsumerRecord.key()).thenReturn(null);
      when(mockConsumerRecord.value()).thenReturn(null);
      final ConsumerRecords<?, ?> consumerRecords = new ConsumerRecords<>(Map.of(
          new TopicPartition(topicA, partition),
          List.of(mockConsumerRecord)));
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, offset, group, timeout);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).isEmpty();
        assertThat(result.get(topicB)).isEmpty();
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.WARN)
                && log.getFormattedMessage().contains("consume() NULL event.value()"))
            .anyMatch(log -> log.getLevel().equals(Level.INFO)
                && log.getFormattedMessage()
                    .contains(
                        "consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #0"));
      }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void when_no_events_for_topics_offset_group_timeout_expect_result() throws JsonProcessingException {
      final ConsumerRecords<?, ?> consumerRecords = new ConsumerRecords<>(Map.of());
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaConsumerClient client = instantiateClient(config);
      try (final MockedConstruction mockedConsumerConstruction = mockConstruction(KafkaConsumer.class,
          withSettings().defaultAnswer(getConsumerRecords(consumerRecords)))) {

        final var result = client.consume(topics, offset, group, timeout);

        assertThat(result).hasSameSizeAs(topics);
        assertThat(result.get(topicA)).isEmpty();
        assertThat(result.get(topicB)).isEmpty();
        verifySubscriptionToTopics(mockedConsumerConstruction);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage()
                .contains("consume() from topics [[dev.inditex.karate.kafka.public.A, dev.inditex.karate.kafka.public.B]] => #0"));
      }
    }

    private ConsumerRecords<?, ?> getRecordsSingleTopic() {
      return new ConsumerRecords<>(Map.of(
          new TopicPartition(topic, partition),
          List.of(new ConsumerRecord<>(topic, partition, Integer.parseInt(offset), key, event1),
              new ConsumerRecord<>(topic, partition, Integer.parseInt(offset), key, event2))));
    }

    private ConsumerRecords<?, ?> getRecordsMultipleTopics() {
      return new ConsumerRecords<>(Map.of(
          new TopicPartition(topicA, partition),
          List.of(new ConsumerRecord<>(topicA, partition, Integer.parseInt(offset), key, event1),
              new ConsumerRecord<>(topicA, partition, Integer.parseInt(offset), key, event2)),
          new TopicPartition(topicB, partition),
          List.of(new ConsumerRecord<>(topicB, partition, Integer.parseInt(offset), key, event3))));
    }

    private void assertEvent(final Map<String, Object> actual, final KarateEvent expected) {
      assertThat(actual).containsEntry("id", expected.getId());
      assertThat(actual).containsEntry("name", expected.getName());
      assertThat(actual).containsEntry("value", (int) expected.getValue());
    }

    private Answer<?> getConsumerRecords(final ConsumerRecords<?, ?> consumerRecords) {
      return invocationOnMock -> {
        if (invocationOnMock.getMethod().getName().equals("poll")) {
          return consumerRecords;
        }
        return null;
      };
    }

    private Answer<?> getKafkaException() {
      return invocationOnMock -> {
        throw new KafkaException("KafkaConsumerClient.poll()");
      };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void verifySubscriptionToTopic(final MockedConstruction mockedConsumerConstruction) {
      assertThat(mockedConsumerConstruction.constructed()).hasSize(1);
      final KafkaConsumer consumer = (KafkaConsumer) mockedConsumerConstruction.constructed().get(0);
      verify(consumer, times(1)).subscribe(Arrays.asList(topic));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void verifySubscriptionToTopics(final MockedConstruction mockedConsumerConstruction) {
      assertThat(mockedConsumerConstruction.constructed()).hasSize(1);
      final KafkaConsumer consumer = (KafkaConsumer) mockedConsumerConstruction.constructed().get(0);
      verify(consumer, times(1)).subscribe(Arrays.asList(topics));
    }
  }

  @Override
  protected Map<Object, Object> getDefaultConfig() {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "producer.client.id", "KARATETOOLS", // to be ignored by getApplicableKey
        "consumer.group.id", "KARATE-local",
        "consumer.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer",
        "consumer.value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
  }

  protected Map<Object, Object> getConfig(final String deserializerKeyClass, final String deserializerValueClass) {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        // no consumer.group.id => default
        "consumer.key.deserializer", deserializerKeyClass, // "org.apache.kafka.common.serialization.StringDeserializer"
        "consumer.value.deserializer", deserializerValueClass); // "io.confluent.kafka.serializers.KafkaAvroDeserializer"
  }

  protected KafkaConsumerClient instantiateClient(final Map<Object, Object> config) {
    return new KafkaConsumerClient(config);
  }
}
