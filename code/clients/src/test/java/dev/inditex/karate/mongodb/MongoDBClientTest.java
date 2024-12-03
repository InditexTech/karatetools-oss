package dev.inditex.karate.mongodb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import dev.inditex.karate.AbstractClientTest;
import dev.inditex.karate.logging.KarateClientLogger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class MongoDBClientTest extends AbstractClientTest {

  public static final String HOSTS = "hosts";

  public static final String PORT = "port";

  public static final String DB_NAME = "db-name";

  public static final String USER = "user";

  public static final String PASSWORD = "password";

  public static final String CONNECT_TIMEOUT = "connect-timeout";

  public static final String SOCKET_TIMEOUT = "socket-timeout";

  public static final String SERVER_SELECTION_TIMEOUT = "server-selection-timeout";

  public static final int DEFAULT_TIMEOUT = 15000;

  public static final Document PING = new Document("ping", 1);

  public static final Document PING_OK = new Document("ok", 1);

  @Mock
  MongoDatabase mongoDatabase;

  @Mock
  MongoCollection<Document> mongoCollection;

  @Nested
  class Constructor {
    @Test
    void when_valid_config_expect_fields_informed() {
      final Map<Object, Object> config = getValidConfig();

      final MongoDBClient client = instantiateClient(config);

      assertThat(client.getServerAddresses()).isNotNull().hasSize(1);
      assertThat(client.getServerAddresses().get(0).getHost()).isEqualTo(config.get(HOSTS));
      assertThat(client.getServerAddresses().get(0).getPort()).isEqualTo(config.get(PORT));
      assertThat(client.getDbName()).isNotNull().isEqualTo(config.get(DB_NAME));
      assertThat(client.getMongoCredential()).isNotNull();
      assertThat(client.getMongoCredential().getUserName()).isEqualTo(config.get(USER));
      assertThat(String.valueOf(client.getMongoCredential().getPassword())).isEqualTo(config.get(PASSWORD));
      assertThat(client.getMongoClientSettings()).isNotNull();
      assertThat(client.getMongoClientSettings().getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS))
          .isEqualTo(config.get(CONNECT_TIMEOUT));
      assertThat(client.getMongoClientSettings().getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS))
          .isEqualTo(config.get(SOCKET_TIMEOUT));
      assertThat(client.getMongoClientSettings().getClusterSettings().getServerSelectionTimeout(TimeUnit.MILLISECONDS))
          .isEqualTo(config.get(SERVER_SELECTION_TIMEOUT));
      assertThat(client.getDatabase()).isNotNull();
    }

    @Test
    void when_empty_config_expect_default_fields_only() {
      final Map<Object, Object> config = new HashMap<>();

      final MongoDBClient client = instantiateClient(config);

      assertThat(client.getServerAddresses()).isNotNull().hasSize(1);
      assertThat(client.getServerAddresses().get(0).getHost()).isNotNull().isEqualTo("null");
      assertThat(client.getServerAddresses().get(0).getPort()).isZero();
      assertThat(client.getDbName()).isNotNull().isEqualTo("null");
      assertThat(client.getMongoCredential()).isNotNull();
      assertThat(client.getMongoCredential().getUserName()).isEqualTo("null");
      assertThat(String.valueOf(client.getMongoCredential().getPassword())).isEqualTo("null");
      assertThat(client.getMongoClientSettings()).isNotNull();
      assertThat(client.getMongoClientSettings().getSocketSettings()
          .getConnectTimeout(TimeUnit.MILLISECONDS)).isEqualTo(DEFAULT_TIMEOUT);
      assertThat(client.getMongoClientSettings().getSocketSettings()
          .getReadTimeout(TimeUnit.MILLISECONDS)).isEqualTo(DEFAULT_TIMEOUT);
      assertThat(client.getMongoClientSettings().getClusterSettings()
          .getServerSelectionTimeout(TimeUnit.MILLISECONDS)).isEqualTo(DEFAULT_TIMEOUT);
      assertThat(client.getDatabase()).isNotNull();
    }
  }

  @Nested
  class Available {
    @ParameterizedTest
    @MethodSource
    void when_first_call_expect_driver_test(final Document ping, final Boolean available) {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      when(mongoDatabase.runCommand(PING)).thenReturn(ping);

      final var result = client.available();

      assertThat(result).isEqualTo(available);
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
          && log.getFormattedMessage().contains("available()=" + available));
    }

    static Stream<Arguments> when_first_call_expect_driver_test() {
      return Stream.of(
          Arguments.of(new Document(), false),
          Arguments.of(new Document("other", true), false),
          Arguments.of(new Document("ok", true), true),
          Arguments.of(new Document("ok", false), false),
          Arguments.of(new Document("ok", 1), true),
          Arguments.of(new Document("ok", 0), false),
          Arguments.of(new Document("ok", 1.0), true),
          Arguments.of(new Document("ok", 0.0), false));
    }

    @Test
    void when_null_client_expect_not_available() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      client.setClient(null);

      final var result = client.available();

      assertThat(result).isFalse();
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
          && log.getFormattedMessage().contains("available()=false"));
    }

    @Test
    void when_driver_test_exception_expect_not_available() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      when(mongoDatabase.runCommand(PING)).thenThrow(new RuntimeException("PING"));

      final var result = client.available();

      assertThat(result).isFalse();
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
          && log.getFormattedMessage().contains("available()=false"));
    }

    @Test
    void when_already_available_expect_no_driver_test() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      when(mongoDatabase.runCommand(PING)).thenReturn(PING_OK);

      final var resultFirst = client.available();
      final var result = client.available();

      assertThat(resultFirst).isTrue();
      assertThat(result).isTrue();
      verify(mongoDatabase, times(1)).runCommand(PING);
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
          && log.getFormattedMessage().contains("available()=true"));
    }
  }

  @Nested
  class Count {
    @Test
    void when_count_expect_result() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Long value = 1L;
      when(mongoCollection.countDocuments()).thenReturn(value);

      final var result = client.count(collection);

      assertThat(result).isEqualTo(value);
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
          && log.getFormattedMessage().contains("count()      [" + collection + "]=[" + value + "]"));
    }

    @Test
    void when_count_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      when(mongoCollection.countDocuments()).thenThrow(new RuntimeException("COUNT"));

      assertThatThrownBy(() -> {
        client.count(collection);
      }).isInstanceOf(MongoException.class).hasMessageContaining("count() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("count()      [" + collection + "]"));
    }
  }

  @Nested
  class Delete {
    @Test
    void when_delete_expect_result() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> filter = Map.of("_id", "1", "name", "name");
      final Long value = 1L;
      final DeleteResult deleteResult = mock(DeleteResult.class);
      when(mongoCollection.deleteOne(any())).thenReturn(deleteResult);
      when(deleteResult.getDeletedCount()).thenReturn(value);

      final var result = client.delete(collection, filter);

      assertThat(result).isEqualTo(value);
      assertThat(logWatcher.list)
          .anyMatch(log -> log.getLevel().equals(Level.INFO)
              && log.getFormattedMessage().contains("delete()     [" + collection + "]=[" + value + "]"))
          .anyMatch(log -> log.getLevel().equals(Level.DEBUG)
              && log.getFormattedMessage().contains("delete()     [" + collection + "]VALUE"));
    }

    @Test
    void when_delete_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> filter = Map.of("_id", "1", "name", "name");
      when(mongoCollection.deleteOne(any())).thenThrow(new RuntimeException("DELETE"));

      assertThatThrownBy(() -> {
        client.delete(collection, filter);
      }).isInstanceOf(MongoException.class).hasMessageContaining("delete() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("delete()     [" + collection + "]"));
    }
  }

  @Nested
  class DeleteMany {
    @Test
    void when_deleteMany_expect_result() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> filter = Map.of("_id", "1", "name", "name");
      final Long value = 10L;
      final DeleteResult deleteResult = mock(DeleteResult.class);
      when(mongoCollection.deleteMany(any())).thenReturn(deleteResult);
      when(deleteResult.getDeletedCount()).thenReturn(value);

      final var result = client.deleteMany(collection, filter);

      assertThat(result).isEqualTo(value);
      assertThat(logWatcher.list)
          .anyMatch(log -> log.getLevel().equals(Level.INFO)
              && log.getFormattedMessage().contains("deleteMany() [" + collection + "]=[" + value + "]"))
          .anyMatch(log -> log.getLevel().equals(Level.DEBUG)
              && log.getFormattedMessage().contains("deleteMany() [" + collection + "]FILTER"));
    }

    @Test
    void when_deleteMany_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> filter = Map.of("_id", "1", "name", "name");
      when(mongoCollection.deleteMany(any())).thenThrow(new RuntimeException("DELETE"));

      assertThatThrownBy(() -> {
        client.deleteMany(collection, filter);
      }).isInstanceOf(MongoException.class).hasMessageContaining("deleteMany() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("deleteMany() [" + collection + "]"));
    }
  }

  @Nested
  class Find {
    @SuppressWarnings("unchecked")
    @Test
    void when_find_expect_result() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final List<Document> values = List.of(
          new Document("_id", "1").append("name", "name-1"),
          new Document("_id", "2").append("name", "name-2"));
      final List<Map<String, Object>> expected = List.of(
          Map.of("_id", "1", "name", "name-1"),
          Map.of("_id", "2", "name", "name-2"));
      final FindIterable<Document> iterable = mock(FindIterable.class);
      when(mongoCollection.find()).thenReturn(iterable);
      doAnswer(invocation -> {
        final List<Document> entities = invocation.getArgument(0);
        entities.addAll(values);
        return null;
      }).when(iterable).into(anyList());

      final var result = client.find(collection);

      assertThat(result).usingRecursiveFieldByFieldElementComparator().isEqualTo(expected);
      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.INFO)
          && log.getFormattedMessage().contains("find()        [" + collection + "]=[" + expected.size() + "]"));
    }

    @Test
    void when_find_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      when(mongoCollection.find()).thenThrow(new RuntimeException("FIND"));

      assertThatThrownBy(() -> {
        client.find(collection);
      }).isInstanceOf(MongoException.class).hasMessageContaining("find() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("find()        [" + collection + "]"));
    }
  }

  @Nested
  class FindWithFilter {
    @SuppressWarnings("unchecked")
    @Test
    void when_findWithFilter_expect_result() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final List<Document> values = List.of(
          new Document("_id", "1").append("name", "name-1"),
          new Document("_id", "2").append("name", "name-2"));
      final List<Map<String, Object>> expected = List.of(
          Map.of("_id", "1", "name", "name-1"),
          Map.of("_id", "2", "name", "name-2"));
      final Map<String, Object> filter = Map.of("_id", "1");
      final FindIterable<Document> iterable = mock(FindIterable.class);
      when(mongoCollection.find(any(Bson.class))).thenReturn(iterable);
      doAnswer(invocation -> {
        final List<Document> entities = invocation.getArgument(0);
        entities.addAll(values);
        return null;
      }).when(iterable).into(anyList());

      final var result = client.find(collection, filter);

      assertThat(result).usingRecursiveFieldByFieldElementComparator().isEqualTo(expected);
      assertThat(logWatcher.list)
          .anyMatch(log -> log.getLevel().equals(Level.INFO)
              && log.getFormattedMessage().contains("find()       [" + collection + "]=[" + expected.size() + "]"))
          .anyMatch(log -> log.getLevel().equals(Level.DEBUG)
              && log.getFormattedMessage().contains("find()       [" + collection + "]FILTER"));
    }

    @Test
    void when_findWithFilter_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> filter = Map.of("_id", "1");
      when(mongoCollection.find(any(Bson.class))).thenThrow(new RuntimeException("FIND"));

      assertThatThrownBy(() -> {
        client.find(collection, filter);
      }).isInstanceOf(MongoException.class).hasMessageContaining("find() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("find()       [" + collection + "]"));
    }
  }

  @Nested
  class Insert {
    @Test
    void when_insert_expect_delegate() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> document = Map.of("_id", "1", "name", "name");
      final ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);

      client.insert(collection, document);

      verify(mongoCollection, times(1)).insertOne(captor.capture());
      assertThat(captor.getValue()).isNotNull();
      assertThat(captor.getValue().get("_id")).isNotNull().isEqualTo(document.get("_id"));
      assertThat(captor.getValue().get("name")).isNotNull().isEqualTo(document.get("name"));
      assertThat(logWatcher.list)
          .anyMatch(log -> log.getLevel().equals(Level.INFO)
              && log.getFormattedMessage().contains("insert()     [" + collection + "]"))
          .anyMatch(log -> log.getLevel().equals(Level.DEBUG)
              && log.getFormattedMessage().contains("insert()     [" + collection + "]=[Document"));
    }

    @Test
    void when_insert_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> document = Map.of("_id", "1", "name", "name");
      doThrow(new RuntimeException("INSERT")).when(mongoCollection).insertOne(any());

      assertThatThrownBy(() -> {
        client.insert(collection, document);
      }).isInstanceOf(MongoException.class).hasMessageContaining("insert() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("insert()     [" + collection + "]"));
    }
  }

  @Nested
  class InsertMany {
    @SuppressWarnings("unchecked")
    @Test
    void when_condition_expect_result() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final List<Map<String, Object>> values = List.of(
          Map.of("_id", "1", "name", "name-1"),
          Map.of("_id", "2", "name", "name-2"));
      final ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);

      client.insertMany(collection, values);

      verify(mongoCollection, times(1)).insertMany(captor.capture());
      assertThat(captor.getValue()).isNotNull().hasSameSizeAs(values);
      assertThat(captor.getValue().get(0).get("_id")).isNotNull().isEqualTo(values.get(0).get("_id"));
      assertThat(captor.getValue().get(0).get("name")).isNotNull().isEqualTo(values.get(0).get("name"));
      assertThat(captor.getValue().get(1).get("_id")).isNotNull().isEqualTo(values.get(1).get("_id"));
      assertThat(captor.getValue().get(1).get("name")).isNotNull().isEqualTo(values.get(1).get("name"));
      assertThat(logWatcher.list)
          .anyMatch(log -> log.getLevel().equals(Level.INFO)
              && log.getFormattedMessage().contains("insertMany() [" + collection + "]=[" + values.size() + "]"))
          .anyMatch(log -> log.getLevel().equals(Level.DEBUG)
              && log.getFormattedMessage().contains("insertMany() [" + collection + "]=[[Document"));
    }

    @Test
    void when_insertMany_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final List<Map<String, Object>> values = List.of(
          Map.of("_id", "1", "name", "name-1"),
          Map.of("_id", "2", "name", "name-2"));
      doThrow(new RuntimeException("INSERTMANY")).when(mongoCollection).insertMany(any());

      assertThatThrownBy(() -> {
        client.insertMany(collection, values);
      }).isInstanceOf(MongoException.class).hasMessageContaining("insertMany() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("insertMany() [" + collection + "]"));
    }
  }

  @Nested
  class Replace {
    @Test
    void when_condition_expect_result() {
      ((Logger) LoggerFactory.getLogger(KarateClientLogger.class)).setLevel(Level.DEBUG);
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> filter = Map.of("_id", "1", "name", "name");
      final Map<String, Object> document = Map.of("_id", "1", "name", "name");
      final Long value = 1L;
      final UpdateResult updateResult = mock(UpdateResult.class);
      when(mongoCollection.replaceOne(any(), any())).thenReturn(updateResult);
      when(updateResult.getModifiedCount()).thenReturn(value);

      final var result = client.replace(collection, filter, document);

      assertThat(result).isEqualTo(value);
      assertThat(logWatcher.list)
          .anyMatch(log -> log.getLevel().equals(Level.INFO)
              && log.getFormattedMessage().contains("replace()     [" + collection + "]=[" + value + "]"))
          .anyMatch(
              log -> log.getLevel().equals(Level.DEBUG) && log.getFormattedMessage().contains("replace()     [" + collection + "]FILTER"));
    }

    @Test
    void when_replace_exception_expect_exception() {
      final Map<Object, Object> config = getValidConfig();
      final MongoDBClient client = instantiateClient(config);
      // Mocked MongoDatabase
      client.setDatabase(mongoDatabase);
      final String collection = "collection";
      when(mongoDatabase.getCollection(collection)).thenReturn(mongoCollection);
      final Map<String, Object> filter = Map.of("_id", "1", "name", "name");
      final Map<String, Object> document = Map.of("_id", "1", "name", "name");
      when(mongoCollection.replaceOne(any(), any())).thenThrow(new RuntimeException("REPLACE"));

      assertThatThrownBy(() -> {
        client.replace(collection, filter, document);
      }).isInstanceOf(MongoException.class).hasMessageContaining("replace() " + collection);

      assertThat(logWatcher.list).anyMatch(log -> log.getLevel().equals(Level.ERROR)
          && log.getFormattedMessage().contains("replace()     [" + collection + "]"));
    }
  }

  protected static Map<Object, Object> getValidConfig() {
    return Map.of(HOSTS, "host",
        PORT, 9999,
        DB_NAME, "instance",
        USER, "admin",
        PASSWORD, "password",
        CONNECT_TIMEOUT, 10000,
        SERVER_SELECTION_TIMEOUT, 10000L,
        SOCKET_TIMEOUT, 10000);
  }

  protected MongoDBClient instantiateClient(final Map<Object, Object> config) {
    return new MongoDBClient(config);
  }
}
