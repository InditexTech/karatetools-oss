@inditex-oss-karate @karate-clients
@kafka
@kafka-client @kafka-consumer-client @kafka-producer-client
@env=local
Feature: Kafka Clients Available Operations - String Serialization

Background:
Given def config = read('classpath:config/kafka/kafka-config-no-auth-registry-no-auth-string-serialization.yml')

# public KafkaConsumerClient(final Map<Object, Object> config)
# Instantiate KafkaConsumerClient
Given def KafkaConsumerClient = Java.type('dev.inditex.karate.kafka.KafkaConsumerClient')
Given def kafkaConsumerClient = new KafkaConsumerClient(config)

# public KafkaProducerClient(final Map<Object, Object> config)
# Instantiate KafkaProducerClient
Given def KafkaProducerClient = Java.type('dev.inditex.karate.kafka.KafkaProducerClient')
Given def kafkaProducerClient = new KafkaProducerClient(config)

# public Boolean available()
When def consumerAvailable = kafkaConsumerClient.available()
When def producerAvailable = kafkaProducerClient.available()
Then if (!consumerAvailable || !producerAvailable) karate.fail('Kafka Client not available')

# Define Consumer Topics, Headers, Events and Consumer Groups
Given def topicA = "e2e.local.karate.test-string.public.A.v1"
Given def topicB = "e2e.local.karate.test-string.public.B.v1"
Given def topicC = "e2e.local.karate.test-string.public.C.v1"
Given def topicD = "e2e.local.karate.test-string.public.D.v1"
Given def topics = [ "#(topicC)", "#(topicD)" ]
Given def allTopics = [ "#(topicA)", "#(topicB)", "#(topicC)", "#(topicD)" ]

Given def headersA = { "contentType": [ "text/plain" ], "status": [ "A" ] }
Given def headersB = { "contentType": [ "text/plain" ], "status": [ "B" ] }
Given def headersC = { "contentType": [ "text/plain" ], "status": [ "C" ] }
Given def headersD = { "contentType": [ "text/plain" ], "status": [ "D" ] }

Given string eventA1 = { "id": "A1", "name": "karate-A1", "value": 11 }
Given string eventA2 = { "id": "A2", "name": "karate-A2", "value": 12 }
Given string eventA3 = { "id": "A3", "name": "karate-A3", "value": 13 }
Given string eventA4 = { "id": "A4", "name": "karate-A4", "value": 14 }
Given string eventB1 = { "id": "B1", "name": "karate-B1", "value": 21 }
Given string eventB2 = { "id": "B2", "name": "karate-B2", "value": 22 }
Given string eventB3 = { "id": "B3", "name": "karate-B3", "value": 23 }
Given string eventB4 = { "id": "B4", "name": "karate-B4", "value": 24 }
Given string eventC1 = { "id": "C1", "name": "karate-C1", "value": 31 }
Given string eventC2 = { "id": "C2", "name": "karate-C2", "value": 32 }
Given string eventC3 = { "id": "C3", "name": "karate-C3", "value": 33 }
Given string eventC4 = { "id": "C4", "name": "karate-C4", "value": 34 }
Given string eventD1 = { "id": "D1", "name": "karate-D1", "value": 41 }
Given string eventD2 = { "id": "D2", "name": "karate-D2", "value": 42 }
Given string eventD3 = { "id": "D3", "name": "karate-D3", "value": 43 }
Given string eventD4 = { "id": "D4", "name": "karate-D4", "value": 44 }

Given def group1 = "karate-kafka-consumer-group-1"
Given def group2 = "karate-kafka-consumer-group-2"
Given def group3 = "karate-kafka-consumer-group-3"
Given def group4 = "karate-kafka-consumer-group-4"

# consume - initial to clean up
When def initialMessages = kafkaConsumerClient.consume(allTopics)
Then karate.log('initialMessages=', initialMessages)
When def initialMessagesG1 = kafkaConsumerClient.consume(allTopics, group1)
Then karate.log('initialMessagesG1=', initialMessagesG1)
When def initialMessagesG2 = kafkaConsumerClient.consume(allTopics, group2)
Then karate.log('initialMessagesG2=', initialMessagesG2)
When def initialMessagesG3 = kafkaConsumerClient.consume(allTopics, group3)
Then karate.log('initialMessagesG3=', initialMessagesG3)
When def initialMessagesG4 = kafkaConsumerClient.consume(allTopics, group4)
Then karate.log('initialMessagesG4=', initialMessagesG4)

Scenario: Kafka Clients Available Operations - Single Topic - String Serialization
# public void send(final String topic, final Object event)
When kafkaProducerClient.send(topicA, eventA1)
# public void send(final String topic, final Object event, final Map<String, List<String>> headers)
When kafkaProducerClient.send(topicA, eventA2, headersA)

# public List<Map<String, Object>> consume(final String topic)
When def messagesTopicA = kafkaConsumerClient.consume(topicA)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[2]'
Then match messagesTopicA[0] == karate.fromString(eventA1)
Then match messagesTopicA[1] == karate.fromString(eventA2)

# public void send(final String topic, final Object event)
When kafkaProducerClient.send(topicA, eventA3)
# public void send(final String topic, final Object event, final Map<String, List<String>> headers)
When kafkaProducerClient.send(topicA, eventA4, headersA)

# public List<Map<String, Object>> consume(final String topic, final long timeout)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, 10000)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[2]'
Then match messagesTopicA[0] == karate.fromString(eventA3)
Then match messagesTopicA[1] == karate.fromString(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, group1)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.fromString(eventA1)
Then match messagesTopicA[1] == karate.fromString(eventA2)
Then match messagesTopicA[2] == karate.fromString(eventA3)
Then match messagesTopicA[3] == karate.fromString(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group, final String offset)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, group2, 'earliest')
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.fromString(eventA1)
Then match messagesTopicA[1] == karate.fromString(eventA2)
Then match messagesTopicA[2] == karate.fromString(eventA3)
Then match messagesTopicA[3] == karate.fromString(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group, final long timeout)
When def messagesTopicB = kafkaConsumerClient.consume(topicA, group3, 10000)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.fromString(eventA1)
Then match messagesTopicA[1] == karate.fromString(eventA2)
Then match messagesTopicA[2] == karate.fromString(eventA3)
Then match messagesTopicA[3] == karate.fromString(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group, final String offset, final long timeout)
When def messagesTopicB = kafkaConsumerClient.consume(topicA, group4, 'latest', 10000)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.fromString(eventA1)
Then match messagesTopicA[1] == karate.fromString(eventA2)
Then match messagesTopicA[2] == karate.fromString(eventA3)
Then match messagesTopicA[3] == karate.fromString(eventA4)

Scenario: Kafka Clients Available Operations - Multiple Topics - String Serialization
# public void send(final String topic, final Object event)
When kafkaProducerClient.send(topicC, eventC1)
When kafkaProducerClient.send(topicD, eventD1)
# public void send(final String topic, final Object event, final Map<String, List<String>> headers)
When kafkaProducerClient.send(topicC, eventC2, headersC)
When kafkaProducerClient.send(topicD, eventD2, headersD)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics)
When def messagesTopics = kafkaConsumerClient.consume(topics)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[2]'
Then match messagesTopicC[0] == karate.fromString(eventC1)
Then match messagesTopicC[1] == karate.fromString(eventC2)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[2]'
Then match messagesTopicD[0] == karate.fromString(eventD1)
Then match messagesTopicD[1] == karate.fromString(eventD2)

# public void send(final String topic, final Object event)
When kafkaProducerClient.send(topicC, eventC3)
When kafkaProducerClient.send(topicD, eventD3)
# public void send(final String topic, final Object event, final Map<String, List<String>> headers)
When kafkaProducerClient.send(topicC, eventC4, headersC)
When kafkaProducerClient.send(topicD, eventD4, headersD)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final long timeout)
When def messagesTopics = kafkaConsumerClient.consume(topics, 10000)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[2]'
Then match messagesTopicC[0] == karate.fromString(eventC3)
Then match messagesTopicC[1] == karate.fromString(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[2]'
Then match messagesTopicD[0] == karate.fromString(eventD3)
Then match messagesTopicD[1] == karate.fromString(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group)
When def messagesTopics = kafkaConsumerClient.consume(topics, group1)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.fromString(eventC1)
Then match messagesTopicC[1] == karate.fromString(eventC2)
Then match messagesTopicC[2] == karate.fromString(eventC3)
Then match messagesTopicC[3] == karate.fromString(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.fromString(eventD1)
Then match messagesTopicD[1] == karate.fromString(eventD2)
Then match messagesTopicD[2] == karate.fromString(eventD3)
Then match messagesTopicD[3] == karate.fromString(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final String offset)
When def messagesTopics = kafkaConsumerClient.consume(topics, group2, 'earliest')
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.fromString(eventC1)
Then match messagesTopicC[1] == karate.fromString(eventC2)
Then match messagesTopicC[2] == karate.fromString(eventC3)
Then match messagesTopicC[3] == karate.fromString(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.fromString(eventD1)
Then match messagesTopicD[1] == karate.fromString(eventD2)
Then match messagesTopicD[2] == karate.fromString(eventD3)
Then match messagesTopicD[3] == karate.fromString(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final long timeout)
When def messagesTopics = kafkaConsumerClient.consume(topics, group3, 10000)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.fromString(eventC1)
Then match messagesTopicC[1] == karate.fromString(eventC2)
Then match messagesTopicC[2] == karate.fromString(eventC3)
Then match messagesTopicC[3] == karate.fromString(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.fromString(eventD1)
Then match messagesTopicD[1] == karate.fromString(eventD2)
Then match messagesTopicD[2] == karate.fromString(eventD3)
Then match messagesTopicD[3] == karate.fromString(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final String offset, final long timeout)
When def messagesTopics = kafkaConsumerClient.consume(topics, group4, 'latest', 10000)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.fromString(eventC1)
Then match messagesTopicC[1] == karate.fromString(eventC2)
Then match messagesTopicC[2] == karate.fromString(eventC3)
Then match messagesTopicC[3] == karate.fromString(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.fromString(eventD1)
Then match messagesTopicD[1] == karate.fromString(eventD2)
Then match messagesTopicD[2] == karate.fromString(eventD3)
Then match messagesTopicD[3] == karate.fromString(eventD4)
