package dev.inditex.karate.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.KarateTestUtils;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.TestInfo;

@Slf4j
public abstract class AbstractKafkaClientsIT {

  protected abstract KafkaProducerClient instantiateProducerClient(final Map<Object, Object> config);

  protected abstract KafkaConsumerClient instantiateConsumerClient(final Map<Object, Object> config);

  protected boolean when_kafka_clients_avro_expect_results(final String type, final String file, final TestInfo testInfo)
      throws IOException {
    log.info(" >>>>>>>>>>>> Executing test: {}", testInfo.getDisplayName());
    final Map<Object, Object> config = KarateTestUtils.readYaml(file);
    final KafkaProducerClient kafkaProducerClient = instantiateProducerClient(config);
    final KafkaConsumerClient kafkaConsumerClient = instantiateConsumerClient(config);
    final String topicV1 = "it.local.karate.test-avro.public." + type + ".v1";
    final String topicV2 = "it.local.karate.test-avro.public." + type + ".v2";
    final String[] topics = Arrays.array(topicV1, topicV2);
    final Map<String, List<String>> headersOk = Map.of("contentType", List.of("application/*+avro"), "status", List.of("ok"));
    final Map<String, List<String>> headersKo = Map.of("contentType", List.of("application/*+avro"), "status", List.of("ko"));
    final KarateEvent event1 = new KarateEvent("1", "karate-01", 1L);
    final KarateEvent event2 = new KarateEvent("2", "karate-02", 2L);
    final KarateEvent event3 = new KarateEvent("3", "karate-03", 3L);
    final KarateEvent event4 = new KarateEvent("4", "karate-04", 4L);
    final Map<String, Object> expected1 = Map.of("id", "1", "name", "karate-01", "value", 1);
    final Map<String, Object> expected2 = Map.of("id", "2", "name", "karate-02", "value", 2);
    final Map<String, Object> expected3 = Map.of("id", "3", "name", "karate-03", "value", 3);
    final Map<String, Object> expected4 = Map.of("id", "4", "name", "karate-04", "value", 4);

    // available
    final var availableProducer = kafkaProducerClient.available();
    final var availableConsumer = kafkaConsumerClient.available();
    // consume - initial to clean up
    final var initialMessages = kafkaConsumerClient.consume(topics);
    // send(final String topic, final Object event)
    kafkaProducerClient.send(topicV1, event1);
    // send(final String topic, final Object event, final Map<String, String> headers)
    kafkaProducerClient.send(topicV2, event2, headersOk);
    // List<Map<String, Object>> consume(final String topic)
    final var messagesTopicA = kafkaConsumerClient.consume(topicV1);
    final var messagesTopicB = kafkaConsumerClient.consume(topicV2);
    // send(final String topic, final Object event)
    kafkaProducerClient.send(topicV1, event3);
    // send(final String topic, final Object event, final Map<String, String> headers)
    kafkaProducerClient.send(topicV2, event4, headersKo);
    // Map<String, List<Map<String, Object>>> consume(final String[] topics)
    final var messagesTopics = kafkaConsumerClient.consume(topics);

    assertThat(availableProducer).isTrue();
    assertThat(availableConsumer).isTrue();
    assertThat(initialMessages).containsKeys(topicV1, topicV2);
    assertThat(messagesTopicA).isEqualTo(List.of(expected1));
    assertThat(messagesTopicB).isEqualTo(List.of(expected2));
    assertThat(messagesTopics).isEqualTo(Map.of(topicV1, List.of(expected3), topicV2, List.of(expected4)));

    log.info(" >>>>>>>>>>>> Executed test: {}", testInfo.getDisplayName());
    return true;
  }

  protected boolean when_kafka_clients_string_expect_results(final String type, final String file, final TestInfo testInfo)
      throws IOException {
    log.info(" >>>>>>>>>>>> Executing test: {}", testInfo.getDisplayName());
    final Map<Object, Object> config = KarateTestUtils.readYaml(file);
    final KafkaProducerClient kafkaProducerClient = instantiateProducerClient(config);
    final KafkaConsumerClient kafkaConsumerClient = instantiateConsumerClient(config);
    final String topicV1 = "it.local.karate.test-string.public." + type + ".v1";
    final String topicV2 = "it.local.karate.test-string.public." + type + ".v2";
    final String[] topics = Arrays.array(topicV1, topicV2);
    final Map<String, List<String>> headersOk = Map.of("contentType", List.of("text/plain"), "status", List.of("ok"));
    final Map<String, List<String>> headersKo = Map.of("contentType", List.of("text/plain"), "status", List.of("ko"));
    final String event1 = """
        {
          "id": "1",
          "name": "karate-01",
          "value": 1
        }
        """;
    final String event2 = """
          {
          "id": "2",
          "name": "karate-02",
          "value": 2
        }
        """;
    final String event3 = """
          {
          "id": "3",
          "name": "karate-03",
          "value": 3
        }
        """;
    final String event4 = """
          {
          "id": "4",
          "name": "karate-04",
          "value": 4
        }
        """;
    final Map<String, Object> expected1 = Map.of("id", "1", "name", "karate-01", "value", 1);
    final Map<String, Object> expected2 = Map.of("id", "2", "name", "karate-02", "value", 2);
    final Map<String, Object> expected3 = Map.of("id", "3", "name", "karate-03", "value", 3);
    final Map<String, Object> expected4 = Map.of("id", "4", "name", "karate-04", "value", 4);

    // available
    final var availableProducer = kafkaProducerClient.available();
    final var availableConsumer = kafkaConsumerClient.available();
    // consume - initial to clean up
    final var initialMessages = kafkaConsumerClient.consume(topics);
    // send(final String topic, final Object event)
    kafkaProducerClient.send(topicV1, event1);
    // send(final String topic, final Object event, final Map<String, String> headers)
    kafkaProducerClient.send(topicV2, event2, headersOk);
    // List<Map<String, Object>> consume(final String topic)
    final var messagesTopicA = kafkaConsumerClient.consume(topicV1);
    final var messagesTopicB = kafkaConsumerClient.consume(topicV2);
    // send(final String topic, final Object event)
    kafkaProducerClient.send(topicV1, event3);
    // send(final String topic, final Object event, final Map<String, String> headers)
    kafkaProducerClient.send(topicV2, event4, headersKo);
    // Map<String, List<Map<String, Object>>> consume(final String[] topics)
    final var messagesTopics = kafkaConsumerClient.consume(topics);

    assertThat(availableProducer).isTrue();
    assertThat(availableConsumer).isTrue();
    assertThat(initialMessages).containsKeys(topicV1, topicV2);
    assertThat(messagesTopicA).isEqualTo(List.of(expected1));
    assertThat(messagesTopicB).isEqualTo(List.of(expected2));
    assertThat(messagesTopics).isEqualTo(Map.of(topicV1, List.of(expected3), topicV2, List.of(expected4)));

    log.info(" >>>>>>>>>>>> Executed test: {}", testInfo.getDisplayName());
    return true;
  }
}
