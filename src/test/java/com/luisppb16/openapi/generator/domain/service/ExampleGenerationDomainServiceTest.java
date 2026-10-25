/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.PropertyDefinition;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleGenerationDomainServiceTest {

    private ExampleGenerationDomainService service;

    @BeforeEach
    void setUp() {
        service = new ExampleGenerationDomainService();
    }

    private SchemaDefinition schema(String name, PropertyDefinition... props) {
        return new SchemaDefinition(name, null, List.of(props), false);
    }

    private SchemaDefinition arraySchema(String name, PropertyDefinition... props) {
        return new SchemaDefinition(name, null, List.of(props), true);
    }

    private PropertyDefinition prop(String name, String type) {
        return PropertyDefinition.builder().name(name).type(type).build();
    }

    private PropertyDefinition prop(String name, String type, String format) {
        return PropertyDefinition.builder().name(name).type(type).format(format).build();
    }

    private PropertyDefinition propWithExample(String name, String type, Object example) {
        return PropertyDefinition.builder().name(name).type(type).exampleValue(example).build();
    }

    private PropertyDefinition propWithEnum(String name, String type, List<String> enumValues) {
        return PropertyDefinition.builder().name(name).type(type).enumValues(enumValues).build();
    }

    private PropertyDefinition propWithItems(String name, PropertyDefinition items) {
        return PropertyDefinition.builder().name(name).type("array").items(items).build();
    }

    private PropertyDefinition propWithNested(String name, Map<String, PropertyDefinition> nested) {
        return PropertyDefinition.builder().name(name).type("object").nestedProperties(nested).build();
    }

    private JsonNode value(PropertyDefinition p, int index) {
        return service.generatePropertyValue(p, index, 0);
    }

    @Test
    void generateExamplesReturnsCorrectSchemaName() {
        GeneratedExample result = service.generateExamples(schema("User"), 3);

        assertThat(result.schemaName()).isEqualTo("User");
    }

    @Test
    void generateExamplesReturnsCorrectCount() {
        GeneratedExample result = service.generateExamples(schema("User", prop("name", "string")), 5);

        assertThat(result.count()).isEqualTo(5);
    }

    @Test
    void generateExamplesArrayTypeWrapsInArray() {
        GeneratedExample result = service.generateExamples(arraySchema("Tags", prop("tag", "string")), 1);

        assertThat(result.examples().get(0).isArray()).isTrue();
        assertThat(result.examples().get(0).get(0).isObject()).isTrue();
    }

    @Test
    void generateExamplesEmptyPropertiesProducesEmptyObject() {
        GeneratedExample result = service.generateExamples(schema("Empty"), 1);

        assertThat(result.examples().get(0).isObject()).isTrue();
        assertThat(result.examples().get(0).isEmpty()).isTrue();
    }

    @Test
    void generatePropertyValueStringExample() {
        JsonNode node = value(propWithExample("name", "string", "Hello"), 0);

        assertThat(node.isTextual()).isTrue();
        assertThat(node.asText()).isEqualTo("Hello");
    }

    @Test
    void generatePropertyValueNonStringExample() {
        JsonNode node = value(propWithExample("count", "integer", 42), 0);

        assertThat(node.isInt()).isTrue();
        assertThat(node.asInt()).isEqualTo(42);
    }

    @Test
    void generatePropertyValueEnumCycles() {
        PropertyDefinition p = propWithEnum("status", "string", List.of("on", "off"));

        assertThat(value(p, 0).asText()).isEqualTo("on");
        assertThat(value(p, 1).asText()).isEqualTo("off");
    }

    @Test
    void generatePropertyValueNumericEnumEmitsNumberNode() {
        PropertyDefinition p = propWithEnum("priority", "integer", List.of("1", "2", "3"));
        JsonNode node = value(p, 0);

        assertThat(node.isNumber()).isTrue();
        assertThat(node.asLong()).isEqualTo(1L);
    }

    @Test
    void generatePropertyValueIntegerType() {
        assertThat(value(prop("age", "integer"), 0).asInt()).isEqualTo(18);
    }

    @Test
    void generatePropertyValueIntegerYear() {
        assertThat(value(prop("birthYear", "integer"), 0).asInt()).isEqualTo(2020);
    }

    @Test
    void generatePropertyValueIntegerCount() {
        assertThat(value(prop("itemCount", "integer"), 2).asInt()).isEqualTo(15);
    }

    @Test
    void generatePropertyValueIntegerPrice() {
        assertThat(value(prop("item_price", "integer"), 0).asInt()).isEqualTo(100);
    }

    @Test
    void generatePropertyValueIntegerId() {
        assertThat(value(prop("userId", "integer"), 4).asInt()).isEqualTo(5);
    }

    @Test
    void generatePropertyValueIntegerDefault() {
        assertThat(value(prop("value", "integer"), 0).asInt()).isEqualTo(10);
    }

    @Test
    void generatePropertyValueIntegerInt64() {
        assertThat(value(prop("val", "integer", "int64"), 0).asLong()).isEqualTo(1000000L);
    }

    @Test
    void generatePropertyValueIntegerRespectsMinimum() {
        PropertyDefinition p =
                PropertyDefinition.builder()
                        .name("value")
                        .type("integer")
                        .minimum(BigDecimal.valueOf(500))
                        .build();

        assertThat(value(p, 0).asLong()).isEqualTo(500L);
    }

    @Test
    void generatePropertyValueIntegerRespectsMaximum() {
        PropertyDefinition p =
                PropertyDefinition.builder()
                        .name("age")
                        .type("integer")
                        .maximum(BigDecimal.valueOf(10))
                        .build();

        assertThat(value(p, 5).asLong()).isLessThanOrEqualTo(10L);
    }

    @Test
    void generatePropertyValueNumberPrice() {
        assertThat(value(prop("unit_price", "number"), 0).asDouble()).isEqualTo(9.99);
    }

    @Test
    void generatePropertyValueNumberPercent() {
        assertThat(value(prop("discount_rate", "number"), 5).asDouble()).isBetween(0.0, 100.0);
    }

    @Test
    void generatePropertyValueNumberRespectsBounds() {
        PropertyDefinition p =
                PropertyDefinition.builder()
                        .name("score")
                        .type("number")
                        .minimum(BigDecimal.valueOf(50))
                        .maximum(BigDecimal.valueOf(60))
                        .build();

        assertThat(value(p, 0).asDouble()).isBetween(50.0, 60.0);
        assertThat(value(p, 100).asDouble()).isBetween(50.0, 60.0);
    }

    @Test
    void generatePropertyValueBoolean() {
        assertThat(value(prop("active", "boolean"), 0).asBoolean()).isTrue();
        assertThat(value(prop("active", "boolean"), 1).asBoolean()).isFalse();
    }

    @Test
    void generatePropertyValueStringEmailFormat() {
        assertThat(value(prop("contact", "string", "email"), 0).asText()).contains("@");
    }

    @Test
    void generatePropertyValueStringUuidFormat() {
        assertThat(value(prop("id", "string", "uuid"), 0).asText()).matches("[0-9a-f-]+");
    }

    @Test
    void generatePropertyValueStringDateFormat() {
        assertThat(value(prop("birthday", "string", "date"), 0).asText()).contains("-");
    }

    @Test
    void generatePropertyValueStringDateTimeFormat() {
        assertThat(value(prop("created", "string", "date-time"), 0).asText()).contains("T");
    }

    @Test
    void generatePropertyValueStringTimeFormat() {
        assertThat(value(prop("openingTime", "string", "time"), 0).asText()).contains(":");
    }

    @Test
    void generatePropertyValueStringDurationFormat() {
        assertThat(value(prop("timeout", "string", "duration"), 0).asText()).startsWith("PT");
    }

    @Test
    void generatePropertyValueStringUriFormat() {
        assertThat(value(prop("link", "string", "uri"), 0).asText()).contains("https://");
    }

    @Test
    void generatePropertyValueStringIpv4Format() {
        assertThat(value(prop("ip", "string", "ipv4"), 0).asText()).contains("192.168.");
    }

    @Test
    void generatePropertyValueStringIpv6Format() {
        assertThat(value(prop("ip", "string", "ipv6"), 0).asText()).startsWith("2001:db8::");
    }

    @Test
    void generatePropertyValueStringHostnameFormat() {
        assertThat(value(prop("server", "string", "hostname"), 0).asText()).contains(".example.com");
    }

    @Test
    void generatePropertyValueStringPasswordFormat() {
        assertThat(value(prop("secret", "string", "password"), 0).asText()).contains("P@ssw0rd");
    }

    @Test
    void generatePropertyValueStringByteFormat() {
        assertThat(value(prop("data", "string", "byte"), 0).asText()).isNotEmpty();
    }

    @Test
    void generatePropertyValueStringByNameEmail() {
        assertThat(value(prop("userEmail", "string"), 0).asText()).contains("@");
    }

    @Test
    void generatePropertyValueStringByNameFirstname() {
        assertThat(value(prop("firstname", "string"), 0).asText()).doesNotContain("@");
    }

    @Test
    void generatePropertyValueStringByNameLastname() {
        assertThat(value(prop("lastname", "string"), 0).asText()).doesNotContain("@");
    }

    @Test
    void generatePropertyValueStringByNameEndsWithName() {
        assertThat(value(prop("full_name", "string"), 0).asText()).contains(" ");
    }

    @Test
    void generatePropertyValueStringByNamePhone() {
        assertThat(value(prop("phoneNumber", "string"), 0).asText()).contains("+1-555-");
    }

    @Test
    void generatePropertyValueStringByNameCity() {
        assertThat(value(prop("cityName", "string"), 0).asText()).isNotEmpty();
    }

    @Test
    void generatePropertyValueStringByNameZip() {
        assertThat(value(prop("zipCode", "string"), 0).asText()).matches("\\d{5}");
    }

    @Test
    void generatePropertyValueStringByNameUrl() {
        assertThat(value(prop("resourceUrl", "string"), 0).asText()).contains("https://");
    }

    @Test
    void generatePropertyValueStringByNameDescription() {
        assertThat(value(prop("itemDescription", "string"), 0).asText()).contains("Sample");
    }

    @Test
    void generatePropertyValueStringByNameTitle() {
        assertThat(value(prop("pageTitle", "string"), 0).asText()).contains("Example");
    }

    @Test
    void generatePropertyValueStringByNameStatus() {
        assertThat(value(prop("orderStatus", "string"), 0).asText())
                .isIn("active", "inactive", "pending", "completed", "cancelled");
    }

    @Test
    void generatePropertyValueStringByNameCode() {
        assertThat(value(prop("couponCode", "string"), 0).asText()).contains("COUPONCODE_");
    }

    @Test
    void generatePropertyValueStringByNameColor() {
        assertThat(value(prop("bgColor", "string"), 0).asText()).startsWith("#");
    }

    @Test
    void generatePropertyValueStringByNameVersion() {
        assertThat(value(prop("app_version", "string"), 0).asText()).contains(".");
    }

    @Test
    void generatePropertyValueStringByNameEndsWithId() {
        assertThat(value(prop("userId", "string"), 0).asText()).matches("[0-9a-f-]+");
    }

    @Test
    void generatePropertyValueStringByNameDefault() {
        assertThat(value(prop("randomField", "string"), 0).asText()).contains("_example_");
    }

    @Test
    void generatePropertyValueStringRespectsMinLength() {
        PropertyDefinition p =
                PropertyDefinition.builder().name("shortField").type("string").minLength(50).build();

        assertThat(value(p, 0).asText()).hasSizeGreaterThanOrEqualTo(50);
    }

    @Test
    void generatePropertyValueStringRespectsMaxLength() {
        PropertyDefinition p =
                PropertyDefinition.builder().name("itemDescription").type("string").maxLength(10).build();

        assertThat(value(p, 0).asText()).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void generateArrayValueWithItems() {
        JsonNode node = value(propWithItems("tags", prop("tag", "string")), 0);

        assertThat(node.isArray()).isTrue();
        assertThat(node).hasSize(1);
    }

    @Test
    void generateArrayValueWithoutItems() {
        JsonNode node = value(prop("tags", "array"), 0);

        assertThat(node.isArray()).isTrue();
        assertThat(node.get(0).asText()).isEqualTo("item_1");
    }

    @Test
    void generateArrayValueRespectsMinItems() {
        PropertyDefinition p =
                PropertyDefinition.builder()
                        .name("tags")
                        .type("array")
                        .items(prop("tag", "string"))
                        .minItems(3)
                        .build();

        assertThat(value(p, 0)).hasSize(3);
    }

    @Test
    void generateArrayValueRespectsMaxItems() {
        PropertyDefinition p =
                PropertyDefinition.builder()
                        .name("tags")
                        .type("array")
                        .items(prop("tag", "string"))
                        .minItems(5)
                        .maxItems(2)
                        .build();

        assertThat(value(p, 0)).hasSize(2);
    }

    @Test
    void generateArrayValueMaxDepth() {
        JsonNode node = service.generatePropertyValue(propWithItems("tags", prop("name", "string")), 0, 4);

        assertThat(node.isArray()).isTrue();
        assertThat(node.isEmpty()).isTrue();
    }

    @Test
    void generateNestedObjectValue() {
        PropertyDefinition p =
                propWithNested(
                        "address", Map.of("street", prop("street", "string"), "city", prop("city", "string")));
        JsonNode node = value(p, 0);

        assertThat(node.isObject()).isTrue();
        assertThat(node.has("street")).isTrue();
        assertThat(node.has("city")).isTrue();
    }

    @Test
    void generateNestedObjectValueEmpty() {
        JsonNode node = value(propWithNested("address", Map.of()), 0);

        assertThat(node.isObject()).isTrue();
        assertThat(node.isEmpty()).isTrue();
    }

    @Test
    void generateNestedObjectValueMaxDepth() {
        PropertyDefinition p = propWithNested("address", Map.of("street", prop("street", "string")));
        JsonNode node = service.generatePropertyValue(p, 0, 4);

        assertThat(node.isEmpty()).isTrue();
    }

    @Test
    void generateOneOfPicksAVariant() {
        PropertyDefinition p =
                PropertyDefinition.builder()
                        .name("pet")
                        .type("object")
                        .oneOfVariants(List.of(prop("age", "integer"), prop("nickname", "string")))
                        .build();
        JsonNode node = value(p, 0);

        assertThat(node.isInt() || node.isTextual()).isTrue();
    }

    @Test
    void generateAnyOfPicksAVariant() {
        PropertyDefinition p =
                PropertyDefinition.builder()
                        .name("value")
                        .type("string")
                        .anyOfVariants(List.of(prop("flag", "boolean")))
                        .build();

        assertThat(value(p, 0).isBoolean()).isTrue();
    }

    @Test
    void generatePropertyValueNullTypeDefaultsToString() {
        assertThat(value(prop("field", null), 0).isTextual()).isTrue();
    }

    @Test
    void generatePropertyValueNullName() {
        assertThat(value(prop(null, "string"), 0).asText()).isNotEmpty();
    }

    @Test
    void generatedJsonContainsPropertyName() {
        SchemaDefinition s = schema("User", prop("name", "string"), prop("email", "string", "email"));
        JsonNode example = service.generateExamples(s, 1).examples().get(0);

        assertThat(example.has("name")).isTrue();
        assertThat(example.has("email")).isTrue();
    }

    @Test
    void multipleExamplesHaveDifferentValues() {
        SchemaDefinition s = schema("User", prop("age", "integer"));
        List<JsonNode> examples = service.generateExamples(s, 3).examples();

        assertThat(examples).hasSize(3);
        assertThat(examples.get(0)).isNotEqualTo(examples.get(1));
    }
}
