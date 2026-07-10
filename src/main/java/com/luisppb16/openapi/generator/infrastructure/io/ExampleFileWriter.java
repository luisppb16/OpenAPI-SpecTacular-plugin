/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.infrastructure.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.OutputFormat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes generated examples to disk (plain NIO, no VFS — callers on the UI side are responsible
 * for refreshing the IntelliJ virtual file system afterwards) and renders them to text for
 * previewing.
 */
public class ExampleFileWriter {

    private static final ObjectWriter JSON_WRITER =
            new ObjectMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectWriter YAML_WRITER =
            YAMLMapper.builder()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .build()
                    .writer();

    public List<WrittenFile> write(
            Path outputDir, List<GeneratedExample> results, boolean combine, OutputFormat format)
            throws IOException {
        Files.createDirectories(outputDir);
        List<WrittenFile> written = new ArrayList<>();

        if (combine) {
            Path filePath = outputDir.resolve("all-examples." + format.extension());
            Files.writeString(filePath, serialize(combinedNode(results), format), StandardCharsets.UTF_8);
            written.add(new WrittenFile(filePath, null));
            return written;
        }

        for (GeneratedExample result : results) {
            Path filePath = outputDir.resolve(result.schemaName() + "-examples." + format.extension());
            Files.writeString(filePath, serialize(examplesNode(result), format), StandardCharsets.UTF_8);
            written.add(new WrittenFile(filePath, result.schemaName()));
        }
        return written;
    }

    /**
     * Serializes the results to a single text block without touching the filesystem.
     */
    public String render(List<GeneratedExample> results, boolean combine, OutputFormat format) {
        if (combine) {
            return serialize(combinedNode(results), format);
        }
        StringBuilder sb = new StringBuilder();
        for (GeneratedExample result : results) {
            if (!sb.isEmpty()) sb.append(format == OutputFormat.JSON ? "\n\n" : "\n");
            if (format == OutputFormat.YAML) sb.append("# ").append(result.schemaName()).append('\n');
            sb.append(serialize(examplesNode(result), format));
        }
        return sb.toString();
    }

    private ArrayNode examplesNode(GeneratedExample result) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        result.examples().forEach(array::add);
        return array;
    }

    private ObjectNode combinedNode(List<GeneratedExample> results) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        results.forEach(result -> root.set(result.schemaName(), examplesNode(result)));
        return root;
    }

    private String serialize(JsonNode node, OutputFormat format) {
        try {
            return (format == OutputFormat.YAML ? YAML_WRITER : JSON_WRITER).writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize generated examples", e);
        }
    }

    /**
     * A file written to disk for a given schema ({@code schemaName} is null for combined files).
     */
    public record WrittenFile(Path path, String schemaName) {
    }
}
