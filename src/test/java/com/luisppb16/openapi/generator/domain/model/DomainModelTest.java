/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PropertyDefinitionTest {

  @Test
  void compactConstructorDefaultsTypeToString() {
    PropertyDefinition prop =
        new PropertyDefinition("test", null, null, null, false, null, null, null, null);

    assertThat(prop.type()).isEqualTo("string");
  }

  @Test
  void compactConstructorDefaultsEnumValuesToEmpty() {
    PropertyDefinition prop =
        new PropertyDefinition("test", "integer", null, null, false, null, null, null, null);

    assertThat(prop.enumValues()).isEmpty();
  }

  @Test
  void compactConstructorDefaultsNestedPropertiesToEmpty() {
    PropertyDefinition prop =
        new PropertyDefinition("test", "integer", null, null, false, List.of(), null, null, null);

    assertThat(prop.nestedProperties()).isEmpty();
  }

  @Test
  void explicitValuesArePreserved() {
    PropertyDefinition nested =
        new PropertyDefinition(
            "inner", "string", null, null, false, List.of(), null, Map.of(), null);
    PropertyDefinition prop =
        new PropertyDefinition(
            "outer",
            "object",
            null,
            "desc",
            true,
            List.of("a", "b"),
            nested,
            Map.of("inner", nested),
            42);

    assertThat(prop.name()).isEqualTo("outer");
    assertThat(prop.type()).isEqualTo("object");
    assertThat(prop.required()).isTrue();
    assertThat(prop.enumValues()).containsExactly("a", "b");
    assertThat(prop.items()).isSameAs(nested);
    assertThat(prop.nestedProperties()).containsKey("inner");
    assertThat(prop.exampleValue()).isEqualTo(42);
  }
}

class SchemaDefinitionTest {

  @Test
  void recordAccessors() {
    PropertyDefinition prop =
        new PropertyDefinition("id", "string", "uuid", null, true, List.of(), null, Map.of(), null);
    SchemaDefinition schema = new SchemaDefinition("User", "A user", List.of(prop), false);

    assertThat(schema.name()).isEqualTo("User");
    assertThat(schema.description()).isEqualTo("A user");
    assertThat(schema.properties()).hasSize(1);
    assertThat(schema.isArrayType()).isFalse();
  }
}

class GeneratedExampleTest {

  @Test
  void recordAccessors() {
    GeneratedExample example = new GeneratedExample("User", List.of("{}", "[]"));

    assertThat(example.schemaName()).isEqualTo("User");
    assertThat(example.exampleJsonStrings()).hasSize(2);
  }

  @Test
  void getCount() {
    GeneratedExample example = new GeneratedExample("User", List.of("a", "b", "c"));

    assertThat(example.getCount()).isEqualTo(3);
  }
}

class ExampleGenerationRequestTest {

  @Test
  void recordAccessors() {
    ExampleGenerationRequest request =
        new ExampleGenerationRequest("/path/spec.yaml", Map.of("User", 5), "/out", true);

    assertThat(request.specFilePath()).isEqualTo("/path/spec.yaml");
    assertThat(request.schemaExampleCounts()).containsEntry("User", 5);
    assertThat(request.outputDirectory()).isEqualTo("/out");
    assertThat(request.combineOutput()).isTrue();
  }
}
