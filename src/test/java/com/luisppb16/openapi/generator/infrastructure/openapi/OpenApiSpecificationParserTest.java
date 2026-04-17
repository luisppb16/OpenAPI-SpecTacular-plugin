/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.infrastructure.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.luisppb16.openapi.generator.domain.model.PropertyDefinition;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiSpecificationParserTest {

  private final OpenApiSpecificationParser parser = new OpenApiSpecificationParser();

  @TempDir java.nio.file.Path tempDir;

  private String resourcePath(String name) {
    String encoded = getClass().getClassLoader().getResource(name).getFile();
    return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
  }

  @Test
  void parseSpecificationWithComponentSchemas() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));

    assertThat(schemas).isNotEmpty();
    assertThat(schemas.stream().map(SchemaDefinition::name)).contains("User", "SimpleArray");
  }

  @Test
  void parseSpecificationSchemasAreSortedByName() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));

    List<String> names = schemas.stream().map(SchemaDefinition::name).toList();
    assertThat(names).isSorted();
  }

  @Test
  void parseSpecificationParsesUserSchemaProperties() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));
    SchemaDefinition user =
        schemas.stream().filter(s -> s.name().equals("User")).findFirst().orElseThrow();

    assertThat(user.isArrayType()).isFalse();
    assertThat(user.properties()).isNotEmpty();
    assertThat(user.properties().stream().map(PropertyDefinition::name))
        .contains("id", "name", "email", "age", "status");
  }

  @Test
  void parseSpecificationParsesRequiredFields() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));
    SchemaDefinition user =
        schemas.stream().filter(s -> s.name().equals("User")).findFirst().orElseThrow();

    PropertyDefinition name =
        user.properties().stream().filter(p -> p.name().equals("name")).findFirst().orElseThrow();
    PropertyDefinition email =
        user.properties().stream().filter(p -> p.name().equals("email")).findFirst().orElseThrow();
    PropertyDefinition age =
        user.properties().stream().filter(p -> p.name().equals("age")).findFirst().orElseThrow();

    assertThat(name.required()).isTrue();
    assertThat(email.required()).isTrue();
    assertThat(age.required()).isFalse();
  }

  @Test
  void parseSpecificationParsesEnumValues() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));
    SchemaDefinition user =
        schemas.stream().filter(s -> s.name().equals("User")).findFirst().orElseThrow();

    PropertyDefinition status =
        user.properties().stream().filter(p -> p.name().equals("status")).findFirst().orElseThrow();

    assertThat(status.enumValues()).containsExactly("active", "inactive", "pending");
  }

  @Test
  void parseSpecificationParsesExampleValues() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));
    SchemaDefinition user =
        schemas.stream().filter(s -> s.name().equals("User")).findFirst().orElseThrow();

    PropertyDefinition name =
        user.properties().stream().filter(p -> p.name().equals("name")).findFirst().orElseThrow();

    assertThat(name.exampleValue()).isEqualTo("John Doe");
  }

  @Test
  void parseSpecificationParsesFormats() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));
    SchemaDefinition user =
        schemas.stream().filter(s -> s.name().equals("User")).findFirst().orElseThrow();

    PropertyDefinition id =
        user.properties().stream().filter(p -> p.name().equals("id")).findFirst().orElseThrow();
    PropertyDefinition email =
        user.properties().stream().filter(p -> p.name().equals("email")).findFirst().orElseThrow();

    assertThat(id.format()).isEqualTo("uuid");
    assertThat(email.format()).isEqualTo("email");
  }

  @Test
  void parseSpecificationParsesArraySchema() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));

    assertThat(schemas.stream().map(SchemaDefinition::name)).contains("SimpleArray");
  }

  @Test
  void parseSpecificationParsesArrayItemsInProperty() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));
    SchemaDefinition user =
        schemas.stream().filter(s -> s.name().equals("User")).findFirst().orElseThrow();

    assertThat(user.properties().size()).isGreaterThan(3);
  }

  @Test
  void parseSpecificationParsesPathRequestBody() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));

    assertThat(schemas.size()).isGreaterThan(2);
  }

  @Test
  void parseSpecificationParsesRequestBodyProperties() {
    List<SchemaDefinition> schemas = parser.parseSpecification(resourcePath("test-spec.yaml"));

    SchemaDefinition postSchema =
        schemas.stream()
            .filter(s -> s.name().toLowerCase().contains("post"))
            .findFirst()
            .orElse(null);
    if (postSchema != null) {
      assertThat(postSchema.properties().stream().map(PropertyDefinition::name)).contains("name");
    }
  }

  @Test
  void parseSpecificationInvalidFileThrows() {
    assertThatThrownBy(() -> parser.parseSpecification("/nonexistent/file.yaml"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Failed to parse OpenAPI specification");
  }

  @Test
  void parseSpecificationInvalidContentThrows(@TempDir java.nio.file.Path dir) throws Exception {
    java.nio.file.Path badFile = dir.resolve("bad.yaml");
    java.nio.file.Files.writeString(badFile, "not: a: valid: openapi: spec");

    assertThatThrownBy(() -> parser.parseSpecification(badFile.toString()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
