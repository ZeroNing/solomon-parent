package com.steven.solomon.persistence.enums;

import cn.hutool.core.util.StrUtil;

/**
 * 数据库类型枚举。
 *
 * <p>枚举同时维护 JDBC 驱动、Druid 防火墙 dbType、默认校验 SQL 和 JDBC URL 识别前缀。
 * 这样连接池创建、SQL 方言选择、运行期租户新增都能走同一套数据库能力描述，避免各处重复写
 * if/else。</p>
 */
public enum DataBaseTypeEnum {

  MYSQL("com.mysql.cj.jdbc.Driver", "mysql", "SELECT 1", "jdbc:mysql:"),
  MARIADB("org.mariadb.jdbc.Driver", "mariadb", "SELECT 1", "jdbc:mariadb:"),
  POSTGRESQL("org.postgresql.Driver", "postgresql", "SELECT 1", "jdbc:postgresql:"),
  SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver", "SELECT 1", "jdbc:sqlserver:"),
  ORACLE("oracle.jdbc.OracleDriver", "oracle", "SELECT 1 FROM DUAL", "jdbc:oracle:"),

  /** H2 常用于单元测试、嵌入式运行和轻量管理端。 */
  H2("org.h2.Driver", "h2", "SELECT 1", "jdbc:h2:"),

  /** SQLite 常用于本地轻量存储；分页使用 LIMIT/OFFSET。 */
  SQLITE("org.sqlite.JDBC", "sqlite", "SELECT 1", "jdbc:sqlite:"),

  /** IBM DB2，分页使用 OFFSET/FETCH。 */
  DB2("com.ibm.db2.jcc.DB2Driver", "db2", "SELECT 1 FROM SYSIBM.SYSDUMMY1", "jdbc:db2:"),

  /** ClickHouse 分析型数据库，常用于 OLAP 场景。 */
  CLICKHOUSE("com.clickhouse.jdbc.ClickHouseDriver", "clickhouse", "SELECT 1", "jdbc:clickhouse:"),

  /** TiDB 兼容 MySQL 协议和大部分 MySQL 语法。 */
  TIDB("com.mysql.cj.jdbc.Driver", "mysql", "SELECT 1", "jdbc:mysql:"),

  /** OceanBase 可按 MySQL 模式接入，Oracle 模式可显式配置 ORACLE。 */
  OCEANBASE("com.mysql.cj.jdbc.Driver", "mysql", "SELECT 1", "jdbc:oceanbase:", "jdbc:mysql:"),

  /** 达梦数据库，常见分页可使用 OFFSET/FETCH。 */
  DM("dm.jdbc.driver.DmDriver", "dm", "SELECT 1", "jdbc:dm:"),

  /** 人大金仓兼容 PostgreSQL 生态。 */
  KINGBASE("com.kingbase8.Driver", "postgresql", "SELECT 1", "jdbc:kingbase8:"),

  /** 华为 GaussDB/openGauss 兼容 PostgreSQL 生态。 */
  GAUSSDB("org.postgresql.Driver", "postgresql", "SELECT 1", "jdbc:opengauss:", "jdbc:postgresql:");

  private final String driverClassName;

  private final String druidDbType;

  private final String validationQuery;

  private final String[] jdbcUrlPrefixes;

  DataBaseTypeEnum(String driverClassName, String druidDbType, String validationQuery,
      String... jdbcUrlPrefixes) {
    this.driverClassName = driverClassName;
    this.druidDbType = druidDbType;
    this.validationQuery = validationQuery;
    this.jdbcUrlPrefixes = jdbcUrlPrefixes;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public String getDruidDbType() {
    return druidDbType;
  }

  public String getValidationQuery() {
    return validationQuery;
  }

  /**
   * 根据 JDBC URL 推断数据库类型。
   *
   * <p>该方法用于用户未显式配置 databaseType 时兜底识别。若多个数据库共享同一 URL 前缀，
   * 例如 TiDB/OceanBase MySQL 模式，会优先识别为更通用的 MYSQL，业务可通过配置显式指定。</p>
   */
  public static DataBaseTypeEnum fromJdbcUrl(String jdbcUrl) {
    if (StrUtil.isBlank(jdbcUrl)) {
      return null;
    }
    String normalizedUrl = jdbcUrl.trim().toLowerCase();
    for (DataBaseTypeEnum databaseType : values()) {
      for (String prefix : databaseType.jdbcUrlPrefixes) {
        if (normalizedUrl.startsWith(prefix.toLowerCase())) {
          return databaseType;
        }
      }
    }
    return null;
  }
}
