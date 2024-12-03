package dev.inditex.karate.openapi.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import dev.inditex.karate.openapi.data.KarateFunctionalFeature.FunctionalTestStep;
import dev.inditex.karate.openapi.data.KarateSmokeFeature.SmokeTestResponse;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

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
      final Path operationFolder = getOperationFolder(root, artifact, operationPath);
      outputs.addAll(KarateOperation.save(root, operationFolder, artifact, operationPath, getResponseSchemas(operationPath)));
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
          new SmokeTestResponse(code, getResponseSchemaClasspath(root, artifact, op, code))));
      outputs.addAll(KarateSmokeFeature.save(targetPath, op.operation(),
          getOperationClasspath(root, artifact, op), responses, openApi));
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
          getOperationClasspath(root, artifact, operationPath),
          getResponseSchemaClasspath(root, artifact, operationPath, response))));
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
    final Path targetPath = getMockDataTargetPath(root, artifact, inlineMocks, functionalArtifact, testName);
    final List<Path> outputs = new ArrayList<>();
    operationsResponses.entrySet().forEach(entry -> {
      final OperationPath operationPath = entry.getKey();
      entry.getValue().forEach(response -> outputs.add(KarateMockData.save(targetPath, operationPath, response, openApi)));
    });
    return outputs;
  }

  /**
   * Gets the operation classpath.
   *
   * @param root the root
   * @param artifact the artifact
   * @param operationPath the operation path
   * @return the operation classpath
   */
  public static String getOperationClasspath(final Path root, final MavenArtifact artifact, final OperationPath operationPath) {
    final Path operationFolder = getOperationFolder(root, artifact, operationPath);
    final Path operationFile = operationFolder.resolve(operationPath.operation().getOperationId() + ".feature");
    return root.relativize(operationFile).toString().replace("\\", "/");
  }

  /**
   * Gets the response schema classpath.
   *
   * @param root the root
   * @param artifact the artifact
   * @param operationPath the operation path
   * @param code the code
   * @return the response schema classpath
   */
  public static String getResponseSchemaClasspath(final Path root, final MavenArtifact artifact, final OperationPath operationPath,
      final String code) {
    final Path operationFolder = getOperationFolder(root, artifact, operationPath);
    return KarateSchema.getSchemaClasspath(root, operationFolder, operationPath.operation().getOperationId(), code);
  }

  /**
   * Gets the response schema.
   *
   * @param operation the operation
   * @param code the code
   * @return the response schema
   */
  @SuppressWarnings("rawtypes")
  public static Schema getResponseSchema(final Operation operation, final String code) {
    if (operation.getResponses() != null
        && operation.getResponses().get(code) != null
        && operation.getResponses().get(code).getContent() != null) {
      final Optional<MediaType> mediaType = operation.getResponses().get(code).getContent().values().stream().findFirst();
      if (mediaType.isPresent() && mediaType.get().getSchema() != null) {
        return mediaType.get().getSchema();
      }
    }
    return null;
  }

  /**
   * Gets the request schema.
   *
   * @param operation the operation
   * @return the request schema
   */
  @SuppressWarnings("rawtypes")
  public static Schema getRequestSchema(final Operation operation) {
    if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
      final Optional<MediaType> mediaType =
          operation.getRequestBody().getContent().values().stream().findFirst();
      if (mediaType.isPresent() && mediaType.get().getSchema() != null) {
        return mediaType.get().getSchema();
      }
    }
    return null;
  }

  /**
   * Gets the mock data target path.
   *
   * @param root the root
   * @param artifact the artifact
   * @param inlineMocks the inline mocks
   * @param functionalArtifact the functional artifact
   * @param testName the test name
   * @return the mock data target path
   */
  protected static Path getMockDataTargetPath(final Path root, final MavenArtifact artifact, final Boolean inlineMocks,
      final MavenArtifact functionalArtifact, final String testName) {
    // PATH: mocks/templates/standalone/ARTIFACT_ID/TAG/XXXX_operationId_XXX.yml"
    Path targetPath = root.resolve("mocks/templates/standalone").resolve(artifact.artifactIdToPath());
    if (Boolean.TRUE.equals(inlineMocks)) {
      // PATH: <FUNCTIONAL_TEST_PATH>/mocks/ARTIFACT_ID/TAG/XXXX_operationId_XXX.yml"
      targetPath = root.resolve(functionalArtifact.toPath()).resolve("functional").resolve(testName).resolve("mocks")
          .resolve(artifact.artifactIdToPath());
    }
    return targetPath;
  }

  /**
   * Gets the operation folder.
   *
   * @param root the root
   * @param artifact the artifact
   * @param operationPath the operation path
   * @return the operation folder
   */
  protected static Path getOperationFolder(final Path root, final MavenArtifact artifact, final OperationPath operationPath) {
    final Path targetPath = root.resolve("apis").resolve(artifact.toPath());
    final String tag = operationPath.operation().getTags() != null ? operationPath.operation().getTags().get(0) : "NoTag";
    return targetPath.resolve(tag).resolve(operationPath.operation().getOperationId());
  }

  /**
   * Gets the response schemas.
   *
   * @param operationPath the operation path
   * @return the response schemas
   */
  @SuppressWarnings("rawtypes")
  protected static Map<String, Schema> getResponseSchemas(final OperationPath operationPath) {
    final Map<String, Schema> schemas = new TreeMap<>();
    operationPath.operation().getResponses().entrySet().forEach(e -> {
      final String returnCode = e.getKey();
      if (e.getValue().getContent() != null) {
        final Optional<MediaType> mediaType = e.getValue().getContent().values().stream().findFirst();
        if (mediaType.isPresent() && mediaType.get().getSchema() != null) {
          schemas.put(returnCode, mediaType.get().getSchema());
        }
      }
    });
    return schemas;
  }

}
