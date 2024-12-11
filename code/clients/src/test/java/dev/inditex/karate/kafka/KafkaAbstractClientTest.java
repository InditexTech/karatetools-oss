package dev.inditex.karate.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import dev.inditex.karate.AbstractClientTest;

import ch.qos.logback.classic.Level;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.SaslConfigs;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class KafkaAbstractClientTest extends AbstractClientTest {

  public static final long DEFAULT_TIMEOUT = 5000;

  @Nested
  class Constructor {
    @Test
    void when_valid_config_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfig();

      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));

      // Common
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.HTTP_CONNECT_TIMEOUT_MS))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.HTTP_READ_TIMEOUT_MS))
          .isEqualTo(String.valueOf(DEFAULT_TIMEOUT));
      // Broker
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      // Registry
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
    }

    @Test
    void when_broker_security_enabled_config_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfigWithBrokerSecurity();

      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));

      // Broker
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      // Broker Security
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
      assertThat(client.getConfiguration().getProperty(SaslConfigs.SASL_MECHANISM))
          .isEqualTo(config.get(SaslConfigs.SASL_MECHANISM));
      assertThat(client.getConfiguration().getProperty(SaslConfigs.SASL_JAAS_CONFIG))
          .isEqualTo(config.get(SaslConfigs.SASL_JAAS_CONFIG));

    }

    @Test
    void when_registry_security_enabled_config_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfigWithRegistrySecurity();

      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));

      // Registry
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
      // Registry Security
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE))
          .isEqualTo(config.get(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE));
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.USER_INFO_CONFIG))
          .isEqualTo(config.get(SchemaRegistryClientConfig.USER_INFO_CONFIG));
    }

    @Test
    void when_broker_and_registry_security_enabled_config_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfigWithSecurity();

      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));

      // Broker
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
      // Broker Security
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG))
          .isEqualTo(config.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
      assertThat(client.getConfiguration().getProperty(SaslConfigs.SASL_MECHANISM))
          .isEqualTo(config.get(SaslConfigs.SASL_MECHANISM));
      assertThat(client.getConfiguration().getProperty(SaslConfigs.SASL_JAAS_CONFIG))
          .isEqualTo(config.get(SaslConfigs.SASL_JAAS_CONFIG));
      // Registry
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo(config.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
      // Registry Security
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE))
          .isEqualTo(config.get(SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE));
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.USER_INFO_CONFIG))
          .isEqualTo(config.get(SchemaRegistryClientConfig.USER_INFO_CONFIG));
    }

    @Test
    void when_config_defaults_overridden_expect_fields_informed() {
      final Map<Object, Object> config = getDefaultConfigWithDefaultsOverriden();

      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));

      // Common
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(1000));
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG))
          .isEqualTo(String.valueOf(2000));
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.HTTP_CONNECT_TIMEOUT_MS))
          .isEqualTo(String.valueOf(3000));
      assertThat(client.getConfiguration().getProperty(SchemaRegistryClientConfig.HTTP_READ_TIMEOUT_MS))
          .isEqualTo(String.valueOf(4000));
      // Broker
      assertThat(client.getConfiguration().getProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG))
          .isEqualTo("host:9999");
      // Registry
      assertThat(client.getConfiguration().getProperty(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG))
          .isEqualTo("http://schemaregistry.host:8888");
    }
  }

  @Nested
  class Available {

    @Test
    void when_first_call_with_null_admin_expect_exception() {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(null);

        final ThrowingCallable result = client::available;

        assertThatThrownBy(result).isInstanceOf(KafkaException.class).hasMessage("Unable to access Kafka Admin client");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaAbstractClient => available() KafkaException")
            && log.getThrowableProxy().getMessage().contains("Unable to access Kafka Admin client"));
      }
    }

    @Test
    void when_first_call_with_null_cluster_expect_exception() {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(null);

        final ThrowingCallable result = client::available;

        assertThatThrownBy(result).isInstanceOf(KafkaException.class).hasMessage("Unable to access Kafka cluster");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaAbstractClient => available() KafkaException")
            && log.getThrowableProxy().getMessage().contains("Unable to access Kafka cluster"));
      }
    }

    @Test
    void when_first_call_with_null_nodes_future_expect_exception() {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        final DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(null);

        final ThrowingCallable result = client::available;

        assertThatThrownBy(result).isInstanceOf(KafkaException.class).hasMessage("Unable to access Kafka cluster nodes [null]");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaAbstractClient => available() KafkaException")
            && log.getThrowableProxy().getMessage().contains("Unable to access Kafka cluster nodes [null]"));
      }
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_first_call_with_null_nodes_list_expect_exception() throws InterruptedException, ExecutionException {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        final DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        final KafkaFuture<Collection<Node>> nodes = mock(KafkaFuture.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodes);
        when(nodes.get()).thenReturn(null);

        final ThrowingCallable result = client::available;

        assertThatThrownBy(result).isInstanceOf(KafkaException.class).hasMessage("Unable to access Kafka cluster nodes [null list]");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaAbstractClient => available() KafkaException")
            && log.getThrowableProxy().getMessage().contains("Unable to access Kafka cluster nodes [null list]"));
      }
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_first_call_with_empty_nodes_expect_exception() throws InterruptedException, ExecutionException {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        final DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        final KafkaFuture<Collection<Node>> nodes = mock(KafkaFuture.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodes);
        when(nodes.get()).thenReturn(List.of());

        final ThrowingCallable result = client::available;

        assertThatThrownBy(result).isInstanceOf(KafkaException.class).hasMessage("Unable to access Kafka cluster nodes [empty list]");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaAbstractClient => available() KafkaException")
            && log.getThrowableProxy().getMessage().contains("Unable to access Kafka cluster nodes [empty list]"));

      }
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_first_call_with_interrupted_exception_expect_exception() throws InterruptedException, ExecutionException {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        final DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        final KafkaFuture<Collection<Node>> nodes = mock(KafkaFuture.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodes);
        when(nodes.get()).thenThrow(new InterruptedException("Interrupted exception"));

        final ThrowingCallable result = client::available;

        assertThatThrownBy(result).isInstanceOf(KafkaException.class).hasMessage("Thread interrupted");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaAbstractClient => available() InterruptedException")
            && log.getThrowableProxy().getMessage().contains("Interrupted exception"));
      }
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_first_call_with_other_exception_expect_exception() throws InterruptedException, ExecutionException {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        final DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        final KafkaFuture<Collection<Node>> nodes = mock(KafkaFuture.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodes);
        when(nodes.get()).thenThrow(new RuntimeException("Unknown exception"));

        final ThrowingCallable result = client::available;

        assertThatThrownBy(result).isInstanceOf(KafkaException.class).hasMessage("Unable to access Kafka cluster");
        assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
            && log.getFormattedMessage().contains("KafkaAbstractClient => available() Exception")
            && log.getThrowableProxy().getMessage().contains("Unknown exception"));
      }
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_first_call_with_nodes_available_expect_connection_test() throws InterruptedException, ExecutionException {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        final DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        final KafkaFuture<Collection<Node>> nodes = mock(KafkaFuture.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodes);
        when(nodes.get()).thenReturn(List.of(new Node(1001, "test-host", 9999, "test-rack")));

        final var result = client.available();

        assertThat(result).isTrue();
        verify(admin, times(1)).describeCluster();
        verify(describeClusterResult, times(1)).nodes();
        verify(nodes, times(1)).get();
      }
    }

    @SuppressWarnings("unchecked")
    @Test
    void when_already_available_expect_no_connection_test() throws InterruptedException, ExecutionException {
      final Map<Object, Object> config = getDefaultConfig();
      final KafkaAbstractClient client = mock(KafkaAbstractClient.class,
          withSettings().useConstructor(config).defaultAnswer(Mockito.CALLS_REAL_METHODS));
      try (final MockedStatic<Admin> staticAdmin = mockStatic(Admin.class)) {
        final Admin admin = mock(Admin.class);
        final DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        final KafkaFuture<Collection<Node>> nodes = mock(KafkaFuture.class);
        staticAdmin.when(() -> Admin.create(any(Properties.class))).thenReturn(admin);
        when(admin.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.nodes()).thenReturn(nodes);
        when(nodes.get()).thenReturn(List.of(new Node(1001, "test-host", 9999, "test-rack")));

        final var resultFirst = client.available();
        final var result = client.available();

        assertThat(resultFirst).isTrue();
        assertThat(result).isTrue();
        verify(admin, times(1)).describeCluster();
      }
    }
  }

  protected Map<Object, Object> getDefaultConfig() {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "producer.client.id", "KARATETOOLS");
  }

  protected Map<Object, Object> getDefaultConfigWithBrokerSecurity() {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "security.protocol", "SASL_PLAINTEXT",
        "sasl.mechanism", "PLAIN",
        "sasl.jaas.config",
        "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"test\" password=\"test\";");
  }

  protected Map<Object, Object> getDefaultConfigWithRegistrySecurity() {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "basic.auth.credentials.source", "USER_INFO",
        "basic.auth.user.info", "user:password");
  }

  protected Map<Object, Object> getDefaultConfigWithSecurity() {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "security.protocol", "SASL_PLAINTEXT",
        "sasl.mechanism", "PLAIN",
        "sasl.jaas.config",
        "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"test\" password=\"test\";",
        "basic.auth.credentials.source", "USER_INFO",
        "basic.auth.user.info", "user:password");
  }

  protected Map<Object, Object> getDefaultConfigWithDefaultsOverriden() {
    return Map.of("bootstrap.servers", "host:9999",
        "schema.registry.url", "http://schemaregistry.host:8888",
        "request.timeout.ms", 1000,
        "default.api.timeout.ms", 2000,
        "http.connect.timeout.ms", 3000,
        "http.read.timeout.ms", 4000);
  }
}
