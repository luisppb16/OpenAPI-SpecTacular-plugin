/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.infrastructure.openapi;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Helpers to normalize spec locations before handing them to the Swagger parser.
 */
final class OpenApiLocations {

    private OpenApiLocations() {
    }

    /**
     * Converts a local filesystem path into a percent-encoded {@code file:} URI so the parser does
     * not choke on characters that are illegal in URIs (e.g. spaces). Remote or non-filesystem
     * locations are returned untouched.
     */
    static String toParserLocation(String location) {
        if (location == null || location.startsWith("http://") || location.startsWith("https://")) {
            return location;
        }
        try {
            Path path = Path.of(location);
            if (Files.exists(path)) {
                return path.toUri().toString();
            }
        } catch (InvalidPathException ignored) {
            // Not a filesystem path; let the parser interpret it as-is.
        }
        return location;
    }
}
