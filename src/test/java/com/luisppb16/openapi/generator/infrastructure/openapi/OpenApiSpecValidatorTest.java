/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.infrastructure.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class OpenApiSpecValidatorTest {

  private final OpenApiSpecValidator validator = new OpenApiSpecValidator();

  private String resourcePath(String name) {
    String encoded = getClass().getClassLoader().getResource(name).getFile();
    return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
  }

  @Test
  void validSpecHasNoMessages() {
    OpenApiSpecValidator.ValidationResult result =
        validator.validate(resourcePath("test-spec.yaml"));

    assertThat(result.parsed()).isTrue();
    assertThat(result.isValid()).isTrue();
    assertThat(result.openApiVersion()).isNotNull();
  }

  @Test
  void specWithBrokenRefReportsMessages() {
    OpenApiSpecValidator.ValidationResult result =
        validator.validate(resourcePath("invalid-spec.yaml"));

    assertThat(result.isValid()).isFalse();
    assertThat(result.messages()).isNotEmpty();
  }

  @Test
  void nonexistentFileIsNotParsed() {
    OpenApiSpecValidator.ValidationResult result = validator.validate("/nonexistent/spec.yaml");

    assertThat(result.parsed()).isFalse();
    assertThat(result.isValid()).isFalse();
  }
}
