package com.openapi.generator.domain.model;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a request to generate examples from an OpenAPI specification.
 * This is a Value Object in the DDD structure.
 */
public final class ExampleGenerationRequest {
    private final String specFilePath;
    private final Map<String, Integer> schemaExampleCounts;
    private final String outputDirectory;

    public ExampleGenerationRequest(String specFilePath, Map<String, Integer> schemaExampleCounts, String outputDirectory) {
        this.specFilePath = Objects.requireNonNull(specFilePath, "specFilePath must not be null");
        this.schemaExampleCounts = Map.copyOf(Objects.requireNonNull(schemaExampleCounts, "schemaExampleCounts must not be null"));
        this.outputDirectory = outputDirectory;
    }

    public String getSpecFilePath() { return specFilePath; }
    public Map<String, Integer> getSchemaExampleCounts() { return schemaExampleCounts; }
    public String getOutputDirectory() { return outputDirectory; }

    public int getTotalExampleCount() {
        return schemaExampleCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
}
