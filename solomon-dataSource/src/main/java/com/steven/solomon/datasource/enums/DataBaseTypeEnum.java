package com.steven.solomon.datasource.enums;

/**
 * 主流关系型数据库类型。
 */
public enum DataBaseTypeEnum {

  MYSQL("com.mysql.cj.jdbc.Driver"),
  MARIADB("org.mariadb.jdbc.Driver"),
  POSTGRESQL("org.postgresql.Driver"),
  SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
  ORACLE("oracle.jdbc.OracleDriver");

  private final String driverClassName;

  DataBaseTypeEnum(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getDriverClassName() {
    return driverClassName;
  }
}
