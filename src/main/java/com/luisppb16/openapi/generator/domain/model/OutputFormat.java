/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.domain.model;

/**
 * Supported serialization formats for generated example files.
 */
public enum OutputFormat {
    JSON("json"),
    YAML("yaml");

    private final String extension;

    OutputFormat(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }

    public static OutputFormat fromName(String name) {
        try {
            return OutputFormat.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException wrongValue) {
            return JSON;
        }
    }
}
