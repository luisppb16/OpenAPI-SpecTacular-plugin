/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.ui.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.luisppb16.openapi.generator.ui.dialog.GenerateExamplesDialog;
import org.jetbrains.annotations.NotNull;

/** IntelliJ action that triggers the OpenAPI Example Generator dialog from the editor or project view context menu. */
public class GenerateExamplesAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    GenerateExamplesDialog dialog = new GenerateExamplesDialog(project);
    VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (vf != null && isOpenApiFile(vf)) {
      dialog.setSpecFilePath(vf.getPath());
    }
    dialog.show();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(true);
    VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (vf == null) return;
    String place = e.getPlace();
    boolean isContextMenu = "ProjectViewPopupMenu".equals(place) || "EditorPopupMenu".equals(place);
    e.getPresentation().setEnabledAndVisible(isContextMenu && isOpenApiFile(vf));
  }

  private boolean isOpenApiFile(VirtualFile file) {
    String ext = file.getExtension();
    return "yaml".equalsIgnoreCase(ext)
        || "yml".equalsIgnoreCase(ext)
        || "json".equalsIgnoreCase(ext);
  }
}
