package com.steven.solomon.datasource.sql.dialect;

/**
 * Oracle 12c及以上分页方言。
 */
public class OracleDialect implements SqlDialect {

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
