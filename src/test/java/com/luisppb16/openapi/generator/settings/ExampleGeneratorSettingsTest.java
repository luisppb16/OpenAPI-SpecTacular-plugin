/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.settings;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleGeneratorSettingsTest {

    @Test
    void stateHasSensibleDefaults() {
        ExampleGeneratorSettings.State state = new ExampleGeneratorSettings.State();

        assertThat(state.defaultOutputDir).isEqualTo("src/main/resources/openapi-examples");
        assertThat(state.defaultExampleCount).isEqualTo(1);
        assertThat(state.outputFormat).isEqualTo("JSON");
        assertThat(state.combineOutput).isFalse();
    }

    @Test
    void loadStateRoundTrip() {
        ExampleGeneratorSettings settings = new ExampleGeneratorSettings();
        ExampleGeneratorSettings.State state = new ExampleGeneratorSettings.State();
        state.defaultOutputDir = "custom/dir";
        state.defaultExampleCount = 7;
        state.outputFormat = "YAML";
        state.combineOutput = true;

        settings.loadState(state);

        ExampleGeneratorSettings.State loaded = settings.getState();
        assertThat(loaded.defaultOutputDir).isEqualTo("custom/dir");
        assertThat(loaded.defaultExampleCount).isEqualTo(7);
        assertThat(loaded.outputFormat).isEqualTo("YAML");
        assertThat(loaded.combineOutput).isTrue();
    }
}
