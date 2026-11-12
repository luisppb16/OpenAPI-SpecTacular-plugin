/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.luisppb16.openapi.generator.domain.model.ExampleGenerationRequest;
import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.OutputFormat;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import com.luisppb16.openapi.generator.domain.service.ExampleGenerationDomainService;
import com.luisppb16.openapi.generator.infrastructure.io.ExampleFileWriter;
import com.luisppb16.openapi.generator.infrastructure.openapi.OpenApiSpecificationParser;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExampleGenerationApplicationServiceTest {

  @TempDir Path tempDir;
  private ExampleGenerationApplicationService applicationService;

  private String resourcePath(String name) {
    String encoded = getClass().getClassLoader().getResource(name).getFile();
    return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
  }

  @BeforeEach
  void setUp() {
    applicationService =
        new ExampleGenerationApplicationService(
            new OpenApiSpecificationParser(),
            new ExampleGenerationDomainService(),
            new ExampleFileWriter());
  }

  @Test
  void loadSchemasReturnsSchemasFromSpec() {
    List<SchemaDefinition> schemas = applicationService.loadSchemas(resourcePath("test-spec.yaml"));

    assertThat(schemas).isNotEmpty();
  }

  @Test
  void generateExamplesWithSchemaCounts() {
    List<SchemaDefinition> schemas = applicationService.loadSchemas(resourcePath("test-spec.yaml"));
    String firstSchema = schemas.get(0).name();

    ExampleGenerationRequest request =
        new ExampleGenerationRequest(
            resourcePath("test-spec.yaml"), Map.of(firstSchema, 2), "/tmp/out", false);

    List<GeneratedExample> results = applicationService.generateExamples(request);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).schemaName()).isEqualTo(firstSchema);
    assertThat(results.get(0).count()).isEqualTo(2);
  }

  @Test
  void generateExamplesFiltersZeroCountSchemas() {
    List<SchemaDefinition> schemas = applicationService.loadSchemas(resourcePath("test-spec.yaml"));
    String first = schemas.get(0).name();
    String second = schemas.size() > 1 ? schemas.get(1).name() : first;

    ExampleGenerationRequest request =
        new ExampleGenerationRequest(
            resourcePath("test-spec.yaml"), Map.of(first, 3, second, 0), "/tmp/out", false);

    List<GeneratedExample> results = applicationService.generateExamples(request);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).schemaName()).isEqualTo(first);
  }

  @Test
  void generateExamplesWithAllZeroCountsReturnsEmpty() {
    ExampleGenerationRequest request =
        new ExampleGenerationRequest(
            resourcePath("test-spec.yaml"), Map.of("NonExistent", 0), "/tmp/out", false);

    List<GeneratedExample> results = applicationService.generateExamples(request);

    assertThat(results).isEmpty();
  }

  @Test
  void generateAndWriteWritesFilesAndReportsSummary() throws IOException {
    List<SchemaDefinition> schemas = applicationService.loadSchemas(resourcePath("test-spec.yaml"));
    String firstSchema = schemas.get(0).name();

    ExampleGenerationRequest request =
        new ExampleGenerationRequest(
            resourcePath("test-spec.yaml"),
            Map.of(firstSchema, 2),
            tempDir.toString(),
            false,
            OutputFormat.JSON);

    ExampleGenerationApplicationService.GenerationSummary summary =
        applicationService.generateAndWrite(request);

    assertThat(summary.totalExamples()).isEqualTo(2);
    assertThat(summary.files()).hasSize(1);
    assertThat(Files.exists(summary.files().get(0).path())).isTrue();
  }

  @Test
  void renderPreviewProducesTextWithoutWriting() {
    List<SchemaDefinition> schemas = applicationService.loadSchemas(resourcePath("test-spec.yaml"));
    String firstSchema = schemas.get(0).name();

    ExampleGenerationRequest request =
        new ExampleGenerationRequest(
            resourcePath("test-spec.yaml"),
            Map.of(firstSchema, 1),
            tempDir.toString(),
            true,
            OutputFormat.JSON);

    String preview = applicationService.renderPreview(request);

    assertThat(preview).contains(firstSchema);
    assertThat(tempDir.toFile().list()).isEmpty();
  }
}
