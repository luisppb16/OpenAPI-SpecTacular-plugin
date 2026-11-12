/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.luisppb16.openapi.generator.domain.model.OutputFormat;
import javax.swing.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

/** Settings page (Settings > Tools > OpenAPI SpecTacular) for the plugin defaults. */
public class ExampleGeneratorConfigurable implements Configurable {

  private JBTextField outputDirField;
  private JSpinner exampleCountSpinner;
  private ComboBox<OutputFormat> formatCombo;
  private JBCheckBox combineCheckbox;
  private JPanel panel;

  private static int spinnerValue(JSpinner spinner) {
    return ((Number) spinner.getValue()).intValue();
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
    return "OpenAPI SpecTacular";
  }

  @Override
  public @Nullable JComponent createComponent() {
    outputDirField = new JBTextField();
    exampleCountSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    formatCombo = new ComboBox<>(OutputFormat.values());
    combineCheckbox = new JBCheckBox("Combine all schemas into a single file");

    panel =
        FormBuilder.createFormBuilder()
            .addLabeledComponent("Default output directory (relative to project):", outputDirField)
            .addLabeledComponent("Default examples per schema:", exampleCountSpinner)
            .addLabeledComponent("Default output format:", formatCombo)
            .addComponent(combineCheckbox)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    reset();
    return panel;
  }

  @Override
  public boolean isModified() {
    ExampleGeneratorSettings.State state = ExampleGeneratorSettings.getInstance().getState();
    return !outputDirField.getText().equals(state.defaultOutputDir)
        || spinnerValue(exampleCountSpinner) != state.defaultExampleCount
        || !String.valueOf(formatCombo.getSelectedItem()).equals(state.outputFormat)
        || combineCheckbox.isSelected() != state.combineOutput;
  }

  @Override
  public void apply() {
    ExampleGeneratorSettings.State state = ExampleGeneratorSettings.getInstance().getState();
    state.defaultOutputDir = outputDirField.getText().trim();
    state.defaultExampleCount = spinnerValue(exampleCountSpinner);
    state.outputFormat = String.valueOf(formatCombo.getSelectedItem());
    state.combineOutput = combineCheckbox.isSelected();
  }

  @Override
  public void reset() {
    ExampleGeneratorSettings.State state = ExampleGeneratorSettings.getInstance().getState();
    outputDirField.setText(state.defaultOutputDir);
    exampleCountSpinner.setValue(state.defaultExampleCount);
    formatCombo.setSelectedItem(OutputFormat.fromName(state.outputFormat));
    combineCheckbox.setSelected(state.combineOutput);
  }

  @Override
  public void disposeUIResources() {
    panel = null;
    outputDirField = null;
    exampleCountSpinner = null;
    formatCombo = null;
    combineCheckbox = null;
  }
}
