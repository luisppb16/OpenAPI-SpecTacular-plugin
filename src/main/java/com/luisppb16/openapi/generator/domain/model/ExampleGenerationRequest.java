/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

import java.util.Map;

/**
 * Encapsulates the user request for generating examples, including the spec file path, per-schema
 * counts, output directory, and combine flag.
 */
public record ExampleGenerationRequest(
    String specFilePath,
    Map<String, Integer> schemaExampleCounts,
    String outputDirectory,
    boolean combineOutput) {}
