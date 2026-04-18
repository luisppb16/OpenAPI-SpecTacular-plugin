/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.ui.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.luisppb16.openapi.generator.application.service.ExampleGenerationApplicationService;
import com.luisppb16.openapi.generator.domain.model.ExampleGenerationRequest;
import com.luisppb16.openapi.generator.domain.model.GeneratedExample;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import com.luisppb16.openapi.generator.domain.service.ExampleGenerationDomainService;
import com.luisppb16.openapi.generator.infrastructure.openapi.OpenApiSpecificationParser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main dialog for the OpenAPI Example Generator plugin. Provides a modern, intuitive UI for
 * selecting schemas and generating examples.
 */
@SuppressWarnings("this-escape")
public class GenerateExamplesDialog extends DialogWrapper {

  private final Project project;
  private final ExampleGenerationApplicationService applicationService;
  private final SchemaCountTableModel tableModel;
  private TextFieldWithBrowseButton specFileField;
  private TextFieldWithBrowseButton outputDirField;
  private JBLabel totalCountLabel;
  private JBLabel statusLabel;
  private JSpinner globalCountSpinner;
  private JButton parseButton;
  private JBCheckBox combineCheckbox;
  private List<SchemaDefinition> loadedSchemas;
  private boolean generationCompleted;
  private String autoParsePath;

  public GenerateExamplesDialog(@Nullable Project project) {
    super(project, true);
    this.project = project;
    this.applicationService =
        new ExampleGenerationApplicationService(
            new OpenApiSpecificationParser(), new ExampleGenerationDomainService());
    this.tableModel = new SchemaCountTableModel();
    setTitle("OpenAPI Example Generator");
    setSize(800, 600);
    init();
  }

  private static void addAutoCommitOnType(JSpinner spinner, JTextField textField) {
    textField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent e) {
                commitSpinnerEdit(spinner);
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                commitSpinnerEdit(spinner);
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                commitSpinnerEdit(spinner);
              }
            });
  }

  private static void commitSpinnerEdit(JSpinner spinner) {
    SwingUtilities.invokeLater(
        () -> {
          try {
            spinner.commitEdit();
          } catch (ParseException ignored) {
          }
        });
  }

  public void setSpecFilePath(String path) {
    this.autoParsePath = path;
    if (specFileField != null) specFileField.setText(path);
  }

  @Override
  public void show() {
    super.show();
    if (autoParsePath != null && !autoParsePath.isEmpty()) {
      SwingUtilities.invokeLater(() -> onParseSpec(null));
    }
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout(0, JBUI.scale(8)));
    mainPanel.setBorder(JBUI.Borders.empty(12));

    mainPanel.add(createTopPanel(), BorderLayout.NORTH);
    mainPanel.add(createTablePanel(), BorderLayout.CENTER);
    mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

    return mainPanel;
  }

  private JPanel createTopPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1),
            "OpenAPI Specification",
            0,
            0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            JBColor.foreground()));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = JBUI.insets(4, 8);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    panel.add(new JBLabel("Spec file:"), gbc);

    specFileField = new TextFieldWithBrowseButton();
    specFileField.addBrowseFolderListener(
        project,
        FileChooserDescriptorFactory.singleFile()
            .withTitle("Select OpenAPI Specification")
            .withDescription("Choose a YAML or JSON OpenAPI specification file")
            .withExtensionFilter("YAML & JSON Files", "yaml", "yml", "json"));
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    panel.add(specFileField, gbc);

    parseButton = new JButton("Parse Spec");
    parseButton.setIcon(AllIcons.Actions.Find);
    parseButton.addActionListener(this::onParseSpec);
    gbc.gridx = 2;
    gbc.weightx = 0;
    panel.add(parseButton, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0;
    panel.add(new JBLabel("Output dir:"), gbc);

    outputDirField = new TextFieldWithBrowseButton();
    if (project != null && project.getBasePath() != null)
      outputDirField.setText(project.getBasePath() + "/src/main/resources/openapi-examples");
    outputDirField.addBrowseFolderListener(
        project,
        FileChooserDescriptorFactory.singleDir()
            .withTitle("Select Output Directory")
            .withDescription("Choose where to save the generated example files"));
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    panel.add(outputDirField, gbc);

    statusLabel = new JBLabel(" ");
    statusLabel.setForeground(JBColor.GRAY);
    gbc.gridx = 2;
    gbc.weightx = 0;
    panel.add(statusLabel, gbc);

    return panel;
  }

  private JPanel createTablePanel() {
    JBTable schemaTable;
    JButton applyGlobalButton;
    JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(4)));
    panel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1),
            "Schemas",
            0,
            0,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            JBColor.foreground()));

    JPanel globalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), JBUI.scale(4)));

    globalPanel.add(new JBLabel("Set all counts to:"));
    globalCountSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    JSpinner.NumberEditor globalSpinnerEditor = new JSpinner.NumberEditor(globalCountSpinner, "#");
    globalCountSpinner.setEditor(globalSpinnerEditor);
    addAutoCommitOnType(globalCountSpinner, globalSpinnerEditor.getTextField());
    globalSpinnerEditor
        .getTextField()
        .addFocusListener(
            new FocusAdapter() {
              @Override
              public void focusLost(FocusEvent e) {
                try {
                  globalCountSpinner.commitEdit();
                } catch (ParseException ignored) {
                }
              }
            });
    globalCountSpinner.setPreferredSize(
        new Dimension(JBUI.scale(80), globalCountSpinner.getPreferredSize().height));
    globalPanel.add(globalCountSpinner);

    applyGlobalButton = new JButton("Apply to All");
    applyGlobalButton.addActionListener(e -> applyGlobalCount());
    globalPanel.add(applyGlobalButton);

    totalCountLabel = new JBLabel("Total examples: 0");
    totalCountLabel.setFont(totalCountLabel.getFont().deriveFont(Font.BOLD));
    totalCountLabel.setForeground(JBColor.namedColor("Button.foreground", JBColor.foreground()));
    totalCountLabel.setBorder(JBUI.Borders.emptyLeft(JBUI.scale(16)));
    globalPanel.add(totalCountLabel);

    panel.add(globalPanel, BorderLayout.NORTH);

    schemaTable = new JBTable(tableModel);
    schemaTable.setRowHeight(JBUI.scale(28));
    schemaTable.setFillsViewportHeight(true);
    schemaTable.setShowGrid(true);
    schemaTable.setGridColor(JBColor.border());
    schemaTable.getTableHeader().setReorderingAllowed(false);

    setColumnWidth(schemaTable.getColumnModel().getColumn(0), JBUI.scale(180), JBUI.scale(300));
    setColumnWidth(schemaTable.getColumnModel().getColumn(1), JBUI.scale(80), JBUI.scale(80));
    setColumnWidth(schemaTable.getColumnModel().getColumn(2), JBUI.scale(200), JBUI.scale(400));
    setColumnWidth(schemaTable.getColumnModel().getColumn(3), JBUI.scale(90), JBUI.scale(90));

    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    schemaTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
    schemaTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

    TableColumn examplesColumn = schemaTable.getColumnModel().getColumn(3);
    examplesColumn.setCellEditor(new SpinnerCellEditor(0, 9999));

    tableModel.addTableModelListener(e -> updateTotalCount());

    JBScrollPane scrollPane = new JBScrollPane(schemaTable);
    scrollPane.setPreferredSize(new Dimension(-1, JBUI.scale(300)));
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createBottomPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    infoPanel.add(new JBLabel(AllIcons.General.Information));
    infoPanel.add(
        new JBLabel("Generated files will be saved to the output directory as JSON files."));

    panel.add(infoPanel, BorderLayout.WEST);

    combineCheckbox = new JBCheckBox("Combine into single JSON");
    panel.add(combineCheckbox, BorderLayout.EAST);

    return panel;
  }

  private void refreshProject() {
    if (project == null || project.getBasePath() == null) return;
    VirtualFileManager.getInstance().syncRefresh();
  }

  private void setColumnWidth(TableColumn column, int preferred, int max) {
    column.setPreferredWidth(preferred);
    column.setMaxWidth(max);
    column.setMinWidth(Math.min(preferred, JBUI.scale(50)));
  }

  private void onParseSpec(ActionEvent e) {
    String specPath = specFileField.getText().trim();
    if (specPath.isEmpty()) {
      statusLabel.setText("Please select a spec file.");
      statusLabel.setForeground(JBColor.RED);
      return;
    }

    parseButton.setEnabled(false);
    statusLabel.setText("Parsing...");
    statusLabel.setForeground(JBColor.GRAY);

    ProgressManager.getInstance()
        .run(
            new Task.Backgroundable(project, "Parsing OpenAPI specification", false) {
              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.setText("Loading OpenAPI specification...");
                try {
                  List<SchemaDefinition> schemas = applicationService.loadSchemas(specPath);
                  SwingUtilities.invokeLater(
                      () -> {
                        loadedSchemas = schemas;
                        tableModel.setSchemas(
                            schemas, ((Number) globalCountSpinner.getValue()).intValue());
                        updateTotalCount();
                        statusLabel.setText("Found " + schemas.size() + " schema(s).");
                        statusLabel.setForeground(JBColor.GREEN);
                        parseButton.setEnabled(true);
                      });
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(
                      () -> {
                        statusLabel.setText("Parse error.");
                        statusLabel.setForeground(JBColor.RED);
                        Messages.showErrorDialog(
                            project,
                            "Failed to parse the OpenAPI specification:\n" + ex.getMessage(),
                            "Parse Error");
                        parseButton.setEnabled(true);
                      });
                }
              }
            });
  }

  private void applyGlobalCount() {
    int count = ((Number) globalCountSpinner.getValue()).intValue();
    tableModel.setAllCounts(count);
    updateTotalCount();
  }

  private void updateTotalCount() {
    int total = tableModel.getTotalCount();
    totalCountLabel.setText("Total examples: " + total);
  }

  @Override
  protected void doOKAction() {
    if (generationCompleted) {
      refreshProject();
      super.doOKAction();
      return;
    }

    if (!validateInput()) {
      return;
    }

    executeGeneration();
  }

  private boolean validateInput() {
    if (loadedSchemas == null || loadedSchemas.isEmpty()) {
      Messages.showWarningDialog(
          project, "Please parse an OpenAPI specification file first.", "No Schemas Loaded");
      return false;
    }

    if (tableModel.getTotalCount() == 0) {
      Messages.showWarningDialog(
          project,
          "Please set at least 1 example for at least one schema.",
          "No Examples Requested");
      return false;
    }

    String outputDir = outputDirField.getText().trim();
    if (outputDir.isEmpty()) {
      Messages.showWarningDialog(
          project, "Please specify an output directory.", "No Output Directory");
      return false;
    }

    return true;
  }

  private void executeGeneration() {
    getOKAction().setEnabled(false);

    ExampleGenerationRequest request = buildGenerationRequest();

    ProgressManager.getInstance()
        .run(
            new Task.Backgroundable(project, "Generating OpenAPI examples", false) {
              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                performGeneration(indicator, request);
              }
            });
  }

  private ExampleGenerationRequest buildGenerationRequest() {
    Map<String, Integer> counts = tableModel.getSchemaCountMap();
    String outputDir = outputDirField.getText().trim();
    boolean combine = combineCheckbox.isSelected();

    return new ExampleGenerationRequest(specFileField.getText().trim(), counts, outputDir, combine);
  }

  private void performGeneration(ProgressIndicator indicator, ExampleGenerationRequest request) {
    indicator.setIndeterminate(false);
    indicator.setText("Generating examples...");

    try {
      List<GeneratedExample> results = applicationService.generateExamples(request);
      Path outputPath = Paths.get(request.outputDirectory());
      Files.createDirectories(outputPath);

      writeResults(indicator, outputPath, results, request.combineOutput());

      int totalWritten = results.stream().mapToInt(GeneratedExample::getCount).sum();
      handleGenerationSuccess(totalWritten, request.outputDirectory());
    } catch (Exception ex) {
      handleGenerationError(ex);
    }
  }

  private void writeResults(
      ProgressIndicator indicator, Path outputPath, List<GeneratedExample> results, boolean combine)
      throws IOException {
    if (combine) {
      indicator.setText("Writing combined file...");
      writeCombinedFile(outputPath, results);
    } else {
      writeIndividualFiles(indicator, outputPath, results);
    }
  }

  private void writeIndividualFiles(
      ProgressIndicator indicator, Path outputPath, List<GeneratedExample> results) {
    results.forEach(
        result -> {
          indicator.setText("Writing " + result.schemaName() + "...");
          indicator.setFraction((double) results.indexOf(result) / results.size());
          try {
            writeExamplesToFile(outputPath, result);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private void handleGenerationSuccess(int totalWritten, String outputDir) {
    SwingUtilities.invokeLater(
        () -> {
          notifySuccess(totalWritten, outputDir);
          generationCompleted = true;
          getOKAction().putValue(Action.NAME, "Done");
          getOKAction().setEnabled(true);
        });
  }

  private void handleGenerationError(Exception ex) {
    SwingUtilities.invokeLater(
        () -> {
          Messages.showErrorDialog(
              project, "Failed to generate examples:\n" + ex.getMessage(), "Generation Error");
          getOKAction().setEnabled(true);
        });
  }

  private void writeExamplesToFile(Path outputDir, GeneratedExample result) throws IOException {
    Path filePath = outputDir.resolve(result.schemaName() + "-examples.json");
    List<String> examples = result.exampleJsonStrings();

    String content =
        examples.stream()
            .map(
                example ->
                    example
                        .lines()
                        .map(line -> "  " + line)
                        .collect(java.util.stream.Collectors.joining("\n")))
            .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));

    Files.writeString(filePath, content, StandardCharsets.UTF_8);
  }

  private void writeCombinedFile(Path outputDir, List<GeneratedExample> results)
      throws IOException {
    Path filePath = outputDir.resolve("all-examples.json");

    String content =
        results.stream()
            .map(
                result -> {
                  String examplesContent =
                      result.exampleJsonStrings().stream()
                          .map(
                              example ->
                                  example
                                      .lines()
                                      .map(line -> "    " + line)
                                      .collect(java.util.stream.Collectors.joining("\n")))
                          .collect(java.util.stream.Collectors.joining(",\n"));
                  return "  \"" + result.schemaName() + "\": [\n" + examplesContent + "\n  ]";
                })
            .collect(java.util.stream.Collectors.joining(",\n", "{\n", "\n}"));

    Files.writeString(filePath, content, StandardCharsets.UTF_8);
  }

  private void notifySuccess(int totalExamples, String outputDir) {
    try {
      NotificationGroupManager.getInstance()
          .getNotificationGroup("OpenAPI Generator")
          .createNotification(
              "Examples generated successfully",
              "Generated " + totalExamples + " example(s) in " + outputDir,
              NotificationType.INFORMATION)
          .notify(project);
    } catch (Exception e) {
      Messages.showInfoMessage(
          project,
          "Generated " + totalExamples + " example(s) in:\n" + outputDir,
          "Examples Generated Successfully");
    }
  }

  @Override
  protected Action[] createActions() {
    return new Action[] {getOKAction(), getCancelAction()};
  }

  @Override
  protected @Nullable String getHelpId() {
    return null;
  }

  private static class SpinnerCellEditor extends DefaultCellEditor {
    @Serial private static final long serialVersionUID = 1L;
    private final JSpinner spinner;
    private final SpinnerNumberModel spinnerModel;

    public SpinnerCellEditor(int min, int max) {
      super(new JTextField());
      spinnerModel = new SpinnerNumberModel(1, min, max, 1);
      spinner = new JSpinner(spinnerModel);
      JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
      spinner.setEditor(editor);

      addAutoCommitOnType(spinner, editor.getTextField());

      editor
          .getTextField()
          .addFocusListener(
              new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                  try {
                    spinner.commitEdit();
                  } catch (ParseException ignored) {
                  }
                  stopCellEditing();
                }
              });

      spinner.addChangeListener(e -> stopCellEditing());

      editorComponent = spinner;
      clickCountToStart = 1;
      delegate =
          new EditorDelegate() {
            @Override
            public void setValue(Object value) {
              spinnerModel.setValue(value instanceof Number n ? n.intValue() : 1);
            }

            @Override
            public Object getCellEditorValue() {
              return ((Number) spinnerModel.getValue()).intValue();
            }
          };
    }

    @Override
    public boolean stopCellEditing() {
      try {
        spinner.commitEdit();
      } catch (ParseException ignored) {
      }
      return super.stopCellEditing();
    }
  }
}
