package dev.inditex.karate.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dev.inditex.karate.logging.KarateClientLogger;
import dev.inditex.karate.parser.SystemPropertiesParser;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.AccessLevel;
import lombok.Getter;
import org.bson.Document;
import org.slf4j.event.Level;

/**
 * The Class MongoDBClient.
 */
@Getter(AccessLevel.PROTECTED)
public class MongoDBClient {

  /** The Constant HOSTS. */
  public static final String HOSTS = "hosts";

  /** The Constant PORT. */
  public static final String PORT = "port";

  /** The Constant DB_NAME. */
  public static final String DB_NAME = "db-name";

  /** The Constant USER. */
  public static final String USER = "user";

  /** The Constant PASSWORD. */
  public static final String PASSWORD = "password";

  /** The Constant CONNECT_TIMEOUT. */
  public static final String CONNECT_TIMEOUT = "connect-timeout";

  /** The Constant SOCKET_TIMEOUT. */
  public static final String SOCKET_TIMEOUT = "socket-timeout";

  /** The Constant SERVER_SELECTION_TIMEOUT. */
  public static final String SERVER_SELECTION_TIMEOUT = "server-selection-timeout";

  /** The Constant HOSTS_SEPARATOR. */
  public static final String HOSTS_SEPARATOR = ",";

  /** The Constant DEFAULT_TIMEOUT. */
  public static final int DEFAULT_TIMEOUT = 15000;

  /** The Constant PING. */
  protected static final Document PING = new Document("ping", 1);

  /** The server addresses. */
  protected final List<ServerAddress> serverAddresses;

  /** The mongo credential. */
  protected final MongoCredential mongoCredential;

  /** The mongo client settings. */
  protected final MongoClientSettings mongoClientSettings;

  /** The db name. */
  protected final String dbName;

  /** The is mongo available. */
  protected Boolean isMongoAvailable;

  /** The client. */
  protected MongoClient client;

  /** The database. */
  protected MongoDatabase database;

  /** The log. */
  protected final KarateClientLogger log = new KarateClientLogger();

  /**
   * Instantiates a new mongo DB client.
   *
   * @param configMap the config map
   */
  public MongoDBClient(final Map<Object, Object> configMap) {
    super();
    final Map<Object, Object> config = SystemPropertiesParser.parseConfiguration(configMap);

    final String hosts = String.valueOf(config.get(HOSTS));
    int port = 0;
    if (config.get(PORT) != null) {
      port = Integer.parseInt(String.valueOf(config.get(PORT)));
    }

    dbName = String.valueOf(config.get(DB_NAME));
    final String user = String.valueOf(config.get(USER));
    final String password = String.valueOf(config.get(PASSWORD));

    final int connectTimeout = Integer.parseInt(String.valueOf(config.getOrDefault(CONNECT_TIMEOUT, DEFAULT_TIMEOUT)));
    final int socketTimeout = Integer.parseInt(String.valueOf(config.getOrDefault(SOCKET_TIMEOUT, DEFAULT_TIMEOUT)));
    final int serverSelectionTimeout = Integer.parseInt(String.valueOf(config.getOrDefault(SERVER_SELECTION_TIMEOUT, DEFAULT_TIMEOUT)));

    serverAddresses = new ArrayList<>();
    for (final String host : hosts.split(HOSTS_SEPARATOR)) {
      final ServerAddress serverAddress = new ServerAddress(host, port);
      serverAddresses.add(serverAddress);
    }
    mongoCredential = MongoCredential.createCredential(user, dbName, password.toCharArray());

    final ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();

    mongoClientSettings = MongoClientSettings.builder()
        .applyToClusterSettings(builder -> builder
            .hosts(serverAddresses)
            .serverSelectionTimeout(serverSelectionTimeout, TimeUnit.MILLISECONDS))
        .applyToSocketSettings(builder -> builder
            .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(socketTimeout, TimeUnit.MILLISECONDS))
        .credential(mongoCredential)
        .serverApi(serverApi)
        .build();

    client = MongoClients.create(mongoClientSettings);
    log.debug("client  [{}]", client);
    database = client.getDatabase(dbName);
    log.debug("database[{}]", database);
  }

  /**
   * Available.
   *
   * @return the boolean
   */
  public Boolean available() {
    log.debug("available() ... ");
    if (isMongoAvailable == null) {
      try {
        if (client != null) {
          final Document ping = database.runCommand(PING);
          log.debug("available() = #{}", ping);
          if (ping.containsKey("ok") && getPing(ping)) {
            isMongoAvailable = true;
          } else {
            isMongoAvailable = false;
          }
        } else {
          isMongoAvailable = false;
        }
      } catch (final Exception e) {
        log.debug("available() Exception={}", e.getMessage());
        isMongoAvailable = false;
      }
    }
    log.info("available()={}", isMongoAvailable);
    return isMongoAvailable;

  }

  /**
   * Count.
   *
   * @param collectionName the collection name
   * @return the long
   */
  public Long count(final String collectionName) {
    log.debug("count()      [{}]", collectionName);
    try {
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      final long count = collection.countDocuments();
      log.info("count()      [{}]=[{}]", collectionName, count);
      return count;
    } catch (final Exception e) {
      log.error("count()      [{}]={}", collectionName, e.getMessage());
      throw new MongoException("count() " + collectionName, e);
    }
  }

  /**
   * Delete.
   *
   * @param collectionName the collection name
   * @param value the value
   * @return the long
   */
  public long delete(final String collectionName, final Map<String, Object> value) {
    log.debug("delete()     [{}][{}]", collectionName, value);
    try {
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      final Document document = new Document(value);
      final DeleteResult result = collection.deleteOne(document);
      log.info("delete()     [{}]=[{}]", collectionName, result.getDeletedCount());
      if (log.isEnabledForLevel(Level.DEBUG)) {
        log.debug("delete()     [{}]VALUE[{}]=[{}]", collectionName, document, result.getDeletedCount());
      }
      return result.getDeletedCount();
    } catch (final Exception e) {
      log.error("delete()     [{}]={}", collectionName, e.getMessage());
      throw new MongoException("delete() " + collectionName, e);
    }

  }

  /**
   * Delete many.
   *
   * @param collectionName the collection name
   * @param filter the filter
   * @return the long
   */
  public long deleteMany(final String collectionName, final Map<String, Object> filter) {
    log.debug("deleteMany() [{}][{}]", collectionName, filter);
    try {
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      final Document document = new Document(filter);
      final DeleteResult result = collection.deleteMany(document);
      log.info("deleteMany() [{}]=[{}]", collectionName, result.getDeletedCount());
      if (log.isEnabledForLevel(Level.DEBUG)) {
        log.debug("deleteMany() [{}]FILTER[{}]=[{}]", collectionName, document, result.getDeletedCount());
      }
      return result.getDeletedCount();
    } catch (final Exception e) {
      log.error("deleteMany() [{}]={}", collectionName, e.getMessage());
      throw new MongoException("deleteMany() " + collectionName, e);
    }
  }

  /**
   * Find.
   *
   * @param collectionName the collection name
   * @return the list
   */
  public List<Map<String, Object>> find(final String collectionName) {
    log.debug("find()        [{}]", collectionName);
    try {
      final List<Map<String, Object>> results = new ArrayList<>();
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      collection.find().into(results);
      log.info("find()        [{}]=[{}]", collectionName, results.size());
      return results;
    } catch (final Exception e) {
      log.error("find()        [{}]={}", collectionName, e.getMessage());
      throw new MongoException("find() " + collectionName, e);
    }
  }

  /**
   * Find.
   *
   * @param collectionName the collection name
   * @param filter the filter
   * @return the list
   */
  public List<Map<String, Object>> find(final String collectionName, final Map<String, Object> filter) {
    log.debug("find()       [{}][{}]", collectionName, filter);
    try {
      final List<Map<String, Object>> results = new ArrayList<>();
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      final BasicDBObject bsonFilter = new BasicDBObject(filter);
      collection.find(bsonFilter).into(results);
      log.info("find()       [{}]=[{}]", collectionName, results.size());
      if (log.isEnabledForLevel(Level.DEBUG)) {
        log.debug("find()       [{}]FILTER[{}]=[{}]", collectionName, bsonFilter, results.size());
      }
      return results;
    } catch (final Exception e) {
      log.error("find()       [{}]={}", collectionName, e.getMessage());
      throw new MongoException("find() " + collectionName, e);
    }
  }

  /**
   * Insert.
   *
   * @param collectionName the collection name
   * @param value the value
   */
  public void insert(final String collectionName, final Map<String, Object> value) {
    log.debug("insert()     [{}]", collectionName, value);
    try {
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      final Document document = new Document(value);
      collection.insertOne(document);
      log.info("insert()     [{}]", collectionName);
      if (log.isEnabledForLevel(Level.DEBUG)) {
        log.debug("insert()     [{}]=[{}]", collectionName, document);
      }
    } catch (final Exception e) {
      log.error("insert()     [{}]={}", collectionName, e.getMessage());
      throw new MongoException("insert() " + collectionName, e);
    }
  }

  /**
   * Insert many.
   *
   * @param collectionName the collection name
   * @param values the values
   */
  public void insertMany(final String collectionName, final List<Map<String, Object>> values) {
    log.debug("insertMany() [{}][{}]", collectionName, values);
    try {
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      final List<Document> documents = new ArrayList<>();
      for (final Map<String, Object> value : values) {
        final Document document = new Document(value);
        documents.add(document);
      }
      collection.insertMany(documents);
      log.info("insertMany() [{}]=[{}]", collectionName, values.size());
      if (log.isEnabledForLevel(Level.DEBUG)) {
        log.debug("insertMany() [{}]=[{}]", collectionName, documents);
      }
    } catch (final Exception e) {
      log.error("insertMany() [{}]={}", collectionName, e.getMessage());
      throw new MongoException("insertMany() " + collectionName, e);
    }
  }

  /**
   * Replace.
   *
   * @param collectionName the collection name
   * @param filter the filter
   * @param value the value
   * @return the long
   */
  public long replace(final String collectionName, final Map<String, Object> filter, final Map<String, Object> value) {
    log.debug("replace()     [{}][{}][{}]", collectionName, filter, value);
    try {
      final MongoCollection<Document> collection = database.getCollection(collectionName);
      final Document document = new Document(value);
      final BasicDBObject bsonFilter = new BasicDBObject(filter);
      final UpdateResult result = collection.replaceOne(bsonFilter, document);
      log.info("replace()     [{}]=[{}]", collectionName, result.getModifiedCount());
      if (log.isEnabledForLevel(Level.DEBUG)) {
        log.debug("replace()     [{}]FILTER[{}]VALUE[{}]=[{}]", collectionName, bsonFilter, document, result.getModifiedCount());
      }
      return result.getModifiedCount();
    } catch (final Exception e) {
      log.error("replace()     [{}]={}", collectionName, e.getMessage());
      throw new MongoException("replace() " + collectionName, e);
    }
  }

  /**
   * Gets the ping.
   *
   * @param ping the ping
   * @return the ping
   */
  protected static boolean getPing(final Document ping) {
    return ping.get("ok") instanceof Integer && ping.getInteger("ok") == 1
        ||
        ping.get("ok") instanceof Double && ping.getDouble("ok") == 1
        ||
        ping.get("ok") instanceof Boolean && Boolean.TRUE.equals(ping.getBoolean("ok"));
  }

  /**
   * Sets the database.
   *
   * @param database the new database
   */
  public void setDatabase(final MongoDatabase database) {
    this.database = database;
  }

  /**
   * Sets the client.
   *
   * @param client the new client
   */
  public void setClient(final MongoClient client) {
    this.client = client;
  }
}
