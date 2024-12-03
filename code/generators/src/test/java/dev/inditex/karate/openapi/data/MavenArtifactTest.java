package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MavenArtifactTest {

  @Nested
  class FromId {
    @ParameterizedTest
    @MethodSource
    void when_artifact_id_expect_artifact(final String id, final MavenArtifact expected) {

      final var result = MavenArtifact.fromId(id);

      assertThat(result).isEqualTo(expected);
    }

    public static Stream<Arguments> when_artifact_id_expect_artifact() {
      return Stream.of(
          Arguments.of("dev.inditex.openapi.karatetools:test", new MavenArtifact("dev.inditex.openapi.karatetools", "test")),
          Arguments.of("dev.inditex.openapi.karatetools:", null),
          Arguments.of(":test", new MavenArtifact("", "test")),
          Arguments.of(":", null),
          Arguments.of("test", null));
    }
  }

  @Nested
  class GetId {
    @ParameterizedTest
    @MethodSource
    void when_artifact_expect_id(final MavenArtifact artifact, final String expected) {

      final var id = artifact.getId();

      assertThat(id).isEqualTo(expected);
    }

    public static Stream<Arguments> when_artifact_expect_id() {
      return Stream.of(
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build(),
              "dev.inditex.openapi.karatetools:test"),
          Arguments.of(MavenArtifact.builder().artifactId("test").build(),
              "null:test"),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").build(),
              "dev.inditex.openapi.karatetools:null"));
    }
  }

  @Nested
  class ToPath {
    @ParameterizedTest
    @MethodSource
    void when_artifact_expect_path(final MavenArtifact artifact, final String expected) {

      final var id = artifact.toPath();

      assertThat(id).isEqualTo(expected);
    }

    public static Stream<Arguments> when_artifact_expect_path() {
      return Stream.of(
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/test")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test-api").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/test-api")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test-Api").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/test-api")),
          Arguments.of(MavenArtifact.builder().artifactId("test").build(),
              FilenameUtils.separatorsToSystem("null/test")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/null")),
          Arguments.of(
              MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("artifact-api-rest-stable").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/artifact-api-rest-stable")));
    }
  }

  @Nested
  class ArtifactIdToPath {
    @ParameterizedTest
    @MethodSource
    void when_artifact_expect_path(final MavenArtifact artifact, final String expected) {

      final var id = artifact.artifactIdToPath();

      assertThat(id).isEqualTo(expected);
    }

    public static Stream<Arguments> when_artifact_expect_path() {
      return Stream.of(
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build(),
              FilenameUtils.separatorsToSystem("test")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test-api").build(),
              FilenameUtils.separatorsToSystem("test-api")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test-Api").build(),
              FilenameUtils.separatorsToSystem("test-api")),
          Arguments.of(MavenArtifact.builder().artifactId("test").build(),
              FilenameUtils.separatorsToSystem("test")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").build(), null),
          Arguments.of(
              MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("artifact-api-rest-stable").build(),
              FilenameUtils.separatorsToSystem("artifact-api-rest-stable")));
    }
  }

  @Nested
  class ToPackage {
    @ParameterizedTest
    @MethodSource
    void when_artifact_expect_package(final MavenArtifact artifact, final String expected) {

      final var id = artifact.toPackage();

      assertThat(id).isEqualTo(expected);
    }

    public static Stream<Arguments> when_artifact_expect_package() {
      return Stream.of(
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/test")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test-api").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/testapi")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test-Api").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/testapi")),
          Arguments.of(MavenArtifact.builder().artifactId("test").build(),
              FilenameUtils.separatorsToSystem("null/test")),
          Arguments.of(MavenArtifact.builder().build(),
              FilenameUtils.separatorsToSystem("null/null")),
          Arguments.of(new MavenArtifact(null, null),
              FilenameUtils.separatorsToSystem("null/null")),
          Arguments.of(MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/null")),
          Arguments.of(
              MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("artifact-api-rest-stable").build(),
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/artifactapireststable")));
    }

    @ParameterizedTest
    @MethodSource
    void when_string_expect_value(final String value, final String expected) {
      final var id = MavenArtifact.toPackage(value);

      assertThat(id).isEqualTo(expected);
    }

    public static Stream<Arguments> when_string_expect_value() {
      return Stream.of(
          Arguments.of("dev.inditex.openapi.karatetools:test",
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/test")),
          Arguments.of("dev.inditex.openapi.karatetools:test-api",
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/testapi")),
          Arguments.of("dev.inditex.openapi.karatetools:test-Api",
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/testapi")),
          Arguments.of("test",
              FilenameUtils.separatorsToSystem("test")),
          Arguments.of(null, null),
          Arguments.of("dev.inditex.openapi.karatetools",
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools")),
          Arguments.of("dev.inditex.openapi.karatetools:artifact-api-rest-stable",
              FilenameUtils.separatorsToSystem("dev/inditex/openapi/karatetools/artifactapireststable")));

    }
  }

  @Nested
  class ToApiUrl {
    @ParameterizedTest
    @MethodSource
    void when_artifact_expect_api_url(final MavenArtifact artifact, final String expected) {

      final var id = artifact.toApiUrl();

      assertThat(id).isEqualTo(expected);
    }

    public static Stream<Arguments> when_artifact_expect_api_url() {
      return Stream.of(
          Arguments.of(MavenArtifact.builder().build(), "testApiUrl"),
          Arguments.of(MavenArtifact.builder().artifactId("test").build(), "testUrl"),
          Arguments.of(MavenArtifact.builder().artifactId("test-api").build(), "testApiUrl"),
          Arguments.of(MavenArtifact.builder().artifactId("test-API").build(), "testApiUrl"),
          Arguments.of(MavenArtifact.builder().artifactId("test_api").build(), "testApiUrl"),
          Arguments.of(MavenArtifact.builder().artifactId("test api").build(), "testApiUrl"),
          Arguments.of(MavenArtifact.builder().artifactId("group-artifact-api-rest-stable").build(), "groupArtifactApiRestStableUrl"));
    }
  }

}
