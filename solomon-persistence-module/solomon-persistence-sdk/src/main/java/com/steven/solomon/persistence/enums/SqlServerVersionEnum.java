package com.steven.solomon.persistence.enums;

/**
 * SQL Server版本。
 */
public enum SqlServerVersionEnum {

  /** SQL Server 2005及以上，使用ROW_NUMBER分页。 */
  SQL_SERVER_2005,

  /** SQL Server 2012及以上，使用OFFSET FETCH分页。 */
  SQL_SERVER_2012
}
