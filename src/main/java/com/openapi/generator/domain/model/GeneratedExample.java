/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.domain.model;

import java.util.List;

/** Holds the generated JSON example strings for a specific schema name. */
public record GeneratedExample(String schemaName, List<String> exampleJsonStrings) {
  public int getCount() {
    return exampleJsonStrings.size();
  }
}
