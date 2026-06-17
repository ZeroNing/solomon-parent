package com.steven.solomon.persistence.sql.dialect;

/**
 * LIMIT/OFFSET分页方言。
 *
 * <p>适用于MySQL、MariaDB、PostgreSQL。</p>
 */
public class LimitOffsetSqlDialect implements SqlDialect {

  /**
   * 生成LIMIT/OFFSET分页SQL。
   *
   * @param sql 原始查询SQL
   * @param pageNo 页码，从1开始
   * @param pageSize 每页条数
   * @return 分页SQL
   */
  @Override
  public String pageSql(String sql, int pageNo, int pageSize) {
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.max(pageSize, 1);
    long offset = (long) (safePageNo - 1) * safePageSize;
    return trimEndSemicolon(sql) + " LIMIT " + safePageSize + " OFFSET " + offset;
  }

  /**
   * 生成LIMIT限制行数SQL。
   *
   * @param sql 原始查询SQL
   * @param pageSize 限制返回条数
   * @return 限制行数SQL
   */
  @Override
  public String limitSql(String sql, int pageSize) {
    int safePageSize = Math.max(pageSize, 1);
    return trimEndSemicolon(sql) + " LIMIT " + safePageSize;
  }
}
