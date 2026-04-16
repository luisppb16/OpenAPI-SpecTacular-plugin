/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.application.service;

import com.openapi.generator.domain.model.ExampleGenerationRequest;
import com.openapi.generator.domain.model.GeneratedExample;
import com.openapi.generator.domain.model.SchemaDefinition;
import com.openapi.generator.domain.service.ExampleGenerationDomainService;
import com.openapi.generator.infrastructure.openapi.OpenApiSpecificationParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Application service that orchestrates the OpenAPI parsing and example generation use case. */
public class ExampleGenerationApplicationService {

  private final OpenApiSpecificationParser parser;
  private final ExampleGenerationDomainService domainService;

  public ExampleGenerationApplicationService() {
    this.parser = new OpenApiSpecificationParser();
    this.domainService = new ExampleGenerationDomainService();
  }

  public List<SchemaDefinition> loadSchemas(String specFilePath) {
    return parser.parseSpecification(specFilePath);
  }

  public List<GeneratedExample> generateExamples(ExampleGenerationRequest request) {
    List<SchemaDefinition> schemas = parser.parseSpecification(request.getSpecFilePath());
    Map<String, Integer> counts = request.getSchemaExampleCounts();

    List<GeneratedExample> results = new ArrayList<>();
    for (SchemaDefinition schema : schemas) {
      int count = counts.getOrDefault(schema.getName(), 0);
      if (count > 0) {
        GeneratedExample example = domainService.generateExamples(schema, count);
        results.add(example);
      }
    }
    return results;
  }
}
