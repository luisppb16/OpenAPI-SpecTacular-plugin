/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.infrastructure.openapi;

import com.openapi.generator.domain.model.PropertyDefinition;
import com.openapi.generator.domain.model.SchemaDefinition;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/** Infrastructure service that parses OpenAPI specification files into domain models. */
public class OpenApiSpecificationParser {

  public List<SchemaDefinition> parseSpecification(String filePath) {
    ParseOptions options = new ParseOptions();
    options.setResolve(true);
    options.setResolveFully(true);

    SwaggerParseResult result = new OpenAPIParser().readLocation(filePath, null, options);

    if (result.getOpenAPI() == null) {
      String messages =
          result.getMessages() != null ? String.join("; ", result.getMessages()) : "Unknown error";
      throw new IllegalArgumentException("Failed to parse OpenAPI specification: " + messages);
    }

    OpenAPI openAPI = result.getOpenAPI();
    List<SchemaDefinition> schemas = new ArrayList<>();

    if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
      openAPI.getComponents().getSchemas().entrySet().stream()
          .map(entry -> convertSchema(entry.getKey(), entry.getValue()))
          .forEach(schemas::add);
    }

    if (openAPI.getPaths() != null) {
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
          .filter(entry -> schemas.stream().noneMatch(s -> s.name().equals(entry.getKey())))
          .map(entry -> convertSchema(entry.getKey(), entry.getValue()))
          .forEach(schemas::add);
    }

    schemas.sort(Comparator.comparing(SchemaDefinition::name));
    return schemas;
  }

  private SchemaDefinition convertSchema(String name, Schema<?> schema) {
    boolean isArray = schema instanceof ArraySchema;
    Schema<?> targetSchema =
        (schema instanceof ArraySchema arraySchema) ? arraySchema.getItems() : schema;
    List<PropertyDefinition> properties =
        (targetSchema != null && targetSchema.getProperties() != null)
            ? convertProperties(targetSchema)
            : new ArrayList<>();

    String description = schema != null ? schema.getDescription() : null;
    return new SchemaDefinition(name, description, properties, isArray);
  }

  private List<PropertyDefinition> convertProperties(Schema<?> targetSchema) {
    Set<String> requiredFields =
        targetSchema.getRequired() != null
            ? new HashSet<>(targetSchema.getRequired())
            : new HashSet<>();

    return targetSchema.getProperties().entrySet().stream()
        .map(
            entry ->
                convertProperty(
                    entry.getKey(), entry.getValue(), requiredFields.contains(entry.getKey())))
        .toList();
  }

  private PropertyDefinition convertProperty(String name, Schema<?> schema, boolean required) {
    String type = schema.getType();
    String format = schema.getFormat();
    String description = schema.getDescription();
    Object exampleValue = schema.getExample();

    List<String> enumValues =
        schema.getEnum() != null
            ? schema.getEnum().stream().filter(Objects::nonNull).map(String::valueOf).toList()
            : new ArrayList<>();

    PropertyDefinition items = null;
    if (schema instanceof ArraySchema arraySchema && arraySchema.getItems() != null) {
      items = convertProperty("items", arraySchema.getItems(), false);
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
                              nestedRequired.contains(entry.getKey()))),
                  LinkedHashMap::putAll);
    }

    return new PropertyDefinition(
        name,
        type,
        format,
        description,
        required,
        enumValues,
        items,
        nestedProperties,
        exampleValue);
  }

  private String buildSchemaNameFromPath(String method, String path) {
    StringBuilder name = new StringBuilder(method.toLowerCase());
    Stream.of(path.split("/"))
        .filter(part -> !part.isEmpty() && !part.startsWith("{"))
        .map(this::capitalize)
        .forEach(name::append);
    name.append("RequestBody");

    return name.toString();
  }

  private String capitalize(String s) {
    return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
}
