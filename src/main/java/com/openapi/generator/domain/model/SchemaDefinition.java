/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.domain.model;

import java.util.List;
import javax.validation.constraints.NotNull;

/** Represents a parsed OpenAPI schema with its name, description, properties, and whether it is an array type. */
public record SchemaDefinition(
    String name,
    String description,
    @NotNull List<PropertyDefinition> properties,
    boolean isArrayType) {}
