package com.openapi.generator.ui.dialog;

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
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBSpinner;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.openapi.generator.application.service.ExampleGenerationApplicationService;
import com.openapi.generator.domain.model.ExampleGenerationRequest;
import com.openapi.generator.domain.model.GeneratedExample;
import com.openapi.generator.domain.model.SchemaDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Main dialog for the OpenAPI Example Generator plugin.
 * Provides a modern, intuitive UI for selecting schemas and generating examples.
 */
public class GenerateExamplesDialog extends DialogWrapper {

    private final Project project;
    private final ExampleGenerationApplicationService applicationService;
    private final SchemaCountTableModel tableModel;

    private TextFieldWithBrowseButton specFileField;
    private TextFieldWithBrowseButton outputDirField;
    private JBTable schemaTable;
    private JBLabel totalCountLabel;
    private JBLabel statusLabel;
    private JSpinner globalCountSpinner;
    private JButton parseButton;
    private JButton applyGlobalButton;

    private List<SchemaDefinition> loadedSchemas;

    public GenerateExamplesDialog(@Nullable Project project) {
        super(project, true);
        this.project = project;
        this.applicationService = new ExampleGenerationApplicationService();
        this.tableModel = new SchemaCountTableModel();
        setTitle("OpenAPI Example Generator");
        setSize(800, 600);
        init();
    }

    public void setSpecFilePath(String path) {
        if (specFileField != null) {
            specFileField.setText(path);
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
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1),
                "OpenAPI Specification",
                0, 0,
                new Font(Font.SANS_SERIF, Font.BOLD, 12),
                JBColor.foreground()
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JBLabel("Spec File:"), gbc);

        specFileField = new TextFieldWithBrowseButton();
        specFileField.addBrowseFolderListener(
                "Select OpenAPI Specification",
                "Choose a YAML or JSON OpenAPI specification file",
                project,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withFileFilter(vf -> {
                            String ext = vf.getExtension();
                            return "yaml".equalsIgnoreCase(ext)
                                    || "yml".equalsIgnoreCase(ext)
                                    || "json".equalsIgnoreCase(ext);
                        })
        );
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(specFileField, gbc);

        parseButton = new JButton("Parse Spec");
        parseButton.setIcon(com.intellij.icons.AllIcons.Actions.Find);
        parseButton.addActionListener(this::onParseSpec);
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(parseButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JBLabel("Output Dir:"), gbc);

        outputDirField = new TextFieldWithBrowseButton();
        if (project != null && project.getBasePath() != null) {
            outputDirField.setText(project.getBasePath() + "/openapi-examples");
        }
        outputDirField.addBrowseFolderListener(
                "Select Output Directory",
                "Choose where to save the generated example files",
                project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(outputDirField, gbc);

        statusLabel = new JBLabel(" ");
        statusLabel.setForeground(JBColor.GRAY);
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(statusLabel, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, JBUI.scale(4)));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1),
                "Schemas",
                0, 0,
                new Font(Font.SANS_SERIF, Font.BOLD, 12),
                JBColor.foreground()
        ));

        JPanel globalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(8), JBUI.scale(4)));

        globalPanel.add(new JBLabel("Set all counts to:"));
        globalCountSpinner = new JBSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        globalCountSpinner.setPreferredSize(new Dimension(JBUI.scale(80), globalCountSpinner.getPreferredSize().height));
        globalPanel.add(globalCountSpinner);

        applyGlobalButton = new JButton("Apply to All");
        applyGlobalButton.addActionListener(e -> applyGlobalCount());
        globalPanel.add(applyGlobalButton);

        totalCountLabel = new JBLabel("Total examples: 0");
        totalCountLabel.setFont(totalCountLabel.getFont().deriveFont(Font.BOLD));
        totalCountLabel.setForeground(JBColor.namedColor("Button.foreground", JBColor.foreground()));
        totalCountLabel.setBorder(JBUI.Borders.empty(0, JBUI.scale(16), 0, 0));
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
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        schemaTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        schemaTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        TableColumn examplesColumn = schemaTable.getColumnModel().getColumn(3);
        examplesColumn.setCellEditor(new SpinnerCellEditor(0, 100));

        tableModel.addTableModelListener(e -> updateTotalCount());

        JBScrollPane scrollPane = new JBScrollPane(schemaTable);
        scrollPane.setPreferredSize(new Dimension(-1, JBUI.scale(300)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JBLabel(com.intellij.icons.AllIcons.General.Information));
        infoPanel.add(new JBLabel("Generated files will be saved to the output directory as JSON files."));

        panel.add(infoPanel, BorderLayout.WEST);
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

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Parsing OpenAPI Specification", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                indicator.setText("Loading OpenAPI specification...");
                try {
                    List<SchemaDefinition> schemas = applicationService.loadSchemas(specPath);
                    SwingUtilities.invokeLater(() -> {
                        loadedSchemas = schemas;
                        int defaultCount = (Integer) globalCountSpinner.getValue();
                        tableModel.setSchemas(schemas, defaultCount);
                        updateTotalCount();
                        statusLabel.setText("Found " + schemas.size() + " schema(s).");
                        statusLabel.setForeground(JBColor.GREEN);
                        parseButton.setEnabled(true);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Parse error.");
                        statusLabel.setForeground(JBColor.RED);
                        Messages.showErrorDialog(project,
                                "Failed to parse the OpenAPI specification:\n" + ex.getMessage(),
                                "Parse Error");
                        parseButton.setEnabled(true);
                    });
                }
            }
        });
    }

    private void applyGlobalCount() {
        int count = (Integer) globalCountSpinner.getValue();
        tableModel.setAllCounts(count);
        updateTotalCount();
    }

    private void updateTotalCount() {
        int total = tableModel.getTotalCount();
        totalCountLabel.setText("Total examples: " + total);
    }

    @Override
    protected void doOKAction() {
        if (loadedSchemas == null || loadedSchemas.isEmpty()) {
            Messages.showWarningDialog(project,
                    "Please parse an OpenAPI specification file first.",
                    "No Schemas Loaded");
            return;
        }

        Map<String, Integer> counts = tableModel.getSchemaCountMap();
        int total = tableModel.getTotalCount();
        if (total == 0) {
            Messages.showWarningDialog(project,
                    "Please set at least 1 example for at least one schema.",
                    "No Examples Requested");
            return;
        }

        String outputDir = outputDirField.getText().trim();
        if (outputDir.isEmpty()) {
            Messages.showWarningDialog(project, "Please specify an output directory.", "No Output Directory");
            return;
        }

        ExampleGenerationRequest request = new ExampleGenerationRequest(
                specFileField.getText().trim(), counts, outputDir);

        getOKAction().setEnabled(false);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Generating OpenAPI Examples", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setText("Generating examples...");
                try {
                    List<GeneratedExample> results = applicationService.generateExamples(request);
                    Path outputPath = Paths.get(outputDir);
                    Files.createDirectories(outputPath);

                    int count = 0;
                    for (GeneratedExample result : results) {
                        indicator.setText("Writing " + result.getSchemaName() + "...");
                        indicator.setFraction((double) count / results.size());
                        writeExamplesToFile(outputPath, result);
                        count++;
                    }

                    int totalWritten = results.stream().mapToInt(GeneratedExample::getCount).sum();
                    SwingUtilities.invokeLater(() -> {
                        notifySuccess(totalWritten, outputDir);
                        GenerateExamplesDialog.super.doOKAction();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog(project,
                                "Failed to generate examples:\n" + ex.getMessage(),
                                "Generation Error");
                        getOKAction().setEnabled(true);
                    });
                }
            }
        });
    }

    private void writeExamplesToFile(Path outputDir, GeneratedExample result) throws IOException {
        String fileName = result.getSchemaName() + "-examples.json";
        Path filePath = outputDir.resolve(fileName);

        StringBuilder content = new StringBuilder("[\n");
        List<String> examples = result.getExampleJsonStrings();
        for (int i = 0; i < examples.size(); i++) {
            String[] lines = examples.get(i).split("\n");
            for (String line : lines) {
                content.append("  ").append(line).append('\n');
            }
            if (i < examples.size() - 1) {
                content.setLength(content.length() - 1);
                content.append(",\n");
            }
        }
        content.append("]");

        Files.writeString(filePath, content.toString(), StandardCharsets.UTF_8);
    }

    private void notifySuccess(int totalExamples, String outputDir) {
        try {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("OpenAPI Generator")
                    .createNotification(
                            "Examples Generated Successfully",
                            "Generated " + totalExamples + " example(s) in " + outputDir,
                            NotificationType.INFORMATION
                    )
                    .notify(project);
        } catch (Exception e) {
            Messages.showInfoMessage(project,
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
        private final JSpinner spinner;
        private final SpinnerNumberModel spinnerModel;

        public SpinnerCellEditor(int min, int max) {
            super(new JTextField());
            spinnerModel = new SpinnerNumberModel(1, min, max, 1);
            spinner = new JSpinner(spinnerModel);
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
            spinner.setEditor(editor);

            editorComponent = spinner;
            delegate = new EditorDelegate() {
                @Override
                public void setValue(Object value) {
                    spinnerModel.setValue(value instanceof Integer ? value : 1);
                }

                @Override
                public Object getCellEditorValue() {
                    return spinnerModel.getValue();
                }
            };
        }
    }
}
