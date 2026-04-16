package com.openapi.generator.ui.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.openapi.generator.ui.dialog.GenerateExamplesDialog;
import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ action that opens the Generate Examples dialog.
 * Available from the Tools menu and from the right-click context menu on OpenAPI files.
 */
public class GenerateExamplesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        GenerateExamplesDialog dialog = new GenerateExamplesDialog(project);

        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && isOpenApiFile(virtualFile)) {
            dialog.setSpecFilePath(virtualFile.getPath());
        }

        dialog.show();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(true);
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile != null) {
            boolean isOpenApi = isOpenApiFile(virtualFile);
            String place = e.getPlace();
            if ("ProjectViewPopupMenu".equals(place) || "EditorPopupMenu".equals(place)) {
                e.getPresentation().setEnabledAndVisible(isOpenApi);
            }
        }
    }

    private boolean isOpenApiFile(VirtualFile file) {
        String ext = file.getExtension();
        return "yaml".equalsIgnoreCase(ext) || "yml".equalsIgnoreCase(ext) || "json".equalsIgnoreCase(ext);
    }
}
