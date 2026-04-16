/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.ui.dialog;

import com.openapi.generator.domain.model.SchemaDefinition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/** Table model for the schema selection table in the Generate Examples dialog. */
public class SchemaCountTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES = {
    "Schema Name", "Properties", "Description", "Examples"
  };
  private static final Class<?>[] COLUMN_TYPES = {
    String.class, Integer.class, String.class, Integer.class
  };

  private final List<SchemaDefinition> schemas = new ArrayList<>();
  private final List<Integer> exampleCounts = new ArrayList<>();

  public void setSchemas(List<SchemaDefinition> schemas, int defaultCount) {
    this.schemas.clear();
    this.exampleCounts.clear();
    this.schemas.addAll(schemas);
    for (int i = 0; i < schemas.size(); i++) {
      this.exampleCounts.add(defaultCount);
    }
    fireTableDataChanged();
  }

  public void setAllCounts(int count) {
    for (int i = 0; i < exampleCounts.size(); i++) {
      exampleCounts.set(i, Math.max(0, count));
    }
    fireTableDataChanged();
  }

  public List<SchemaDefinition> getSchemas() {
    return List.copyOf(schemas);
  }

  public int getCountForSchema(int rowIndex) {
    return exampleCounts.get(rowIndex);
  }

  public int getTotalCount() {
    return exampleCounts.stream().mapToInt(Integer::intValue).sum();
  }

  public Map<String, Integer> getSchemaCountMap() {
    Map<String, Integer> map = new LinkedHashMap<>();
    for (int i = 0; i < schemas.size(); i++) {
      map.put(schemas.get(i).getName(), exampleCounts.get(i));
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
      case 0 -> schema.getName();
      case 1 -> schema.getProperties().size();
      case 2 -> schema.getDescription() != null ? schema.getDescription() : "";
      case 3 -> exampleCounts.get(rowIndex);
      default -> "";
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 3 && aValue instanceof Integer count) {
      exampleCounts.set(rowIndex, Math.max(0, count));
      fireTableCellUpdated(rowIndex, columnIndex);
    }
  }
}
