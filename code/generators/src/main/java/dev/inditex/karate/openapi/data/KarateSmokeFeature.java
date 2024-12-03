package dev.inditex.karate.openapi.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dev.inditex.karate.openapi.OpenApiGeneratorRuntimeException;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * The Class KarateSmokeFeature.
 */
@Slf4j
public class KarateSmokeFeature extends KarateWriter {

  /**
   * The Record SmokeTestResponse.
   *
   * @param statusCode the status code
   * @param responseSchemaClassPath the response schema class path
   */
  public record SmokeTestResponse(String statusCode, String responseSchemaClassPath) {
    // Record
  }

  /** The Constant TEMPLATE. */
  protected static final String TEMPLATE = "smoke-test-feature";

  /**
   * Instantiates a new karate smoke feature.
   */
  protected KarateSmokeFeature() {
  }

  /**
   * Process template.
   *
   * @param operation the operation
   * @param operationFeatureClassPath the operation feature class path
   * @param responses the responses
   * @return the string
   */
  public static String processTemplate(final Operation operation, final String operationFeatureClassPath,
      final List<SmokeTestResponse> responses) {
    final var resolver = new ClassLoaderTemplateResolver();
    resolver.setTemplateMode(TemplateMode.TEXT);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setPrefix("/open-api-generator/");
    resolver.setSuffix(Constants.TEMPLATE_EXTENSION);

    final var context = new Context();
    context.setVariable("operation", operation);
    context.setVariable("responses", responses);
    context.setVariable("operationFeatureClassPath", operationFeatureClassPath);
    final var templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(resolver);

    return templateEngine.process(TEMPLATE, context);
  }

  /**
   * Save.
   *
   * @param target the target
   * @param operation the operation
   * @param operationFeatureClassPath the operation feature class path
   * @param responses the responses
   * @param openApi the open api
   * @return the list
   */
  public static List<Path> save(final Path target, final Operation operation, final String operationFeatureClassPath,
      final List<SmokeTestResponse> responses, final OpenAPI openApi) {
    final List<Path> outputs = new ArrayList<>();
    try {
      // Generate Feature
      final String feature = processTemplate(operation, operationFeatureClassPath, responses);
      final String tag = operation.getTags() != null ? operation.getTags().get(0) : "NoTag";
      final Path outputFolder = target.resolve(tag).resolve(operation.getOperationId());
      final Path featureOutput = outputFolder.resolve(operation.getOperationId() + ".feature");
      uncheckedDirExists(featureOutput.getParent());
      Files.writeString(featureOutput, feature);
      outputs.add(featureOutput);
      // Generate Test Data
      responses.forEach(r -> outputs.add(KarateTestData.save(outputFolder, operation, r.statusCode, r.responseSchemaClassPath, openApi)));
    } catch (final IOException e) {
      log.error("SmokeTestResponse.save() Exception {}", e);
      throw new OpenApiGeneratorRuntimeException(e);
    }
    return outputs;
  }
}
