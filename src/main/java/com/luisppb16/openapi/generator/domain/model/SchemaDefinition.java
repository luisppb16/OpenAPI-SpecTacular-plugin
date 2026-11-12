/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

import java.util.List;

/**
 * Represents a parsed OpenAPI schema with its name, description, properties, and whether it is an
 * array type.
 */
public record SchemaDefinition(
    String name, String description, List<PropertyDefinition> properties, boolean isArrayType) {
  public SchemaDefinition {
    properties = properties == null ? List.of() : List.copyOf(properties);
  }
}
