/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.infrastructure.openapi;

import com.intellij.openapi.util.text.StringUtil;
import com.luisppb16.openapi.generator.domain.model.PropertyDefinition;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Infrastructure service that parses OpenAPI specification files into domain models. */
public class OpenApiSpecificationParser {

  public List<SchemaDefinition> parseSpecification(String filePath) {
    ParseOptions options = new ParseOptions();
    options.setResolve(true);
    options.setResolveFully(true);

    SwaggerParseResult result =
        new OpenAPIParser()
            .readLocation(OpenApiLocations.toParserLocation(filePath), null, options);

    if (result.getOpenAPI() == null) {
      String messages =
          result.getMessages() != null ? String.join("; ", result.getMessages()) : "Unknown error";
      throw new IllegalArgumentException("Failed to parse OpenAPI specification: " + messages);
    }

    OpenAPI openAPI = result.getOpenAPI();
    List<SchemaDefinition> schemas = new ArrayList<>();

    Map<String, Schema> components =
        (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null)
            ? openAPI.getComponents().getSchemas()
            : Map.of();

    if (!components.isEmpty()) {
      components.entrySet().stream()
          .map(entry -> convertSchema(entry.getKey(), entry.getValue(), components))
          .forEach(schemas::add);
    }

    if (openAPI.getPaths() != null) {
      Set<String> schemaNames =
          schemas.stream().map(SchemaDefinition::name).collect(Collectors.toSet());
      openAPI.getPaths().entrySet().stream()
          .flatMap(
              pathEntry -> {
                PathItem pathItem = pathEntry.getValue();
                String path = pathEntry.getKey();
                if (pathItem.readOperationsMap() == null) {
                  return Stream.empty();
                }
                return pathItem.readOperationsMap().entrySet().stream()
                    .flatMap(
                        operationEntry -> {
                          PathItem.HttpMethod method = operationEntry.getKey();
                          Operation operation = operationEntry.getValue();
                          if (operation.getRequestBody() == null
                              || operation.getRequestBody().getContent() == null) {
                            return Stream.empty();
                          }
                          return operation.getRequestBody().getContent().values().stream()
                              .filter(
                                  mediaType ->
                                      mediaType.getSchema() != null
                                          && mediaType.getSchema().getProperties() != null)
                              .map(
                                  mediaType -> {
                                    String schemaName =
                                        buildSchemaNameFromPath(method.toString(), path);
                                    return Map.entry(schemaName, mediaType.getSchema());
                                  });
                        });
              })
          .filter(entry -> schemaNames.add(entry.getKey()))
          .map(entry -> convertSchema(entry.getKey(), entry.getValue(), components))
          .forEach(schemas::add);
    }

    schemas.sort(Comparator.comparing(SchemaDefinition::name));
    return schemas;
  }

  private SchemaDefinition convertSchema(
      String name, Schema<?> schema, Map<String, Schema> components) {
    boolean isArray = isArraySchema(schema);
    Schema<?> targetSchema = isArray ? itemsOf(schema) : schema;
    if (targetSchema != null && targetSchema.getAllOf() != null) {
      targetSchema = mergeAllOf(targetSchema, components);
    }
    List<PropertyDefinition> properties =
        (targetSchema != null && targetSchema.getProperties() != null)
            ? convertProperties(targetSchema, components)
            : new ArrayList<>();

    String description = schema != null ? schema.getDescription() : null;
    return new SchemaDefinition(name, description, properties, isArray);
  }

  /** Resolves a lingering {@code #/components/schemas/...} reference against the components map. */
  private Schema<?> resolveRef(Schema<?> schema, Map<String, Schema> components) {
    if (schema == null || schema.get$ref() == null) return schema;
    String ref = schema.get$ref();
    String prefix = "#/components/schemas/";
    if (!ref.startsWith(prefix)) return schema;
    Schema<?> resolved = components.get(ref.substring(prefix.length()));
    return resolved != null ? resolved : schema;
  }

  private boolean isArraySchema(Schema<?> schema) {
    return schema instanceof ArraySchema || "array".equals(resolveType(schema));
  }

  private Schema<?> itemsOf(Schema<?> schema) {
    return schema != null ? schema.getItems() : null;
  }

  /** Resolves the effective type, covering both OpenAPI 3.0 (type) and 3.1 (types set). */
  private String resolveType(Schema<?> schema) {
    if (schema == null) return null;
    if (schema.getType() != null) return schema.getType();
    if (schema.getTypes() != null && !schema.getTypes().isEmpty()) {
      return schema.getTypes().stream().filter(t -> !"null".equals(t)).findFirst().orElse(null);
    }
    return null;
  }

  /** Flattens an allOf composition into a single schema with merged properties and required. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private Schema<?> mergeAllOf(Schema<?> schema, Map<String, Schema> components) {
    Schema merged = new Schema<>();
    merged.setType("object");
    merged.setDescription(schema.getDescription());
    Map<String, Schema> mergedProperties = new LinkedHashMap<>();
    List<String> mergedRequired = new ArrayList<>();

    List<Schema> parts = new ArrayList<>();
    parts.add(schema);
    if (schema.getAllOf() != null) parts.addAll(schema.getAllOf());

    for (Schema part : parts) {
      if (part == null) continue;
      Schema target = resolveRef(part, components);
      if (target.getAllOf() != null && target != schema) {
        target = mergeAllOf(target, components);
      }
      if (target.getProperties() != null) mergedProperties.putAll(target.getProperties());
      if (target.getRequired() != null) mergedRequired.addAll(target.getRequired());
    }

    merged.setProperties(mergedProperties);
    merged.setRequired(mergedRequired);
    return merged;
  }

  private List<PropertyDefinition> convertProperties(
      Schema<?> targetSchema, Map<String, Schema> components) {
    Set<String> requiredFields =
        targetSchema.getRequired() != null
            ? new HashSet<>(targetSchema.getRequired())
            : new HashSet<>();

    return targetSchema.getProperties().entrySet().stream()
        .map(
            entry ->
                convertProperty(
                    entry.getKey(),
                    entry.getValue(),
                    requiredFields.contains(entry.getKey()),
                    components))
        .toList();
  }

  private PropertyDefinition convertProperty(
      String name, Schema<?> schema, boolean required, Map<String, Schema> components) {
    if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
      schema = mergeAllOf(schema, components);
    }

    String type = resolveType(schema);
    String format = schema.getFormat();
    String description = schema.getDescription();
    Object exampleValue = schema.getExample();

    List<String> enumValues =
        schema.getEnum() != null
            ? schema.getEnum().stream().filter(Objects::nonNull).map(String::valueOf).toList()
            : new ArrayList<>();

    PropertyDefinition items = null;
    Schema<?> itemsSchema = itemsOf(schema);
    if ("array".equals(type) && itemsSchema != null) {
      items = convertProperty("items", itemsSchema, false, components);
    }

    Map<String, PropertyDefinition> nestedProperties = new LinkedHashMap<>();
    if ("object".equals(type) && schema.getProperties() != null) {
      Set<String> nestedRequired =
          schema.getRequired() != null ? new HashSet<>(schema.getRequired()) : new HashSet<>();

      nestedProperties =
          schema.getProperties().entrySet().stream()
              .collect(
                  LinkedHashMap::new,
                  (map, entry) ->
                      map.put(
                          entry.getKey(),
                          convertProperty(
                              entry.getKey(),
                              entry.getValue(),
                              nestedRequired.contains(entry.getKey()),
                              components)),
                  LinkedHashMap::putAll);
    }

    return PropertyDefinition.builder()
        .name(name)
        .type(type)
        .format(format)
        .description(description)
        .required(required)
        .enumValues(enumValues)
        .items(items)
        .nestedProperties(nestedProperties)
        .exampleValue(exampleValue)
        .minimum(schema.getMinimum())
        .maximum(schema.getMaximum())
        .minLength(schema.getMinLength())
        .maxLength(schema.getMaxLength())
        .pattern(schema.getPattern())
        .minItems(schema.getMinItems())
        .maxItems(schema.getMaxItems())
        .oneOfVariants(convertVariants(name, schema.getOneOf(), components))
        .anyOfVariants(convertVariants(name, schema.getAnyOf(), components))
        .build();
  }

  private List<PropertyDefinition> convertVariants(
      String name, List<Schema> variants, Map<String, Schema> components) {
    if (variants == null || variants.isEmpty()) return List.of();
    return variants.stream()
        .filter(Objects::nonNull)
        .map(variant -> convertProperty(name, resolveRef(variant, components), false, components))
        .toList();
  }

  private String buildSchemaNameFromPath(String method, String path) {
    StringBuilder name = new StringBuilder(method.toLowerCase());
    Stream.of(path.split("/"))
        .filter(part -> !part.isEmpty() && !part.startsWith("{"))
        .map(StringUtil::capitalize)
        .forEach(name::append);
    name.append("RequestBody");

    return name.toString();
  }
}
