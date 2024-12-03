package dev.inditex.karate.openapi.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.inditex.karate.openapi.OpenApiGeneratorRuntimeException;
import dev.inditex.karate.openapi.data.OpenApiParser.OperationPath;

import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * The Class KarateOperation.
 */
@Slf4j
public class KarateOperation extends KarateWriter {

  /** The Constant TEMPLATE. */
  protected static final String TEMPLATE = "operation-feature";

  /**
   * Instantiates a new karate operation.
   */
  protected KarateOperation() {
  }

  /**
   * Process template.
   *
   * @param artifact the artifact
   * @param operationPath the operation path
   * @return the string
   */
  static String processTemplate(final MavenArtifact artifact, final OperationPath operationPath) {
    final var resolver = new ClassLoaderTemplateResolver();
    resolver.setTemplateMode(TemplateMode.TEXT);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setPrefix("/open-api-generator/");
    final String path = operationPath.path();
    resolver.setSuffix(Constants.TEMPLATE_EXTENSION);

    final var context = new Context();
    context.setVariable("operation", operationPath.operation());
    context.setVariable("method", operationPath.method());
    context.setVariable("artifactNameUrl", artifact.toApiUrl());
    context.setVariable("pathVars", pathToParams(path));

    final var templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(resolver);

    return templateEngine.process(TEMPLATE, context);
  }

  /**
   * Save.
   *
   * @param root the root
   * @param target the target
   * @param artifact the artifact
   * @param operationPath the operation path
   * @param responseSchemas the response schemas
   * @return the list
   */
  @SuppressWarnings("rawtypes")
  public static List<Path> save(final Path root, final Path target, final MavenArtifact artifact, final OperationPath operationPath,
      final Map<String, Schema> responseSchemas) {
    final List<Path> outputs = new ArrayList<>();
    try {
      // Generate Feature
      final String feature = processTemplate(artifact, operationPath);
      final Path featureOutput = target.resolve(operationPath.operation().getOperationId() + ".feature");
      uncheckedDirExists(featureOutput.getParent());
      Files.writeString(featureOutput, feature);
      outputs.add(featureOutput);
      // Generate Schemas
      final OpenApiSchemaParser schemaBuilder = new OpenApiSchemaParser();
      responseSchemas.entrySet().forEach(e -> {
        final String code = e.getKey();
        final Schema schema = e.getValue();
        final KarateSchema karateSchema = schemaBuilder.build(schema);
        final List<Path> schemaOutputs = karateSchema.writeToPath(root, target, operationPath.operation().getOperationId(), code);
        outputs.addAll(schemaOutputs);
      });

    } catch (final IOException e) {
      log.error("KarateFunctionalFeature.save() Exception {}", e);
      throw new OpenApiGeneratorRuntimeException(e);
    }
    return outputs;
  }

  /**
   * Path to params.
   *
   * @param path the path
   * @return the string
   */
  public static String pathToParams(final String path) {
    return ("'" + path + "'").replace("{", "', req.params.").replace("}", ", '").replace(", ''", "");
  }
}
