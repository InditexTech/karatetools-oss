package dev.inditex.karate.jms.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.util.Map;

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
    @ValueSource(strings = {"jmsFactory", "Active MQ"})
    void when_invalid_expect_expection(final String jmsFactoryName) {
      final Map<Object, Object> config = getConfig(jmsFactoryName);

      assertThatThrownBy(() -> JMSClientFactory.createConnectionFactory(config))
          .isInstanceOf(JMSException.class)
          .hasMessage("Invalid JMSClientFactory: " + jmsFactoryName);
    }
  }

  protected Map<Object, Object> getConfig(final String value) {
    return Map.of(JMS_FACTORY, value);
  }
}
