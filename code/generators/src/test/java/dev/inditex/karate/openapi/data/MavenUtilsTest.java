package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MavenUtilsTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(MavenUtils::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GetPomArtifacts {

    @Test
    void when_exception_expect_empty() {
      final var result = MavenUtils.getPomArtifacts("src/test/resources/maven-model/pom-not-found.xml");

      assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @MethodSource
    void when_pom_expect_artifacts(final String path, final Map<String, MavenArtifact> expected) {

      final var result = MavenUtils.getPomArtifacts(path);

      assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> when_pom_expect_artifacts() {
      return Stream.of(
          Arguments.of("src/test/resources/maven-model/pom-no-dependencies.xml", // No dependencies
              Map.of("dev.inditex.karate.maven-model:pom-no-dependencies",
                  new MavenArtifact("dev.inditex.karate.maven-model", "pom-no-dependencies"))),
          Arguments.of("src/test/resources/maven-model/pom-single-dependency.xml", // Single dependency
              Map.of("dev.inditex.karate.maven-model:pom-single-dependency",
                  new MavenArtifact("dev.inditex.karate.maven-model", "pom-single-dependency"),
                  "dev.inditex.karate.maven-model:dependency-1",
                  new MavenArtifact("dev.inditex.karate.maven-model", "dependency-1"))),
          Arguments.of("src/test/resources/maven-model/pom-multiple-dependencies.xml", // Multiple dependencies
              Map.of("dev.inditex.karate.maven-model:pom-multiple-dependencies",
                  new MavenArtifact("dev.inditex.karate.maven-model", "pom-multiple-dependencies"),
                  "dev.inditex.karate.maven-model:dependency-1",
                  new MavenArtifact("dev.inditex.karate.maven-model", "dependency-1"),
                  "dev.inditex.karate.maven-model:dependency-2",
                  new MavenArtifact("dev.inditex.karate.maven-model", "dependency-2"),
                  "dev.inditex.karate.maven-model:dependency-3",
                  new MavenArtifact("dev.inditex.karate.maven-model", "dependency-3"),
                  "dev.inditex.karate.maven-model:dependency-4",
                  new MavenArtifact("dev.inditex.karate.maven-model", "dependency-4"))));
    }
  }
}
