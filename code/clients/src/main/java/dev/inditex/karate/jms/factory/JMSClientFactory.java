package dev.inditex.karate.jms.factory;

import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;

/**
 * A factory for creating JMSClient objects.
 */
public class JMSClientFactory {

  /** The Constant JMS_FACTORY. */
  public static final String JMS_FACTORY = "jmsFactory"; // JMS Factory

  /** The Constant AMQP. */
  public static final String AMQP = "amqp"; // Whether to use AMQP protocol for RabbitMQ

  /** The Constant AMQP_DEFAULT. */
  public static final boolean AMQP_DEFAULT = false; // Default value for AMQP protocol usage

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
      } else if (jmsFactory == JMSFactory.RABBITMQ) {
        return RabbitMQClientFactory.createConnectionFactory(config);
      }
    } catch (final IllegalArgumentException e) {
      throw new JMSException("Invalid JMSClientFactory: " + jmsFactoryConfig);
    }
    return null;
  }

  /**
   * Creates a new Destination object.
   *
   * @param context the JMS context
   * @param queue the queue name
   * @param config the config
   * @return the destination
   */
  public static Destination createDestination(final JMSContext context, final String queue,
      final Map<Object, Object> config) {
    final String jmsFactoryConfig = String.valueOf(config.get(JMS_FACTORY));
    try {
      final JMSFactory jmsFactory = JMSFactory.valueOf(jmsFactoryConfig.toUpperCase());
      if (jmsFactory == JMSFactory.RABBITMQ
          && Boolean.parseBoolean(String.valueOf(config.getOrDefault(AMQP, AMQP_DEFAULT)))) {
        return RabbitMQClientFactory.createAmqpDestination(queue);
      }
    } catch (final IllegalArgumentException e) {
      // fall through to default
    }
    return context.createQueue(queue);
  }
}

enum JMSFactory {
  ACTIVEMQ,
  RABBITMQ
}
