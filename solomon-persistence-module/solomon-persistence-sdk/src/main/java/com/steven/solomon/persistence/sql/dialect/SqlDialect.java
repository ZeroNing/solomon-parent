package com.steven.solomon.persistence.sql.dialect;

import cn.hutool.core.util.StrUtil;

/**
 * SQL分页方言�?
 */
public interface SqlDialect {

  /**
   * 普通分页SQL�?
   *
   * @param sql 原始查询SQL，不包含分页语句
   * @param pageNo 页码，从1开�?
   * @param pageSize 每页条数
   * @return 拼接分页语法后的SQL
   */
  String pageSql(String sql, int pageNo, int pageSize);

  /**
   * 只限制返回行数的SQL�?
   *
   * @param sql 原始查询SQL，不包含分页语句
   * @param pageSize 限制返回条数
   * @return 拼接限制行数语法后的SQL
   */
  String limitSql(String sql, int pageSize);

  /**
   * 统计总数SQL�?
   *
   * @param sql 原始查询SQL，会被包装成子查�?
   * @return 统计总数SQL
   */
  default String countSql(String sql) {
    return "SELECT COUNT(1) FROM (" + trimEndSemicolon(sql) + ") t";
  }

  /**
   * 去掉SQL末尾分号，避免包装分�?统计SQL时报错�?
   *
   * @param sql 原始SQL文本
   * @return 去掉末尾分号后的SQL文本
   */
  default String trimEndSemicolon(String sql) {
    String value = StrUtil.nullToEmpty(sql).trim();
    if (value.endsWith(";")) {
      return value.substring(0, value.length() - 1);
    }
    return value;
  }
}
