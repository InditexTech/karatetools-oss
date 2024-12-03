package dev.inditex.karate.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import dev.inditex.karate.AbstractClientTest;
import dev.inditex.karate.jms.factory.JMSClientFactory;
import dev.inditex.karate.logging.KarateClientLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;

public class JMSClientTest extends AbstractClientTest {

  public static final String JMS_FACTORY = "jmsFactory"; // JMS Factory

  public static final long DEFAULT_TIMEOUT = 5000;

  public static final String PLAIN_TEXT_MESSAGE = "Plain Text Message";

  public static final String XML_MESSAGE = """
      <?xml version="1.0"?>
      <karate id="1">
        <name>name</name>
        <value>1</value>
      </karate>
      """;

  @Nested
  class Constructor {
    @Test
    void when_valid_config_expect_fields_informed() {
      final Map<Object, Object> config = getConfig();

      final JMSClient client = instantiateClient(config);

      assertThat(client.getConfig()).isEqualTo(config);
    }
  }

  @Nested
  class CreateConnectionFactory {
    @Test
    void when_valid_config_expect_delegate() throws JMSException {
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        createConnectionFactoryMock(clientFactory, config);

        final var result = client.createConnectionFactory();

        assertThat(result).isNotNull();
        clientFactory.verify(() -> JMSClientFactory.createConnectionFactory(config), times(1));
      }
    }
  }

  @Nested
  class Available {
    @Test
    void when_first_call_expect_connection_test() {
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);

        final var result = client.available();

        assertThat(result).isTrue();
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("available()=true"));
      }
    }

    @Test
    void when_connection_test_exception_expect_not_available() {
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        when(connectionFactory.createContext()).thenThrow(new RuntimeException("JMS"));

        final var result = client.available();

        assertThat(result).isFalse();
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.ERROR) && log.getFormattedMessage().contains("available() Exception=JMS"))
            .anyMatch(log -> log.getLevel().equals(Level.INFO) && log.getFormattedMessage().contains("available()=false"));
      }
    }

    @Test
    void when_already_available_expect_no_connection_test() {
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);

        final var resultFirst = client.available();
        final var result = client.available();

        assertThat(resultFirst).isTrue();
        assertThat(result).isTrue();
        verify(connectionFactory, times(1)).createContext();
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("available()=true"));
      }
    }
  }

  @Nested
  class Send {
    @Test
    void when_send_map_expect_delegate() throws JMSException, JsonProcessingException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenReturn(jmsTextMessage);
        final Map<String, Object> message = Map.of("id", "1", "name", "name", "value", 1);
        final String messageText = new ObjectMapper().writeValueAsString(message);

        client.send(queue, message);

        verify(jmsContext, times(1)).createTextMessage(messageText);
        verify(jmsTextMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, times(1)).send(destination, jmsTextMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to queue [queue] completed"));
      }
    }

    @Test
    void when_send_map_with_properties_expect_delegate() throws JMSException, JsonProcessingException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenReturn(jmsTextMessage);
        final Map<String, Object> message = Map.of("id", "1", "name", "name", "value", 1);
        final String messageText = new ObjectMapper().writeValueAsString(message);
        final Map<String, Object> properties = Map.of("status", "01");

        client.send(queue, message, properties);

        verify(jmsContext, times(1)).createTextMessage(messageText);
        verify(jmsTextMessage, times(1)).setObjectProperty("status", "01");
        verify(jmsProducer, times(1)).send(destination, jmsTextMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to queue [queue] completed"));
      }
    }

    @Test
    void when_send_map_exception_expect_exception() throws JMSException, JsonProcessingException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenThrow(new JMSRuntimeException("JMSClient.send()"));
        final Map<String, Object> message = Map.of("id", "1", "name", "name", "value", 1);
        final String messageText = new ObjectMapper().writeValueAsString(message);

        assertThatThrownBy(() -> {
          client.send(queue, message);
        }).isInstanceOf(JMSRuntimeException.class).hasMessage("JMSClient.send()");

        verify(jmsContext, times(1)).createTextMessage(messageText);
        verify(jmsTextMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, never()).send(destination, jmsTextMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("send() Exception"));
      }
    }

    @Test
    void when_send_object_expect_delegate() throws JMSException, JsonProcessingException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final ObjectMessage jmsObjectMessage = mock(ObjectMessage.class);
        when(jmsContext.createObjectMessage(any())).thenReturn(jmsObjectMessage);
        final JMSKarateObject message = new JMSKarateObject("1", "name", 1);

        client.send(queue, message);

        verify(jmsContext, times(1)).createObjectMessage(message);
        verify(jmsObjectMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, times(1)).send(destination, jmsObjectMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to queue [queue] completed"));
      }
    }

    @Test
    void when_send_object_with_properties_expect_delegate() throws JMSException, JsonProcessingException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final ObjectMessage jmsObjectMessage = mock(ObjectMessage.class);
        when(jmsContext.createObjectMessage(any())).thenReturn(jmsObjectMessage);
        final JMSKarateObject message = new JMSKarateObject("1", "name", 1);
        final Map<String, Object> properties = Map.of("status", "01");

        client.send(queue, message, properties);

        verify(jmsContext, times(1)).createObjectMessage(message);
        verify(jmsObjectMessage, times(1)).setObjectProperty("status", "01");
        verify(jmsProducer, times(1)).send(destination, jmsObjectMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to queue [queue] completed"));
      }
    }

    @Test
    void when_send_object_exception_expect_exception() throws JMSException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final ObjectMessage jmsObjectMessage = mock(ObjectMessage.class);
        when(jmsContext.createObjectMessage(any())).thenThrow(new JMSRuntimeException("JMSClient.send()"));
        final JMSKarateObject message = new JMSKarateObject("1", "name", 1);

        assertThatThrownBy(() -> {
          client.send(queue, message);
        }).isInstanceOf(JMSRuntimeException.class).hasMessage("JMSClient.send()");

        verify(jmsContext, times(1)).createObjectMessage(message);
        verify(jmsObjectMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, never()).send(destination, jmsObjectMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("send() Exception [JMSClient.send()]"));
      }
    }

    @Test
    void when_send_plain_text_expect_delegate() throws JMSException, JsonProcessingException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenReturn(jmsTextMessage);
        final String message = PLAIN_TEXT_MESSAGE;

        client.send(queue, message);

        verify(jmsContext, times(1)).createTextMessage(message);
        verify(jmsTextMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, times(1)).send(destination, jmsTextMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to queue [queue] completed"));
      }
    }

    @Test
    void when_send_plain_text_with_properties_expect_delegate() throws JMSException, JsonProcessingException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenReturn(jmsTextMessage);
        final String message = PLAIN_TEXT_MESSAGE;
        final Map<String, Object> properties = Map.of("status", "01");

        client.send(queue, message, properties);

        verify(jmsContext, times(1)).createTextMessage(message);
        verify(jmsTextMessage, times(1)).setObjectProperty("status", "01");
        verify(jmsProducer, times(1)).send(destination, jmsTextMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
            && log.getFormattedMessage().contains("send() to queue [queue] completed"));
      }
    }

    @Test
    void when_send_plain_text_exception_expect_exception() throws JMSException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenThrow(new JMSRuntimeException("JMSClient.send()"));
        final String message = PLAIN_TEXT_MESSAGE;

        assertThatThrownBy(() -> {
          client.send(queue, message);
        }).isInstanceOf(JMSRuntimeException.class).hasMessage("JMSClient.send()");

        verify(jmsContext, times(1)).createTextMessage(message);
        verify(jmsTextMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, never()).send(destination, jmsTextMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("send() Exception [JMSClient.send()]"));
      }
    }

    @Test
    void when_send_xml_expect_delegate() throws JMSException, JsonProcessingException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenReturn(jmsTextMessage);
        final String message = XML_MESSAGE;

        client.send(queue, message);

        verify(jmsContext, times(1)).createTextMessage(message);
        verify(jmsTextMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, times(1)).send(destination, jmsTextMessage);
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.INFO) && log.getFormattedMessage().contains("send() to queue [queue] completed"))
            .anyMatch(log -> log.getLevel().equals(Level.DEBUG) && log.getFormattedMessage().contains("send() Mock for"));
      }
    }

    @Test
    void when_send_xml_with_properties_expect_delegate() throws JMSException, JsonProcessingException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenReturn(jmsTextMessage);
        final String message = XML_MESSAGE;
        final Map<String, Object> properties = Map.of("status", "01");

        client.send(queue, message, properties);

        verify(jmsContext, times(1)).createTextMessage(message);
        verify(jmsTextMessage, times(1)).setObjectProperty("status", "01");
        verify(jmsProducer, times(1)).send(destination, jmsTextMessage);
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.INFO) && log.getFormattedMessage().contains("send() to queue [queue] completed"))
            .anyMatch(log -> log.getLevel().equals(Level.DEBUG) && log.getFormattedMessage().contains("send() Mock for"));
      }
    }

    @Test
    void when_send_xml_exception_expect_exception() throws JMSException {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSProducer jmsProducer = mock(JMSProducer.class);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsContext.createTextMessage(any())).thenThrow(new JMSRuntimeException("JMSClient.send()"));
        final String message = XML_MESSAGE;

        assertThatThrownBy(() -> {
          client.send(queue, message);
        }).isInstanceOf(JMSRuntimeException.class).hasMessage("JMSClient.send()");

        verify(jmsContext, times(1)).createTextMessage(message);
        verify(jmsTextMessage, never()).setObjectProperty(any(), any());
        verify(jmsProducer, never()).send(destination, jmsTextMessage);
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("send() Exception [JMSClient.send()]"));
      }
    }

  }

  @Nested
  class Consume {
    @Test
    void when_consume_exception_expect_exception() {
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSConsumer jmsConsumer = mock(JMSConsumer.class);
        when(jmsContext.createConsumer(any())).thenReturn(jmsConsumer);
        when(jmsConsumer.receive(DEFAULT_TIMEOUT)).thenThrow(new JMSRuntimeException("JMSClient.consume()"));

        assertThatThrownBy(() -> {
          client.consume(queue);
        }).isInstanceOf(JMSRuntimeException.class).hasMessage("JMSClient.consume()");

        verify(jmsConsumer, times(1)).receive(DEFAULT_TIMEOUT);
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.ERROR)
                && log.getFormattedMessage().contains("consume() Exception [JMSClient.consume()]"));
      }
    }

    @Test
    void when_consume_map_expect_delegate() throws JMSException, JsonProcessingException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSConsumer jmsConsumer = mock(JMSConsumer.class);
        when(jmsContext.createConsumer(any())).thenReturn(jmsConsumer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsConsumer.receive(DEFAULT_TIMEOUT)).thenReturn(jmsTextMessage).thenReturn(null);
        final Map<String, Object> message = Map.of("id", "1", "name", "name", "value", 1);
        final String messageText = new ObjectMapper().writeValueAsString(message);
        when(jmsTextMessage.getText()).thenReturn(messageText);

        final var result = client.consume(queue);

        verify(jmsConsumer, times(2)).receive(DEFAULT_TIMEOUT);
        assertThat(result).isNotNull().isEqualTo(List.of(message));
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.INFO) && log.getFormattedMessage().contains("consume() from queue [queue] => #1"))
            .anyMatch(log -> log.getLevel().equals(Level.DEBUG) && log.getFormattedMessage().contains("consume() jsonMessages="));
      }
    }

    @Test
    void when_consume_object_expect_delegate() throws JMSException, JsonProcessingException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSConsumer jmsConsumer = mock(JMSConsumer.class);
        when(jmsContext.createConsumer(any())).thenReturn(jmsConsumer);
        final ObjectMessage jmsObjectMessage = mock(ObjectMessage.class);
        when(jmsConsumer.receive(DEFAULT_TIMEOUT)).thenReturn(jmsObjectMessage).thenReturn(null);
        final JMSKarateObject message = new JMSKarateObject("1", "name", 1);
        final Map<String, Object> messageObject = Map.of("id", "1", "name", "name", "value", 1);
        when(jmsObjectMessage.getBody(Object.class)).thenReturn(message);

        final var result = client.consume(queue);

        verify(jmsConsumer, times(2)).receive(DEFAULT_TIMEOUT);
        assertThat(result).isNotNull().isEqualTo(List.of(messageObject));
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.INFO) && log.getFormattedMessage().contains("consume() from queue [queue] => #1"))
            .anyMatch(log -> log.getLevel().equals(Level.DEBUG) && log.getFormattedMessage().contains("consume() jsonMessages="));
      }
    }

    @Test
    void when_consume_plain_text_expect_delegate() throws JMSException, JsonProcessingException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSConsumer jmsConsumer = mock(JMSConsumer.class);
        when(jmsContext.createConsumer(any())).thenReturn(jmsConsumer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsConsumer.receive(DEFAULT_TIMEOUT)).thenReturn(jmsTextMessage).thenReturn(null);
        final String message = PLAIN_TEXT_MESSAGE;
        when(jmsTextMessage.getText()).thenReturn(message);

        final var result = client.consume(queue);

        verify(jmsConsumer, times(2)).receive(DEFAULT_TIMEOUT);
        assertThat(result).isNotNull().isEqualTo(List.of(Map.of("textMessage", message)));
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.INFO) && log.getFormattedMessage().contains("consume() from queue [queue] => #1"))
            .anyMatch(log -> log.getLevel().equals(Level.DEBUG) && log.getFormattedMessage().contains("consume() jsonMessages="));
      }
    }

    @Test
    void when_consume_xml_expect_delegate() throws JMSException, JsonProcessingException {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final String queue = "queue";
      try (final MockedStatic<? extends JMSClientFactory> clientFactory = mockStaticFactory()) {
        final Map<Object, Object> config = getConfig();
        final JMSClient client = instantiateClient(config);
        final ConnectionFactory connectionFactory = createConnectionFactoryMock(clientFactory, config);
        final JMSContext jmsContext = mock(JMSContext.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        final Queue destination = mock(Queue.class);
        when(jmsContext.createQueue(queue)).thenReturn(destination);
        final JMSConsumer jmsConsumer = mock(JMSConsumer.class);
        when(jmsContext.createConsumer(any())).thenReturn(jmsConsumer);
        final TextMessage jmsTextMessage = mock(TextMessage.class);
        when(jmsConsumer.receive(DEFAULT_TIMEOUT)).thenReturn(jmsTextMessage).thenReturn(null);
        final String message = XML_MESSAGE;
        when(jmsTextMessage.getText()).thenReturn(message);

        final var result = client.consume(queue);

        verify(jmsConsumer, times(2)).receive(DEFAULT_TIMEOUT);
        assertThat(result).isNotNull().isEqualTo(List.of(Map.of("textMessage", message)));
        assertThat(logWatcher.list)
            .anyMatch(log -> log.getLevel().equals(Level.INFO) && log.getFormattedMessage().contains("consume() from queue [queue] => #1"))
            .anyMatch(log -> log.getLevel().equals(Level.DEBUG) && log.getFormattedMessage().contains("consume() jsonMessages="));
      }
    }
  }

  protected static Map<Object, Object> getConfig() {
    return Map.of(JMS_FACTORY, "any");
  }

  protected JMSClient instantiateClient(final Map<Object, Object> config) {
    return new JMSClient(config);
  }

  protected MockedStatic<? extends JMSClientFactory> mockStaticFactory() {
    return mockStatic(JMSClientFactory.class);
  }

  protected ConnectionFactory createConnectionFactoryMock(final MockedStatic<? extends JMSClientFactory> clientFactory,
      final Map<Object, Object> config) {
    final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    clientFactory.when(() -> JMSClientFactory.createConnectionFactory(config)).thenReturn(connectionFactory);
    return connectionFactory;
  }

}
