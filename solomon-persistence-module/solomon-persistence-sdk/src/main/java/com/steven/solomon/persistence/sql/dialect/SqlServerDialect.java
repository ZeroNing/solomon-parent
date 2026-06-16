package com.steven.solomon.persistence.sql.dialect;

/**
 * SQL Server 2005/2008分页方言�?
 *
 * <p>老版本不支持OFFSET FETCH，使用ROW_NUMBER包装查询�?/p>
 */
public class SqlServerDialect implements SqlDialect {

  @Override
  public String pageSql(String sql, int pageNo, int pageSize) {
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.max(pageSize, 1);
    long startRow = (long) (safePageNo - 1) * safePageSize + 1;
    long endRow = (long) safePageNo * safePageSize;
    return rowNumberSql(sql, startRow, endRow);
  }

  @Override
  public String limitSql(String sql, int pageSize) {
    int safePageSize = Math.max(pageSize, 1);
    return rowNumberSql(sql, 1, safePageSize);
  }

  private String rowNumberSql(String sql, long startRow, long endRow) {
    String baseSql = trimEndSemicolon(sql);
    String orderBy = resolveOrderBy(baseSql);
    String bodySql = removeLastOrderBy(baseSql);
    return "SELECT * FROM (SELECT ROW_NUMBER() OVER (" + orderBy + ") AS __row_number__, t.* "
        + "FROM (" + bodySql + ") t) p WHERE p.__row_number__ BETWEEN " + startRow + " AND "
        + endRow;
  }

  private String resolveOrderBy(String sql) {
    String lowerSql = sql.toLowerCase();
    int index = lowerSql.lastIndexOf(" order by ");
    if (index >= 0) {
      return sql.substring(index);
    }
    return "ORDER BY (SELECT NULL)";
  }

  private String removeLastOrderBy(String sql) {
    String lowerSql = sql.toLowerCase();
    int index = lowerSql.lastIndexOf(" order by ");
    if (index >= 0) {
      return sql.substring(0, index);
    }
    return sql;
  }
}
