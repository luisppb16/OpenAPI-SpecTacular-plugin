/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a collection of generated example JSON strings for a schema. This is a Value Object in
 * the DDD structure.
 */
public final class GeneratedExample {
  private final String schemaName;
  private final List<String> exampleJsonStrings;

  public GeneratedExample(String schemaName, List<String> exampleJsonStrings) {
    this.schemaName = Objects.requireNonNull(schemaName, "schemaName must not be null");
    this.exampleJsonStrings =
        exampleJsonStrings != null ? List.copyOf(exampleJsonStrings) : List.of();
  }

  public String getSchemaName() {
    return schemaName;
  }

  public List<String> getExampleJsonStrings() {
    return exampleJsonStrings;
  }

  public int getCount() {
    return exampleJsonStrings.size();
  }
}
