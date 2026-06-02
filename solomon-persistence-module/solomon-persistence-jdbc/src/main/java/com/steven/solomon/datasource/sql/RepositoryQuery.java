package com.steven.solomon.datasource.sql;

import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.sql.param.DataSourcePageParam;
import com.steven.solomon.datasource.sql.result.PageResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Repository查询执行器。
 *
 * <p>用于把SQL构建和执行收敛到一个更容易读的入口。调用方可以先构造SQL，再通过
 * {@code repository.query(sql).list()}、{@code repository.query(sql).page(param)} 执行，
 * 避免在业务代码里记忆过多方法名。</p>
 *
 * @param <TModel> 当前Repository绑定的实体类型
 */
public class RepositoryQuery<TModel> {

  private final Repository<TModel> repository;

  private final Sql sql;

  /**
   * 构造Repository查询执行器。
   *
   * @param repository 当前Repository实例，用于委托执行SQL
   * @param sql 待执行SQL对象，包含SQL文本和命名参数
   */
  public RepositoryQuery(Repository<TModel> repository, Sql sql) {
    this.repository = repository;
    this.sql = sql;
  }

  /**
   * 查询当前实体类型的第一条数据。
   *
   * @return 第一条实体记录；无结果时返回null
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public TModel one() throws DataSourceException {
    return repository.get(sql);
  }

  /**
   * 查询当前实体类型列表。
   *
   * @return 实体列表
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public List<TModel> list() throws DataSourceException {
    return repository.find(sql);
  }

  /**
   * 查询Map列表。
   *
   * @return Map列表，每一行以列名作为key
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public List<Map<String, Object>> maps() throws DataSourceException {
    return repository.findMapList(sql);
  }

  /**
   * 查询当前实体类型分页。
   *
   * @param param 分页参数，包含页码、页大小和排序
   * @return 分页结果，包含总数和是否有下一页
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<TModel> page(DataSourcePageParam param) throws DataSourceException {
    return repository.findPage(sql, param);
  }

  /**
   * 查询Map分页。
   *
   * @param param 分页参数，包含页码、页大小和排序
   * @return Map分页结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<Map<String, Object>> mapPage(DataSourcePageParam param)
      throws DataSourceException {
    return repository.findMapPage(sql, param);
  }

  /**
   * 统计当前SQL结果总数。
   *
   * @return 总行数
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public long count() throws DataSourceException {
    return repository.count(sql);
  }

  /**
   * 对当前SQL结果求和。
   *
   * @param columnName 聚合字段名或表达式，基于子查询别名 {@code t} 的可见字段
   * @return 求和结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public BigDecimal sum(String columnName) throws DataSourceException {
    return repository.sum(sql, columnName);
  }

  /**
   * 对当前SQL结果求最大值。
   *
   * @param columnName 聚合字段名或表达式，基于子查询别名 {@code t} 的可见字段
   * @param resultType 返回值类型
   * @param <T> 返回值泛型
   * @return 最大值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T max(String columnName, Class<T> resultType) throws DataSourceException {
    return repository.max(sql, columnName, resultType);
  }

  /**
   * 对当前SQL结果求最小值。
   *
   * @param columnName 聚合字段名或表达式，基于子查询别名 {@code t} 的可见字段
   * @param resultType 返回值类型
   * @param <T> 返回值泛型
   * @return 最小值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T min(String columnName, Class<T> resultType) throws DataSourceException {
    return repository.min(sql, columnName, resultType);
  }

  /**
   * 对当前SQL结果求平均值。
   *
   * @param columnName 聚合字段名或表达式，基于子查询别名 {@code t} 的可见字段
   * @return 平均值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public BigDecimal avg(String columnName) throws DataSourceException {
    return repository.avg(sql, columnName);
  }
}
