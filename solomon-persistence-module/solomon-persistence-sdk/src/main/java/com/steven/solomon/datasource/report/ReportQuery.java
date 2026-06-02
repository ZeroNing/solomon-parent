package com.steven.solomon.datasource.report;

import com.steven.solomon.datasource.sql.Sql;
import java.util.Map;

/**
 * 自定义报表查询。
 *
 * <p>报表 SQL 使用命名参数绑定，避免业务代码拼接用户输入。</p>
 */
public final class ReportQuery {

  private final Sql sql;

  private ReportQuery(Sql sql) {
    this.sql = sql;
  }

  public static ReportQuery of(String text) {
    return new ReportQuery(Sql.of(text));
  }

  public static ReportQuery of(String text, Map<String, Object> params) {
    return new ReportQuery(Sql.of(text, params));
  }

  public static ReportQuery of(Sql sql) {
    return new ReportQuery(sql);
  }

  public Sql getSql() {
    return sql;
  }
}
