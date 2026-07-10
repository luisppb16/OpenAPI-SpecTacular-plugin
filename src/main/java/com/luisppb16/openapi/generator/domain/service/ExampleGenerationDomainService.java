/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.PropertyDefinition;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Domain service that generates example values based on OpenAPI property types, formats, and
 * constraints. Contains the core business logic for example generation.
 */
public class ExampleGenerationDomainService {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_DEPTH = 3;

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
        List<JsonNode> examples =
                IntStream.range(0, count)
                        .mapToObj(i -> generateExample(schema, new GenerationContext(i, 0)))
                        .toList();
        return new GeneratedExample(schema.name(), examples);
    }

    private JsonNode generateExample(SchemaDefinition schema, GenerationContext ctx) {
        if (schema.isArrayType()) {
            ArrayNode array = NODES.arrayNode();
            array.add(generateObject(schema.properties(), ctx.deeper()));
            return array;
        }
        return generateObject(schema.properties(), ctx);
    }

    private ObjectNode generateObject(List<PropertyDefinition> properties, GenerationContext ctx) {
        ObjectNode node = NODES.objectNode();
        properties.forEach(prop -> node.set(prop.name(), generatePropertyValue(prop, ctx)));
        return node;
    }

    JsonNode generatePropertyValue(PropertyDefinition prop, GenerationContext ctx) {
        if (prop.exampleValue() != null) return MAPPER.valueToTree(prop.exampleValue());
        if (!prop.enumValues().isEmpty()) return generateEnumValue(prop, ctx);
        if (!prop.oneOfVariants().isEmpty()) return generateVariant(prop.oneOfVariants(), ctx);
        if (!prop.anyOfVariants().isEmpty()) return generateVariant(prop.anyOfVariants(), ctx);

        String type = prop.type() != null ? prop.type().toLowerCase() : "string";
        String format = prop.format() != null ? prop.format().toLowerCase() : "";
        String name = prop.name() != null ? prop.name().toLowerCase() : "";

        return switch (type) {
            case "integer", "int" -> generateIntegerValue(prop, name, format, ctx);
            case "number", "float", "double" -> generateNumberValue(prop, name, ctx);
            case "boolean", "bool" -> NODES.booleanNode(ctx.pick(2) == 0);
            case "array" -> generateArrayValue(prop, ctx);
            case "object" -> generateNestedObjectValue(prop, ctx);
            default -> generateStringNode(prop, name, format, ctx);
        };
    }

    /**
     * Convenience entry point used by tests: generates a value for an isolated property.
     */
    JsonNode generatePropertyValue(PropertyDefinition prop, int index, int depth) {
        return generatePropertyValue(prop, new GenerationContext(index, depth));
    }

    private JsonNode generateEnumValue(PropertyDefinition prop, GenerationContext ctx) {
        String value = prop.enumValues().get(ctx.pick(prop.enumValues().size()));
        String type = prop.type() != null ? prop.type().toLowerCase() : "string";
        try {
            if (type.equals("integer") || type.equals("int")) return NODES.numberNode(Long.parseLong(value));
            if (type.equals("number")) return NODES.numberNode(new BigDecimal(value));
        } catch (NumberFormatException ignored) {
            // fall through to text
        }
        return NODES.textNode(value);
    }

    private JsonNode generateVariant(List<PropertyDefinition> variants, GenerationContext ctx) {
        PropertyDefinition variant = variants.get(ctx.pick(variants.size()));
        return generatePropertyValue(variant, ctx);
    }

    private JsonNode generateIntegerValue(
            PropertyDefinition prop, String name, String format, GenerationContext ctx) {
        int index = ctx.index();
        long value =
                switch (name) {
                    case String n when n.contains("age") -> 18 + index % 62;
                    case String n when n.contains("year") -> 2020 + index % 10;
                    case String n when n.contains("count") || n.contains("total") || n.contains("quantity") ->
                            (index + 1) * 5L;
                    case String n when n.contains("price") || n.contains("amount") || n.contains("cost") ->
                            (index + 1) * 100L;
                    case String n when n.contains("id") -> index + 1L;
                    default -> "int64".equals(format) ? (index + 1) * 1000000L : (index + 1) * 10L;
                };
        if ("int32".equals(format)) {
            value = Math.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        value = clampToBounds(value, prop.minimum(), prop.maximum());
        return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE
                ? NODES.numberNode((int) value)
                : NODES.numberNode(value);
    }

    private long clampToBounds(long value, BigDecimal minimum, BigDecimal maximum) {
        if (minimum != null && BigDecimal.valueOf(value).compareTo(minimum) < 0) {
            value = minimum.setScale(0, java.math.RoundingMode.CEILING).longValueExact();
        }
        if (maximum != null && BigDecimal.valueOf(value).compareTo(maximum) > 0) {
            value = maximum.setScale(0, java.math.RoundingMode.FLOOR).longValueExact();
        }
        return value;
    }

    private JsonNode generateNumberValue(PropertyDefinition prop, String name, GenerationContext ctx) {
        int index = ctx.index();
        double value =
                switch (name) {
                    case String n when n.contains("price") || n.contains("amount") || n.contains("cost") ->
                            (index + 1) * 9.99;
                    case String n when n.contains("lat") || n.contains("latitude") -> -90.0 + (index * 11.37) % 180.0;
                    case String n when n.contains("lon") || n.contains("longitude") || n.contains("lng") ->
                            -180.0 + (index * 22.73) % 360.0;
                    case String n when n.contains("percent") || n.contains("rate") || n.contains("ratio") ->
                            (index % 100) * 1.0;
                    default -> (index + 1) * 3.14;
                };
        value = Math.round(value * 100.0) / 100.0;
        if (prop.minimum() != null && value < prop.minimum().doubleValue())
            value = prop.minimum().doubleValue();
        if (prop.maximum() != null && value > prop.maximum().doubleValue())
            value = prop.maximum().doubleValue();
        return NODES.numberNode(value);
    }

    private JsonNode generateArrayValue(PropertyDefinition prop, GenerationContext ctx) {
        ArrayNode array = NODES.arrayNode();
        if (ctx.depth() > MAX_DEPTH) return array;

        int size = prop.minItems() != null ? Math.max(prop.minItems(), 1) : 1;
        if (prop.maxItems() != null) size = Math.min(size, Math.max(prop.maxItems(), 0));

        if (prop.items() == null) {
            IntStream.range(0, size).forEach(i -> array.add("item_" + (ctx.index() + i + 1)));
            return array;
        }
        IntStream.range(0, size)
                .forEach(
                        i ->
                                array.add(
                                        generatePropertyValue(
                                                prop.items(), ctx.deeper().withIndex(ctx.index() + i))));
        return array;
    }

    private JsonNode generateNestedObjectValue(PropertyDefinition prop, GenerationContext ctx) {
        if (ctx.depth() > MAX_DEPTH || prop.nestedProperties().isEmpty()) return NODES.objectNode();
        return generateObject(new ArrayList<>(prop.nestedProperties().values()), ctx.deeper());
    }

    private JsonNode generateStringNode(
            PropertyDefinition prop, String name, String format, GenerationContext ctx) {
        String value = generateStringValue(name, format, ctx);
        if (format.isEmpty()) value = applyLengthConstraints(value, prop.minLength(), prop.maxLength());
        return NODES.textNode(value);
    }

    private String applyLengthConstraints(String value, Integer minLength, Integer maxLength) {
        if (minLength != null && value.length() < minLength) {
            StringBuilder sb = new StringBuilder(value);
            while (sb.length() < minLength) sb.append('x');
            value = sb.toString();
        }
        if (maxLength != null && value.length() > maxLength) {
            value = value.substring(0, maxLength);
        }
        return value;
    }

    private String generateStringValue(String name, String format, GenerationContext ctx) {
        int index = ctx.index();

        return switch (format) {
            case "date" -> LocalDate.now().minusDays(index).format(DateTimeFormatter.ISO_DATE);
            case "date-time", "datetime" ->
                    LocalDateTime.now().minusHours(index).format(DateTimeFormatter.ISO_DATE_TIME);
            case "time" -> LocalTime.of((8 + index) % 24, (index * 7) % 60, 0).format(DateTimeFormatter.ISO_TIME);
            case "duration" -> Duration.ofMinutes(15L * (index + 1)).toString();
            case "email" -> {
                String first = FIRST_NAMES.get(index % FIRST_NAMES.size()).toLowerCase();
                yield first + (index + 1) + "@" + DOMAINS.get(index % DOMAINS.size());
            }
            case "uuid" -> generateUUID(index);
            case "uri", "url" -> "https://api.example.com/resource/" + (index + 1);
            case "hostname" -> "host-" + (index + 1) + ".example.com";
            case "ipv4" -> "192.168." + (index % 256) + "." + ((index * 7 + 1) % 256);
            case "ipv6" -> "2001:db8::" + Integer.toHexString(index + 1);
            case "password" -> "P@ssw0rd" + (index + 1) + "!";
            case "byte" -> Base64.getEncoder().encodeToString(("example" + index).getBytes());
            default -> generateStringByName(name, index);
        };
    }

    private String generateStringByName(String name, int index) {
        return switch (name) {
            case String n when n.contains("email") || n.contains("mail") -> {
                String first = FIRST_NAMES.get(index % FIRST_NAMES.size()).toLowerCase();
                yield first + (index + 1) + "@" + DOMAINS.get(index % DOMAINS.size());
            }
            case String n
                    when n.contains("firstname")
                    || (n.contains("first") && n.contains("name"))
                    || n.contains("givenname") -> FIRST_NAMES.get(index % FIRST_NAMES.size());
            case String n
                    when n.contains("lastname")
                    || n.contains("familyname")
                    || n.contains("surname")
                    || (n.contains("last") && n.contains("name")) -> LAST_NAMES.get(index % LAST_NAMES.size());
            case String n when n.endsWith("name") -> FIRST_NAMES.get(index % FIRST_NAMES.size())
                    + " "
                    + LAST_NAMES.get(index % LAST_NAMES.size());
            case String n when n.contains("phone") || n.contains("tel") -> "+1-555-"
                    + String.format("%03d", index + 100)
                    + "-"
                    + String.format("%04d", index * 7 + 1000);
            case String n when n.contains("city") -> CITIES.get(index % CITIES.size());
            case String n when n.contains("country") -> COUNTRIES.get(index % COUNTRIES.size());
            case String n when n.contains("street") || n.contains("address") ->
                    (index * 10 + 1) + " " + STREETS.get(index % STREETS.size());
            case String n when n.contains("zip") || n.contains("postal") -> String.format("%05d", (index + 1) * 10001);
            case String n when n.contains("url") || n.contains("link") || n.contains("href") ->
                    "https://example.com/" + name + "/" + (index + 1);
            case String n
                    when n.contains("description")
                    || n.contains("text")
                    || n.contains("content")
                    || n.contains("body") ->
                    "Sample " + name + " " + (index + 1) + " - This is a generated example value.";
            case String n when n.contains("title") || n.contains("subject") || n.contains("label") ->
                    "Example " + capitalize(name) + " " + (index + 1);
            case String n when n.contains("status") || n.contains("state") -> {
                String[] statuses = {"active", "inactive", "pending", "completed", "cancelled"};
                yield statuses[index % statuses.length];
            }
            case String n when n.contains("type") || n.contains("category") || n.contains("kind") -> {
                String[] types = {"type_a", "type_b", "type_c", "category_1", "category_2"};
                yield types[index % types.length];
            }
            case String n when n.contains("code") || n.contains("key") ->
                    name.toUpperCase() + "_" + String.format("%04d", index + 1);
            case String n when n.contains("color") || n.contains("colour") -> {
                String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#FF33F5", "#F5FF33"};
                yield colors[index % colors.length];
            }
            case String n when n.contains("version") -> (index + 1) + "." + (index % 10) + "." + (index % 5);
            case String n when n.endsWith("id") -> generateUUID(index);
            default -> capitalize(name) + "_example_" + (index + 1);
        };
    }

    private String generateUUID(int index) {
        long mostSig = 0x0000000000004000L | (index * 0x123456789ABCDEFL & 0x0000FFFFFFFFFFFFL);
        long leastSig = 0x8000000000000000L | (index * 0xFEDCBA987654321L & 0x3FFFFFFFFFFFFFFFL);
        return new UUID(mostSig, leastSig).toString();
    }

    private String capitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Generation state carried through the recursive example tree build.
     */
    record GenerationContext(int index, int depth) {
        GenerationContext deeper() {
            return new GenerationContext(index, depth + 1);
        }

        GenerationContext withIndex(int newIndex) {
            return new GenerationContext(newIndex, depth);
        }

        int pick(int bound) {
            if (bound <= 0) return 0;
            return index % bound;
        }
    }
}
