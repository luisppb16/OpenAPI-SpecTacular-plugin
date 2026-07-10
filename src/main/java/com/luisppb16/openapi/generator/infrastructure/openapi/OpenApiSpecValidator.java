/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.infrastructure.openapi;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.List;

/**
 * Validates OpenAPI specification files and reports parser errors and warnings.
 */
public class OpenApiSpecValidator {

    public ValidationResult validate(String filePath) {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);

        SwaggerParseResult result =
                new OpenAPIParser().readLocation(OpenApiLocations.toParserLocation(filePath), null, options);

        List<String> messages =
                result.getMessages() == null
                        ? List.of()
                        : result.getMessages().stream().distinct().toList();
        boolean parsed = result.getOpenAPI() != null;
        String version = parsed ? result.getOpenAPI().getOpenapi() : null;
        return new ValidationResult(parsed, version, messages);
    }

    /**
     * Result of validating a spec: whether it could be parsed at all, the detected OpenAPI version
     * (null when unparseable), and the parser messages (empty when the spec is clean).
     */
    public record ValidationResult(boolean parsed, String openApiVersion, List<String> messages) {
        public boolean isValid() {
            return parsed && messages.isEmpty();
        }
    }
}
