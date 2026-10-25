/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

/** Application-level persistent settings for the OpenAPI SpecTacular plugin. */
@State(name = "OpenApiSpecTacularSettings", storages = @Storage("openapiSpecTacular.xml"))
@Service(Service.Level.APP)
public final class ExampleGeneratorSettings
    implements PersistentStateComponent<ExampleGeneratorSettings.State> {

  private State state = new State();

  public static ExampleGeneratorSettings getInstance() {
    return ApplicationManager.getApplication().getService(ExampleGeneratorSettings.class);
  }

  @Override
  public @NotNull State getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull State state) {
    this.state = state;
  }

  /** Serializable settings bean; public fields as required by the XML serializer. */
  public static class State {
    public String defaultOutputDir = "src/main/resources/openapi-examples";
    public int defaultExampleCount = 1;
    public String outputFormat = "JSON";
    public boolean combineOutput = false;
  }
}
