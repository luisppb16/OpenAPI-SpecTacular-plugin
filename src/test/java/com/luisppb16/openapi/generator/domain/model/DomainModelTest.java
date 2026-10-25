/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void builderPopulatesConstraintFields() {
        PropertyDefinition prop =
                PropertyDefinition.builder()
                        .name("score")
                        .type("number")
                        .minimum(BigDecimal.ONE)
                        .maximum(BigDecimal.TEN)
                        .minLength(2)
                        .maxLength(8)
                        .pattern("^[a-z]+$")
                        .minItems(1)
                        .maxItems(3)
                        .oneOfVariants(List.of(PropertyDefinition.builder().name("v").type("string").build()))
                        .build();

        assertThat(prop.minimum()).isEqualTo(BigDecimal.ONE);
        assertThat(prop.maximum()).isEqualTo(BigDecimal.TEN);
        assertThat(prop.minLength()).isEqualTo(2);
        assertThat(prop.maxLength()).isEqualTo(8);
        assertThat(prop.pattern()).isEqualTo("^[a-z]+$");
        assertThat(prop.minItems()).isEqualTo(1);
        assertThat(prop.maxItems()).isEqualTo(3);
        assertThat(prop.oneOfVariants()).hasSize(1);
        assertThat(prop.anyOfVariants()).isEmpty();
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
        GeneratedExample example =
                new GeneratedExample(
                        "User",
                        List.of(JsonNodeFactory.instance.objectNode(), JsonNodeFactory.instance.arrayNode()));

        assertThat(example.schemaName()).isEqualTo("User");
        assertThat(example.examples()).hasSize(2);
    }

    @Test
    void count() {
        GeneratedExample example =
                new GeneratedExample(
                        "User",
                        List.of(
                                JsonNodeFactory.instance.objectNode(),
                                JsonNodeFactory.instance.objectNode(),
                                JsonNodeFactory.instance.objectNode()));

        assertThat(example.count()).isEqualTo(3);
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
        assertThat(request.outputFormat()).isEqualTo(OutputFormat.JSON);
    }

    @Test
    void fullConstructorKeepsFormat() {
        ExampleGenerationRequest request =
                new ExampleGenerationRequest(
                        "/path/spec.yaml", Map.of("User", 5), "/out", false, OutputFormat.YAML);

        assertThat(request.outputFormat()).isEqualTo(OutputFormat.YAML);
    }

    @Test
    void nullFormatDefaultsToJson() {
        ExampleGenerationRequest request =
                new ExampleGenerationRequest("/spec.yaml", Map.of(), "/out", false, null);

        assertThat(request.outputFormat()).isEqualTo(OutputFormat.JSON);
    }
}
