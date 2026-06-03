package com.steven.solomon.datasource.sql;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.lambda.LambdaProperty;
import com.steven.solomon.datasource.lambda.SFunction;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Lambda 条件构建基类。
 *
 * <p>查询、更新、删除等链式对象都可以复用这一组条件方法，避免重复实现字段解析和条件拼接。</p>
 *
 * @param <TSelf> 当前链式对象类型
 * @param <TModel> 实体类型
 */
abstract class LambdaConditionSupport<TSelf, TModel> {

  protected final Repository<TModel> repository;
  protected final List<Cond> conditions = new java.util.ArrayList<>();

  LambdaConditionSupport(Repository<TModel> repository) {
    this.repository = repository;
  }

  public TSelf eq(SFunction<TModel, ?> column, Object value) throws DataSourceException {
    return add(Cond.eq(columnName(column), value, true));
  }

  public TSelf eqIf(boolean condition, SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    return condition ? eq(column, value) : self();
  }

  public TSelf ne(SFunction<TModel, ?> column, Object value) throws DataSourceException {
    return add(Cond.ne(columnName(column), value, true));
  }

  public TSelf neIf(boolean condition, SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    return condition ? ne(column, value) : self();
  }

  public TSelf gt(SFunction<TModel, ?> column, Object value) throws DataSourceException {
    return add(Cond.gt(columnName(column), value, true));
  }

  public TSelf gtIf(boolean condition, SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    return condition ? gt(column, value) : self();
  }

  public TSelf ge(SFunction<TModel, ?> column, Object value) throws DataSourceException {
    return add(Cond.ge(columnName(column), value, true));
  }

  public TSelf geIf(boolean condition, SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    return condition ? ge(column, value) : self();
  }

  public TSelf lt(SFunction<TModel, ?> column, Object value) throws DataSourceException {
    return add(Cond.lt(columnName(column), value, true));
  }

  public TSelf ltIf(boolean condition, SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    return condition ? lt(column, value) : self();
  }

  public TSelf le(SFunction<TModel, ?> column, Object value) throws DataSourceException {
    return add(Cond.le(columnName(column), value, true));
  }

  public TSelf leIf(boolean condition, SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    return condition ? le(column, value) : self();
  }

  public TSelf in(SFunction<TModel, ?> column, Collection<?> values)
      throws DataSourceException {
    return add(Cond.in(columnName(column), values, true));
  }

  public TSelf inIf(boolean condition, SFunction<TModel, ?> column, Collection<?> values)
      throws DataSourceException {
    return condition ? in(column, values) : self();
  }

  public TSelf between(SFunction<TModel, ?> column, Object start, Object end)
      throws DataSourceException {
    return add(Cond.between(columnName(column), start, end));
  }

  public TSelf betweenIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object start,
      Object end) throws DataSourceException {
    return condition ? between(column, start, end) : self();
  }

  public TSelf isNull(SFunction<TModel, ?> column) throws DataSourceException {
    return add(Cond.isNull(columnName(column)));
  }

  public TSelf isNullIf(boolean condition, SFunction<TModel, ?> column)
      throws DataSourceException {
    return condition ? isNull(column) : self();
  }

  public TSelf isNotNull(SFunction<TModel, ?> column) throws DataSourceException {
    return add(Cond.isNotNull(columnName(column)));
  }

  public TSelf isNotNullIf(boolean condition, SFunction<TModel, ?> column)
      throws DataSourceException {
    return condition ? isNotNull(column) : self();
  }

  protected abstract TSelf self();

  protected boolean hasConditions() {
    return ObjectUtil.isNotEmpty(conditions);
  }

  protected String columnName(SFunction<TModel, ?> column) throws DataSourceException {
    return repository.columnName(LambdaProperty.name(column));
  }

  protected void appendWhere(StringBuilder builder, Map<String, Object> params) {
    builder.append(" WHERE ");
    for (int i = 0; i < conditions.size(); i++) {
      Cond condition = conditions.get(i);
      if (i > 0) {
        builder.append(" AND ");
      }
      builder.append(condition.getText());
      params.putAll(condition.getParams());
    }
  }

  private TSelf add(Cond condition) {
    if (ObjectUtil.isNotEmpty(condition)) {
      conditions.add(condition);
    }
    return self();
  }
}
