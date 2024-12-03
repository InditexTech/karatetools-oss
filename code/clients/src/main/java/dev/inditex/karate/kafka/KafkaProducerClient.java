package dev.inditex.karate.kafka;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;

/**
 * The Class KafkaProducerClient.
 */
@Getter(AccessLevel.PROTECTED)
public class KafkaProducerClient extends KafkaAbstractClient {

  /**
   * Instantiates a new kafka producer client.
   *
   * @param configMap the config map
   */
  public KafkaProducerClient(final Map<Object, Object> configMap) {
    super(configMap);
    if (configuration.getProperty(ProducerConfig.ACKS_CONFIG) == null) {
      configuration.setProperty(ProducerConfig.ACKS_CONFIG, "all");
    }
    if (configuration.getProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG) == null) {
      configuration.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    }
    if (configuration.getProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION) == null) {
      configuration.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
    }
    if (configuration.getProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG) == null) {
      configuration.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, String.valueOf(DEFAULT_TIMEOUT));
    }
  }

  /**
   * Send.
   *
   * @param topic the topic
   * @param event the event
   */
  public void send(final String topic, final Object event) {
    this.send(topic, event, null);
  }

  /**
   * Send.
   *
   * @param topic the topic
   * @param event the event
   * @param headers the headers
   */
  public void send(final String topic, final Object event, final Map<String, List<String>> headers) {
    try (final KafkaProducer<Object, Object> producer = new KafkaProducer<>(configuration)) {
      final ProducerRecord<Object, Object> producerRecord = new ProducerRecord<>(topic, event);
      if (headers != null) {
        final Headers recordHeaders = producerRecord.headers();
        for (final Entry<String, List<String>> header : headers.entrySet()) {
          for (final String headerValue : header.getValue()) {
            recordHeaders.add(new RecordHeader(header.getKey(), headerValue.getBytes(StandardCharsets.UTF_8.name())));
          }
        }
      }
      log.debug("send() {}", producerRecord);
      producer.send(producerRecord, (metadata, exception) -> {
        if (exception != null) {
          log.error("producer.send() Exception", exception);
          if (exception instanceof final KafkaException kafkaException) {
            log.error("producer.send() kafkaException", kafkaException);
            throw kafkaException;
          } else {
            log.error("producer.send() Exception", exception);
            throw new KafkaException(exception);
          }
        } else {
          log.debug("producer.send() Record sent [{}] with offset[{}]", producerRecord, metadata.offset());
        }
      });
      log.info("send() to topic [{}] completed", topic);
      producer.flush();
    } catch (final KafkaException e) {
      log.error("send() KafkaException", e);
      throw e;
    } catch (final Exception e) {
      log.error("send() Exception", e);
      throw new KafkaException(e);
    }
  }

  /**
   * Gets the applicable key.
   *
   * @param key the key
   * @return the applicable key
   */
  @Override
  protected String getApplicableKey(final Object key) {
    if (key instanceof final String keyString && (keyString.startsWith("producer.") || !keyString.startsWith("consumer."))) {
      return keyString.replace("producer.", "");
    }
    return null;
  }
}
