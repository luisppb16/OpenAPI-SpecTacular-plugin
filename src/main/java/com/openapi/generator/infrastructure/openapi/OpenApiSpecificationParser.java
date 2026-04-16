package com.openapi.generator.infrastructure.openapi;

import com.openapi.generator.domain.model.PropertyDefinition;
import com.openapi.generator.domain.model.SchemaDefinition;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.*;

/**
 * Infrastructure service that parses OpenAPI specification files into domain models.
 */
public class OpenApiSpecificationParser {

    public List<SchemaDefinition> parseSpecification(String filePath) {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(true);

        SwaggerParseResult result = new OpenAPIParser().readLocation(filePath, null, options);

        if (result.getOpenAPI() == null) {
            String messages = result.getMessages() != null ? String.join("; ", result.getMessages()) : "Unknown error";
            throw new IllegalArgumentException("Failed to parse OpenAPI specification: " + messages);
        }

        OpenAPI openAPI = result.getOpenAPI();
        List<SchemaDefinition> schemas = new ArrayList<>();

        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            for (Map.Entry<String, Schema> entry : openAPI.getComponents().getSchemas().entrySet()) {
                SchemaDefinition schemaDef = convertSchema(entry.getKey(), entry.getValue());
                schemas.add(schemaDef);
            }
        }

        if (openAPI.getPaths() != null) {
            openAPI.getPaths().forEach((path, pathItem) -> {
                if (pathItem.readOperationsMap() != null) {
                    pathItem.readOperationsMap().forEach((method, operation) -> {
                        if (operation.getRequestBody() != null
                                && operation.getRequestBody().getContent() != null) {
                            operation.getRequestBody().getContent().forEach((mediaType, mediaTypeObj) -> {
                                if (mediaTypeObj.getSchema() != null
                                        && mediaTypeObj.getSchema().getProperties() != null) {
                                    String schemaName = buildSchemaNameFromPath(method.toString(), path);
                                    if (schemas.stream().noneMatch(s -> s.getName().equals(schemaName))) {
                                        schemas.add(convertSchema(schemaName, mediaTypeObj.getSchema()));
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }

        schemas.sort(Comparator.comparing(SchemaDefinition::getName));
        return schemas;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SchemaDefinition convertSchema(String name, Schema<?> schema) {
        boolean isArray = schema instanceof ArraySchema;
        List<PropertyDefinition> properties = new ArrayList<>();

        Schema<?> targetSchema = isArray ? ((ArraySchema) schema).getItems() : schema;

        if (targetSchema != null && targetSchema.getProperties() != null) {
            Set<String> requiredFields = new HashSet<>();
            if (targetSchema.getRequired() != null) {
                requiredFields.addAll(targetSchema.getRequired());
            }

            for (Map.Entry<String, Schema> entry : ((Map<String, Schema>) targetSchema.getProperties()).entrySet()) {
                PropertyDefinition prop = convertProperty(entry.getKey(), entry.getValue(),
                        requiredFields.contains(entry.getKey()));
                properties.add(prop);
            }
        }

        String description = schema.getDescription();
        return new SchemaDefinition(name, description, properties, isArray);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private PropertyDefinition convertProperty(String name, Schema<?> schema, boolean required) {
        String type = schema.getType();
        String format = schema.getFormat();
        String description = schema.getDescription();
        Object exampleValue = schema.getExample();

        List<String> enumValues = new ArrayList<>();
        if (schema.getEnum() != null) {
            for (Object enumVal : schema.getEnum()) {
                if (enumVal != null) {
                    enumValues.add(String.valueOf(enumVal));
                }
            }
        }

        PropertyDefinition items = null;
        if (schema instanceof ArraySchema arraySchema && arraySchema.getItems() != null) {
            items = convertProperty("items", arraySchema.getItems(), false);
        }

        Map<String, PropertyDefinition> nestedProperties = new LinkedHashMap<>();
        if ("object".equals(type) && schema.getProperties() != null) {
            Set<String> nestedRequired = new HashSet<>();
            if (schema.getRequired() != null) {
                nestedRequired.addAll(schema.getRequired());
            }
            for (Map.Entry<String, Schema> entry : ((Map<String, Schema>) schema.getProperties()).entrySet()) {
                nestedProperties.put(entry.getKey(),
                        convertProperty(entry.getKey(), entry.getValue(),
                                nestedRequired.contains(entry.getKey())));
            }
        }

        return new PropertyDefinition(name, type, format, description, required,
                enumValues, items, nestedProperties, exampleValue);
    }

    private String buildSchemaNameFromPath(String method, String path) {
        String[] parts = path.split("/");
        StringBuilder name = new StringBuilder(method.toLowerCase());
        for (String part : parts) {
            if (!part.isEmpty() && !part.startsWith("{")) {
                name.append(capitalize(part));
            }
        }
        name.append("RequestBody");
        return name.toString();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
