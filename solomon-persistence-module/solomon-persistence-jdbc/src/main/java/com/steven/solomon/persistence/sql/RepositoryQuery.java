package com.steven.solomon.persistence.sql;

import com.steven.solomon.persistence.exception.PersistenceException;
import com.steven.solomon.persistence.sql.param.PersistencePageParam;
import com.steven.solomon.persistence.sql.result.PageResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Repository查询执行器�?
 *
 * <p>用于把SQL构建和执行收敛到一个更容易读的入口。调用方可以先构造SQL，再通过
 * {@code repository.query(sql).list()}、{@code repository.query(sql).page(param)} 执行�?
 * 避免在业务代码里记忆过多方法名�?/p>
 *
 * @param <TModel> 当前Repository绑定的实体类�?
 */
public class RepositoryQuery<TModel> {

  private final Repository<TModel> repository;

  private final Sql sql;

  /**
   * 构造Repository查询执行器�?
   *
   * @param repository 当前Repository实例，用于委托执行SQL
   * @param sql 待执行SQL对象，包含SQL文本和命名参�?
   */
  public RepositoryQuery(Repository<TModel> repository, Sql sql) {
    this.repository = repository;
    this.sql = sql;
  }

  /**
   * 查询当前实体类型的第一条数据�?
   *
   * @return 第一条实体记录；无结果时返回null
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public TModel one() throws PersistenceException {
    return repository.get(sql);
  }

  /**
   * 查询指定投影类型的第一条数据�?   *
   * @param resultType DTO/VO/简单值类�?   * @param <TResult> 返回结果泛型
   * @return 第一条记录；无结果时返回null
   * @throws PersistenceException SQL执行失败时抛�?   */
  public <TResult> TResult one(Class<TResult> resultType) throws PersistenceException {
    return repository.getAs(sql, resultType);
  }

  /**
   * 查询当前实体类型列表�?
   *
   * @return 实体列表
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public List<TModel> list() throws PersistenceException {
    return repository.find(sql);
  }

  /**
   * 查询指定投影类型列表�?   *
   * @param resultType DTO/VO/简单值类�?   * @param <TResult> 返回结果泛型
   * @return 查询结果列表
   * @throws PersistenceException SQL执行失败时抛�?   */
  public <TResult> List<TResult> list(Class<TResult> resultType) throws PersistenceException {
    return repository.findAs(sql, resultType);
  }

  /**
   * 查询Map列表�?
   *
   * @return Map列表，每一行以列名作为key
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public List<Map<String, Object>> maps() throws PersistenceException {
    return repository.findMapList(sql);
  }

  /**
   * 查询当前实体类型分页�?
   *
   * @param param 分页参数，包含页码、页大小和排�?
   * @return 分页结果，包含总数和是否有下一�?
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public PageResult<TModel> page(PersistencePageParam param) throws PersistenceException {
    return repository.findPage(sql, param);
  }

  /**
   * 查询指定投影类型分页�?   *
   * @param param 分页参数
   * @param resultType DTO/VO/简单值类�?   * @param <TResult> 返回结果泛型
   * @return 分页结果
   * @throws PersistenceException SQL执行失败时抛�?   */
  public <TResult> PageResult<TResult> page(
      PersistencePageParam param,
      Class<TResult> resultType) throws PersistenceException {
    return repository.findPageAs(sql, param, resultType);
  }

  /**
   * 查询Map分页�?
   *
   * @param param 分页参数，包含页码、页大小和排�?
   * @return Map分页结果
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public PageResult<Map<String, Object>> mapPage(PersistencePageParam param)
      throws PersistenceException {
    return repository.findMapPage(sql, param);
  }

  /**
   * 统计当前SQL结果总数�?
   *
   * @return 总行�?
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public long count() throws PersistenceException {
    return repository.count(sql);
  }

  /**
   * 判断当前查询是否存在数据�?   *
   * @return 至少存在一行返回true
   * @throws PersistenceException SQL执行失败时抛�?   */
  public boolean exists() throws PersistenceException {
    return repository.exists(sql);
  }

  /**
   * 对当前SQL结果求和�?
   *
   * @param columnName 聚合字段名或表达式，基于子查询别�?{@code t} 的可见字�?
   * @return 求和结果
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public BigDecimal sum(String columnName) throws PersistenceException {
    return repository.sum(sql, columnName);
  }

  /**
   * 对当前SQL结果求最大值�?
   *
   * @param columnName 聚合字段名或表达式，基于子查询别�?{@code t} 的可见字�?
   * @param resultType 返回值类�?
   * @param <T> 返回值泛�?
   * @return 最大�?
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public <T> T max(String columnName, Class<T> resultType) throws PersistenceException {
    return repository.max(sql, columnName, resultType);
  }

  /**
   * 对当前SQL结果求最小值�?
   *
   * @param columnName 聚合字段名或表达式，基于子查询别�?{@code t} 的可见字�?
   * @param resultType 返回值类�?
   * @param <T> 返回值泛�?
   * @return 最小�?
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public <T> T min(String columnName, Class<T> resultType) throws PersistenceException {
    return repository.min(sql, columnName, resultType);
  }

  /**
   * 对当前SQL结果求平均值�?
   *
   * @param columnName 聚合字段名或表达式，基于子查询别�?{@code t} 的可见字�?
   * @return 平均�?
   * @throws PersistenceException SQL执行失败时抛出的国际化业务异�?
   */
  public BigDecimal avg(String columnName) throws PersistenceException {
    return repository.avg(sql, columnName);
  }
}
