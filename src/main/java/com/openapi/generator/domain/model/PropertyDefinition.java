package com.openapi.generator.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a property within an OpenAPI schema.
 * This is a Value Object in the DDD structure.
 */
public final class PropertyDefinition {
    private final String name;
    private final String type;
    private final String format;
    private final String description;
    private final boolean required;
    private final List<String> enumValues;
    private final PropertyDefinition items;
    private final Map<String, PropertyDefinition> nestedProperties;
    private final Object exampleValue;

    public PropertyDefinition(String name, String type, String format, String description,
                               boolean required, List<String> enumValues, PropertyDefinition items,
                               Map<String, PropertyDefinition> nestedProperties, Object exampleValue) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = type != null ? type : "string";
        this.format = format;
        this.description = description;
        this.required = required;
        this.enumValues = enumValues != null ? List.copyOf(enumValues) : List.of();
        this.items = items;
        this.nestedProperties = nestedProperties != null ? Map.copyOf(nestedProperties) : Map.of();
        this.exampleValue = exampleValue;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getFormat() { return format; }
    public String getDescription() { return description; }
    public boolean isRequired() { return required; }
    public List<String> getEnumValues() { return enumValues; }
    public PropertyDefinition getItems() { return items; }
    public Map<String, PropertyDefinition> getNestedProperties() { return nestedProperties; }
    public Object getExampleValue() { return exampleValue; }
}
