package com.steven.solomon.persistence.properties;

import jakarta.validation.constraints.NotBlank;

/**
 * SQL 脚本执行记录配置。
 */
public class ScriptProperties {

  @NotBlank(message = "persistence.script.record-table must not be blank")
  private String recordTable = "solomon_sql_script_record";

  public String getRecordTable() {
    return recordTable;
  }

  public void setRecordTable(String recordTable) {
    this.recordTable = recordTable;
  }
}
