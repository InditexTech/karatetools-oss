package dev.inditex.karate.jms.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class RabbitMQClientFactoryTest {
  public static final String HOST = "host"; // RabbitMQ server host.

  public static final String PORT = "port"; // RabbitMQ server port.

  public static final String USERNAME = "username"; // User name that the application uses to connect to RabbitMQ.

  public static final String PASSWORD = "password"; // Password that the application uses to connect to RabbitMQ.

  public static final String VIRTUAL_HOST = "virtual-host"; // virtual host to be used when creating a connection to RabbitMQ.

  public static final String CONNECTION_TIMEOUT = "on-message-timeout"; // Timeout in milliseconds for processing messages.

  public static final int DEFAULT_PORT = 5672;

  public static final int DEFAULT_TIMEOUT = 5000;

  public static final String DEFAULT_VIRTUAL_HOST = "/";

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(RabbitMQClientFactory::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class CreateConnectionFactory {
    @Test
    void when_create_expect_valid() {
      final Map<Object, Object> config = getValidConfig(null, null, null);
      final String host = String.valueOf(config.get(HOST));
      final String user = String.valueOf(config.get(USERNAME));
      final String pwd = String.valueOf(config.get(PASSWORD));
      try (final MockedConstruction<RMQConnectionFactory> mockedConnectionFactoryConstruction =
          mockConstruction(RMQConnectionFactory.class)) {

        final var result = RabbitMQClientFactory.createConnectionFactory(config);

        assertThat(result).isNotNull();
        assertThat(mockedConnectionFactoryConstruction.constructed()).hasSize(1);
        final RMQConnectionFactory jmsConnectionFactory = mockedConnectionFactoryConstruction.constructed().get(0);
        verify(jmsConnectionFactory, times(1)).setHost(host);
        verify(jmsConnectionFactory, times(1)).setPort(DEFAULT_PORT);
        verify(jmsConnectionFactory, times(1)).setUsername(user);
        verify(jmsConnectionFactory, times(1)).setPassword(pwd);
        verify(jmsConnectionFactory, times(1)).setVirtualHost(DEFAULT_VIRTUAL_HOST);
        verify(jmsConnectionFactory, times(1)).setOnMessageTimeoutMs(DEFAULT_TIMEOUT);
      }
    }

    @Test
    void when_create_with_custom_values_expect_valid() {
      final int customPort = 5678;
      final int customTimeout = 1234;
      final String customVhost = "myvhost";
      final Map<Object, Object> config = getValidConfig(customPort, customTimeout, customVhost);
      final String host = String.valueOf(config.get(HOST));
      final int port = customPort;
      final String user = String.valueOf(config.get(USERNAME));
      final String pwd = String.valueOf(config.get(PASSWORD));
      final String vhost = customVhost;
      final int timeout = customTimeout;
      try (final MockedConstruction<RMQConnectionFactory> mockedConnectionFactoryConstruction =
          mockConstruction(RMQConnectionFactory.class)) {

        final var result = RabbitMQClientFactory.createConnectionFactory(config);

        assertThat(result).isNotNull();
        assertThat(mockedConnectionFactoryConstruction.constructed()).hasSize(1);
        final RMQConnectionFactory jmsConnectionFactory = mockedConnectionFactoryConstruction.constructed().get(0);
        verify(jmsConnectionFactory, times(1)).setHost(host);
        verify(jmsConnectionFactory, times(1)).setPort(port);
        verify(jmsConnectionFactory, times(1)).setUsername(user);
        verify(jmsConnectionFactory, times(1)).setPassword(pwd);
        verify(jmsConnectionFactory, times(1)).setVirtualHost(vhost);
        verify(jmsConnectionFactory, times(1)).setOnMessageTimeoutMs(timeout);
      }
    }
  }

  @Nested
  class CreateAmqpDestination {
    @Test
    void when_create_amqp_destination_expect_valid() {
      final String queue = "myQueue";

      final RMQDestination result = RabbitMQClientFactory.createAmqpDestination(queue);

      assertThat(result).isNotNull();
      assertThat(result.isAmqp()).isTrue();
      assertThat(result.getAmqpExchangeName()).isEmpty();
      assertThat(result.getAmqpRoutingKey()).isEqualTo(queue);
    }
  }

  private static Map<Object, Object> getValidConfig(final Integer port, final Integer timeout, final String vhost) {
    final Map<Object, Object> defaultConfig = Map.of(HOST, "localhost",
        USERNAME, "user",
        PASSWORD, "pwd");
    final Map<Object, Object> config = new HashMap<>(defaultConfig);
    if (port != null) {
      config.put(PORT, port);
    }
    if (timeout != null) {
      config.put(CONNECTION_TIMEOUT, timeout);
    }
    if (vhost != null) {
      config.put(VIRTUAL_HOST, vhost);
    }
    return config;
  }
}
