package com.steven.solomon.datasource.sql;

/**
 * 项目仓储基类。
 *
 * @param <TModel> 实体类型
 */
public class BaseRepository<TModel> extends Repository<TModel> {

  /**
   * 构造项目仓储基类。
   *
   * @param sqlExecutor SQL执行器
   */
  public BaseRepository(SqlExecutor sqlExecutor) {
    super(sqlExecutor);
  }
}
