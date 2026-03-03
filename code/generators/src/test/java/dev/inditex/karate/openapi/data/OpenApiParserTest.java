package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import dev.inditex.karate.openapi.ExampleBasicApiMother;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenApiParserTest extends KarateTest {

  private static final String OPERATION_ID_SANITIZE_MODE_PROPERTY = "open-api-operation-id-sanitize.mode";

  private static final String OPERATION_ID_SANITIZE_MODE_LETTERS_ONLY = "letters-only";

  private static final String OPERATION_ID_SANITIZE_MODE_ALPHANUMERIC = "alphanumeric";

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiParser::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GetOperationsByTag {

    @AfterEach
    void afterEach() {
      System.clearProperty(OPERATION_ID_SANITIZE_MODE_PROPERTY);
    }

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

    @ParameterizedTest(name = "sanitizeOperationIds_{0}")
    @MethodSource
    void when_parse_OpenApi_sanitize_operationId_from_file_expect_parsed(final String testId, final String mode,
        final String[] expectedOperationIds) {
      if (mode != null) {
        System.setProperty(OPERATION_ID_SANITIZE_MODE_PROPERTY, mode);
      }
      final String file = getPathFromResources("/openapi/unit/parser/openapi-test-sanitize-operationId.yml");

      final Map<String, List<OpenApiParser.OperationPath>> operationsByTag = OpenApiParser.getOperationsByTag(file);

      assertThat(operationsByTag.get("NoTag")).hasSize(expectedOperationIds.length);
      operationsByTag.get("NoTag").forEach(op -> {
        assertThat(op.operation().getOperationId()).isNotNull().isIn((Object[]) expectedOperationIds);
        assertThat(op.operation().getTags()).isNull();
        assertThat(op.method()).isNotNull();
        assertThat(op.path()).isNotNull();
      });
    }

    static Stream<Arguments> when_parse_OpenApi_sanitize_operationId_from_file_expect_parsed() {
      final String[] expectedOperationIdsLettersOnly = {"getuser", "postUser", "putuser", "deleteUser", "patchuser",
          "getUserV", "getUserV", "NoOp", "NoOp", "patchuser",
          "postUserV", "VpostUserV", "NoOp", "deleteUser", "patchuser"};
      final String[] expectedOperationIdsAlphanumeric = {"getuser", "post_User", "putuser", "deleteUser", "patchuser01",
          "getUserV2", "getUserV3", "NoOp", "NoOp", "patchuser02",
          "_postUserV2", "VpostUserV3", "NoOp", "deleteUser3", "patchuser03"};
      return Stream.of(
          Arguments.of("sanitizeOperationIds_LettersOnly", OPERATION_ID_SANITIZE_MODE_LETTERS_ONLY, expectedOperationIdsLettersOnly),
          Arguments.of("sanitizeOperationIds_Alphanumeric", OPERATION_ID_SANITIZE_MODE_ALPHANUMERIC, expectedOperationIdsAlphanumeric),
          Arguments.of("sanitizeOperationIds_Blank", "", expectedOperationIdsAlphanumeric), // default is alphanumeric
          Arguments.of("sanitizeOperationIds_Null", null, expectedOperationIdsAlphanumeric) // default is alphanumeric
      );
    }
  }
}
