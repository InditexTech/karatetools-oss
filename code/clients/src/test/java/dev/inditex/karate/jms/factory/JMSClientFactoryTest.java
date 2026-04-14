package dev.inditex.karate.jms.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

public class JMSClientFactoryTest {

  public static final String JMS_FACTORY = "jmsFactory"; // JMS Factory

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(JMSClientFactory::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class CreateConnectionFactory {

    @ParameterizedTest
    @ValueSource(strings = {"ActiveMQ", "activemq", "ACTIVEMQ"})
    void when_ActiveMQ_expect_delegate(final String jmsFactoryName) throws JMSException {
      try (final MockedStatic<ActiveMQClientFactory> clientFactoryMock = mockStatic(ActiveMQClientFactory.class)) {
        final ActiveMQConnectionFactory connectionFactoryMock = mock(ActiveMQConnectionFactory.class);
        final Map<Object, Object> config = getConfig(jmsFactoryName);
        clientFactoryMock.when(() -> JMSClientFactory.createConnectionFactory(config)).thenReturn(connectionFactoryMock);

        final var result = JMSClientFactory.createConnectionFactory(config);

        assertThat(result).isNotNull();
        clientFactoryMock.verify(() -> JMSClientFactory.createConnectionFactory(config), times(1));
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {"RabbitMQ", "rabbitmq", "RABBITMQ"})
    void when_RabbitMQ_expect_delegate(final String jmsFactoryName) throws JMSException {
      try (final MockedStatic<RabbitMQClientFactory> clientFactoryMock = mockStatic(RabbitMQClientFactory.class)) {
        final var connectionFactoryMock = mock(RMQConnectionFactory.class);
        final Map<Object, Object> config = getConfig(jmsFactoryName);
        clientFactoryMock.when(() -> JMSClientFactory.createConnectionFactory(config)).thenReturn(connectionFactoryMock);

        final var result = JMSClientFactory.createConnectionFactory(config);

        assertThat(result).isNotNull();
        clientFactoryMock.verify(() -> JMSClientFactory.createConnectionFactory(config), times(1));
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {"jmsFactory", "Active MQ"})
    void when_invalid_expect_expection(final String jmsFactoryName) {
      final Map<Object, Object> config = getConfig(jmsFactoryName);

      assertThatThrownBy(() -> JMSClientFactory.createConnectionFactory(config))
          .isInstanceOf(JMSException.class)
          .hasMessage("Invalid JMSClientFactory: " + jmsFactoryName);
    }
  }

  @Nested
  class CreateDestination {
    @Test
    void when_rabbitmq_amqp_config_expect_amqp_destination() {
      final JMSContext context = mock(JMSContext.class);
      final String queue = "queue";
      final Map<Object, Object> config = Map.of(JMS_FACTORY, "RabbitMQ", "amqp", "true");

      final Destination result = JMSClientFactory.createDestination(context, queue, config);

      assertThat(result).isNotNull().isInstanceOf(com.rabbitmq.jms.admin.RMQDestination.class);
      verify(context, never()).createQueue(any());
    }

    @Test
    void when_default_config_expect_queue_destination() {
      final JMSContext context = mock(JMSContext.class);
      final String queue = "queue";
      final Map<Object, Object> config = Map.of(JMS_FACTORY, "RabbitMQ", "amqp", "false");
      final javax.jms.Queue queueDestination = mock(javax.jms.Queue.class);
      when(context.createQueue(queue)).thenReturn(queueDestination);

      final Destination result = JMSClientFactory.createDestination(context, queue, config);

      assertThat(result).isNotNull().isEqualTo(queueDestination);
      verify(context, times(1)).createQueue(queue);
    }

    @Test
    void when_invalid_factory_expect_queue_destination() {
      final JMSContext context = mock(JMSContext.class);
      final String queue = "queue";
      final Map<Object, Object> config = Map.of(JMS_FACTORY, "invalid_factory");
      final javax.jms.Queue queueDestination = mock(javax.jms.Queue.class);
      when(context.createQueue(queue)).thenReturn(queueDestination);

      final Destination result = JMSClientFactory.createDestination(context, queue, config);

      assertThat(result).isNotNull().isEqualTo(queueDestination);
      verify(context, times(1)).createQueue(queue);
    }
  }

  protected Map<Object, Object> getConfig(final String value) {
    return Map.of(JMS_FACTORY, value);
  }
}
