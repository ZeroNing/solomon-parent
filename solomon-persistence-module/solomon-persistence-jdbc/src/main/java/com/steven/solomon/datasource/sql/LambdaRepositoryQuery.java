package com.steven.solomon.datasource.sql;

import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.lambda.LambdaProperty;
import com.steven.solomon.datasource.lambda.SFunction;
import com.steven.solomon.datasource.sql.param.DataSourcePageParam;
import com.steven.solomon.datasource.sql.result.PageResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Lambda 仓储查询器。
 *
 * <p>业务侧通过 {@code User::getName} 这类方法引用选择字段，框架会映射到实体
 * {@code @Column} 声明的数据库列名，减少硬编码字段和 SQL 注入风险。</p>
 *
 * @param <TModel> 当前 Repository 绑定的实体类型
 */
public class LambdaRepositoryQuery<TModel> {

  private final Repository<TModel> repository;
  private final Sql sql;

  /**
   * 构造 Lambda 查询器。
   *
   * @param repository 当前仓储
   * @param sql 查询 SQL
   */
  public LambdaRepositoryQuery(Repository<TModel> repository, Sql sql) {
    this.repository = repository;
    this.sql = sql;
  }

  public LambdaRepositoryQuery<TModel> eq(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    sql.eq(columnName(column), value);
    return this;
  }

  public LambdaRepositoryQuery<TModel> eqIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? eq(column, value) : this;
  }

  public LambdaRepositoryQuery<TModel> ne(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    sql.ne(columnName(column), value);
    return this;
  }

  public LambdaRepositoryQuery<TModel> neIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? ne(column, value) : this;
  }

  public LambdaRepositoryQuery<TModel> gt(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    sql.gt(columnName(column), value);
    return this;
  }

  public LambdaRepositoryQuery<TModel> gtIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? gt(column, value) : this;
  }

  public LambdaRepositoryQuery<TModel> ge(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    sql.ge(columnName(column), value);
    return this;
  }

  public LambdaRepositoryQuery<TModel> geIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? ge(column, value) : this;
  }

  public LambdaRepositoryQuery<TModel> lt(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    sql.lt(columnName(column), value);
    return this;
  }

  public LambdaRepositoryQuery<TModel> ltIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? lt(column, value) : this;
  }

  public LambdaRepositoryQuery<TModel> le(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    sql.le(columnName(column), value);
    return this;
  }

  public LambdaRepositoryQuery<TModel> leIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? le(column, value) : this;
  }

  public LambdaRepositoryQuery<TModel> like(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    sql.like(columnName(column), value);
    return this;
  }

  public LambdaRepositoryQuery<TModel> likeIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? like(column, value) : this;
  }

  public LambdaRepositoryQuery<TModel> in(
      SFunction<TModel, ?> column,
      Collection<?> values) throws DataSourceException {
    sql.in(columnName(column), values);
    return this;
  }

  public LambdaRepositoryQuery<TModel> inIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Collection<?> values) throws DataSourceException {
    return condition ? in(column, values) : this;
  }

  public LambdaRepositoryQuery<TModel> between(
      SFunction<TModel, ?> column,
      Object start,
      Object end) throws DataSourceException {
    sql.between(columnName(column), start, end);
    return this;
  }

  public LambdaRepositoryQuery<TModel> betweenIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object start,
      Object end) throws DataSourceException {
    return condition ? between(column, start, end) : this;
  }

  public LambdaRepositoryQuery<TModel> isNull(SFunction<TModel, ?> column)
      throws DataSourceException {
    sql.isNull(columnName(column));
    return this;
  }

  public LambdaRepositoryQuery<TModel> isNullIf(boolean condition, SFunction<TModel, ?> column)
      throws DataSourceException {
    return condition ? isNull(column) : this;
  }

  public LambdaRepositoryQuery<TModel> isNotNull(SFunction<TModel, ?> column)
      throws DataSourceException {
    sql.isNotNull(columnName(column));
    return this;
  }

  public LambdaRepositoryQuery<TModel> isNotNullIf(boolean condition, SFunction<TModel, ?> column)
      throws DataSourceException {
    return condition ? isNotNull(column) : this;
  }

  public LambdaRepositoryQuery<TModel> orderByAsc(SFunction<TModel, ?> column)
      throws DataSourceException {
    sql.orderByAsc(columnName(column));
    return this;
  }

  public LambdaRepositoryQuery<TModel> orderByAscIf(
      boolean condition,
      SFunction<TModel, ?> column) throws DataSourceException {
    return condition ? orderByAsc(column) : this;
  }

  public LambdaRepositoryQuery<TModel> orderByDesc(SFunction<TModel, ?> column)
      throws DataSourceException {
    sql.orderByDesc(columnName(column));
    return this;
  }

  public LambdaRepositoryQuery<TModel> orderByDescIf(
      boolean condition,
      SFunction<TModel, ?> column) throws DataSourceException {
    return condition ? orderByDesc(column) : this;
  }

  public TModel one() throws DataSourceException {
    return repository.get(sql);
  }

  public <TResult> TResult one(Class<TResult> resultType) throws DataSourceException {
    return repository.getAs(sql, resultType);
  }

  public List<TModel> list() throws DataSourceException {
    return repository.find(sql);
  }

  public <TResult> List<TResult> list(Class<TResult> resultType) throws DataSourceException {
    return repository.findAs(sql, resultType);
  }

  public List<Map<String, Object>> maps() throws DataSourceException {
    return repository.findMapList(sql);
  }

  public PageResult<TModel> page(DataSourcePageParam param) throws DataSourceException {
    return repository.findPage(sql, param);
  }

  public <TResult> PageResult<TResult> page(
      DataSourcePageParam param,
      Class<TResult> resultType) throws DataSourceException {
    return repository.findPageAs(sql, param, resultType);
  }

  public PageResult<TModel> page(int pageNo, int pageSize) throws DataSourceException {
    return repository.findPage(sql, DataSourcePageParam.of(pageNo, pageSize));
  }

  public PageResult<TModel> page(
      int pageNo,
      int pageSize,
      SFunction<TModel, ?> orderColumn,
      boolean asc) throws DataSourceException {
    DataSourcePageParam param = DataSourcePageParam.of(pageNo, pageSize);
    if (asc) {
      param.asc(columnName(orderColumn));
    } else {
      param.desc(columnName(orderColumn));
    }
    return repository.findPage(sql, param);
  }

  public long count() throws DataSourceException {
    return repository.count(sql);
  }

  public boolean exists() throws DataSourceException {
    return repository.exists(sql);
  }

  public Sql sql() {
    return sql;
  }

  private String columnName(SFunction<TModel, ?> column) throws DataSourceException {
    return repository.columnName(LambdaProperty.name(column));
  }
}
