package dev.inditex.karate.openapi.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import dev.inditex.karate.openapi.OpenApiGeneratorRuntimeException;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.EmailSchema;
import io.swagger.v3.oas.models.media.FileSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.PasswordSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

/**
 * The Class OpenApiSchemaParser.
 */
public class OpenApiSchemaParser {

  /** The Constant KARATE_SCHEMA_OPTIONAL. */
  protected static final String KARATE_SCHEMA_OPTIONAL = "##";

  /** The Constant KARATE_SCHEMA_OPTIONAL_NESTED. */
  protected static final String KARATE_SCHEMA_OPTIONAL_NESTED = "^^";

  /** The Constant KARATE_SCHEMA_REQUIRED. */
  protected static final String KARATE_SCHEMA_REQUIRED = "#";

  /** The Constant KARATE_SCHEMA_TYPE_OBJECT. */
  protected static final String KARATE_SCHEMA_TYPE_OBJECT = "object";

  /** The Constant KARATE_SCHEMA_TYPE_BOOLEAN. */
  protected static final String KARATE_SCHEMA_TYPE_BOOLEAN = "boolean";

  /** The Constant KARATE_SCHEMA_TYPE_UUID. */
  protected static final String KARATE_SCHEMA_TYPE_UUID = "uuid";

  /** The Constant KARATE_SCHEMA_TYPE_NUMBER. */
  protected static final String KARATE_SCHEMA_TYPE_NUMBER = "number";

  /** The Constant KARATE_SCHEMA_TYPE_STRING. */
  protected static final String KARATE_SCHEMA_TYPE_STRING = "string";

  /** The Constant SIMPLE_TYPES. */
  @SuppressWarnings("rawtypes")
  protected static final Map<Class, String> SIMPLE_TYPES;

  static {
    @SuppressWarnings("rawtypes")
    final HashMap<Class, String> map = new HashMap<>();
    map.put(StringSchema.class, KARATE_SCHEMA_TYPE_STRING);
    map.put(IntegerSchema.class, KARATE_SCHEMA_TYPE_NUMBER);
    map.put(NumberSchema.class, KARATE_SCHEMA_TYPE_NUMBER);
    map.put(UUIDSchema.class, KARATE_SCHEMA_TYPE_UUID);
    map.put(EmailSchema.class, KARATE_SCHEMA_TYPE_STRING);
    map.put(PasswordSchema.class, KARATE_SCHEMA_TYPE_STRING);
    map.put(FileSchema.class, KARATE_SCHEMA_TYPE_STRING);
    map.put(DateSchema.class, KARATE_SCHEMA_TYPE_STRING);
    map.put(DateTimeSchema.class, KARATE_SCHEMA_TYPE_STRING);
    map.put(BooleanSchema.class, KARATE_SCHEMA_TYPE_BOOLEAN);
    map.put(ByteArraySchema.class, KARATE_SCHEMA_TYPE_STRING);
    map.put(MapSchema.class, KARATE_SCHEMA_TYPE_OBJECT);
    map.put(ComposedSchema.class, KARATE_SCHEMA_TYPE_OBJECT);
    SIMPLE_TYPES = Map.copyOf(map);
  }

  /** The object definitions. */
  Map<String, Object> objectDefinitions = new HashMap<>();

  /** The root. */
  Object root = new Object();

  /** The link id. */
  String linkId;

  /**
   * Instantiates a new open api schema parser.
   */
  public OpenApiSchemaParser() {
    init();
  }

  /**
   * Inits the.
   */
  protected void init() {
    objectDefinitions = new HashMap<>();
    root = new Object();
    linkId = UUID.randomUUID().toString();
  }

  /**
   * Builds the.
   *
   * @param schema the schema
   * @return the karate schema
   */
  @SuppressWarnings("rawtypes")
  public KarateSchema build(final Schema schema) {
    buildSchema(schema);
    final KarateSchema openApiSchemaParserResult = new KarateSchema(root, objectDefinitions, linkId);
    init();
    return openApiSchemaParserResult;
  }

  /**
   * Builds the.
   *
   * @param schema the schema
   * @param required the required
   * @param key the key
   * @return the object
   */
  @SuppressWarnings("rawtypes")
  protected Object build(final Schema schema, final boolean required, final String key) {
    if (schema instanceof final ObjectSchema objectSchema) {
      return processObject(objectSchema, key, required);
    } else if (schema instanceof final ArraySchema arraySchema) {
      return processArray(required, key, arraySchema);
    } else if (schema.getClass().equals(Schema.class)) {
      return processObject(schema, key, required);
    }
    final String simpleType = simpleTypes(schema);
    if (simpleType != null) {
      return karatePrefix(required, isSchemaNullable(schema)) + simpleType;
    }
    throw new OpenApiGeneratorRuntimeException("Not supported " + schema.getClass().getName());
  }

  /**
   * Builds the schema.
   *
   * @param schema the schema
   */
  @SuppressWarnings("rawtypes")
  protected void buildSchema(final Schema schema) {
    root = build(schema, true, "root");
  }

  /**
   * Process array.
   *
   * @param required the required
   * @param key the key
   * @param arraySchema the array schema
   * @return the string
   */
  protected String processArray(final boolean required, final String key, final ArraySchema arraySchema) {
    final boolean nullable = isSchemaNullable(arraySchema);
    if (arraySchema.getItems() instanceof ArraySchema) {
      return karatePrefix(required, nullable) + "[]";
    }
    final Schema<?> items = arraySchema.getItems();
    if (isSimpleType(items)) {
      return karatePrefix(required, nullable) + "[] " + build(items, true, key);
    }
    final String schemaName = addSchema(items, arraySchema.getTitle() == null ? key : arraySchema.getTitle());
    return karatePrefix(required, nullable) + "[] " + linkSchema(schemaName);
  }

  /**
   * Process object.
   *
   * @param objectSchema the object schema
   * @param key the key
   * @param required the required
   * @return the object
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected Object processObject(final Schema objectSchema, final String key, final boolean required) {
    final boolean nullable = isSchemaNullable(objectSchema);
    final String prefix = karatePrefix(required, nullable);
    if (objectSchema.getProperties() == null) {
      return prefix + KARATE_SCHEMA_TYPE_OBJECT;
    }
    if (required && !nullable) {
      if (objectSchema.getTitle() != null && objectDefinitions.containsKey(objectSchema.getTitle())) {
        return prefix + "(" + linkSchema(objectSchema.getTitle()) + ")";
      }
      final Set<String> requiredSet =
          new HashSet<>(Objects.requireNonNullElse((List<String>) objectSchema.getRequired(), Collections.emptyList()));
      final Map<String, Object> result = new HashMap<>();
      ((Map<String, Schema>) objectSchema.getProperties()).forEach((k, v) -> result.put(k, build(v, requiredSet.contains(k), k)));
      return result;
    }
    final String schemaName = addSchema(objectSchema, objectSchema.getTitle() == null ? key : objectSchema.getTitle());
    return required ? prefix + "(" + linkSchema(schemaName) + ")"
        : prefix + "(" + KARATE_SCHEMA_OPTIONAL_NESTED + linkSchema(schemaName) + ")";
  }

  /**
   * Karate prefix.
   *
   * @param required the required
   * @param nullable the nullable
   * @return the string
   */
  protected static String karatePrefix(final boolean required, final boolean nullable) {
    return required && !nullable ? KARATE_SCHEMA_REQUIRED : KARATE_SCHEMA_OPTIONAL;
  }

  /**
   * Checks if is schema nullable.
   *
   * @param schema the schema
   * @return true, if is schema nullable
   */
  @SuppressWarnings("rawtypes")
  protected static boolean isSchemaNullable(final Schema schema) {
    return Boolean.TRUE.equals(schema.getNullable());
  }

  /**
   * Simple types.
   *
   * @param schema the schema
   * @return the string
   */
  @SuppressWarnings("rawtypes")
  protected static String simpleTypes(final Schema schema) {
    final String s = SIMPLE_TYPES.get(schema.getClass());
    if (s == null) {
      if (schema.getClass().equals(Schema.class)) {
        return KARATE_SCHEMA_TYPE_OBJECT; // NOSONAR
      } else if (schema instanceof final ObjectSchema objectSchema && objectSchema.getProperties() == null) {
        return KARATE_SCHEMA_TYPE_OBJECT; // NOSONAR
      }
    }
    return s;
  }

  /**
   * Checks if is simple type.
   *
   * @param schema the schema
   * @return true, if is simple type
   */
  @SuppressWarnings("rawtypes")
  protected static boolean isSimpleType(final Schema schema) {
    return simpleTypes(schema) != null;
  }

  /**
   * Link schema.
   *
   * @param schemaName the schema name
   * @return the string
   */
  protected String linkSchema(final String schemaName) {
    return linkId + schemaName + linkId;
  }

  /**
   * Adds the schema.
   *
   * @param objectSchema the object schema
   * @param key the key
   * @return the string
   */
  @SuppressWarnings("rawtypes")
  protected String addSchema(final Schema objectSchema, final String key) {
    final Object value = build(objectSchema, true, key);
    objectDefinitions.putIfAbsent(key, value);
    return key;
  }
}
