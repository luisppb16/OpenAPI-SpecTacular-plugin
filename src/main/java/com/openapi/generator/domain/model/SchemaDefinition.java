package com.openapi.generator.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents an OpenAPI schema definition.
 * This is a Domain Model in the DDD structure.
 */
public final class SchemaDefinition {
    private final String name;
    private final String description;
    private final List<PropertyDefinition> properties;
    private final boolean isArrayType;

    public SchemaDefinition(String name, String description, List<PropertyDefinition> properties, boolean isArrayType) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.properties = properties != null ? List.copyOf(properties) : List.of();
        this.isArrayType = isArrayType;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<PropertyDefinition> getProperties() { return properties; }
    public boolean isArrayType() { return isArrayType; }

    @Override
    public String toString() {
        return "SchemaDefinition{name='" + name + "', properties=" + properties.size() + "}";
    }
}
