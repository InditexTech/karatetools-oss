@inditex-oss-karate @karate-clients
@kafka
@kafka-client @kafka-consumer-client @kafka-producer-client
@env=local
Feature: Kafka Clients Available Operations

Background:
# Define Common Headers, Events and Consumer Groups
Given def topicA = "e2e.local.karate.test-avro.public." + type + ".A"
Given def topicB = "e2e.local.karate.test-avro.public." + type + ".B"
Given def topicC = "e2e.local.karate.test-avro.public." + type + ".C"
Given def topicD = "e2e.local.karate.test-avro.public." + type + ".D"
Given def topics = [ "#(topicC)", "#(topicD)" ]
Given def allTopics = [ "#(topicA)", "#(topicB)", "#(topicC)", "#(topicD)" ]

Given def headersA = { "contentType": [ "application/*+avro" ], "status": [ "A" ] }
Given def headersB = { "contentType": [ "application/*+avro" ], "status": [ "B" ] }
Given def headersC = { "contentType": [ "application/*+avro" ], "status": [ "C" ] }
Given def headersD = { "contentType": [ "application/*+avro" ], "status": [ "D" ] }

Given def KarateEvent = Java.type('dev.inditex.karate.kafka.KarateEvent')

Given def eventA1 = new KarateEvent("A1", "karate-A1", 11)
Given def eventA2 = new KarateEvent("A2", "karate-A2", 12)
Given def eventA3 = new KarateEvent("A3", "karate-A3", 13)
Given def eventA4 = new KarateEvent("A4", "karate-A4", 14)
Given def eventB1 = new KarateEvent("B1", "karate-B1", 21)
Given def eventB2 = new KarateEvent("B2", "karate-B2", 22)
Given def eventB3 = new KarateEvent("B3", "karate-B3", 23)
Given def eventB4 = new KarateEvent("B4", "karate-B4", 24)
Given def eventC1 = new KarateEvent("C1", "karate-C1", 31)
Given def eventC2 = new KarateEvent("C2", "karate-C2", 32)
Given def eventC3 = new KarateEvent("C3", "karate-C3", 33)
Given def eventC4 = new KarateEvent("C4", "karate-C4", 34)
Given def eventD1 = new KarateEvent("D1", "karate-D1", 41)
Given def eventD2 = new KarateEvent("D2", "karate-D2", 42)
Given def eventD3 = new KarateEvent("D3", "karate-D3", 43)
Given def eventD4 = new KarateEvent("D4", "karate-D4", 44)

Given def group1 = "karate-kafka-consumer-group-1"
Given def group2 = "karate-kafka-consumer-group-2"
Given def group3 = "karate-kafka-consumer-group-3"
Given def group4 = "karate-kafka-consumer-group-4"

Scenario Outline: Kafka Clients Available Operations - Single Topic - <type>

Given def config = read('classpath:config/kafka/kafka-config-<config>.yml')

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

# Define Topics
Given def topicA = "e2e.local.karate.test-avro.public." + type + ".A"
Given def topicB = "e2e.local.karate.test-avro.public." + type + ".B"
Given def topicC = "e2e.local.karate.test-avro.public." + type + ".C"
Given def topicD = "e2e.local.karate.test-avro.public." + type + ".D"
Given def topics = [ "#(topicC)", "#(topicD)" ]
Given def allTopics = [ "#(topicA)", "#(topicB)", "#(topicC)", "#(topicD)" ]

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

# public void send(final String topic, final Object event)
When kafkaProducerClient.send(topicA, eventA1)
# public void send(final String topic, final Object event, final Map<String, List<String>> headers)
When kafkaProducerClient.send(topicA, eventA2, headersA)

# public List<Map<String, Object>> consume(final String topic)
When def messagesTopicA = kafkaConsumerClient.consume(topicA)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[2]'
Then match messagesTopicA[0] == karate.toJson(eventA1)
Then match messagesTopicA[1] == karate.toJson(eventA2)

# public void send(final String topic, final Object event)
When kafkaProducerClient.send(topicA, eventA3)
# public void send(final String topic, final Object event, final Map<String, List<String>> headers)
When kafkaProducerClient.send(topicA, eventA4, headersA)

# public List<Map<String, Object>> consume(final String topic, final long timeout)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, 10000)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[2]'
Then match messagesTopicA[0] == karate.toJson(eventA3)
Then match messagesTopicA[1] == karate.toJson(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, group1)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.toJson(eventA1)
Then match messagesTopicA[1] == karate.toJson(eventA2)
Then match messagesTopicA[2] == karate.toJson(eventA3)
Then match messagesTopicA[3] == karate.toJson(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group, final String offset)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, group2, 'earliest')
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.toJson(eventA1)
Then match messagesTopicA[1] == karate.toJson(eventA2)
Then match messagesTopicA[2] == karate.toJson(eventA3)
Then match messagesTopicA[3] == karate.toJson(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group, final long timeout)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, group3, 10000)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.toJson(eventA1)
Then match messagesTopicA[1] == karate.toJson(eventA2)
Then match messagesTopicA[2] == karate.toJson(eventA3)
Then match messagesTopicA[3] == karate.toJson(eventA4)

# public List<Map<String, Object>> consume(final String topic, final String group, final String offset, final long timeout)
When def messagesTopicA = kafkaConsumerClient.consume(topicA, group4, 'latest', 10000)
Then karate.log('messagesTopicA=', messagesTopicA)
Then match messagesTopicA == '#[4]'
Then match messagesTopicA[0] == karate.toJson(eventA1)
Then match messagesTopicA[1] == karate.toJson(eventA2)
Then match messagesTopicA[2] == karate.toJson(eventA3)
Then match messagesTopicA[3] == karate.toJson(eventA4)

Examples:
|type                       |config                                |
|kafka-noauth.reg-noauth    |no-auth-registry-basic-auth           |
|kafka-noauth.reg-basic     |no-auth-registry-no-auth              |
|kafka-sasl-plain.reg-noauth|sasl-plain-registry-basic-auth        |
|kafka-sasl-plain.reg-basic |sasl-plain-registry-no-auth           |
|kafka-sasl-scram.reg-noauth|sasl-scram-sha-512-registry-basic-auth|
|kafka-sasl-scram.reg-basic |sasl-scram-sha-512-registry-no-auth   |

Scenario Outline: Kafka Clients Available Operations - Multiple Topics - <type>

Given def config = read('classpath:config/kafka/kafka-config-<config>.yml')

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

# Define Topics
Given def topicA = "e2e.local.karate.test-avro.public." + type + ".A"
Given def topicB = "e2e.local.karate.test-avro.public." + type + ".B"
Given def topicC = "e2e.local.karate.test-avro.public." + type + ".C"
Given def topicD = "e2e.local.karate.test-avro.public." + type + ".D"
Given def topics = [ "#(topicC)", "#(topicD)" ]
Given def allTopics = [ "#(topicA)", "#(topicB)", "#(topicC)", "#(topicD)" ]

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
Then match messagesTopicC[0] == karate.toJson(eventC1)
Then match messagesTopicC[1] == karate.toJson(eventC2)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[2]'
Then match messagesTopicD[0] == karate.toJson(eventD1)
Then match messagesTopicD[1] == karate.toJson(eventD2)

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
Then match messagesTopicC[0] == karate.toJson(eventC3)
Then match messagesTopicC[1] == karate.toJson(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[2]'
Then match messagesTopicD[0] == karate.toJson(eventD3)
Then match messagesTopicD[1] == karate.toJson(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group)
When def messagesTopics = kafkaConsumerClient.consume(topics, group1)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.toJson(eventC1)
Then match messagesTopicC[1] == karate.toJson(eventC2)
Then match messagesTopicC[2] == karate.toJson(eventC3)
Then match messagesTopicC[3] == karate.toJson(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.toJson(eventD1)
Then match messagesTopicD[1] == karate.toJson(eventD2)
Then match messagesTopicD[2] == karate.toJson(eventD3)
Then match messagesTopicD[3] == karate.toJson(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final String offset)
When def messagesTopics = kafkaConsumerClient.consume(topics, group2, 'earliest')
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.toJson(eventC1)
Then match messagesTopicC[1] == karate.toJson(eventC2)
Then match messagesTopicC[2] == karate.toJson(eventC3)
Then match messagesTopicC[3] == karate.toJson(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.toJson(eventD1)
Then match messagesTopicD[1] == karate.toJson(eventD2)
Then match messagesTopicD[2] == karate.toJson(eventD3)
Then match messagesTopicD[3] == karate.toJson(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final long timeout)
When def messagesTopics = kafkaConsumerClient.consume(topics, group3, 10000)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.toJson(eventC1)
Then match messagesTopicC[1] == karate.toJson(eventC2)
Then match messagesTopicC[2] == karate.toJson(eventC3)
Then match messagesTopicC[3] == karate.toJson(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.toJson(eventD1)
Then match messagesTopicD[1] == karate.toJson(eventD2)
Then match messagesTopicD[2] == karate.toJson(eventD3)
Then match messagesTopicD[3] == karate.toJson(eventD4)

# public Map<String, List<Map<String, Object>>> consume(final String[] topics, final String group, final String offset, final long timeout)
When def messagesTopics = kafkaConsumerClient.consume(topics, group4, 'latest', 10000)
Then karate.log('messagesTopics=', messagesTopics)
Then def messagesTopicC = karate.jsonPath(messagesTopics, "$['" + topicC + "']")
Then karate.log('messagesTopicC=', messagesTopicC)
Then match messagesTopicC == '#[4]'
Then match messagesTopicC[0] == karate.toJson(eventC1)
Then match messagesTopicC[1] == karate.toJson(eventC2)
Then match messagesTopicC[2] == karate.toJson(eventC3)
Then match messagesTopicC[3] == karate.toJson(eventC4)
Then def messagesTopicD = karate.jsonPath(messagesTopics, "$['" + topicD + "']")
Then karate.log('messagesTopicD=', messagesTopicD)
Then match messagesTopicD == '#[4]'
Then match messagesTopicD[0] == karate.toJson(eventD1)
Then match messagesTopicD[1] == karate.toJson(eventD2)
Then match messagesTopicD[2] == karate.toJson(eventD3)
Then match messagesTopicD[3] == karate.toJson(eventD4)

Examples:
|type                       |config                                |
|kafka-noauth.reg-noauth    |no-auth-registry-basic-auth           |
|kafka-noauth.reg-basic     |no-auth-registry-no-auth              |
|kafka-sasl-plain.reg-noauth|sasl-plain-registry-basic-auth        |
|kafka-sasl-plain.reg-basic |sasl-plain-registry-no-auth           |
|kafka-sasl-scram.reg-noauth|sasl-scram-sha-512-registry-basic-auth|
|kafka-sasl-scram-.reg-basic|sasl-scram-sha-512-registry-no-auth   |
