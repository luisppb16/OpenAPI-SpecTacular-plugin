/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.infrastructure.io;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.OutputFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExampleFileWriterTest {

  @TempDir Path tempDir;
  private ExampleFileWriter writer;

  @BeforeEach
  void setUp() {
    writer = new ExampleFileWriter();
  }

  private GeneratedExample example(String schemaName, String field, String value) {
    ObjectNode node = JsonNodeFactory.instance.objectNode();
    node.put(field, value);
    return new GeneratedExample(schemaName, List.of(node));
  }

  @Test
  void writeIndividualJsonFiles() throws IOException {
    List<ExampleFileWriter.WrittenFile> written =
        writer.write(
            tempDir,
            List.of(example("User", "name", "Alice"), example("Pet", "name", "Rex")),
            false,
            OutputFormat.JSON);

    assertThat(written).hasSize(2);
    Path userFile = tempDir.resolve("User-examples.json");
    assertThat(userFile).exists();

    JsonNode parsed = new ObjectMapper().readTree(Files.readString(userFile));
    assertThat(parsed.isArray()).isTrue();
    assertThat(parsed.get(0).get("name").asText()).isEqualTo("Alice");
  }

  @Test
  void writeCombinedJsonFile() throws IOException {
    List<ExampleFileWriter.WrittenFile> written =
        writer.write(
            tempDir,
            List.of(example("User", "name", "Alice"), example("Pet", "name", "Rex")),
            true,
            OutputFormat.JSON);

    assertThat(written).hasSize(1);
    Path combined = tempDir.resolve("all-examples.json");
    assertThat(combined).exists();

    JsonNode parsed = new ObjectMapper().readTree(Files.readString(combined));
    assertThat(parsed.get("User").get(0).get("name").asText()).isEqualTo("Alice");
    assertThat(parsed.get("Pet").get(0).get("name").asText()).isEqualTo("Rex");
  }

  @Test
  void writeIndividualYamlFiles() throws IOException {
    writer.write(tempDir, List.of(example("User", "name", "Alice")), false, OutputFormat.YAML);

    Path userFile = tempDir.resolve("User-examples.yaml");
    assertThat(userFile).exists();

    JsonNode parsed = new YAMLMapper().readTree(Files.readString(userFile));
    assertThat(parsed.get(0).get("name").asText()).isEqualTo("Alice");
  }

  @Test
  void writeCombinedYamlFile() throws IOException {
    writer.write(tempDir, List.of(example("User", "name", "Alice")), true, OutputFormat.YAML);

    Path combined = tempDir.resolve("all-examples.yaml");
    assertThat(combined).exists();

    JsonNode parsed = new YAMLMapper().readTree(Files.readString(combined));
    assertThat(parsed.get("User").get(0).get("name").asText()).isEqualTo("Alice");
  }

  @Test
  void writeCreatesMissingOutputDirectory() throws IOException {
    Path nested = tempDir.resolve("a/b/c");

    writer.write(nested, List.of(example("User", "name", "Alice")), false, OutputFormat.JSON);

    assertThat(nested.resolve("User-examples.json")).exists();
  }

  @Test
  void renderIndividualContainsAllSchemas() {
    String rendered =
        writer.render(
            List.of(example("User", "name", "Alice"), example("Pet", "name", "Rex")),
            false,
            OutputFormat.JSON);

    assertThat(rendered).contains("Alice").contains("Rex");
  }

  @Test
  void renderCombinedIsValidJson() throws IOException {
    String rendered =
        writer.render(List.of(example("User", "name", "Alice")), true, OutputFormat.JSON);

    JsonNode parsed = new ObjectMapper().readTree(rendered);
    assertThat(parsed.get("User").get(0).get("name").asText()).isEqualTo("Alice");
  }

  @Test
  void renderYamlHasNoDocStartMarker() {
    String rendered =
        writer.render(List.of(example("User", "name", "Alice")), true, OutputFormat.YAML);

    assertThat(rendered).doesNotStartWith("---");
    assertThat(rendered).contains("Alice");
  }
}
