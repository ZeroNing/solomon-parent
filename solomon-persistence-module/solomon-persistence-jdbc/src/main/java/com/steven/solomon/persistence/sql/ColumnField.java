package com.steven.solomon.persistence.sql;

import java.lang.reflect.Field;

/**
 * 实体字段和数据库字段映射。
 */
public class ColumnField {

  private final Field field;
  private final String columnName;
  private final boolean primaryKey;

  public ColumnField(Field field, String columnName, boolean primaryKey) {
    this.field = field;
    this.columnName = columnName;
    this.primaryKey = primaryKey;
  }

  public Field getField() {
    return field;
  }

  public String getFieldName() {
    return field.getName();
  }

  public String getColumnName() {
    return columnName;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public Object getValue(Object target) throws IllegalAccessException {
    return field.get(target);
  }
}
