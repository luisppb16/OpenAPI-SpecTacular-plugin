/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Holds the generated example trees for a specific schema name.
 */
public record GeneratedExample(String schemaName, List<JsonNode> examples) {
    public GeneratedExample {
        examples = examples == null ? List.of() : List.copyOf(examples);
    }

    public int getCount() {
        return examples.size();
    }
}
