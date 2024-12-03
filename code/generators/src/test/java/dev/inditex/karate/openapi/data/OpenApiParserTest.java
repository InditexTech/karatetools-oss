package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.Map;

import dev.inditex.karate.openapi.ExampleBasicApiMother;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenApiParserTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiParser::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GetOperationsByTag {

    @Test
    void when_parse_pets_OpenApi_from_file_expect_parsed() {
      final String file = getPathFromResources("/openapi/unit/parser/openapi-test.yml");

      final Map<String, List<OpenApiParser.OperationPath>> operationsByTag = OpenApiParser.getOperationsByTag(file);

      assertThat(operationsByTag).isNotEmpty().containsOnlyKeys("pet", "NoTag");
      assertThat(operationsByTag.get("pet")).hasSize(2);
      operationsByTag.get("pet").forEach(op -> {
        assertThat(op.operation().getOperationId()).isNotNull();
        assertThat(op.operation().getTags()).contains("pet");
        assertThat(op.method()).isNotNull();
        assertThat(op.path()).isNotNull();
      });
      assertThat(operationsByTag.get("NoTag")).hasSize(1);
      operationsByTag.get("NoTag").forEach(op -> {
        assertThat(op.operation().getOperationId()).isNotNull();
        assertThat(op.operation().getTags()).isNull();
        assertThat(op.method()).isNotNull();
        assertThat(op.path()).isNotNull();
      });
    }

    @Test
    void when_parse_basic_OpenApi_from_file_expect_parsed() {
      final String file = getPathFromResources(ExampleBasicApiMother.getApiPath());

      final Map<String, List<OpenApiParser.OperationPath>> operationsByTag = OpenApiParser.getOperationsByTag(file);

      assertThat(operationsByTag).isNotEmpty().containsOnlyKeys("BasicApi");
      assertThat(operationsByTag.get("BasicApi")).hasSize(3);
      operationsByTag.get("BasicApi").forEach(op -> {
        assertThat(op.operation().getOperationId()).isNotNull();
        assertThat(op.operation().getTags()).contains("BasicApi");
        assertThat(op.method()).isNotNull();
        assertThat(op.path()).isNotNull();
      });
    }

    @Test
    void when_parse_OpenApi_normalize_operationId_from_file_expect_parsed() {
      final String file = getPathFromResources("/openapi/unit/parser/openapi-test-sanitize-operationId.yml");

      final Map<String, List<OpenApiParser.OperationPath>> operationsByTag = OpenApiParser.getOperationsByTag(file);

      final Object[] expectedOperationIds = {"getuser", "putuser", "postUser", "deleteUser", "patchuser"};
      assertThat(operationsByTag.get("NoTag")).hasSize(5);
      operationsByTag.get("NoTag").forEach(op -> {
        assertThat(op.operation().getOperationId()).isNotNull().isIn(expectedOperationIds);
        assertThat(op.operation().getTags()).isNull();
        assertThat(op.method()).isNotNull();
        assertThat(op.path()).isNotNull();
      });
    }

  }
}
