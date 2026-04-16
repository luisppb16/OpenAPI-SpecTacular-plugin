/*
 * *****************************************************************************
 * Copyright (c)  2026 Luis Paolo Pepe Barra (@LuisPPB16).
 * All rights reserved.
 * *****************************************************************************
 */

package com.openapi.generator.ui.dialog;

import static org.assertj.core.api.Assertions.assertThat;

import com.openapi.generator.domain.model.PropertyDefinition;
import com.openapi.generator.domain.model.SchemaDefinition;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaCountTableModelTest {

  private SchemaCountTableModel model;

  private SchemaDefinition schema(String name) {
    return new SchemaDefinition(name, "desc for " + name, List.of(), false);
  }

  private SchemaDefinition schemaWithNullDesc(String name) {
    return new SchemaDefinition(name, null, List.of(), false);
  }

  @BeforeEach
  void setUp() {
    model = new SchemaCountTableModel();
  }

  @Test
  void emptyModelHasZeroRows() {
    assertThat(model.getRowCount()).isZero();
  }

  @Test
  void columnCount() {
    assertThat(model.getColumnCount()).isEqualTo(4);
  }

  @Test
  void columnNames() {
    assertThat(model.getColumnName(0)).isEqualTo("Schema Name");
    assertThat(model.getColumnName(1)).isEqualTo("Properties");
    assertThat(model.getColumnName(2)).isEqualTo("Description");
    assertThat(model.getColumnName(3)).isEqualTo("Examples");
  }

  @Test
  void columnClasses() {
    assertThat(model.getColumnClass(0)).isEqualTo(String.class);
    assertThat(model.getColumnClass(1)).isEqualTo(Integer.class);
    assertThat(model.getColumnClass(2)).isEqualTo(String.class);
    assertThat(model.getColumnClass(3)).isEqualTo(Integer.class);
  }

  @Test
  void onlyExamplesColumnIsEditable() {
    assertThat(model.isCellEditable(0, 0)).isFalse();
    assertThat(model.isCellEditable(0, 1)).isFalse();
    assertThat(model.isCellEditable(0, 2)).isFalse();
    assertThat(model.isCellEditable(0, 3)).isTrue();
  }

  @Test
  void setSchemasPopulatesTable() {
    model.setSchemas(List.of(schema("A"), schema("B")), 5);

    assertThat(model.getRowCount()).isEqualTo(2);
    assertThat(model.getValueAt(0, 0)).isEqualTo("A");
    assertThat(model.getValueAt(1, 0)).isEqualTo("B");
    assertThat(model.getValueAt(0, 3)).isEqualTo(5);
    assertThat(model.getValueAt(1, 3)).isEqualTo(5);
  }

  @Test
  void setSchemasReplacesExisting() {
    model.setSchemas(List.of(schema("A")), 1);
    model.setSchemas(List.of(schema("X"), schema("Y")), 3);

    assertThat(model.getRowCount()).isEqualTo(2);
    assertThat(model.getValueAt(0, 0)).isEqualTo("X");
  }

  @Test
  void getValueAtReturnsSchemaName() {
    model.setSchemas(List.of(schema("User")), 1);

    assertThat(model.getValueAt(0, 0)).isEqualTo("User");
  }

  @Test
  void getValueAtReturnsPropertyCount() {
    PropertyDefinition prop =
        new PropertyDefinition("id", "string", null, null, false, List.of(), null, Map.of(), null);
    SchemaDefinition s = new SchemaDefinition("User", null, List.of(prop, prop), false);
    model.setSchemas(List.of(s), 1);

    assertThat(model.getValueAt(0, 1)).isEqualTo(2);
  }

  @Test
  void getValueAtReturnsDescription() {
    model.setSchemas(List.of(schema("User")), 1);

    assertThat(model.getValueAt(0, 2)).isEqualTo("desc for User");
  }

  @Test
  void getValueAtReturnsEmptyForNullDescription() {
    model.setSchemas(List.of(schemaWithNullDesc("User")), 1);

    assertThat(model.getValueAt(0, 2)).isEqualTo("");
  }

  @Test
  void getValueAtReturnsExampleCount() {
    model.setSchemas(List.of(schema("A")), 7);

    assertThat(model.getValueAt(0, 3)).isEqualTo(7);
  }

  @Test
  void getValueAtDefaultForUnknownColumn() {
    model.setSchemas(List.of(schema("A")), 1);

    assertThat(model.getValueAt(0, 99)).isEqualTo("");
  }

  @Test
  void setValueAtUpdatesCountWithNumber() {
    model.setSchemas(List.of(schema("A")), 1);
    model.setValueAt(10, 0, 3);

    assertThat(model.getValueAt(0, 3)).isEqualTo(10);
  }

  @Test
  void setValueAtUpdatesCountWithValidString() {
    model.setSchemas(List.of(schema("A")), 1);
    model.setValueAt("5", 0, 3);

    assertThat(model.getValueAt(0, 3)).isEqualTo(5);
  }

  @Test
  void setValueAtIgnoresInvalidString() {
    model.setSchemas(List.of(schema("A")), 3);
    model.setValueAt("abc", 0, 3);

    assertThat(model.getValueAt(0, 3)).isEqualTo(3);
  }

  @Test
  void setValueAtIgnoresNegativeValue() {
    model.setSchemas(List.of(schema("A")), 3);
    model.setValueAt(-1, 0, 3);

    assertThat(model.getValueAt(0, 3)).isEqualTo(3);
  }

  @Test
  void setValueAtIgnoresNonExamplesColumn() {
    model.setSchemas(List.of(schema("A")), 5);
    model.setValueAt(99, 0, 0);

    assertThat(model.getValueAt(0, 0)).isEqualTo("A");
  }

  @Test
  void setAllCounts() {
    model.setSchemas(List.of(schema("A"), schema("B"), schema("C")), 1);
    model.setAllCounts(10);

    assertThat(model.getValueAt(0, 3)).isEqualTo(10);
    assertThat(model.getValueAt(1, 3)).isEqualTo(10);
    assertThat(model.getValueAt(2, 3)).isEqualTo(10);
  }

  @Test
  void getTotalCount() {
    model.setSchemas(List.of(schema("A"), schema("B")), 3);

    assertThat(model.getTotalCount()).isEqualTo(6);
  }

  @Test
  void getSchemaCountMap() {
    model.setSchemas(List.of(schema("Alpha"), schema("Beta")), 2);

    Map<String, Integer> map = model.getSchemaCountMap();

    assertThat(map).containsEntry("Alpha", 2).containsEntry("Beta", 2).hasSize(2);
  }

  @Test
  void setValueAtWithNegativeStringIsIgnored() {
    model.setSchemas(List.of(schema("A")), 5);
    model.setValueAt("-3", 0, 3);

    assertThat(model.getValueAt(0, 3)).isEqualTo(5);
  }

  @Test
  void setValueAtWithUnknownTypeIsIgnored() {
    model.setSchemas(List.of(schema("A")), 5);
    model.setValueAt(new Object(), 0, 3);

    assertThat(model.getValueAt(0, 3)).isEqualTo(5);
  }
}
