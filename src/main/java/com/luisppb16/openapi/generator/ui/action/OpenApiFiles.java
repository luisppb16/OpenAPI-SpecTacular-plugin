/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.ui.action;

import com.intellij.openapi.vfs.VirtualFile;

/** Shared helpers for detecting OpenAPI candidate files. */
final class OpenApiFiles {

  private OpenApiFiles() {}

  static boolean isOpenApiFile(VirtualFile file) {
    String ext = file.getExtension();
    return "yaml".equalsIgnoreCase(ext)
        || "yml".equalsIgnoreCase(ext)
        || "json".equalsIgnoreCase(ext);
  }
}
