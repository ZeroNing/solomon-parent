package com.steven.solomon.datasource.sql;

import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import com.steven.solomon.datasource.enums.SqlServerVersionEnum;
import com.steven.solomon.datasource.sql.dialect.*;

/**
 * SQL方言工厂。
 */
public class SqlDialectFactory {

  private static final SqlDialect MYSQL = new MySqlDialect();

  private static final SqlDialect MARIADB = new MariaDbDialect();

  private static final SqlDialect POSTGRESQL = new PostgreSqlDialect();

  private static final SqlDialect SQL_SERVER_2005 = new SqlServerDialect();

  private static final SqlDialect SQL_SERVER_2012 = new SqlServer2012Dialect();

  private static final SqlDialect ORACLE = new OracleDialect();

  private SqlDialectFactory() {
  }

  /**
   * 根据数据库类型获取SQL方言。
   *
   * @param databaseType 数据库类型；为空或未知时默认使用MySQL方言
   * @return SQL方言实现
   */
  public static SqlDialect getDialect(DataBaseTypeEnum databaseType) {
    return getDialect(databaseType, SqlServerVersionEnum.SQL_SERVER_2012);
  }

  /**
   * 根据数据库类型和SQL Server版本获取SQL方言。
   *
   * @param databaseType 数据库类型；为空或未知时默认使用MySQL方言
   * @param sqlServerVersion SQL Server版本，仅databaseType为SQL_SERVER时生效；
   *     为空时默认使用SQL Server 2012及以上方言
   * @return SQL方言实现
   */
  public static SqlDialect getDialect(
      DataBaseTypeEnum databaseType,
      SqlServerVersionEnum sqlServerVersion) {
    if (databaseType == DataBaseTypeEnum.MARIADB) {
      return MARIADB;
    }
    if (databaseType == DataBaseTypeEnum.POSTGRESQL) {
      return POSTGRESQL;
    }
    if (databaseType == DataBaseTypeEnum.SQL_SERVER) {
      return sqlServerVersion == SqlServerVersionEnum.SQL_SERVER_2005
          ? SQL_SERVER_2005
          : SQL_SERVER_2012;
    }
    if (databaseType == DataBaseTypeEnum.ORACLE) {
      return ORACLE;
    }
    return MYSQL;
  }
}
