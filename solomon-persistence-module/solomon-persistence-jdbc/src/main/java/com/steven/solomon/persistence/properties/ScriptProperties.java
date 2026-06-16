package com.steven.solomon.persistence.properties;

/**
 * SQL 脚本执行记录配置�? */
public class ScriptProperties {

  private String recordTable = "solomon_sql_script_record";

  public String getRecordTable() {
    return recordTable;
  }

  public void setRecordTable(String recordTable) {
    this.recordTable = recordTable;
  }
}
