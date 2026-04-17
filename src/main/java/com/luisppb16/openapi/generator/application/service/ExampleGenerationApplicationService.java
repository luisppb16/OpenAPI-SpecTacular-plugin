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
import com.luisppb16.openapi.generator.infrastructure.openapi.OpenApiSpecificationParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Application service that orchestrates the OpenAPI parsing and example generation use case. */
@RequiredArgsConstructor
public class ExampleGenerationApplicationService {

  private final OpenApiSpecificationParser parser;
  private final ExampleGenerationDomainService domainService;

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
}
