/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.ui.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.luisppb16.openapi.generator.ui.dialog.GenerateExamplesDialog;
import org.jetbrains.annotations.NotNull;

/**
 * IntelliJ action that triggers the OpenAPI Example Generator dialog from the editor or project
 * view context menu.
 */
public class GenerateExamplesAction extends AnAction {

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    GenerateExamplesDialog dialog = new GenerateExamplesDialog(project);
    VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (vf != null && OpenApiFiles.isOpenApiFile(vf)) {
      dialog.setSpecFilePath(vf.getPath());
    }
    dialog.show();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    if (e.isFromContextMenu()) {
      VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
      e.getPresentation().setEnabledAndVisible(vf != null && OpenApiFiles.isOpenApiFile(vf));
    } else {
      // Tools menu: always available; the dialog lets the user pick the spec file.
      e.getPresentation().setEnabledAndVisible(true);
    }
  }
}
