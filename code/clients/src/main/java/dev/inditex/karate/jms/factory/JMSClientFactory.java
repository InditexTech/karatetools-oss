package dev.inditex.karate.jms.factory;

import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * A factory for creating JMSClient objects.
 */
public class JMSClientFactory {

  /** The Constant JMS_FACTORY. */
  public static final String JMS_FACTORY = "jmsFactory"; // JMS Factory

  /**
   * Instantiates a new JMS client factory.
   */
  protected JMSClientFactory() {
  }

  /**
   * Creates a new JMSClient object.
   *
   * @param config the config
   * @return the connection factory
   * @throws JMSException the JMS exception
   */
  public static ConnectionFactory createConnectionFactory(final Map<Object, Object> config) throws JMSException {
    final String jmsFactoryConfig = String.valueOf(config.get(JMS_FACTORY));
    try {
      final JMSFactory jmsFactory = JMSFactory.valueOf(jmsFactoryConfig.toUpperCase());
      if (jmsFactory == JMSFactory.ACTIVEMQ) {
        return ActiveMQClientFactory.createConnectionFactory(config);
      }
    } catch (final IllegalArgumentException e) {
      throw new JMSException("Invalid JMSClientFactory: " + jmsFactoryConfig);
    }
    return null;
  }
}

enum JMSFactory {
  ACTIVEMQ
}
