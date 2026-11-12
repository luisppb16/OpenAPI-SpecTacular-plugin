/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;

/**
 * Represents a single property within an OpenAPI schema, including its type, format, constraints,
 * and nested structure.
 */
@Builder(toBuilder = true)
public record PropertyDefinition(
    String name,
    String type,
    String format,
    String description,
    boolean required,
    List<String> enumValues,
    PropertyDefinition items,
    Map<String, PropertyDefinition> nestedProperties,
    Object exampleValue,
    BigDecimal minimum,
    BigDecimal maximum,
    Integer minLength,
    Integer maxLength,
    String pattern,
    Integer minItems,
    Integer maxItems,
    List<PropertyDefinition> oneOfVariants,
    List<PropertyDefinition> anyOfVariants) {
  public PropertyDefinition {
    if (type == null) type = "string";
    enumValues = enumValues == null ? List.of() : List.copyOf(enumValues);
    nestedProperties = nestedProperties == null ? Map.of() : nestedProperties;
    oneOfVariants = oneOfVariants == null ? List.of() : List.copyOf(oneOfVariants);
    anyOfVariants = anyOfVariants == null ? List.of() : List.copyOf(anyOfVariants);
  }

  /** Convenience constructor that keeps the original nine-component signature. */
  public PropertyDefinition(
      String name,
      String type,
      String format,
      String description,
      boolean required,
      List<String> enumValues,
      PropertyDefinition items,
      Map<String, PropertyDefinition> nestedProperties,
      Object exampleValue) {
    this(
        name,
        type,
        format,
        description,
        required,
        enumValues,
        items,
        nestedProperties,
        exampleValue,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }
}
