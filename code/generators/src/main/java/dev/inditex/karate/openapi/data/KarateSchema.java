package dev.inditex.karate.openapi.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * The Class KarateSchema.
 */
@RequiredArgsConstructor
public class KarateSchema extends KarateWriter {

  /** The root. */
  private final Object root;

  /** The object definitions. */
  private final Map<String, Object> objectDefinitions;

  /** The link id. */
  private final String linkId;

  /** The json mapper. */
  private final ObjectWriter jsonMapper = new ObjectMapper().writerWithDefaultPrettyPrinter();

  /** The yaml mapper. */
  private final ObjectWriter yamlMapper =
      new ObjectMapper(YAMLFactory.builder().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).build())
          .writerWithDefaultPrettyPrinter();

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  @SneakyThrows
  public String toString() {
    final StringBuilder result = new StringBuilder();
    for (var entry : objectDefinitions.entrySet()) {
      result.append("* def %s = %n\"\"\"%n %s %n\"\"\"%n".formatted(entry.getKey(), linkInFile(entry.getValue())));
    }
    result.append("* match result == %n\"\"\"%n %s %n\"\"\"%n".formatted(linkInFile(root)));
    return result.toString();
  }

  /**
   * Link in file.
   *
   * @param root1 the root 1
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  protected String linkInFile(final Object root1) throws JsonProcessingException {
    return link(root1, "$1", jsonMapper);
  }

  /**
   * Write to path.
   *
   * @param basePath the base path
   * @param operationFolder the operation folder
   * @param rootName the root name
   * @return the list
   */
  @SneakyThrows
  public List<Path> writeToPath(final Path basePath, final Path operationFolder, final String rootName) {
    return writeToPath(basePath, operationFolder, rootName, null);
  }

  /**
   * Write to path.
   *
   * @param basePath the base path
   * @param operationFolder the operation folder
   * @param rootName the root name
   * @param code the code
   * @return the list
   */
  @SneakyThrows
  public List<Path> writeToPath(final Path basePath, final Path operationFolder, final String rootName, final String code) {
    final List<Path> outputs = new ArrayList<>();
    final String schemaSuffix = (code != null ? "_" + code : "") + ".schema.yml";
    final Path schemaFolder = operationFolder.resolve("schema");
    uncheckedDirExists(schemaFolder);
    final String relativize = basePath.relativize(schemaFolder).toString();
    for (final var entry : objectDefinitions.entrySet()) {
      final Path schemaOutput = Files.writeString(schemaFolder.resolve(entry.getKey() + schemaSuffix),
          linkExternalFile(relativize, schemaSuffix, entry.getValue()));
      outputs.add(schemaOutput);
    }
    final Path rootSchemaOutput =
        Files.writeString(schemaFolder.resolve(rootName + schemaSuffix), linkExternalFile(relativize, schemaSuffix, root));
    outputs.add(rootSchemaOutput);
    return outputs;
  }

  /**
   * Gets the schema classpath.
   *
   * @param basePath the base path
   * @param operationFolder the operation folder
   * @param rootName the root name
   * @param code the code
   * @return the schema classpath
   */
  public static String getSchemaClasspath(final Path basePath, final Path operationFolder, final String rootName, final String code) {
    final String schemaSuffix = (code != null ? "_" + code : "") + ".schema.yml";
    final Path schemaFile = operationFolder.resolve("schema").resolve(rootName + schemaSuffix);
    final String relativize = basePath.relativize(schemaFile).toString();
    return relativize.replace("\\", "/");
  }

  /**
   * Link external file.
   *
   * @param prefix the prefix
   * @param suffix the suffix
   * @param entry the entry
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  protected String linkExternalFile(final String prefix, final String suffix, final Object entry) throws JsonProcessingException {
    return link(entry, "read('classpath:" + prefix.replace("\\", "/") + "/$1" + suffix + "')", yamlMapper);
  }

  /**
   * Link.
   *
   * @param value the value
   * @param replacement the replacement
   * @param mapper the mapper
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  protected String link(final Object value, final String replacement, final ObjectWriter mapper) throws JsonProcessingException {
    final String s = mapper.writeValueAsString(value);
    final Pattern compile = Pattern.compile(Pattern.quote(linkId) + "(\\w+)" + Pattern.quote(linkId));
    return compile.matcher(s).replaceAll(replacement);
  }
}
