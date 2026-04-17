/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

import java.util.List;
import java.util.Map;

/** Represents a single property within an OpenAPI schema, including its type, format, constraints, and nested structure. */
public record PropertyDefinition(
    String name,
    String type,
    String format,
    String description,
    boolean required,
    List<String> enumValues,
    PropertyDefinition items,
    Map<String, PropertyDefinition> nestedProperties,
    Object exampleValue) {
  public PropertyDefinition {
    if (type == null) type = "string";
    if (enumValues == null) enumValues = List.of();
    if (nestedProperties == null) nestedProperties = Map.of();
  }
}
