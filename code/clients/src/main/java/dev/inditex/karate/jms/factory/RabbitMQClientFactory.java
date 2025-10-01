package dev.inditex.karate.jms.factory;

import java.util.Map;

import com.rabbitmq.jms.admin.RMQConnectionFactory;

/**
 * A factory for creating RabbitMQ ConnectionFactory objects.
 */
public class RabbitMQClientFactory {

  /** The Constant HOST. */
  public static final String HOST = "host"; // RabbitMQ server host

  /** The Constant PORT. */
  public static final String PORT = "port"; // RabbitMQ server port

  /** The Constant USERNAME. */
  public static final String USERNAME = "username"; // Username for authentication

  /** The Constant PASSWORD. */
  public static final String PASSWORD = "password"; // Password for authentication

  /** The Constant VIRTUAL_HOST. */
  public static final String VIRTUAL_HOST = "virtual-host"; // Virtual host

  /** The Constant CONNECTION_TIMEOUT. */
  public static final String CONNECTION_TIMEOUT = "on-message-timeout"; // Connection timeout in ms

  /** The Constant DEFAULT_PORT. */
  public static final int DEFAULT_PORT = 5672;

  /** The Constant DEFAULT_CONNECTION_TIMEOUT. */
  public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

  /** The Constant DEFAULT_VIRTUAL_HOST. */
  public static final String DEFAULT_VIRTUAL_HOST = "/";

  /**
   * Instantiates a new RabbitMQ client factory.
   */
  protected RabbitMQClientFactory() {
  }

  /**
   * Creates a new RabbitMQ ConnectionFactory object.
   *
   * @param config the config
   * @return the RabbitMQ ConnectionFactory
   */
  public static RMQConnectionFactory createConnectionFactory(final Map<Object, Object> config) {
    String host = null;
    int port = DEFAULT_PORT;
    host = String.valueOf(config.get(HOST));
    if (config.containsKey(PORT)) {
      port = Integer.parseInt(String.valueOf(config.get(PORT)));
    }
    final String user = String.valueOf(config.get(USERNAME));
    final String pwd = String.valueOf(config.get(PASSWORD));
    String vhost = DEFAULT_VIRTUAL_HOST;
    if (config.containsKey(VIRTUAL_HOST)) {
      vhost = String.valueOf(config.get(VIRTUAL_HOST));
    }
    int timeout = DEFAULT_CONNECTION_TIMEOUT;
    if (config.containsKey(CONNECTION_TIMEOUT)) {
      timeout = Integer.parseInt(String.valueOf(config.get(CONNECTION_TIMEOUT)));
    }
    final RMQConnectionFactory cf = new RMQConnectionFactory();
    cf.setHost(host);
    cf.setPort(port);
    cf.setUsername(user);
    cf.setPassword(pwd);
    cf.setVirtualHost(vhost);
    cf.setOnMessageTimeoutMs(timeout);
    return cf;
  }
}
