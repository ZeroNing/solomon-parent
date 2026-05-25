package com.steven.solomon.datasource.sql.dialect;

/**
 * SQL:2008 OFFSET/FETCH 分页方言。
 *
 * <p>适用于 DB2、Oracle 12c+、SQL Server 2012+、达梦等支持标准 OFFSET/FETCH 的数据库。</p>
 */
public class OffsetFetchSqlDialect implements SqlDialect {

  @Override
  public String pageSql(String sql, int pageNo, int pageSize) {
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.max(pageSize, 1);
    long offset = (long) (safePageNo - 1) * safePageSize;
    return trimEndSemicolon(sql) + " OFFSET " + offset + " ROWS FETCH NEXT " + safePageSize
        + " ROWS ONLY";
  }

  @Override
  public String limitSql(String sql, int pageSize) {
    int safePageSize = Math.max(pageSize, 1);
    return trimEndSemicolon(sql) + " FETCH NEXT " + safePageSize + " ROWS ONLY";
  }
}
