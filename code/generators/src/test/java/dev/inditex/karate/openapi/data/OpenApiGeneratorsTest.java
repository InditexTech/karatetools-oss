package dev.inditex.karate.openapi.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.inditex.karate.openapi.data.KarateFunctionalFeature.FunctionalTestStep;
import dev.inditex.karate.openapi.data.KarateSmokeFeature.SmokeTestResponse;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OpenApiGeneratorsTest extends KarateTest {

  @Nested
  class Constructor {
    @Test
    void when_instance_expect_no_exception() {
      assertThatCode(OpenApiGenerators::new).doesNotThrowAnyException();
    }
  }

  @Nested
  class GenerateOperations {
    @Test
    void when_generate_expect_delegate() {
      try (
          final MockedStatic<OpenApiUtils> openApiUtils = mockStatic(OpenApiUtils.class);
          final MockedStatic<KarateOperation> karateOperation = mockStatic(KarateOperation.class);) {

        final Path root = mock(Path.class);
        final MavenArtifact mavenArtifact = MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build();
        final OperationPath operationPath = mock(OperationPath.class);
        final List<OperationPath> operations = List.of(operationPath);
        final Path operationFolder = mock(Path.class);
        final Path saved = mock(Path.class);
        openApiUtils.when(
            () -> OpenApiUtils.getOperationFolder(root, mavenArtifact, operationPath))
            .thenReturn(operationFolder);
        karateOperation.when(
            () -> KarateOperation.save(root, operationFolder, mavenArtifact, operationPath, OpenApiUtils.getResponseSchemas(operationPath)))
            .thenReturn(List.of(saved));

        final var result = OpenApiGenerators.generateOperations(root, mavenArtifact, operations);

        assertThat(result).hasSize(1);
      }

    }
  }

  @Nested
  class GenerateSmokeTests {
    @Test
    void when_generate_expect_delegate() {
      try (
          final MockedStatic<OpenApiUtils> openApiUtils = mockStatic(OpenApiUtils.class);
          final MockedStatic<KarateSmokeFeature> karateSmokeFeature = mockStatic(KarateSmokeFeature.class);) {

        final OpenAPI openApi = mock(OpenAPI.class);
        final Path root = mock(Path.class);
        final Path artifact = mock(Path.class);
        final Path smoke = mock(Path.class);
        final MavenArtifact mavenArtifact = MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build();
        final OperationPath operationPath = mock(OperationPath.class);
        final List<OperationPath> operations = List.of(operationPath);
        final Operation operation = mock(Operation.class);
        final ApiResponses responses = new ApiResponses();
        final ApiResponse response = new ApiResponse();
        responses.put("200", response);
        final Path saved = mock(Path.class);
        when(root.resolve(mavenArtifact.toPath())).thenReturn(artifact);
        when(artifact.resolve("smoke")).thenReturn(smoke);
        when(operationPath.operation()).thenReturn(operation);
        when(operation.getResponses()).thenReturn(responses);
        openApiUtils.when(
            () -> OpenApiUtils.getResponseSchemaClasspath(root, mavenArtifact, operationPath, "200"))
            .thenReturn("responseSchemaClasspath");
        openApiUtils.when(
            () -> OpenApiUtils.getOperationClasspath(root, mavenArtifact, operationPath))
            .thenReturn("operationClasspath");
        karateSmokeFeature.when(
            () -> KarateSmokeFeature.save(smoke, operation, "operationClasspath",
                List.of(new SmokeTestResponse("200", "responseSchemaClasspath")), openApi))
            .thenReturn(List.of(saved));

        final var result = OpenApiGenerators.generateSmokeTests(root, mavenArtifact, operations, openApi);

        assertThat(result).hasSize(1);
      }
    }
  }

  @Nested
  class GenerateFunctionalTest {
    @Test
    void when_generate_expect_delegate() {
      try (
          final MockedStatic<OpenApiUtils> openApiUtils = mockStatic(OpenApiUtils.class);
          final MockedStatic<KarateFunctionalFeature> karateFunctionalFeature = mockStatic(KarateFunctionalFeature.class);) {

        final OpenAPI openApi = mock(OpenAPI.class);
        final Path root = mock(Path.class);
        final Path artifact = mock(Path.class);
        final Path functional = mock(Path.class);
        final MavenArtifact mavenArtifact = MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build();
        final OperationPath operationPath = mock(OperationPath.class);
        final Operation operation = mock(Operation.class);
        final ApiResponses responses = new ApiResponses();
        final ApiResponse response = new ApiResponse();
        responses.put("200", response);
        final Path saved = mock(Path.class);
        when(root.resolve(mavenArtifact.toPath())).thenReturn(artifact);
        when(artifact.resolve("functional")).thenReturn(functional);
        when(operationPath.operation()).thenReturn(operation);
        when(operation.getResponses()).thenReturn(responses);
        openApiUtils.when(
            () -> OpenApiUtils.getResponseSchemaClasspath(root, mavenArtifact, operationPath, "200"))
            .thenReturn("responseSchemaClasspath");
        openApiUtils.when(
            () -> OpenApiUtils.getOperationClasspath(root, mavenArtifact, operationPath))
            .thenReturn("operationClasspath");
        karateFunctionalFeature.when(
            () -> KarateFunctionalFeature.save(functional, "testName", false,
                List.of(new FunctionalTestStep(operation, "200", "operationClasspath", "responseSchemaClasspath")), openApi))
            .thenReturn(List.of(saved));

        final var result =
            OpenApiGenerators.generateFunctionalTest(root, mavenArtifact, "testName", false, Map.of(operationPath, Set.of("200")), openApi);

        assertThat(result).hasSize(1);
      }
    }
  }

  @Nested
  class GenerateMockData {
    @Test
    void when_generate_expect_delegate() {
      try (
          final MockedStatic<OpenApiUtils> openApiUtils = mockStatic(OpenApiUtils.class);
          final MockedStatic<KarateMockData> karateMockData = mockStatic(KarateMockData.class);) {

        final OpenAPI openApi = mock(OpenAPI.class);
        final Path root = mock(Path.class);
        final MavenArtifact mavenArtifact = MavenArtifact.builder().groupId("dev.inditex.openapi.karatetools").artifactId("test").build();
        final OperationPath operationPath = mock(OperationPath.class);
        final Path operationFolder = mock(Path.class);
        final Path saved = mock(Path.class);
        openApiUtils.when(
            () -> OpenApiUtils.getMockDataTargetPath(root, mavenArtifact, false, mavenArtifact, null))
            .thenReturn(operationFolder);
        karateMockData.when(
            () -> KarateMockData.save(operationFolder, operationPath, "200", openApi))
            .thenReturn(saved);

        final var result = OpenApiGenerators.generateMockData(root, mavenArtifact, false, mavenArtifact, "testName",
            Map.of(operationPath, Set.of("200")), openApi);

        assertThat(result).hasSize(1);
      }
    }
  }
}
