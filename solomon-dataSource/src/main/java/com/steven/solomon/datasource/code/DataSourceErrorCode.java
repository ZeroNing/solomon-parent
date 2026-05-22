package com.steven.solomon.datasource.code;

import com.steven.solomon.code.BaseExceptionCode;

/**
 * 数据源模块错误码。
 */
public interface DataSourceErrorCode extends BaseExceptionCode {

  /** 数据源配置不存在。 */
  String DATA_SOURCE_CONFIG_NOT_FOUND = "DATASOURCE_CONFIG_NOT_FOUND";

  /** 租户数据源不存在。 */
  String DATA_SOURCE_NOT_FOUND = "DATASOURCE_NOT_FOUND";

  /** 数据源连接池类型不支持。 */
  String DATA_SOURCE_POOL_NOT_SUPPORTED = "DATASOURCE_POOL_NOT_SUPPORTED";

  /** 数据库类型不支持。 */
  String DATA_SOURCE_DB_NOT_SUPPORTED = "DATASOURCE_DB_NOT_SUPPORTED";

  /** 数据源初始化失败。 */
  String DATA_SOURCE_INIT_FAILED = "DATASOURCE_INIT_FAILED";

  /** SQL执行失败。 */
  String DATA_SOURCE_SQL_EXECUTE_FAILED = "DATASOURCE_SQL_EXECUTE_FAILED";

  /** SQL存在注入风险。 */
  String DATA_SOURCE_SQL_INJECTION_RISK = "DATASOURCE_SQL_INJECTION_RISK";

  /** 表注解不存在。 */
  String DATA_SOURCE_TABLE_NOT_FOUND = "DATASOURCE_TABLE_NOT_FOUND";

  /** 主键注解不存在。 */
  String DATA_SOURCE_PRIMARY_KEY_NOT_FOUND = "DATASOURCE_PRIMARY_KEY_NOT_FOUND";

  /** 字段注解不存在。 */
  String DATA_SOURCE_COLUMN_NOT_FOUND = "DATASOURCE_COLUMN_NOT_FOUND";
}
