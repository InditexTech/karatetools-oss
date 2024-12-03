package dev.inditex.karate.jms.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class ActiveMQClientFactoryTest {
  public static final String BROKER_URL = "brokerURL"; // Identifies the URL where the ActiveMQ broker is listening.

  public static final String USERNAME = "username"; // User to establish the connection to the ActiveMQ broker.

  public static final String PASSWORD = "password"; // Password to establish the connection to the ActiveMQ broker.

  public static final String SEND_TIMEOUT = "sendTimeout"; // Timeout for sending messages

  public static final int DEFAULT_TIMEOUT = 5000;

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(ActiveMQClientFactory::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class CreateConnectionFactory {
    @Test
    void when_create_expect_valid() throws JMSException {
      final Map<Object, Object> config = getValidConfig(null);
      final String brokerURL = String.valueOf(config.get(BROKER_URL));
      final String user = String.valueOf(config.get(USERNAME));
      final String pwd = String.valueOf(config.get(PASSWORD));
      try (final MockedConstruction<ActiveMQConnectionFactory> mockedConnectionFactoryConstruction =
          mockConstruction(ActiveMQConnectionFactory.class)) {

        final var result = ActiveMQClientFactory.createConnectionFactory(config);

        assertThat(result).isNotNull();
        assertThat(mockedConnectionFactoryConstruction.constructed()).hasSize(1);
        final ActiveMQConnectionFactory jmsConnectionFactory = mockedConnectionFactoryConstruction.constructed().get(0);
        verify(jmsConnectionFactory, times(1)).setBrokerURL(brokerURL);
        verify(jmsConnectionFactory, times(1)).setUser(user);
        verify(jmsConnectionFactory, times(1)).setPassword(pwd);
        verify(jmsConnectionFactory, times(1)).setCallTimeout(DEFAULT_TIMEOUT);
      }
    }

    @Test
    void when_create_with_custom_timeput_expect_valid() throws JMSException {
      final Map<Object, Object> config = getValidConfig(1000);
      final String brokerURL = String.valueOf(config.get(BROKER_URL));
      final String user = String.valueOf(config.get(USERNAME));
      final String pwd = String.valueOf(config.get(PASSWORD));
      final int timeout = Integer.parseInt(String.valueOf(config.get(SEND_TIMEOUT)));
      try (final MockedConstruction<ActiveMQConnectionFactory> mockedConnectionFactoryConstruction =
          mockConstruction(ActiveMQConnectionFactory.class)) {

        final var result = ActiveMQClientFactory.createConnectionFactory(config);

        assertThat(result).isNotNull();
        assertThat(mockedConnectionFactoryConstruction.constructed()).hasSize(1);
        final ActiveMQConnectionFactory jmsConnectionFactory = mockedConnectionFactoryConstruction.constructed().get(0);
        verify(jmsConnectionFactory, times(1)).setBrokerURL(brokerURL);
        verify(jmsConnectionFactory, times(1)).setUser(user);
        verify(jmsConnectionFactory, times(1)).setPassword(pwd);
        verify(jmsConnectionFactory, times(1)).setCallTimeout(timeout);
      }
    }

  }

  private static Map<Object, Object> getValidConfig(final Integer timeout) {
    final Map<Object, Object> defaultConfig = Map.of(BROKER_URL, "tcp://brokerUrl:61616",
        USERNAME, "user",
        PASSWORD, "pwd");
    final Map<Object, Object> config = new HashMap<>(defaultConfig);
    if (timeout != null) {
      config.put(SEND_TIMEOUT, timeout);
    }
    return config;
  }
}
