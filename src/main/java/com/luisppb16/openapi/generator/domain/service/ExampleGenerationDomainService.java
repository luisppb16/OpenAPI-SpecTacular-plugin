/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.service;

import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.PropertyDefinition;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Domain service that generates example values based on OpenAPI property types and formats.
 * Contains the core business logic, for example, generation.
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
    List<String> examples =
        IntStream.range(0, count).mapToObj(i -> generateExampleJson(schema, i)).toList();
    return new GeneratedExample(schema.name(), examples);
  }

  private String generateExampleJson(SchemaDefinition schema, int index) {
    return schema.isArrayType()
        ? "[\n  " + generateObjectJson(schema.properties(), index, 1) + "\n]"
        : generateObjectJson(schema.properties(), index, 0);
  }

  private String generateObjectJson(List<PropertyDefinition> properties, int index, int depth) {
    if (properties.isEmpty()) return "{}";
    StringBuilder sb = new StringBuilder("{\n");
    String indent = "  ".repeat(depth + 1);
    String closingIndent = "  ".repeat(depth);

    IntStream.range(0, properties.size())
        .forEach(
            i -> {
              PropertyDefinition prop = properties.get(i);
              sb.append(indent).append('"').append(prop.name()).append("\": ");
              sb.append(generatePropertyValue(prop, index, depth));
              if (i < properties.size() - 1) sb.append(',');
              sb.append('\n');
            });
    sb.append(closingIndent).append('}');
    return sb.toString();
  }

  String generatePropertyValue(PropertyDefinition prop, int index, int depth) {
    if (prop.exampleValue() != null)
      return prop.exampleValue() instanceof String s
          ? '"' + escapeJson(s) + '"'
          : String.valueOf(prop.exampleValue());
    if (!prop.enumValues().isEmpty())
      return '"' + escapeJson(prop.enumValues().get(index % prop.enumValues().size())) + '"';

    String type = prop.type() != null ? prop.type().toLowerCase() : "string";
    String format = prop.format() != null ? prop.format().toLowerCase() : "";
    String name = prop.name() != null ? prop.name().toLowerCase() : "";

    return switch (type) {
      case "integer", "int" -> generateIntegerValue(name, format, index);
      case "number", "float", "double" -> generateNumberValue(name, index);
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
    return switch (name) {
      case String n when n.contains("email") || n.contains("mail") -> {
        String first = FIRST_NAMES.get(index % FIRST_NAMES.size()).toLowerCase();
        yield '"' + first + (index + 1) + "@" + DOMAINS.get(index % DOMAINS.size()) + '"';
      }
      case String n
          when n.contains("firstname")
              || (n.contains("first") && n.contains("name"))
              || n.contains("givenname") ->
          '"' + FIRST_NAMES.get(index % FIRST_NAMES.size()) + '"';
      case String n
          when n.contains("lastname")
              || n.contains("familyname")
              || n.contains("surname")
              || (n.contains("last") && n.contains("name")) ->
          '"' + LAST_NAMES.get(index % LAST_NAMES.size()) + '"';
      case String n when n.endsWith("name") ->
          '"'
              + FIRST_NAMES.get(index % FIRST_NAMES.size())
              + " "
              + LAST_NAMES.get(index % LAST_NAMES.size())
              + '"';
      case String n when n.contains("phone") || n.contains("tel") ->
          '"'
              + "+1-555-"
              + String.format("%03d", index + 100)
              + "-"
              + String.format("%04d", index * 7 + 1000)
              + '"';
      case String n when n.contains("city") -> '"' + CITIES.get(index % CITIES.size()) + '"';
      case String n when n.contains("country") ->
          '"' + COUNTRIES.get(index % COUNTRIES.size()) + '"';
      case String n when n.contains("street") || n.contains("address") ->
          '"' + (index * 10 + 1) + " " + STREETS.get(index % STREETS.size()) + '"';
      case String n when n.contains("zip") || n.contains("postal") ->
          '"' + String.format("%05d", (index + 1) * 10001) + '"';
      case String n when n.contains("url") || n.contains("link") || n.contains("href") ->
          '"' + "https://example.com/" + name + "/" + (index + 1) + '"';
      case String n
          when n.contains("description")
              || n.contains("text")
              || n.contains("content")
              || n.contains("body") ->
          '"'
              + "Sample "
              + name
              + " "
              + (index + 1)
              + " - This is a generated example value."
              + '"';
      case String n when n.contains("title") || n.contains("subject") || n.contains("label") ->
          '"' + "Example " + capitalize(name) + " " + (index + 1) + '"';
      case String n when n.contains("status") || n.contains("state") -> {
        String[] statuses = {"active", "inactive", "pending", "completed", "cancelled"};
        yield '"' + statuses[index % statuses.length] + '"';
      }
      case String n when n.contains("type") || n.contains("category") || n.contains("kind") -> {
        String[] types = {"type_a", "type_b", "type_c", "category_1", "category_2"};
        yield '"' + types[index % types.length] + '"';
      }
      case String n when n.contains("code") || n.contains("key") ->
          '"' + name.toUpperCase() + "_" + String.format("%04d", index + 1) + '"';
      case String n when n.contains("color") || n.contains("colour") -> {
        String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#FF33F5", "#F5FF33"};
        yield '"' + colors[index % colors.length] + '"';
      }
      case String n when n.contains("version") ->
          '"' + (index + 1) + "." + (index % 10) + "." + (index % 5) + '"';
      case String n when n.endsWith("id") -> '"' + generateUUID(index) + '"';
      default -> '"' + capitalize(name) + "_example_" + (index + 1) + '"';
    };
  }

  private String generateIntegerValue(String name, String format, int index) {
    return switch (name) {
      case String n when n.contains("age") -> String.valueOf(18 + index % 62);
      case String n when n.contains("year") -> String.valueOf(2020 + index % 10);
      case String n when n.contains("count") || n.contains("total") || n.contains("quantity") ->
          String.valueOf((index + 1) * 5);
      case String n when n.contains("price") || n.contains("amount") || n.contains("cost") ->
          String.valueOf((index + 1) * 100);
      case String n when n.contains("id") -> String.valueOf(index + 1);
      default ->
          "int64".equals(format)
              ? String.valueOf((index + 1) * 1000000L)
              : String.valueOf((index + 1) * 10);
    };
  }

  private String generateNumberValue(String name, int index) {
    return switch (name) {
      case String n when n.contains("price") || n.contains("amount") || n.contains("cost") ->
          String.format("%.2f", (index + 1) * 9.99);
      case String n when n.contains("lat") || n.contains("latitude") ->
          String.format("%.6f", -90.0 + (index * 11.37) % 180.0);
      case String n when n.contains("lon") || n.contains("longitude") || n.contains("lng") ->
          String.format("%.6f", -180.0 + (index * 22.73) % 360.0);
      case String n when n.contains("percent") || n.contains("rate") || n.contains("ratio") ->
          String.format("%.2f", (index % 100) * 1.0);
      default -> String.format("%.2f", (index + 1) * 3.14);
    };
  }

  private String generateArrayValue(PropertyDefinition prop, int index, int depth) {
    if (depth > 3) {
      return "[]";
    }
    if (prop.items() == null) {
      return "[\"item_" + (index + 1) + "\"]";
    }

    String indentNext = "  ".repeat(depth + 1);
    String indentCurrent = "  ".repeat(depth);
    String itemValue = generatePropertyValue(prop.items(), index, depth + 1);
    return "[\n" + indentNext + itemValue + '\n' + indentCurrent + ']';
  }

  private String generateNestedObjectValue(PropertyDefinition prop, int index, int depth) {
    if (depth > 3) {
      return "{}";
    }
    if (prop.nestedProperties().isEmpty()) {
      return "{}";
    }

    return generateObjectJson(new ArrayList<>(prop.nestedProperties().values()), index, depth + 1);
  }

  private String generateUUID(int index) {
    long mostSig = 0x0000000000004000L | (index * 0x123456789ABCDEFL & 0x0000FFFFFFFFFFFFL);
    long leastSig = 0x8000000000000000L | (index * 0xFEDCBA987654321L & 0x3FFFFFFFFFFFFFFFL);
    return new UUID(mostSig, leastSig).toString();
  }

  private String escapeJson(String value) {
    return value == null
        ? ""
        : value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
  }

  private String capitalize(String s) {
    return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
}
