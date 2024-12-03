package dev.inditex.karate.jms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.inditex.karate.jms.factory.JMSClientFactory;
import dev.inditex.karate.logging.KarateClientLogger;
import dev.inditex.karate.parser.SystemPropertiesParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.TextMessage;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * The Class JMSClient.
 */
@Getter(AccessLevel.PROTECTED)
public class JMSClient {

  /** The Constant DEFAULT_TIMEOUT. */
  public static final long DEFAULT_TIMEOUT = 5000;

  /** The config. */
  protected Map<Object, Object> config;

  /** The is JMS available. */
  protected Boolean isJMSAvailable;

  /** The log. */
  protected final KarateClientLogger log = new KarateClientLogger();

  /**
   * Instantiates a new JMS client.
   *
   * @param configMap the config map
   */
  public JMSClient(final Map<Object, Object> configMap) {
    super();
    config = SystemPropertiesParser.parseConfiguration(configMap);
  }

  /**
   * Creates the connection factory.
   *
   * @return the connection factory
   * @throws JMSException the JMS exception
   */
  public ConnectionFactory createConnectionFactory() throws JMSException {
    return JMSClientFactory.createConnectionFactory(config);
  }

  /**
   * Available.
   *
   * @return the boolean
   */
  public Boolean available() {
    log.debug("available() ... ");
    if (isJMSAvailable == null) {
      try {
        final ConnectionFactory cf = createConnectionFactory();

        try (final JMSContext context = cf.createContext()) {
          isJMSAvailable = true;
        }
        log.debug("available() {}", isJMSAvailable);

      } catch (final Exception e) {
        log.error("available() Exception={}", e.getMessage());
        isJMSAvailable = false;
      }
    }
    log.info("available()={}", isJMSAvailable);
    return isJMSAvailable;
  }

  /**
   * Send.
   *
   * @param queue the queue
   * @param value the value
   * @throws JMSException the JMS exception
   * @throws JsonProcessingException the json processing exception
   */
  public void send(final String queue, final Object value) throws JMSException, JsonProcessingException {
    send(queue, value, null);
  }

  /**
   * Send.
   *
   * @param queue the queue
   * @param value the value
   * @param properties the properties
   * @throws JMSException the JMS exception
   * @throws JsonProcessingException the json processing exception
   */
  public void send(final String queue, final Object value, final Map<String, Object> properties)
      throws JMSException, JsonProcessingException {
    log.debug("send() ...");
    final ConnectionFactory cf = createConnectionFactory();
    Message message = null;
    try (final JMSContext context = cf.createContext()) {
      final Destination destination = context.createQueue(queue);
      if (!(value instanceof Map<?, ?> || value instanceof String) && value instanceof final Serializable serial) {
        // If value is not instance of Map or String and Serializable -> Object Message
        message = context.createObjectMessage(serial);
      } else if (value instanceof final String text) {
        // If value is instance of String -> Text Message
        message = context.createTextMessage(text);
      } else {
        // If value is instance of Map or not Serializable -> Text Message via Object Mapper
        final ObjectMapper mapper = new ObjectMapper();
        final var messageJSON = mapper.writeValueAsString(value);
        message = context.createTextMessage(messageJSON);
      }
      if (message == null) {
        log.error("send() Unable to build message for [{}]", value);
        return;
      }
      if (properties != null) {
        for (final Entry<String, Object> entry : properties.entrySet()) {
          message.setObjectProperty(entry.getKey(), entry.getValue());
        }
      }

      log.debug("send() {}", message);
      final JMSProducer producer = context.createProducer();
      producer.send(destination, message);
      log.info("send() to queue [{}] completed", queue);
    } catch (final JMSRuntimeException e) {
      log.error("send() Exception [{}][{}]", e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Consume.
   *
   * @param queue the queue
   * @return the list
   * @throws JMSException the JMS exception
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String queue) throws JMSException, JsonProcessingException {
    return consume(queue, DEFAULT_TIMEOUT);
  }

  /**
   * Consume.
   *
   * @param queue the queue
   * @param timeout the timeout
   * @return the list
   * @throws JMSException the JMS exception
   * @throws JsonProcessingException the json processing exception
   */
  public List<Map<String, Object>> consume(final String queue, final long timeout) throws JMSException, JsonProcessingException {
    final JsonArray jsonMessages = new JsonArray();
    log.debug("consume({}) ...", queue);
    final ConnectionFactory cf = createConnectionFactory();

    try (final JMSContext context = cf.createContext()) {
      final Destination destination = context.createQueue(queue);
      try (final JMSConsumer consumer = context.createConsumer(destination)) {
        while (true) {
          final Message message = consumer.receive(timeout);
          if (message != null) {
            if (message instanceof final TextMessage textMessage) {
              // javax.jms.TextMessage
              jsonMessages.add(parseTextMessage(textMessage));
            } else {
              final Object value = message.getBody(Object.class);
              final ObjectMapper mapper = new ObjectMapper();
              mapper.registerModule(new JavaTimeModule());
              final JsonObject messageJSON = JsonParser.parseString(mapper.writer().writeValueAsString(value)).getAsJsonObject();
              jsonMessages.add(messageJSON);
            }
          } else {
            break;
          }
        }
      }
    } catch (final JMSRuntimeException e) {
      log.error("consume() Exception [{}][{}]", e.getMessage(), e);
      throw e;
    }
    log.debug("consume() jsonMessages={}", jsonMessages);
    log.info("consume() from queue [{}] => #{}", queue, jsonMessages.size());
    final ObjectMapper mapper = new ObjectMapper();

    return mapper.readValue(jsonMessages.toString(), new TypeReference<List<Map<String, Object>>>() {});
  }

  /**
   * Parses the text message.
   *
   * @param textMessage the text message
   * @return the json object
   * @throws JMSException the JMS exception
   */
  protected JsonObject parseTextMessage(final TextMessage textMessage) throws JMSException {
    JsonObject messageJSON = null;
    try {
      // javax.jms.TextMessage
      messageJSON = JsonParser.parseString(textMessage.getText()).getAsJsonObject();
    } catch (final Exception e) {
      log.debug("parseMessage() => {}. Parsing as Text", e.getMessage());
      // Not a JSON - Process as value
      messageJSON = new JsonObject();
      messageJSON.addProperty("textMessage", textMessage.getText());
    }
    return messageJSON;
  }
}
