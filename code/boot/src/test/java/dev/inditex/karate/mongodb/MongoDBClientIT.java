package dev.inditex.karate.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.KarateTestUtils;
import dev.inditex.karate.docker.DockerComposeTestConfiguration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("IT")
@ActiveProfiles({"test-docker"})
@SpringBootTest(classes = {
    DockerComposeTestConfiguration.class
})
public class MongoDBClientIT {

  static final String CONFIG_FILE = "classpath:config/db/mongodb-config.yml";

  @Nested
  class MongoDB {
    @Test
    void when_mongo_expect_results() throws IOException {
      final String manyFilter = """
          {
              "name": {
                  "$regex": "^karate-(.*)"
              }
          }
          """;
      final String oneFilter = """
          {
              "_id": "1"
          }
          """;
      final String someFilter = """
          {
              "value": {
                  $gt: 1
              }
          }
          """;
      final String oneDocument = """
          {
              "_id": "1",
              "name": "karate-01",
              "value": 1
          }
          """;
      final String oneDocumentReplaced = """
          {
              "_id": "1",
              "name": "karate-01BIS",
              "value": 1
          }
          """;
      final String manyDocuments = """
          [
              {
                  "_id": "2",
                  "name": "karate-02",
                  "value": 2
              },
              {
                  "_id": "3",
                  "name": "karate-03",
                  "value": 3
              }
          ]
          """;
      final Map<Object, Object> config = KarateTestUtils.readYaml(CONFIG_FILE);
      final MongoDBClient client = instantiateClient(config);
      final String collection = "data";
      final List<Map<String, Object>> expectedOne = List.of(
          Map.of("_id", "1", "name", "karate-01", "value", 1));
      final List<Map<String, Object>> expectedOneReplaced = List.of(
          Map.of("_id", "1", "name", "karate-01BIS", "value", 1));
      final List<Map<String, Object>> expectedAll = List.of(
          Map.of("_id", "1", "name", "karate-01", "value", 1),
          Map.of("_id", "2", "name", "karate-02", "value", 2),
          Map.of("_id", "3", "name", "karate-03", "value", 3));
      final List<Map<String, Object>> expectedSome = List.of(
          Map.of("_id", "2", "name", "karate-02", "value", 2),
          Map.of("_id", "3", "name", "karate-03", "value", 3));

      // available
      final var available = client.available();
      // deleteMany
      final var deleteMany = client.deleteMany(collection, KarateTestUtils.fromJson(manyFilter));
      // count
      final var initialCount = client.count(collection);
      // insert
      client.insert(collection, KarateTestUtils.fromJson(oneDocument));
      final var countAfterInsertOne = client.count(collection);
      // find
      final var findOne = client.find(collection, KarateTestUtils.fromJson(oneFilter));
      // insertMany
      client.insertMany(collection, KarateTestUtils.fromJsonList(manyDocuments));
      final var countAfterInsertMany = client.count(collection);
      // find All
      final var findAll = client.find(collection);
      // find Some
      final var findSome = client.find(collection, KarateTestUtils.fromJson(someFilter));
      // replace
      final var replace = client.replace(collection, KarateTestUtils.fromJson(oneFilter), KarateTestUtils.fromJson(oneDocumentReplaced));
      final var findOneAfterReplace = client.find(collection, KarateTestUtils.fromJson(oneFilter));
      // deleteOne
      final var deleteOne = client.delete(collection, KarateTestUtils.fromJson(oneFilter));
      final var countAfterDeleteOne = client.count(collection);

      assertThat(available).isTrue();
      assertThat(deleteMany).isNotNegative();
      assertThat(initialCount).isZero();
      assertThat(countAfterInsertOne).isEqualTo(1);
      assertThat(findOne).usingRecursiveFieldByFieldElementComparator().isEqualTo(expectedOne);
      assertThat(countAfterInsertMany).isEqualTo(3);
      assertThat(findAll).usingRecursiveFieldByFieldElementComparator().isEqualTo(expectedAll);
      assertThat(findSome).usingRecursiveFieldByFieldElementComparator().isEqualTo(expectedSome);
      assertThat(replace).isEqualTo(1);
      assertThat(findOneAfterReplace).usingRecursiveFieldByFieldElementComparator().isEqualTo(expectedOneReplaced);
      assertThat(deleteOne).isEqualTo(1);
      assertThat(countAfterDeleteOne).isEqualTo(2);
    }
  }

  protected MongoDBClient instantiateClient(final Map<Object, Object> config) {
    return new MongoDBClient(config);
  }
}
