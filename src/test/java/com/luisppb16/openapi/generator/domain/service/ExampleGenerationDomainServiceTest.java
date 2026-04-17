/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.PropertyDefinition;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    return new PropertyDefinition(name, type, null, null, false, List.of(), null, Map.of(), null);
  }

  private PropertyDefinition prop(String name, String type, String format) {
    return new PropertyDefinition(name, type, format, null, false, List.of(), null, Map.of(), null);
  }

  private PropertyDefinition propWithExample(String name, String type, Object example) {
    return new PropertyDefinition(
        name, type, null, null, false, List.of(), null, Map.of(), example);
  }

  private PropertyDefinition propWithEnum(String name, String type, List<String> enumValues) {
    return new PropertyDefinition(name, type, null, null, false, enumValues, null, Map.of(), null);
  }

  private PropertyDefinition propWithItems(String name, PropertyDefinition items) {
    return new PropertyDefinition(
        name, "array", null, null, false, List.of(), items, Map.of(), null);
  }

  private PropertyDefinition propWithNested(String name, Map<String, PropertyDefinition> nested) {
    return new PropertyDefinition(name, "object", null, null, false, List.of(), null, nested, null);
  }

  @Test
  void generateExamplesReturnsCorrectSchemaName() {
    SchemaDefinition s = schema("User");
    GeneratedExample result = service.generateExamples(s, 3);

    assertThat(result.schemaName()).isEqualTo("User");
  }

  @Test
  void generateExamplesReturnsCorrectCount() {
    SchemaDefinition s = schema("User", prop("name", "string"));
    GeneratedExample result = service.generateExamples(s, 5);

    assertThat(result.getCount()).isEqualTo(5);
  }

  @Test
  void generateExamplesArrayTypeWrapsInArray() {
    SchemaDefinition s = arraySchema("Tags", prop("tag", "string"));
    GeneratedExample result = service.generateExamples(s, 1);

    assertThat(result.exampleJsonStrings().get(0)).startsWith("[").endsWith("]");
  }

  @Test
  void generateExamplesEmptyPropertiesProducesEmptyObject() {
    SchemaDefinition s = schema("Empty");
    GeneratedExample result = service.generateExamples(s, 1);

    assertThat(result.exampleJsonStrings().get(0)).isEqualTo("{}");
  }

  @Test
  void generatePropertyValueStringExample() {
    PropertyDefinition p = propWithExample("name", "string", "Hello");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("\"Hello\"");
  }

  @Test
  void generatePropertyValueNonStringExample() {
    PropertyDefinition p = propWithExample("count", "integer", 42);
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("42");
  }

  @Test
  void generatePropertyValueEnumCycles() {
    PropertyDefinition p = propWithEnum("status", "string", List.of("on", "off"));
    String v0 = service.generatePropertyValue(p, 0, 0);
    String v1 = service.generatePropertyValue(p, 1, 0);

    assertThat(v0).isEqualTo("\"on\"");
    assertThat(v1).isEqualTo("\"off\"");
  }

  @Test
  void generatePropertyValueIntegerType() {
    PropertyDefinition p = prop("age", "integer");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("18");
  }

  @Test
  void generatePropertyValueIntegerYear() {
    PropertyDefinition p = prop("birthYear", "integer");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("2020");
  }

  @Test
  void generatePropertyValueIntegerCount() {
    PropertyDefinition p = prop("itemCount", "integer");
    String value = service.generatePropertyValue(p, 2, 0);

    assertThat(value).isEqualTo("15");
  }

  @Test
  void generatePropertyValueIntegerPrice() {
    PropertyDefinition p = prop("item_price", "integer");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("100");
  }

  @Test
  void generatePropertyValueIntegerId() {
    PropertyDefinition p = prop("userId", "integer");
    String value = service.generatePropertyValue(p, 4, 0);

    assertThat(value).isEqualTo("5");
  }

  @Test
  void generatePropertyValueIntegerDefault() {
    PropertyDefinition p = prop("value", "integer");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("10");
  }

  @Test
  void generatePropertyValueIntegerInt() {
    PropertyDefinition p = prop("val", "int");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(Integer.parseInt(value)).isPositive();
  }

  @Test
  void generatePropertyValueIntegerInt64() {
    PropertyDefinition p = prop("val", "integer", "int64");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(Long.parseLong(value)).isEqualTo(1000000L);
  }

  @Test
  void generatePropertyValueNumberPrice() {
    PropertyDefinition p = prop("unit_price", "number");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value.replace(',', '.')).isEqualTo("9.99");
  }

  @Test
  void generatePropertyValueNumberLatitude() {
    PropertyDefinition p = prop("latitude", "number");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotNull();
  }

  @Test
  void generatePropertyValueNumberLongitude() {
    PropertyDefinition p = prop("longitude", "number");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotNull();
  }

  @Test
  void generatePropertyValueNumberPercent() {
    PropertyDefinition p = prop("discount_rate", "number");
    String value = service.generatePropertyValue(p, 5, 0);

    assertThat(Double.parseDouble(value.replace(',', '.'))).isBetween(0.0, 100.0);
  }

  @Test
  void generatePropertyValueNumberDefault() {
    PropertyDefinition p = prop("value", "number");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueFloat() {
    PropertyDefinition p = prop("val", "float");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueDouble() {
    PropertyDefinition p = prop("val", "double");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueBoolean() {
    PropertyDefinition p = prop("active", "boolean");

    assertThat(service.generatePropertyValue(p, 0, 0)).isEqualTo("true");
    assertThat(service.generatePropertyValue(p, 1, 0)).isEqualTo("false");
  }

  @Test
  void generatePropertyValueBool() {
    PropertyDefinition p = prop("active", "bool");

    assertThat(service.generatePropertyValue(p, 0, 0)).isEqualTo("true");
  }

  @Test
  void generatePropertyValueStringEmailFormat() {
    PropertyDefinition p = prop("contact", "string", "email");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("@");
  }

  @Test
  void generatePropertyValueStringUuidFormat() {
    PropertyDefinition p = prop("id", "string", "uuid");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).matches("\"[0-9a-f-]+\"");
  }

  @Test
  void generatePropertyValueStringDateFormat() {
    PropertyDefinition p = prop("birthday", "string", "date");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("\"").contains("-");
  }

  @Test
  void generatePropertyValueStringDateTimeFormat() {
    PropertyDefinition p = prop("created", "string", "date-time");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("\"").contains("T");
  }

  @Test
  void generatePropertyValueStringDatetimeFormat() {
    PropertyDefinition p = prop("created", "string", "datetime");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("\"");
  }

  @Test
  void generatePropertyValueStringUriFormat() {
    PropertyDefinition p = prop("link", "string", "uri");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("https://");
  }

  @Test
  void generatePropertyValueStringUrlFormat() {
    PropertyDefinition p = prop("link", "string", "url");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("https://");
  }

  @Test
  void generatePropertyValueStringIpv4Format() {
    PropertyDefinition p = prop("ip", "string", "ipv4");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("192.168.");
  }

  @Test
  void generatePropertyValueStringPasswordFormat() {
    PropertyDefinition p = prop("secret", "string", "password");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("P@ssw0rd");
  }

  @Test
  void generatePropertyValueStringByteFormat() {
    PropertyDefinition p = prop("data", "string", "byte");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("\"");
  }

  @Test
  void generatePropertyValueStringByNameEmail() {
    PropertyDefinition p = prop("userEmail", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("@");
  }

  @Test
  void generatePropertyValueStringByNameMail() {
    PropertyDefinition p = prop("mailAddress", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("@");
  }

  @Test
  void generatePropertyValueStringByNameFirstname() {
    PropertyDefinition p = prop("firstname", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).doesNotContain("@");
  }

  @Test
  void generatePropertyValueStringByNameGivenname() {
    PropertyDefinition p = prop("givenname", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).doesNotContain("@");
  }

  @Test
  void generatePropertyValueStringByNameFirstAndName() {
    PropertyDefinition p = prop("first_name", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).doesNotContain("@");
  }

  @Test
  void generatePropertyValueStringByNameLastname() {
    PropertyDefinition p = prop("lastname", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).doesNotContain("@");
  }

  @Test
  void generatePropertyValueStringByNameSurname() {
    PropertyDefinition p = prop("surname", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).doesNotContain("@");
  }

  @Test
  void generatePropertyValueStringByNameFamilyname() {
    PropertyDefinition p = prop("familyname", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).doesNotContain("@");
  }

  @Test
  void generatePropertyValueStringByNameLastAndName() {
    PropertyDefinition p = prop("last_name", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).doesNotContain("@");
  }

  @Test
  void generatePropertyValueStringByNameEndsWithName() {
    PropertyDefinition p = prop("full_name", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains(" ");
  }

  @Test
  void generatePropertyValueStringByNamePhone() {
    PropertyDefinition p = prop("phoneNumber", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("+1-555-");
  }

  @Test
  void generatePropertyValueStringByNameTel() {
    PropertyDefinition p = prop("tel", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("+1-555-");
  }

  @Test
  void generatePropertyValueStringByNameCity() {
    PropertyDefinition p = prop("cityName", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueStringByNameCountry() {
    PropertyDefinition p = prop("countryCode", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueStringByNameStreet() {
    PropertyDefinition p = prop("streetAddress", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value)
        .satisfiesAnyOf(
            v -> assertThat(v).contains("St"),
            v -> assertThat(v).contains("Ave"),
            v -> assertThat(v).contains("Dr"),
            v -> assertThat(v).contains("Ln"),
            v -> assertThat(v).contains("Rd"),
            v -> assertThat(v).contains("Blvd"));
  }

  @Test
  void generatePropertyValueStringByNameAddress() {
    PropertyDefinition p = prop("homeAddress", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueStringByNameZip() {
    PropertyDefinition p = prop("zipCode", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).matches("\"\\d{5}\"");
  }

  @Test
  void generatePropertyValueStringByNamePostal() {
    PropertyDefinition p = prop("postalCode", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).matches("\"\\d{5}\"");
  }

  @Test
  void generatePropertyValueStringByNameUrl() {
    PropertyDefinition p = prop("resourceUrl", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("https://");
  }

  @Test
  void generatePropertyValueStringByNameLink() {
    PropertyDefinition p = prop("resourceLink", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("https://");
  }

  @Test
  void generatePropertyValueStringByNameHref() {
    PropertyDefinition p = prop("hrefValue", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("https://");
  }

  @Test
  void generatePropertyValueStringByNameDescription() {
    PropertyDefinition p = prop("itemDescription", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("Sample");
  }

  @Test
  void generatePropertyValueStringByNameText() {
    PropertyDefinition p = prop("bodyText", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("Sample");
  }

  @Test
  void generatePropertyValueStringByNameContent() {
    PropertyDefinition p = prop("pageContent", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("Sample");
  }

  @Test
  void generatePropertyValueStringByNameBody() {
    PropertyDefinition p = prop("bodyField", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("Sample");
  }

  @Test
  void generatePropertyValueStringByNameTitle() {
    PropertyDefinition p = prop("pageTitle", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("Example");
  }

  @Test
  void generatePropertyValueStringByNameSubject() {
    PropertyDefinition p = prop("message_subject", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("Example");
  }

  @Test
  void generatePropertyValueStringByNameLabel() {
    PropertyDefinition p = prop("fieldLabel", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("Example");
  }

  @Test
  void generatePropertyValueStringByNameStatus() {
    PropertyDefinition p = prop("orderStatus", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value)
        .isIn("\"active\"", "\"inactive\"", "\"pending\"", "\"completed\"", "\"cancelled\"");
  }

  @Test
  void generatePropertyValueStringByNameState() {
    PropertyDefinition p = prop("currentState", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value)
        .isIn("\"active\"", "\"inactive\"", "\"pending\"", "\"completed\"", "\"cancelled\"");
  }

  @Test
  void generatePropertyValueStringByNameType() {
    PropertyDefinition p = prop("entityType", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueStringByNameCategory() {
    PropertyDefinition p = prop("productCategory", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueStringByNameKind() {
    PropertyDefinition p = prop("resourceKind", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueStringByNameCode() {
    PropertyDefinition p = prop("couponCode", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("COUPONCODE_");
  }

  @Test
  void generatePropertyValueStringByNameKey() {
    PropertyDefinition p = prop("apiKey", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("APIKEY_");
  }

  @Test
  void generatePropertyValueStringByNameColor() {
    PropertyDefinition p = prop("bgColor", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("\"#");
  }

  @Test
  void generatePropertyValueStringByNameColour() {
    PropertyDefinition p = prop("font_colour", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("\"#");
  }

  @Test
  void generatePropertyValueStringByNameVersion() {
    PropertyDefinition p = prop("app_version", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains(".");
  }

  @Test
  void generatePropertyValueStringByNameEndsWithId() {
    PropertyDefinition p = prop("userId", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).matches("\"[0-9a-f-]+\"");
  }

  @Test
  void generatePropertyValueStringByNameDefault() {
    PropertyDefinition p = prop("randomField", "string");
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).contains("_example_");
  }

  @Test
  void generateArrayValueWithItems() {
    PropertyDefinition items = prop("tag", "string");
    PropertyDefinition p = propWithItems("tags", items);
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("[").endsWith("]");
  }

  @Test
  void generateArrayValueWithoutItems() {
    PropertyDefinition p =
        new PropertyDefinition("tags", "array", null, null, false, List.of(), null, Map.of(), null);
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("[\"item_1\"]");
  }

  @Test
  void generateArrayValueMaxDepth() {
    PropertyDefinition items = prop("name", "string");
    PropertyDefinition p = propWithItems("tags", items);
    String value = service.generatePropertyValue(p, 0, 4);

    assertThat(value).isEqualTo("[]");
  }

  @Test
  void generateNestedObjectValue() {
    PropertyDefinition street = prop("street", "string");
    PropertyDefinition city = prop("city", "string");
    PropertyDefinition p = propWithNested("address", Map.of("street", street, "city", city));
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).startsWith("{").contains("street").contains("city").endsWith("}");
  }

  @Test
  void generateNestedObjectValueEmpty() {
    PropertyDefinition p = propWithNested("address", Map.of());
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isEqualTo("{}");
  }

  @Test
  void generateNestedObjectValueMaxDepth() {
    PropertyDefinition street = prop("street", "string");
    PropertyDefinition p = propWithNested("address", Map.of("street", street));
    String value = service.generatePropertyValue(p, 0, 4);

    assertThat(value).isEqualTo("{}");
  }

  @Test
  void generatePropertyValueNullTypeDefaultsToString() {
    PropertyDefinition p =
        new PropertyDefinition("field", null, null, null, false, List.of(), null, Map.of(), null);
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueNullName() {
    PropertyDefinition p =
        new PropertyDefinition(null, "string", null, null, false, List.of(), null, Map.of(), null);
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatePropertyValueNullFormat() {
    PropertyDefinition p =
        new PropertyDefinition(
            "field", "string", null, null, false, List.of(), null, Map.of(), null);
    String value = service.generatePropertyValue(p, 0, 0);

    assertThat(value).isNotEmpty();
  }

  @Test
  void generatedJsonContainsPropertyName() {
    SchemaDefinition s = schema("User", prop("name", "string"), prop("email", "string", "email"));
    GeneratedExample result = service.generateExamples(s, 1);

    String json = result.exampleJsonStrings().get(0);
    assertThat(json).contains("\"name\"").contains("\"email\"");
  }

  @Test
  void multipleExamplesHaveDifferentValues() {
    SchemaDefinition s = schema("User", prop("age", "integer"));
    GeneratedExample result = service.generateExamples(s, 3);

    List<String> examples = result.exampleJsonStrings();
    assertThat(examples).hasSize(3);
    assertThat(examples.get(0)).isNotEqualTo(examples.get(1));
  }
}
