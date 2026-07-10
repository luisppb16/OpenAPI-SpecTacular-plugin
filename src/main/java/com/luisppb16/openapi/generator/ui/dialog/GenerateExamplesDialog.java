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
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.*;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.luisppb16.openapi.generator.application.service.ExampleGenerationApplicationService;
import com.luisppb16.openapi.generator.domain.model.ExampleGenerationRequest;
import com.luisppb16.openapi.generator.domain.model.OutputFormat;
import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import com.luisppb16.openapi.generator.domain.service.ExampleGenerationDomainService;
import com.luisppb16.openapi.generator.infrastructure.io.ExampleFileWriter;
import com.luisppb16.openapi.generator.infrastructure.openapi.OpenApiSpecificationParser;
import com.luisppb16.openapi.generator.settings.ExampleGeneratorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.Serial;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Main dialog for the OpenAPI Example Generator plugin. Provides a modern, intuitive UI for
 * selecting schemas, previewing, and generating examples.
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
    private ComboBox<OutputFormat> formatCombo;
    private JBTabbedPane tabbedPane;
    private JBTextArea previewArea;
    private JButton copyPreviewButton;
    private List<SchemaDefinition> loadedSchemas;
    private boolean generationCompleted;
    private String autoParsePath;

    public GenerateExamplesDialog(@Nullable Project project) {
        super(project, true);
        this.project = project;
        this.applicationService =
                new ExampleGenerationApplicationService(
                        new OpenApiSpecificationParser(),
                        new ExampleGenerationDomainService(),
                        new ExampleFileWriter());
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

    private static OutputFormat parseFormat(String value) {
        try {
            return OutputFormat.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return OutputFormat.JSON;
        }
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

        tabbedPane = new JBTabbedPane();
        tabbedPane.addTab("Schemas", createTablePanel());
        tabbedPane.addTab("Preview", createPreviewPanel());

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
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
        outputDirField.setText(defaultOutputDirectory());
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

    private String defaultOutputDirectory() {
        String configured = ExampleGeneratorSettings.getInstance().getState().defaultOutputDir;
        if (project != null && project.getBasePath() != null) {
            Path candidate = Paths.get(configured);
            return candidate.isAbsolute()
                    ? configured
                    : project.getBasePath() + "/" + configured;
        }
        return Paths.get(configured).isAbsolute() ? configured : "";
    }

    private JPanel createTablePanel() {
        JBTable schemaTable;
        JButton applyGlobalButton;
        JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(4)));

        JPanel globalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), JBUI.scale(4)));

        globalPanel.add(new JBLabel("Set all counts to:"));
        int defaultCount = ExampleGeneratorSettings.getInstance().getState().defaultExampleCount;
        globalCountSpinner = new JSpinner(new SpinnerNumberModel(defaultCount, 0, 9999, 1));
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

    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(4)));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), JBUI.scale(4)));
        JButton refreshButton = new JButton("Refresh Preview", AllIcons.Actions.Refresh);
        refreshButton.addActionListener(e -> onPreview());
        toolbar.add(refreshButton);

        copyPreviewButton = new JButton("Copy", AllIcons.Actions.Copy);
        copyPreviewButton.setEnabled(false);
        copyPreviewButton.addActionListener(e -> copyPreviewToClipboard());
        toolbar.add(copyPreviewButton);
        panel.add(toolbar, BorderLayout.NORTH);

        previewArea = new JBTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, previewArea.getFont().getSize()));
        previewArea.setText("Parse a spec and click \"Refresh Preview\" to see the generated output.");
        panel.add(new JBScrollPane(previewArea), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        ExampleGeneratorSettings.State settings = ExampleGeneratorSettings.getInstance().getState();

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0));
        optionsPanel.add(new JBLabel("Format:"));
        formatCombo = new ComboBox<>(OutputFormat.values());
        formatCombo.setSelectedItem(parseFormat(settings.outputFormat));
        optionsPanel.add(formatCombo);

        combineCheckbox = new JBCheckBox("Combine into single file");
        combineCheckbox.setSelected(settings.combineOutput);
        optionsPanel.add(combineCheckbox);

        panel.add(optionsPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        infoPanel.add(new JBLabel(AllIcons.General.Information));
        infoPanel.add(new JBLabel("Files are saved to the output directory."));
        panel.add(infoPanel, BorderLayout.EAST);

        return panel;
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

    private void onPreview() {
        if (loadedSchemas == null || loadedSchemas.isEmpty()) {
            previewArea.setText("Parse an OpenAPI specification first (Schemas tab).");
            return;
        }
        if (tableModel.getTotalCount() == 0) {
            previewArea.setText("Set at least 1 example for at least one schema.");
            return;
        }

        ExampleGenerationRequest request = buildGenerationRequest();
        previewArea.setText("Generating preview...");

        ProgressManager.getInstance()
                .run(
                        new Task.Backgroundable(project, "Generating preview", false) {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                indicator.setIndeterminate(true);
                                try {
                                    String preview = applicationService.renderPreview(request);
                                    SwingUtilities.invokeLater(
                                            () -> {
                                                previewArea.setText(preview);
                                                previewArea.setCaretPosition(0);
                                                copyPreviewButton.setEnabled(true);
                                            });
                                } catch (Exception ex) {
                                    SwingUtilities.invokeLater(
                                            () -> {
                                                previewArea.setText("Preview failed:\n" + ex.getMessage());
                                                copyPreviewButton.setEnabled(false);
                                            });
                                }
                            }
                        });
    }

    private void copyPreviewToClipboard() {
        CopyPasteManager.getInstance().setContents(new StringSelection(previewArea.getText()));
        statusLabel.setText("Preview copied to clipboard.");
        statusLabel.setForeground(JBColor.GREEN);
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
        rememberChoices(request);

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

        return new ExampleGenerationRequest(
                specFileField.getText().trim(),
                counts,
                outputDir,
                combineCheckbox.isSelected(),
                (OutputFormat) formatCombo.getSelectedItem());
    }

    private void rememberChoices(ExampleGenerationRequest request) {
        ExampleGeneratorSettings.State settings = ExampleGeneratorSettings.getInstance().getState();
        settings.outputFormat = request.outputFormat().name();
        settings.combineOutput = request.combineOutput();
    }

    private void performGeneration(ProgressIndicator indicator, ExampleGenerationRequest request) {
        indicator.setIndeterminate(true);
        indicator.setText("Generating examples...");

        try {
            ExampleGenerationApplicationService.GenerationSummary summary =
                    applicationService.generateAndWrite(request);

            indicator.setText("Refreshing files...");
            List<Path> writtenPaths =
                    summary.files().stream().map(ExampleFileWriter.WrittenFile::path).toList();
            LocalFileSystem.getInstance().refreshNioFiles(writtenPaths, true, false, null);

            handleGenerationSuccess(summary.totalExamples(), request.outputDirectory());
        } catch (Exception ex) {
            handleGenerationError(ex);
        }
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
        return new Action[]{getOKAction(), getCancelAction()};
    }

    @Override
    protected @Nullable String getHelpId() {
        return null;
    }

    private static class SpinnerCellEditor extends DefaultCellEditor {
        @Serial
        private static final long serialVersionUID = 1L;
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
