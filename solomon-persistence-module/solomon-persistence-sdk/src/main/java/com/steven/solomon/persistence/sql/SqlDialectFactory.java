package com.steven.solomon.persistence.sql;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.persistence.enums.DataBaseTypeEnum;
import com.steven.solomon.persistence.enums.SqlServerVersionEnum;
import com.steven.solomon.persistence.sql.dialect.LimitOffsetSqlDialect;
import com.steven.solomon.persistence.sql.dialect.MariaDbDialect;
import com.steven.solomon.persistence.sql.dialect.MySqlDialect;
import com.steven.solomon.persistence.sql.dialect.OffsetFetchSqlDialect;
import com.steven.solomon.persistence.sql.dialect.OracleDialect;
import com.steven.solomon.persistence.sql.dialect.PostgreSqlDialect;
import com.steven.solomon.persistence.sql.dialect.SqlDialect;
import com.steven.solomon.persistence.sql.dialect.SqlServer2012Dialect;
import com.steven.solomon.persistence.sql.dialect.SqlServerDialect;
import java.util.EnumMap;
import java.util.Map;

/**
 * SQL 方言工厂�?
 *
 * <p>所有数据库分页方言统一在这里注册。新增数据库时优先复用标准方言�?
 * MySQL 协议族使�?LIMIT/OFFSET，PostgreSQL 协议族使�?LIMIT/OFFSET�?
 * SQL:2008 协议族使�?OFFSET/FETCH�?/p>
 */
public final class SqlDialectFactory {

  private static final SqlDialect MYSQL = new MySqlDialect();
  private static final SqlDialect MARIADB = new MariaDbDialect();
  private static final SqlDialect POSTGRESQL = new PostgreSqlDialect();
  private static final SqlDialect LIMIT_OFFSET = new LimitOffsetSqlDialect();
  private static final SqlDialect OFFSET_FETCH = new OffsetFetchSqlDialect();
  private static final SqlDialect SQL_SERVER_2005 = new SqlServerDialect();
  private static final SqlDialect SQL_SERVER_2012 = new SqlServer2012Dialect();
  private static final SqlDialect ORACLE = new OracleDialect();

  private static final Map<DataBaseTypeEnum, SqlDialect> DIALECTS =
      new EnumMap<>(DataBaseTypeEnum.class);

  static {
    register(DataBaseTypeEnum.MYSQL, MYSQL);
    register(DataBaseTypeEnum.MARIADB, MARIADB);
    register(DataBaseTypeEnum.TIDB, MYSQL);
    register(DataBaseTypeEnum.OCEANBASE, MYSQL);

    register(DataBaseTypeEnum.POSTGRESQL, POSTGRESQL);
    register(DataBaseTypeEnum.KINGBASE, POSTGRESQL);
    register(DataBaseTypeEnum.GAUSSDB, POSTGRESQL);

    register(DataBaseTypeEnum.ORACLE, ORACLE);
    register(DataBaseTypeEnum.DB2, OFFSET_FETCH);
    register(DataBaseTypeEnum.DM, OFFSET_FETCH);

    register(DataBaseTypeEnum.H2, LIMIT_OFFSET);
    register(DataBaseTypeEnum.SQLITE, LIMIT_OFFSET);
    register(DataBaseTypeEnum.CLICKHOUSE, LIMIT_OFFSET);
  }

  private SqlDialectFactory() {}

  public static void register(DataBaseTypeEnum databaseType, SqlDialect dialect) {
    if (ObjectUtil.isNotEmpty(databaseType) && ObjectUtil.isNotEmpty(dialect)) {
      DIALECTS.put(databaseType, dialect);
    }
  }

  /**
   * 根据数据库类型获�?SQL 方言�?
   *
   * @param databaseType 数据库类型；为空或未知时默认使用 MySQL 方言
   * @return SQL 方言实现
   */
  public static SqlDialect getDialect(DataBaseTypeEnum databaseType) {
    return getDialect(databaseType, SqlServerVersionEnum.SQL_SERVER_2012);
  }

  /**
   * 根据数据库类型和 SQL Server 版本获取 SQL 方言�?
   *
   * @param databaseType 数据库类型；为空或未知时默认使用 MySQL 方言
   * @param sqlServerVersion SQL Server 版本，仅 databaseType �?SQL_SERVER 时生�?
   * @return SQL 方言实现
   */
  public static SqlDialect getDialect(
      DataBaseTypeEnum databaseType,
      SqlServerVersionEnum sqlServerVersion) {
    if (databaseType == DataBaseTypeEnum.SQL_SERVER) {
      return sqlServerVersion == SqlServerVersionEnum.SQL_SERVER_2005
          ? SQL_SERVER_2005
          : SQL_SERVER_2012;
    }
    return DIALECTS.getOrDefault(databaseType, MYSQL);
  }
}
