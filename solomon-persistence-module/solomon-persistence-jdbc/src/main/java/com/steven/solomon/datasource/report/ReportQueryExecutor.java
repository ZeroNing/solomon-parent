package com.steven.solomon.datasource.report;

import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.sql.SqlExecutor;
import com.steven.solomon.datasource.sql.param.DataSourcePageParam;
import com.steven.solomon.datasource.sql.result.PageResult;
import java.util.List;
import java.util.Map;

/**
 * 自定义报表查询执行器。
 *
 * <p>执行器复用统一 SQL 执行链，因此自动继承租户切换、深浅分页、方言适配与异常处理。</p>
 */
public class ReportQueryExecutor {

  private final SqlExecutor sqlExecutor;

  public ReportQueryExecutor(SqlExecutor sqlExecutor) {
    this.sqlExecutor = sqlExecutor;
  }

  public List<Map<String, Object>> list(ReportQuery query) throws DataSourceException {
    query.validateParams();
    return sqlExecutor.queryForList(query.getSql());
  }

  public <T> List<T> list(ReportQuery query, Class<T> resultType) throws DataSourceException {
    query.validateParams();
    return sqlExecutor.query(query.getSql(), resultType);
  }

  public PageResult<Map<String, Object>> page(
      ReportQuery query,
      DataSourcePageParam param) throws DataSourceException {
    query.validateParams();
    return sqlExecutor.page(query.getSql(), param);
  }

  public <T> PageResult<T> page(
      ReportQuery query,
      DataSourcePageParam param,
      Class<T> resultType) throws DataSourceException {
    query.validateParams();
    return sqlExecutor.page(query.getSql(), param, resultType);
  }
}
