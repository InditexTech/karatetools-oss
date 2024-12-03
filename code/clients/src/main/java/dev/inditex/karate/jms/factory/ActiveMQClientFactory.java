package dev.inditex.karate.jms.factory;

import java.util.Map;

import javax.jms.JMSException;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

/**
 * A factory for creating ActiveMQClient objects.
 */
public class ActiveMQClientFactory {

  /** The Constant KARATE_JMS_ACTIVE_MQ. */
  public static final String KARATE_JMS_ACTIVE_MQ = "karate-jms-active-mq";

  /** The Constant BROKER_URL. */
  public static final String BROKER_URL = "brokerURL"; // Identifies the URL where the ActiveMQ broker is listening.

  /** The Constant USERNAME. */
  public static final String USERNAME = "username"; // User to establish the connection to the ActiveMQ broker.

  /** The Constant PASSWORD. */
  public static final String PASSWORD = "password"; // Password to establish the connection to the ActiveMQ broker.

  /** The Constant SEND_TIMEOUT. */
  public static final String SEND_TIMEOUT = "sendTimeout"; // Timeout for sending messages

  /** The Constant DEFAULT_TIMEOUT. */
  public static final int DEFAULT_TIMEOUT = 5000;

  /**
   * Instantiates a new active MQ client factory.
   */
  protected ActiveMQClientFactory() {
  }

  /**
   * Creates a new ActiveMQClient object.
   *
   * @param config the config
   * @return the active MQ connection factory
   * @throws JMSException the JMS exception
   */
  public static ActiveMQConnectionFactory createConnectionFactory(final Map<Object, Object> config) throws JMSException {
    final String brokerURL = String.valueOf(config.get(BROKER_URL));
    final String user = String.valueOf(config.get(USERNAME));
    final String pwd = String.valueOf(config.get(PASSWORD));
    int timeout = DEFAULT_TIMEOUT;
    if (config.containsKey(SEND_TIMEOUT)) {
      timeout = Integer.parseInt(String.valueOf(config.get(SEND_TIMEOUT)));
    }
    final ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(); // NOSONAR
    cf.setBrokerURL(brokerURL);
    cf.setUser(user);
    cf.setPassword(pwd);
    cf.setCallTimeout(timeout);
    return cf;
  }
}
