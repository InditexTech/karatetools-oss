package dev.inditex.karate.kafka;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import dev.inditex.karate.logging.KarateClientLogger;
import dev.inditex.karate.parser.SystemPropertiesParser;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.common.KafkaException;

/**
 * The Class KafkaAbstractClient.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class KafkaAbstractClient {

  /** The Constant DEFAULT_TIMEOUT. */
  public static final long DEFAULT_TIMEOUT = 5000;

  /** The configuration. */
  protected Properties configuration = new Properties();

  /** The is kafka available. */
  protected Boolean isKafkaAvailable;

  /** The log. */
  protected final KarateClientLogger log = new KarateClientLogger();

  /**
   * Instantiates a new kafka abstract client.
   *
   * @param configMap the config map
   */
  protected KafkaAbstractClient(final Map<Object, Object> configMap) {
    // Set default Timeouts
    configuration.setProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(DEFAULT_TIMEOUT));
    configuration.setProperty(CommonClientConfigs.DEFAULT_API_TIMEOUT_MS_CONFIG, String.valueOf(DEFAULT_TIMEOUT));
    configuration.setProperty(SchemaRegistryClientConfig.HTTP_CONNECT_TIMEOUT_MS, String.valueOf(DEFAULT_TIMEOUT));
    configuration.setProperty(SchemaRegistryClientConfig.HTTP_READ_TIMEOUT_MS, String.valueOf(DEFAULT_TIMEOUT));

    final Map<Object, Object> config = SystemPropertiesParser.parseConfiguration(configMap);
    config.forEach((k, v) -> {
      // Set applicable configuration properties
      if (getApplicableKey(k) != null) {
        configuration.setProperty(getApplicableKey(k), String.valueOf(v));
      }
    });
  }

  /**
   * Gets the applicable key.
   *
   * @param key the key
   * @return the applicable key
   */
  protected String getApplicableKey(final Object key) {
    return key.toString();
  }

  /**
   * Available.
   *
   * @return the boolean
   */
  public Boolean available() {
    log.debug("available() ... ");
    if (isKafkaAvailable == null) {
      try (final Admin admin = Admin.create(configuration)) {
        getClusterNodes(admin);
        isKafkaAvailable = true;
      } catch (final InterruptedException e) {
        log.error("available() InterruptedException", e);
        Thread.currentThread().interrupt();
        throw new KafkaException("Thread interrupted", e);
      } catch (final KafkaException e) {
        log.error("available() KafkaException", e);
        throw e;
      } catch (final Exception e) {
        log.error("available() Exception", e);
        throw new KafkaException("Unable to access Kafka cluster", e);
      }
    }
    log.info("available()={}", isKafkaAvailable);
    return isKafkaAvailable;
  }

  /**
   * Gets the cluster nodes.
   *
   * @param admin the admin
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  protected void getClusterNodes(final Admin admin) throws InterruptedException, ExecutionException {
    if (admin == null) {
      throw new KafkaException("Unable to access Kafka Admin client");
    }
    final var cluster = admin.describeCluster();
    if (cluster == null) {
      throw new KafkaException("Unable to access Kafka cluster");
    }
    final var nodes = cluster.nodes();
    if (nodes == null) {
      throw new KafkaException("Unable to access Kafka cluster nodes [null]");
    }
    final var nodesList = nodes.get();
    log.debug("available() nodes = {}", nodesList);
    if (nodesList == null) {
      throw new KafkaException("Unable to access Kafka cluster nodes [null list]");
    }
    if (nodesList.isEmpty()) {
      throw new KafkaException("Unable to access Kafka cluster nodes [empty list]");
    }
  }
}
