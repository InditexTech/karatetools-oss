package dev.inditex.karate.openapi.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.inditex.karate.openapi.data.KarateFunctionalFeature.FunctionalTestStep;
import dev.inditex.karate.openapi.data.KarateSmokeFeature.SmokeTestResponse;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * The Class OpenApiGenerators.
 */
public class OpenApiGenerators extends KarateWriter {

  /**
   * Instantiates a new open api generators.
   */
  protected OpenApiGenerators() {
    super();
  }

  /**
   * Generate operations.
   *
   * @param root the root
   * @param artifact the artifact
   * @param operations the operations
   * @return the list
   */
  public static List<Path> generateOperations(final Path root, final MavenArtifact artifact, final List<OperationPath> operations) {
    final List<Path> outputs = new ArrayList<>();
    for (final OperationPath operationPath : operations) {
      final Path operationFolder = OpenApiUtils.getOperationFolder(root, artifact, operationPath);
      outputs.addAll(KarateOperation.save(root, operationFolder, artifact, operationPath, OpenApiUtils.getResponseSchemas(operationPath)));
    }
    return outputs;
  }

  /**
   * Generate smoke tests.
   *
   * @param root the root
   * @param artifact the artifact
   * @param operations the operations
   * @param openApi the open api
   * @return the list
   */
  public static List<Path> generateSmokeTests(final Path root, final MavenArtifact artifact, final List<OperationPath> operations,
      final OpenAPI openApi) {
    final List<Path> outputs = new ArrayList<>();
    final Path targetPath = root.resolve(artifact.toPath()).resolve("smoke");
    operations.forEach(op -> {
      final List<SmokeTestResponse> responses = new ArrayList<>();
      final Set<String> responseCodes = op.operation().getResponses().keySet();
      responseCodes.forEach(code -> responses.add(
          new SmokeTestResponse(code, OpenApiUtils.getResponseSchemaClasspath(root, artifact, op, code))));
      outputs.addAll(KarateSmokeFeature.save(targetPath, op.operation(),
          OpenApiUtils.getOperationClasspath(root, artifact, op), responses, openApi));
    });
    return outputs;
  }

  /**
   * Generate functional test.
   *
   * @param root the root
   * @param artifact the artifact
   * @param testName the test name
   * @param inlineMocks the inline mocks
   * @param operationsResponses the operations responses
   * @param openApi the open api
   * @return the list
   */
  public static List<Path> generateFunctionalTest(final Path root, final MavenArtifact artifact, final String testName,
      final Boolean inlineMocks, final Map<OperationPath, Set<String>> operationsResponses, final OpenAPI openApi) {
    final List<Path> outputs = new ArrayList<>();
    final Path targetPath = root.resolve(artifact.toPath()).resolve("functional");
    final List<FunctionalTestStep> steps = new ArrayList<>();
    operationsResponses.entrySet().forEach(entry -> {
      final OperationPath operationPath = entry.getKey();
      entry.getValue().forEach(response -> steps.add(new FunctionalTestStep(
          operationPath.operation(), response,
          OpenApiUtils.getOperationClasspath(root, artifact, operationPath),
          OpenApiUtils.getResponseSchemaClasspath(root, artifact, operationPath, response))));
    });
    outputs.addAll(KarateFunctionalFeature.save(targetPath, testName, inlineMocks, steps, openApi));
    return outputs;
  }

  /**
   * Generate mock data.
   *
   * @param root the root
   * @param artifact the artifact
   * @param inlineMocks the inline mocks
   * @param functionalArtifact the functional artifact
   * @param testName the test name
   * @param operationsResponses the operations responses
   * @param openApi the open api
   * @return the list
   */
  public static List<Path> generateMockData(final Path root, final MavenArtifact artifact,
      final Boolean inlineMocks, final MavenArtifact functionalArtifact, final String testName,
      final Map<OperationPath, Set<String>> operationsResponses,
      final OpenAPI openApi) {
    final Path targetPath = OpenApiUtils.getMockDataTargetPath(root, artifact, inlineMocks, functionalArtifact, testName);
    final List<Path> outputs = new ArrayList<>();
    operationsResponses.entrySet().forEach(entry -> {
      final OperationPath operationPath = entry.getKey();
      entry.getValue().forEach(response -> outputs.add(KarateMockData.save(targetPath, operationPath, response, openApi)));
    });
    return outputs;
  }

}
