/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.luisppb16.openapi.generator.ui.dialog;

import com.luisppb16.openapi.generator.domain.model.SchemaDefinition;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/** Table model for the schema selection table in the Generate Examples dialog. */
public class SchemaCountTableModel extends AbstractTableModel {

  @Serial private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_NAMES = {
    "Schema Name", "Properties", "Description", "Examples"
  };
  private static final Class<?>[] COLUMN_TYPES = {
    String.class, Integer.class, String.class, Integer.class
  };

  private final transient List<SchemaDefinition> schemas = new ArrayList<>();
  private final transient List<Integer> exampleCounts = new ArrayList<>();

  private static int parseOrDefault(String s) {
    try {
      return Integer.parseInt(s.trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public void setSchemas(List<SchemaDefinition> schemas, int defaultCount) {
    this.schemas.clear();
    this.exampleCounts.clear();
    this.schemas.addAll(schemas);
    schemas.forEach(schema -> this.exampleCounts.add(defaultCount));
    fireTableDataChanged();
  }

  public void setAllCounts(int count) {
    exampleCounts.replaceAll(ignored -> Math.max(0, count));
    fireTableDataChanged();
  }

  public int getTotalCount() {
    return exampleCounts.stream().mapToInt(Integer::intValue).sum();
  }

  public Map<String, Integer> getSchemaCountMap() {
    Map<String, Integer> map = new LinkedHashMap<>();
    for (int i = 0; i < schemas.size(); i++) {
      map.put(schemas.get(i).name(), exampleCounts.get(i));
    }
    return map;
  }

  @Override
  public int getRowCount() {
    return schemas.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_TYPES[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 3;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    SchemaDefinition schema = schemas.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> schema.name();
      case 1 -> schema.properties().size();
      case 2 -> schema.description() != null ? schema.description() : "";
      case 3 -> exampleCounts.get(rowIndex);
      default -> "";
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex != 3) return;
    int value =
        aValue instanceof Number n
            ? n.intValue()
            : aValue instanceof String s ? parseOrDefault(s) : -1;
    if (value < 0) return;
    exampleCounts.set(rowIndex, value);
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}
