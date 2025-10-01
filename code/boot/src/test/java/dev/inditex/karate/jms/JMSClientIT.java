package dev.inditex.karate.jms;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.KarateTestUtils;
import dev.inditex.karate.docker.DockerComposeTestConfiguration;

import javax.jms.JMSException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("IT")
@ActiveProfiles({"test-docker"})
@SpringBootTest(classes = {
    DockerComposeTestConfiguration.class
})
public class JMSClientIT {

  final String message01 = """
      {
          "id": "1",
          "name": "karate-01",
          "value": 1
      }
      """;

  final String properties01 = """
      {
          "status": "01"
      }
      """;

  final String message02 = """
      {
          "id": "2",
          "name": "karate-02",
          "value": 2
      }
      """;

  final String properties02 = """
      {
          "status": "02"
      }
      """;

  final JMSKarateObject object01 = new JMSKarateObject("1", "karate-01", 1);

  final JMSKarateObject object02 = new JMSKarateObject("2", "karate-02", 2);

  final List<Map<String, Object>> expected = List.of(
      Map.of("id", "1", "name", "karate-01", "value", 1),
      Map.of("id", "2", "name", "karate-02", "value", 2));

  final String plainText01 = "karate-01";

  final String plainText02 = "karate-02";

  final List<Map<String, Object>> expectedPlainText = List.of(
      Map.of("textMessage", this.plainText01),
      Map.of("textMessage", this.plainText02));

  final String xml01 = """
      <?xml version="1.0"?>
      <karate id="1">
        <name>karate-01</name>
        <value>1</value>
      </karate>
      """;

  final String xml02 = """
      <?xml version="1.0"?>
      <karate id="2">
        <name>karate-02</name>
        <value>2</value>
      </karate>
      """;

  final List<Map<String, Object>> expectedXML = List.of(
      Map.of("textMessage", this.xml01),
      Map.of("textMessage", this.xml02));

  final List<Map<String, Object>> expectedMixed = List.of(
      Map.of("id", "1", "name", "karate-01", "value", 1),
      Map.of("id", "2", "name", "karate-02", "value", 2),
      Map.of("textMessage", this.plainText01),
      Map.of("textMessage", this.xml02));

  final String queue = "GLOBAL.CORE.KARATE.PUBLIC.QUEUE";

  @Nested
  class JMS {
    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "ActiveMQ,classpath:config/jms/activemq-config.yml",
        "RabbitMQ,classpath:config/jms/rabbitmq-config.yml"
    })
    void when_jms_map_expect_results(final String factory, final String configFile) throws IOException, JMSException {
      JMSClientIT.this.testJmsMap(configFile);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "ActiveMQ,classpath:config/jms/activemq-config.yml",
        "RabbitMQ,classpath:config/jms/rabbitmq-config.yml"
    })
    void when_jms_object_expect_results(final String factory, final String configFile) throws IOException, JMSException {
      JMSClientIT.this.testJmsObject(configFile);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "ActiveMQ,classpath:config/jms/activemq-config.yml",
        "RabbitMQ,classpath:config/jms/rabbitmq-config.yml"
    })
    void when_jms_plain_text_expect_results(final String factory, final String configFile) throws IOException, JMSException {
      JMSClientIT.this.testJmsPlainText(configFile);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "ActiveMQ,classpath:config/jms/activemq-config.yml",
        "RabbitMQ,classpath:config/jms/rabbitmq-config.yml"
    })
    void when_jms_xml_expect_results(final String factory, final String configFile) throws IOException, JMSException {
      JMSClientIT.this.testJmsXml(configFile);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "ActiveMQ,classpath:config/jms/activemq-config.yml",
        "RabbitMQ,classpath:config/jms/rabbitmq-config.yml"
    })
    void when_jms_mixed_expect_results(final String factory, final String configFile) throws IOException, JMSException {
      JMSClientIT.this.testJmsMixed(configFile);
    }

  }

  protected JMSClient instantiateClient(final Map<Object, Object> config) {
    return new JMSClient(config);
  }

  protected void testJmsMap(final String configFile) throws IOException, JMSException {
    final Map<Object, Object> config = KarateTestUtils.readYaml(configFile);
    final JMSClient client = this.instantiateClient(config);

    // available
    final var available = client.available();
    // send
    client.send(this.queue, KarateTestUtils.fromJson(this.message01), KarateTestUtils.fromJson(this.properties01));
    client.send(this.queue, KarateTestUtils.fromJson(this.message02), KarateTestUtils.fromJson(this.properties02));
    // consume
    final var messages = client.consume(this.queue, 10000L);

    assertThat(available).isTrue();
    assertThat(messages).isEqualTo(this.expected);
  }

  protected void testJmsObject(final String configFile) throws IOException, JMSException {
    final Map<Object, Object> config = KarateTestUtils.readYaml(configFile);
    final JMSClient client = this.instantiateClient(config);

    // available
    final var available = client.available();
    // send
    client.send(this.queue, this.object01, KarateTestUtils.fromJson(this.properties01));
    client.send(this.queue, this.object02, KarateTestUtils.fromJson(this.properties02));
    // consume
    final var messages = client.consume(this.queue, 10000L);

    assertThat(available).isTrue();
    assertThat(messages).isEqualTo(this.expected);
  }

  protected void testJmsPlainText(final String configFile) throws IOException, JMSException {
    final Map<Object, Object> config = KarateTestUtils.readYaml(configFile);
    final JMSClient client = this.instantiateClient(config);

    // available
    final var available = client.available();
    // send
    client.send(this.queue, this.plainText01, KarateTestUtils.fromJson(this.properties01));
    client.send(this.queue, this.plainText02, KarateTestUtils.fromJson(this.properties02));
    // consume
    final var messages = client.consume(this.queue, 10000L);

    assertThat(available).isTrue();
    assertThat(messages).isEqualTo(this.expectedPlainText);
  }

  protected void testJmsXml(final String configFile) throws IOException, JMSException {
    final Map<Object, Object> config = KarateTestUtils.readYaml(configFile);
    final JMSClient client = this.instantiateClient(config);

    // available
    final var available = client.available();
    // send
    client.send(this.queue, this.xml01, KarateTestUtils.fromJson(this.properties01));
    client.send(this.queue, this.xml02, KarateTestUtils.fromJson(this.properties02));
    // consume
    final var messages = client.consume(this.queue, 10000L);

    assertThat(available).isTrue();
    assertThat(messages).isEqualTo(this.expectedXML);
  }

  protected void testJmsMixed(final String configFile) throws IOException, JMSException {
    final Map<Object, Object> config = KarateTestUtils.readYaml(configFile);
    final JMSClient client = this.instantiateClient(config);

    // available
    final var available = client.available();
    // send
    client.send(this.queue, KarateTestUtils.fromJson(this.message01), KarateTestUtils.fromJson(this.properties01));
    client.send(this.queue, this.object02, KarateTestUtils.fromJson(this.properties02));
    client.send(this.queue, this.plainText01, KarateTestUtils.fromJson(this.properties01));
    client.send(this.queue, this.xml02, KarateTestUtils.fromJson(this.properties02));
    // consume
    final var messages = client.consume(this.queue, 10000L);

    assertThat(available).isTrue();
    assertThat(messages).isEqualTo(this.expectedMixed);
  }
}
