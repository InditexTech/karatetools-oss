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
 * The Class KarateFunctionalFeature.
 */
@Slf4j
public class KarateFunctionalFeature extends KarateWriter {

  /**
   * The Record FunctionalTestStep.
   *
   * @param operation the operation
   * @param statusCode the status code
   * @param operationFeatureClassPath the operation feature class path
   * @param responseSchemaClassPath the response schema class path
   */
  public record FunctionalTestStep(Operation operation, String statusCode, String operationFeatureClassPath,
      String responseSchemaClassPath) {
    // Record
  }

  /** The Constant TEMPLATE. */
  protected static final String TEMPLATE = "functional-test-feature";

  /**
   * Instantiates a new karate functional feature.
   */
  protected KarateFunctionalFeature() {
  }

  /**
   * Process template.
   *
   * @param testName the test name
   * @param inlineMocks the inline mocks
   * @param steps the steps
   * @return the string
   */
  public static String processTemplate(final String testName, final Boolean inlineMocks, final List<FunctionalTestStep> steps) {
    final var resolver = new ClassLoaderTemplateResolver();
    resolver.setTemplateMode(TemplateMode.TEXT);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setPrefix("/open-api-generator/");
    resolver.setSuffix(Constants.TEMPLATE_EXTENSION);

    final var context = new Context();
    context.setVariable("testName", testName);
    context.setVariable("inlineMocks", inlineMocks);
    context.setVariable("steps", steps);
    context.setVariable("operationIds", steps.stream().map(f -> f.operation.getOperationId()).distinct().toList());
    final var templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(resolver);

    return templateEngine.process(TEMPLATE, context);
  }

  /**
   * Save.
   *
   * @param target the target
   * @param testName the test name
   * @param inlineMocks the inline mocks
   * @param steps the steps
   * @param openApi the open api
   * @return the list
   */
  public static List<Path> save(final Path target, final String testName, final Boolean inlineMocks, final List<FunctionalTestStep> steps,
      final OpenAPI openApi) {
    final List<Path> outputs = new ArrayList<>();
    try {
      // Generate Feature
      final String feature = processTemplate(testName, inlineMocks, steps);
      final Path outputFolder = target.resolve(testName);
      final Path featureOutput = outputFolder.resolve(testName + ".feature");
      uncheckedDirExists(featureOutput.getParent());
      Files.writeString(featureOutput, feature);
      outputs.add(featureOutput);
      // Generate Test Data
      steps.forEach(s -> outputs.add(KarateTestData.save(outputFolder, s.operation, s.statusCode, s.responseSchemaClassPath, openApi)));
    } catch (final IOException e) {
      log.error("KarateFunctionalFeature.save() Exception {}", e);
      throw new OpenApiGeneratorRuntimeException(e);
    }
    return outputs;
  }
}
