/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.application.service;

import com.luisppb16.openapi.generator.domain.model.ExampleGenerationRequest;
import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import com.luisppb16.openapi.generator.domain.service.ExampleGenerationDomainService;
import com.luisppb16.openapi.generator.infrastructure.io.ExampleFileWriter;
import com.luisppb16.openapi.generator.infrastructure.openapi.OpenApiSpecificationParser;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application service that orchestrates the OpenAPI parsing and example generation use case.
 */
@RequiredArgsConstructor
public class ExampleGenerationApplicationService {

    private final OpenApiSpecificationParser parser;
    private final ExampleGenerationDomainService domainService;
    private final ExampleFileWriter fileWriter;

    public List<SchemaDefinition> loadSchemas(String specFilePath) {
        return parser.parseSpecification(specFilePath);
    }

    public List<GeneratedExample> generateExamples(ExampleGenerationRequest request) {
        List<SchemaDefinition> schemas = parser.parseSpecification(request.specFilePath());
        Map<String, Integer> counts = request.schemaExampleCounts();

        List<GeneratedExample> results = new ArrayList<>();
        schemas.stream()
                .filter(schema -> counts.getOrDefault(schema.name(), 0) > 0)
                .forEach(
                        schema -> {
                            int count = counts.get(schema.name());
                            results.add(domainService.generateExamples(schema, count));
                        });
        return results;
    }

    /**
     * Parses, generates, and writes the example files; returns a summary of what was written.
     */
    public GenerationSummary generateAndWrite(ExampleGenerationRequest request) throws IOException {
        List<GeneratedExample> results = generateExamples(request);
        List<ExampleFileWriter.WrittenFile> files =
                fileWriter.write(
                        Paths.get(request.outputDirectory()),
                        results,
                        request.combineOutput(),
                        request.outputFormat());
        int total = results.stream().mapToInt(GeneratedExample::getCount).sum();
        return new GenerationSummary(total, files);
    }

    /**
     * Generates the examples and renders them to text without writing anything to disk.
     */
    public String renderPreview(ExampleGenerationRequest request) {
        List<GeneratedExample> results = generateExamples(request);
        return fileWriter.render(results, request.combineOutput(), request.outputFormat());
    }

    /**
     * Result of a full generate-and-write run.
     */
    public record GenerationSummary(int totalExamples, List<ExampleFileWriter.WrittenFile> files) {
    }
}
