package com.steven.solomon.datasource.report;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.sql.Sql;
import com.steven.solomon.datasource.sql.SqlInjectionGuard;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 自定义报表查询。
 *
 * <p>报表 SQL 使用命名参数绑定，避免业务代码拼接用户输入。</p>
 */
public final class ReportQuery {

  private final Sql sql;

  private final Set<String> allowedParams = new LinkedHashSet<>();

  private ReportQuery(Sql sql) {
    this.sql = sql;
  }

  public static ReportQuery of(String text) {
    SqlInjectionGuard.validateQuerySql(text, "reportQuery");
    return new ReportQuery(Sql.of(text));
  }

  public static ReportQuery of(String text, Map<String, Object> params) {
    SqlInjectionGuard.validateQuerySql(text, "reportQuery");
    return new ReportQuery(Sql.of(text, params));
  }

  public static ReportQuery of(Sql sql) {
    SqlInjectionGuard.validateQuerySql(sql.getText(), "reportQuery");
    return new ReportQuery(sql);
  }

  public Sql getSql() {
    return sql;
  }

  /**
   * 声明报表允许使用的命名参数。
   *
   * <p>不声明时保持兼容，不做白名单拦截；声明后，SQL 参数中出现未声明名称会抛出异常。</p>
   *
   * @param params 参数名，不包含冒号
   * @return 当前报表查询
   */
  public ReportQuery allowedParams(String... params) {
    if (ObjectUtil.isNotEmpty(params)) {
      for (String param : params) {
        if (StrUtil.isNotBlank(param)) {
          SqlInjectionGuard.validateIdentifier(param, "reportParam");
          allowedParams.add(param);
        }
      }
    }
    return this;
  }

  /**
   * 校验报表参数是否都在白名单内。
   *
   * @throws DataSourceException 参数未声明时抛出
   */
  public void validateParams() throws DataSourceException {
    if (ObjectUtil.isEmpty(allowedParams) || ObjectUtil.isEmpty(sql.getParams())) {
      return;
    }
    for (String param : sql.getParams().keySet()) {
      if (!allowedParams.contains(param)) {
        throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_INJECTION_RISK,
            "reportParam", param);
      }
    }
  }
}
