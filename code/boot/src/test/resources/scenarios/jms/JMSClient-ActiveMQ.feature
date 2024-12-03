@inditex-oss-karate @karate-clients
@jms @activemq @jms-activemq
@activemq-client
@env=local

Feature: JMS Client Available Operations - Active MQ

Background:

* def sleep = function(millis){ java.lang.Thread.sleep(millis) }

# public JMSClient(final Map<Object, Object> configMap)
# Instantiate JMSClient
Given def config = read('classpath:config/jms/activemq-config.yml');
Given def JMSClient = Java.type('dev.inditex.karate.jms.JMSClient')
Given def jmsClient = new JMSClient(config)

# Define Queue
Given def queue = 'karate.public.queue'

Scenario: JMS Client Available Operations - Active MQ

# public Boolean available()
When def available = jmsClient.available()
Then if (!available) karate.fail('JMS Client not available')

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
Given def JMSKarateObject = Java.type('dev.inditex.karate.jms.JMSKarateObject')
Given def jmsMessageObject = new JMSKarateObject("2", "karate-02", 2)
Given def jmsProperties = {'PRINT_STATUS':'PRINTING'}
When jmsClient.send(queue, jmsMessageObject, jmsProperties)

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

# public List<Map<String, Object>> consume(final String queue, final long timeout)
When def messages = jmsClient.consume(queue, 10000)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then assert messages.length == 1
Then match messages[0].textMessage == jmsMessageXML

# public void send(final String queue, final Object value, final Map<String, Object> properties)
# Send Message with properties
Given def jmsMessagePlainText = 'Plain Text Message'
Given def jmsProperties = {'PRINT_STATUS':'PRINTING'}
When jmsClient.send(queue, jmsMessagePlainText, jmsProperties)

# Wait for message to be available
Then sleep(5000)

# public List<Map<String, Object>> consume(final String queue)
When def messages = jmsClient.consume(queue)
Then karate.log('messages=#', messages.length)
Then karate.log('messages=', messages)
Then assert messages.length == 1
Then match messages[0].textMessage == jmsMessagePlainText
