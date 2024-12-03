package dev.inditex.karate.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;

/**
 * The Class KafkaConsumerClient.
 */
@Getter(AccessLevel.PROTECTED)
public class KafkaConsumerClient extends KafkaAbstractClient {

  /** The default group. */
  protected String defaultGroup = "karate-kafka-default-consumer-group";

  /** The default offset. */
  protected String defaultOffset = "earliest";

  /**
   * Instantiates a new kafka consumer client.
   *
   * @param configMap the config map
   */
  public KafkaConsumerClient(final Map<Object, Object> configMap) {
    super(configMap);
    if (configuration.containsKey(ConsumerConfig.GROUP_ID_CONFIG)) {
      defaultGroup = configuration.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
    }
    if (configuration.containsKey(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)) {
      defaultOffset = configuration.getProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG);
    }
  }

  /**
   * Consume.
   *
   * @param topic the topic
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String topic) throws JsonProcessingException {
    final String[] topics = {topic};
    return this.consume(topics).get(topic);
  }

  /**
   * Consume.
   *
   * @param topic the topic
   * @param timeout the timeout
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String topic, final long timeout) throws JsonProcessingException {
    final String[] topics = {topic};
    return this.consume(topics, timeout).get(topic);
  }

  /**
   * Consume.
   *
   * @param topic the topic
   * @param group the group
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String topic, final String group) throws JsonProcessingException {
    final String[] topics = {topic};
    return this.consume(topics, group, defaultOffset).get(topic);
  }

  /**
   * Consume.
   *
   * @param topic the topic
   * @param group the group
   * @param timeout the timeout
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String topic, final String group, final long timeout) throws JsonProcessingException {
    final String[] topics = {topic};
    return this.consume(topics, group, defaultOffset, timeout).get(topic);
  }

  /**
   * Consume.
   *
   * @param topic the topic
   * @param group the group
   * @param offset the offset
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String topic, final String group, final String offset) throws JsonProcessingException {
    final String[] topics = {topic};
    return this.consume(topics, group, offset).get(topic);
  }

  /**
   * Consume.
   *
   * @param topic the topic
   * @param group the group
   * @param offset the offset
   * @param timeout the timeout
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String topic, final String group, final String offset, final long timeout)
      throws JsonProcessingException {
    final String[] topics = {topic};
    return this.consume(topics, group, offset, timeout).get(topic);
  }

  /**
   * Consume.
   *
   * @param topics the topics
   * @return the map
   * @throws JsonProcessingException the json processing exception
   */
  public Map<String, List<Map<String, Object>>> consume(final String[] topics) throws JsonProcessingException {
    return this.consume(topics, defaultGroup);
  }

  /**
   * Consume.
   *
   * @param topics the topics
   * @param timeout the timeout
   * @return the map
   * @throws JsonProcessingException the json processing exception
   */
  public Map<String, List<Map<String, Object>>> consume(final String[] topics, final long timeout) throws JsonProcessingException {
    return this.consume(topics, defaultGroup, defaultOffset, timeout);
  }

  /**
   * Consume.
   *
   * @param topics the topics
   * @param group the group
   * @return the map
   * @throws JsonProcessingException the json processing exception
   */
  public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group) throws JsonProcessingException {
    return this.consume(topics, group, defaultOffset);
  }

  /**
   * Consume.
   *
   * @param topics the topics
   * @param group the group
   * @param offset the offset
   * @return the map
   * @throws JsonProcessingException the json processing exception
   */
  public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final String offset)
      throws JsonProcessingException {
    return this.consume(topics, group, offset, DEFAULT_TIMEOUT);
  }

  /**
   * Consume.
   *
   * @param topics the topics
   * @param group the group
   * @param timeout the timeout
   * @return the map
   * @throws JsonProcessingException the json processing exception
   */
  public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final long timeout)
      throws JsonProcessingException {
    return this.consume(topics, group, defaultOffset, timeout);
  }

  /**
   * Consume.
   *
   * @param topics the topics
   * @param group the group
   * @param offset the offset
   * @param timeout the timeout
   * @return the map
   * @throws JsonProcessingException the json processing exception
   */
  public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final String offset, final long timeout)
      throws JsonProcessingException {
    final JsonObject jsonMessages = KafkaConsumerClient.initializeJsonMessages(topics);
    // earliest: automatically reset the offset to the earliest offset
    // latest (default): automatically reset the offset to the latest offset
    // none: throw exception to the consumer if no previous offset is found for the consumer's group
    // anything else: throw exception to the consumer.
    configuration.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
    configuration.setProperty(ConsumerConfig.GROUP_ID_CONFIG, group);
    // Create Consumer
    int consumed = 0;
    try (final KafkaConsumer<?, ?> consumer = new KafkaConsumer<>(configuration)) {
      // Subscribe
      consumer.subscribe(Arrays.asList(topics));

      // consume till no records available or timeout exceeded
      final ConsumerRecords<?, ?> events = consumer.poll(Duration.ofMillis(timeout));
      if ((events != null) && !events.isEmpty()) {
        for (final ConsumerRecord<?, ?> event : events) {
          log.debug("consumed [topic={},offset={},key={},timestamp={},value={}]",
              event.topic(), event.offset(), event.key(), event.timestamp(), event.value());
          if (event.value() != null) {
            final JsonObject messageJSON = JsonParser.parseString(event.value().toString()).getAsJsonObject();
            ((JsonArray) jsonMessages.get(event.topic())).add(messageJSON);
            consumed++;
          } else {
            log.warn("consume() NULL event.value() [{}]", event);
          }
        }
      }
    } catch (final KafkaException | ClassCastException e) {
      log.error("consume() Exception", e);
      throw e;
    }
    log.debug("consume() jsonMessages={}", jsonMessages);
    log.info("consume() from topics [{}] => #{}", Arrays.toString(topics), consumed);
    final ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(jsonMessages.toString(), new TypeReference<Map<String, List<Map<String, Object>>>>() {});
  }

  /**
   * Gets the applicable key.
   *
   * @param key the key
   * @return the applicable key
   */
  @Override
  protected String getApplicableKey(final Object key) {
    if (key instanceof final String keyString && (keyString.startsWith("consumer.") || !keyString.startsWith("producer."))) {
      return keyString.replace("consumer.", "");
    }
    return null;
  }

  /**
   * Initialize json messages.
   *
   * @param topics the topics
   * @return the json object
   */
  protected static JsonObject initializeJsonMessages(final String[] topics) {
    final JsonObject jsonMessages = new JsonObject();
    for (final String topic : topics) {
      jsonMessages.add(topic, new JsonArray());
    }
    return jsonMessages;
  }
}
