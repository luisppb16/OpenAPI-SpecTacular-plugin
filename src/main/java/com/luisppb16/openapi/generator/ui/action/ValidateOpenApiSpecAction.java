/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.ui.action;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.luisppb16.openapi.generator.infrastructure.openapi.OpenApiSpecValidator;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * Validates the selected OpenAPI specification and reports parser errors and warnings.
 */
public class ValidateOpenApiSpecAction extends AnAction {

    private static final int MAX_MESSAGES_IN_BALLOON = 5;

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isSpec = vf != null && OpenApiFiles.isOpenApiFile(vf);
        if (ActionPlaces.isPopupPlace(e.getPlace())) {
            e.getPresentation().setEnabledAndVisible(isSpec);
        } else {
            // Tools menu: keep the entry visible, but it needs a spec file in focus to run.
            e.getPresentation().setVisible(true);
            e.getPresentation().setEnabled(isSpec);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (vf == null) return;
        String path = vf.getPath();

        ProgressManager.getInstance()
                .run(
                        new Task.Backgroundable(project, "Validating OpenAPI specification", false) {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                indicator.setIndeterminate(true);
                                OpenApiSpecValidator.ValidationResult result =
                                        new OpenApiSpecValidator().validate(path);
                                ApplicationManager.getApplication()
                                        .invokeLater(() -> showResult(project, vf.getName(), result));
                            }
                        });
    }

    private void showResult(
            Project project, String fileName, OpenApiSpecValidator.ValidationResult result) {
        if (result.isValid()) {
            notify(
                    project,
                    "OpenAPI spec is valid",
                    fileName + " parsed without errors (OpenAPI " + result.openApiVersion() + ").",
                    NotificationType.INFORMATION);
            return;
        }

        String title =
                result.parsed()
                        ? "OpenAPI spec has " + result.messages().size() + " issue(s)"
                        : "OpenAPI spec could not be parsed";
        String summary =
                result.messages().stream()
                        .limit(MAX_MESSAGES_IN_BALLOON)
                        .collect(Collectors.joining("<br/>"));
        if (result.messages().size() > MAX_MESSAGES_IN_BALLOON) {
            summary += "<br/>…";
        }

        NotificationGroupManager.getInstance()
                .getNotificationGroup("OpenAPI Generator")
                .createNotification(title, summary, result.parsed() ? NotificationType.WARNING : NotificationType.ERROR)
                .addAction(
                        com.intellij.notification.NotificationAction.createSimple(
                                "Show all details",
                                () ->
                                        Messages.showMessageDialog(
                                                project,
                                                String.join("\n\n", result.messages()),
                                                "Validation Details: " + fileName,
                                                Messages.getWarningIcon())))
                .notify(project);
    }

    private void notify(Project project, String title, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("OpenAPI Generator")
                .createNotification(title, content, type)
                .notify(project);
    }
}
