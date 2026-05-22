package com.steven.solomon.datasource.sql.dialect;

/**
 * SQL Server 2012及以上分页方言。
 */
public class SqlServer2012Dialect implements SqlDialect {

  @Override
  public String pageSql(String sql, int pageNo, int pageSize) {
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.max(pageSize, 1);
    long offset = (long) (safePageNo - 1) * safePageSize;
    String baseSql = appendDefaultOrderBy(trimEndSemicolon(sql));
    return baseSql + " OFFSET " + offset + " ROWS FETCH NEXT " + safePageSize + " ROWS ONLY";
  }

  @Override
  public String limitSql(String sql, int pageSize) {
    int safePageSize = Math.max(pageSize, 1);
    String baseSql = appendDefaultOrderBy(trimEndSemicolon(sql));
    return baseSql + " OFFSET 0 ROWS FETCH NEXT " + safePageSize + " ROWS ONLY";
  }

  private String appendDefaultOrderBy(String sql) {
    if (sql.toLowerCase().contains(" order by ")) {
      return sql;
    }
    return sql + " ORDER BY (SELECT NULL)";
  }
}
