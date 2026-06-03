package com.steven.solomon.datasource.sql;

import java.util.List;

/**
 * 实体 SQL 元数据缓存对象。
 *
 * <p>集中保存表名、字段和主键信息，避免每次查询都重复反射解析。</p>
 */
class SqlEntityMetadata {

  private final String tableName;
  private final List<ColumnField> fields;
  private final ColumnField primaryKey;

  SqlEntityMetadata(String tableName, List<ColumnField> fields, ColumnField primaryKey) {
    this.tableName = tableName;
    this.fields = fields;
    this.primaryKey = primaryKey;
  }

  String getTableName() {
    return tableName;
  }

  List<ColumnField> getFields() {
    return fields;
  }

  ColumnField getPrimaryKey() {
    return primaryKey;
  }
}
