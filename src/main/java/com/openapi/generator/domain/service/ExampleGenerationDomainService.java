/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.domain.service;

import com.openapi.generator.domain.model.GeneratedExample;
import com.openapi.generator.domain.model.PropertyDefinition;
import com.openapi.generator.domain.model.SchemaDefinition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Domain service that generates example values based on OpenAPI property types and formats.
 * Contains the core business logic for example generation.
 */
public class ExampleGenerationDomainService {

  private static final List<String> FIRST_NAMES =
      List.of(
          "Alice", "Bob", "Carol", "David", "Eve", "Frank", "Grace", "Henry", "Iris", "Jack",
          "Karen", "Liam", "Mia", "Noah", "Olivia", "Paul");

  private static final List<String> LAST_NAMES =
      List.of(
          "Smith",
          "Johnson",
          "Williams",
          "Brown",
          "Jones",
          "Garcia",
          "Miller",
          "Davis",
          "Wilson",
          "Moore",
          "Taylor",
          "Anderson",
          "Thomas",
          "Jackson");

  private static final List<String> DOMAINS =
      List.of("example.com", "test.org", "demo.io", "sample.net", "mock.dev");

  private static final List<String> CITIES =
      List.of("New York", "London", "Paris", "Tokyo", "Berlin", "Sydney", "Toronto");

  private static final List<String> COUNTRIES =
      List.of("US", "GB", "FR", "JP", "DE", "AU", "CA", "BR", "IT", "ES");

  private static final List<String> STREETS =
      List.of("Main St", "Oak Ave", "Maple Dr", "Cedar Ln", "Pine Rd", "Elm Blvd");

  public GeneratedExample generateExamples(SchemaDefinition schema, int count) {
    List<String> examples = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      String json = generateExampleJson(schema, i);
      examples.add(json);
    }
    return new GeneratedExample(schema.getName(), examples);
  }

  private String generateExampleJson(SchemaDefinition schema, int index) {
    if (schema.isArrayType()) {
      StringBuilder sb = new StringBuilder("[\n");
      sb.append("  ").append(generateObjectJson(schema.getProperties(), index, 1));
      sb.append("\n]");
      return sb.toString();
    }
    return generateObjectJson(schema.getProperties(), index, 0);
  }

  private String generateObjectJson(List<PropertyDefinition> properties, int index, int depth) {
    if (properties.isEmpty()) {
      return "{}";
    }
    StringBuilder sb = new StringBuilder("{\n");
    String indent = "  ".repeat(depth + 1);
    String closingIndent = "  ".repeat(depth);

    for (int i = 0; i < properties.size(); i++) {
      PropertyDefinition prop = properties.get(i);
      sb.append(indent).append('"').append(prop.getName()).append("\": ");
      sb.append(generatePropertyValue(prop, index, depth));
      if (i < properties.size() - 1) {
        sb.append(',');
      }
      sb.append('\n');
    }
    sb.append(closingIndent).append('}');
    return sb.toString();
  }

  private String generatePropertyValue(PropertyDefinition prop, int index, int depth) {
    if (prop.getExampleValue() != null) {
      Object example = prop.getExampleValue();
      if (example instanceof String s) {
        return '"' + escapeJson(s) + '"';
      }
      return String.valueOf(example);
    }

    if (!prop.getEnumValues().isEmpty()) {
      String enumVal = prop.getEnumValues().get(index % prop.getEnumValues().size());
      return '"' + escapeJson(enumVal) + '"';
    }

    String type = prop.getType() != null ? prop.getType().toLowerCase() : "string";
    String format = prop.getFormat() != null ? prop.getFormat().toLowerCase() : "";
    String name = prop.getName() != null ? prop.getName().toLowerCase() : "";

    return switch (type) {
      case "integer", "int" -> generateIntegerValue(name, format, index);
      case "number", "float", "double" -> generateNumberValue(name, format, index);
      case "boolean", "bool" -> String.valueOf(index % 2 == 0);
      case "array" -> generateArrayValue(prop, index, depth);
      case "object" -> generateNestedObjectValue(prop, index, depth);
      default -> generateStringValue(name, format, index);
    };
  }

  private String generateStringValue(String name, String format, int index) {
    return switch (format) {
      case "date" ->
          '"' + LocalDate.now().minusDays(index).format(DateTimeFormatter.ISO_DATE) + '"';
      case "date-time", "datetime" ->
          '"' + LocalDateTime.now().minusHours(index).format(DateTimeFormatter.ISO_DATE_TIME) + '"';
      case "email" -> {
        String first = FIRST_NAMES.get(index % FIRST_NAMES.size()).toLowerCase();
        String domain = DOMAINS.get(index % DOMAINS.size());
        yield '"' + first + (index + 1) + "@" + domain + '"';
      }
      case "uuid" -> '"' + generateUUID(index) + '"';
      case "uri", "url" -> '"' + "https://api.example.com/resource/" + (index + 1) + '"';
      case "ipv4" -> '"' + "192.168." + (index % 256) + "." + ((index * 7 + 1) % 256) + '"';
      case "password" -> '"' + "P@ssw0rd" + (index + 1) + "!" + '"';
      case "byte" -> '"' + Base64.getEncoder().encodeToString(("example" + index).getBytes()) + '"';
      default -> generateStringByName(name, index);
    };
  }

  private String generateStringByName(String name, int index) {
    if (name.contains("email") || name.contains("mail")) {
      String first = FIRST_NAMES.get(index % FIRST_NAMES.size()).toLowerCase();
      return '"' + first + (index + 1) + "@" + DOMAINS.get(index % DOMAINS.size()) + '"';
    } else if (name.contains("firstname")
        || (name.contains("first") && name.contains("name"))
        || name.contains("givenname")) {
      return '"' + FIRST_NAMES.get(index % FIRST_NAMES.size()) + '"';
    } else if (name.contains("lastname")
        || name.contains("familyname")
        || name.contains("surname")
        || (name.contains("last") && name.contains("name"))) {
      return '"' + LAST_NAMES.get(index % LAST_NAMES.size()) + '"';
    } else if (name.equals("name") || name.endsWith("name")) {
      return '"'
          + FIRST_NAMES.get(index % FIRST_NAMES.size())
          + " "
          + LAST_NAMES.get(index % LAST_NAMES.size())
          + '"';
    } else if (name.contains("phone") || name.contains("tel")) {
      return '"'
          + "+1-555-"
          + String.format("%03d", index + 100)
          + "-"
          + String.format("%04d", index * 7 + 1000)
          + '"';
    } else if (name.contains("city")) {
      return '"' + CITIES.get(index % CITIES.size()) + '"';
    } else if (name.contains("country")) {
      return '"' + COUNTRIES.get(index % COUNTRIES.size()) + '"';
    } else if (name.contains("street") || name.contains("address")) {
      return '"' + (index * 10 + 1) + " " + STREETS.get(index % STREETS.size()) + '"';
    } else if (name.contains("zip") || name.contains("postal")) {
      return '"' + String.format("%05d", (index + 1) * 10001) + '"';
    } else if (name.contains("url") || name.contains("link") || name.contains("href")) {
      return '"' + "https://example.com/" + name + "/" + (index + 1) + '"';
    } else if (name.contains("description")
        || name.contains("text")
        || name.contains("content")
        || name.contains("body")) {
      return '"'
          + "Sample "
          + name
          + " "
          + (index + 1)
          + " - This is a generated example value."
          + '"';
    } else if (name.contains("title") || name.contains("subject") || name.contains("label")) {
      return '"' + "Example " + capitalize(name) + " " + (index + 1) + '"';
    } else if (name.contains("status") || name.contains("state")) {
      String[] statuses = {"active", "inactive", "pending", "completed", "cancelled"};
      return '"' + statuses[index % statuses.length] + '"';
    } else if (name.contains("type") || name.contains("category") || name.contains("kind")) {
      String[] types = {"type_a", "type_b", "type_c", "category_1", "category_2"};
      return '"' + types[index % types.length] + '"';
    } else if (name.contains("code") || name.contains("key")) {
      return '"' + name.toUpperCase() + "_" + String.format("%04d", index + 1) + '"';
    } else if (name.contains("color") || name.contains("colour")) {
      String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#FF33F5", "#F5FF33"};
      return '"' + colors[index % colors.length] + '"';
    } else if (name.contains("version")) {
      return '"' + (index + 1) + "." + (index % 10) + "." + (index % 5) + '"';
    } else if (name.endsWith("id") || name.equals("id")) {
      return '"' + generateUUID(index) + '"';
    } else {
      return '"' + capitalize(name) + "_example_" + (index + 1) + '"';
    }
  }

  private String generateIntegerValue(String name, String format, int index) {
    if (name.contains("age")) {
      return String.valueOf(18 + index % 62);
    } else if (name.contains("year")) {
      return String.valueOf(2020 + index % 10);
    } else if (name.contains("count") || name.contains("total") || name.contains("quantity")) {
      return String.valueOf((index + 1) * 5);
    } else if (name.contains("price") || name.contains("amount") || name.contains("cost")) {
      return String.valueOf((index + 1) * 100);
    } else if (name.contains("id")) {
      return String.valueOf(index + 1);
    } else if ("int64".equals(format)) {
      return String.valueOf((long) (index + 1) * 1000000L);
    } else {
      return String.valueOf((index + 1) * 10);
    }
  }

  private String generateNumberValue(String name, String format, int index) {
    if (name.contains("price") || name.contains("amount") || name.contains("cost")) {
      return String.format("%.2f", (index + 1) * 9.99);
    } else if (name.contains("lat") || name.contains("latitude")) {
      return String.format("%.6f", -90.0 + (index * 11.37) % 180.0);
    } else if (name.contains("lon") || name.contains("longitude") || name.contains("lng")) {
      return String.format("%.6f", -180.0 + (index * 22.73) % 360.0);
    } else if (name.contains("percent") || name.contains("rate") || name.contains("ratio")) {
      return String.format("%.2f", (index % 100) * 1.0);
    } else {
      return String.format("%.2f", (index + 1) * 3.14);
    }
  }

  private String generateArrayValue(PropertyDefinition prop, int index, int depth) {
    if (depth > 3) return "[]";
    if (prop.getItems() == null) {
      return "[\"item_" + (index + 1) + "\"]";
    }
    StringBuilder sb = new StringBuilder("[\n");
    String indent = "  ".repeat(depth + 1);
    sb.append(indent).append(generatePropertyValue(prop.getItems(), index, depth + 1));
    sb.append('\n').append("  ".repeat(depth)).append(']');
    return sb.toString();
  }

  private String generateNestedObjectValue(PropertyDefinition prop, int index, int depth) {
    if (depth > 3) return "{}";
    if (prop.getNestedProperties().isEmpty()) {
      return "{}";
    }
    List<PropertyDefinition> nestedProps = new ArrayList<>(prop.getNestedProperties().values());
    return generateObjectJson(nestedProps, index, depth + 1);
  }

  private String generateUUID(int index) {
    long mostSig = 0x0000000000004000L | (index * 0x123456789ABCDEFL & 0x0000FFFFFFFFFFFFL);
    long leastSig = 0x8000000000000000L | (index * 0xFEDCBA987654321L & 0x3FFFFFFFFFFFFFFFL);
    return new UUID(mostSig, leastSig).toString();
  }

  private String escapeJson(String value) {
    if (value == null) return "";
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
}
