@karate-clients
@jms @rabbitmq @jms-rabbitmq
@rabbitmq-client @amqp
@env=local

Feature: JMS Client Available Operations - Rabbit MQ (AMQP)

Background:

* def sleep = function(millis){ java.lang.Thread.sleep(millis) }

# Generate a unique queue name based on the test ID and prefix
# Example:
#           "e2e.activemq.json.karate.public.queue"
#           "e2e.rabbitmq.json.karate.public.queue"
#           "e2e.rabbitmq-amqp.json.karate.public.queue"
* def getQueue = function(testID, prefix){ return "e2e." + testID.toLowerCase().replace(/\s+/g, '-') + '.' + prefix + '.karate.public.queue' }

# public JMSClient(final Map<Object, Object> configMap)
# Instantiate JMSClient with AMQP config
Given def config = read('classpath:config/jms/rabbitmq-amqp-config-' + karate.env + '.yml')
Given def JMSClient = Java.type('dev.inditex.karate.jms.JMSClient')
Given def jmsClient = new JMSClient(config)

# public Boolean available()
When def available = jmsClient.available()
Then if (!available) karate.fail('JMS Client not available')

Scenario: JMS Client Available Operations - Rabbit MQ (AMQP) - JSON

# Define Queue
Given def queue = getQueue ('RabbitMQ-AMQP', 'json')

# Consume Any Previous Messages
# public List<Map<String, Object>> consume(final String queue, final long timeout)
When def messages = jmsClient.consume(queue, 10000)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then match messages == '#[_ >= 0]'

# public void send(final String queue, final Object value, final Map<String, Object> properties)
# Send Message with properties
Given def jmsMessageJSON = {'id': '1', 'name': 'karate-01', 'value': 1}
Given def jmsProperties = {'PRINT_STATUS':'PRINTING'}
When jmsClient.send(queue, jmsMessageJSON, jmsProperties)

# public void send(final String queue, final Object value, final Map<String, Object> properties)
# Send Message with properties
Given def jmsMessageJSON2 = {'id': '2', 'name': 'karate-02', 'value': 2}
Given def jmsProperties = {'PRINT_STATUS':'PRINTING'}
When jmsClient.send(queue, jmsMessageJSON2, jmsProperties)

# public List<Map<String, Object>> consume(final String queue, final long timeout)
When def messages = jmsClient.consume(queue, 10000)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then assert messages.length == 2
Then def result = karate.sort(messages, x => x.id)
Then match result[0].id == '1'
Then match result[0].name == 'karate-01'
Then match result[0].value == 1
Then match result[1].id == '2'
Then match result[1].name == 'karate-02'
Then match result[1].value == 2

Scenario: JMS Client Available Operations - Rabbit MQ (AMQP) - Plain Text

# Define Queue
Given def queue = getQueue ('RabbitMQ-AMQP', 'text')

# Consume Any Previous Messages
# public List<Map<String, Object>> consume(final String queue, final long timeout)
When def messages = jmsClient.consume(queue, 10000)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then match messages == '#[_ >= 0]'

# public void send(final String queue, final Object value, final Map<String, Object> properties)
# Send Message with properties
Given def jmsMessagePlainText = 'Plain Text Message'
Given def jmsProperties = {'PRINT_STATUS':'PRINTING'}
When jmsClient.send(queue, jmsMessagePlainText, jmsProperties)

# public List<Map<String, Object>> consume(final String queue, final long timeout)
When def messages = jmsClient.consume(queue, 10000)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then assert messages.length == 1
Then match messages[0].textMessage == jmsMessagePlainText

Scenario: JMS Client Available Operations - Rabbit MQ (AMQP) - XML

# Define Queue
Given def queue = getQueue ('RabbitMQ-AMQP', 'xml')

# Consume Any Previous Messages
# public List<Map<String, Object>> consume(final String queue, final long timeout)
When def messages = jmsClient.consume(queue, 10000)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then match messages == '#[_ >= 0]'

# public void send(final String queue, final Object value, final Map<String, Object> properties)
# Send Message without properties
Given text jmsMessageXML =
"""
<?xml version="1.0"?>
<karate id="1">
  <name>karate-01</name>
  <value>1</value>
</karate>
"""
When jmsClient.send(queue, jmsMessageXML, null)

# Wait for message to be available
Then sleep(5000)

# consume no timeout
# public List<Map<String, Object>> consume(final String queue)
When def messages = jmsClient.consume(queue)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then assert messages.length == 1
Then match messages[0].textMessage == jmsMessageXML
