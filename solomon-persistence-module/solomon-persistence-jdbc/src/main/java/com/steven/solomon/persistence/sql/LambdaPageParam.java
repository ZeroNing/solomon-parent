package com.steven.solomon.persistence.sql;

import com.steven.solomon.persistence.exception.PersistenceException;
import com.steven.solomon.persistence.lambda.LambdaProperty;
import com.steven.solomon.persistence.lambda.SFunction;
import com.steven.solomon.persistence.sql.param.PersistencePageParam;
import com.steven.solomon.pojo.enums.OrderByEnum;

/**
 * Lambda 分页参数。
 *
 * <p>排序字段和深分页游标字段通过 Getter 方法引用指定，框架会按实体元数据映射为数据库列名，
 * 避免前端或业务代码直接传入未校验的排序字段字符串。</p>
 *
 * @param <TModel> Repository 绑定的实体类型
 */
public class LambdaPageParam<TModel> extends PersistencePageParam {

  private static final long serialVersionUID = -1858836854823236919L;

  private final Repository<TModel> repository;

  /**
   * 构造 Lambda 分页参数。
   *
   * @param repository 当前仓储
   */
  public LambdaPageParam(Repository<TModel> repository) {
    this.repository = repository;
  }

  /**
   * 创建分页参数。
   *
   * @param repository 当前仓储
   * @param pageNo 页码
   * @param pageSize 每页条数
   * @param <TModel> 实体类型
   * @return Lambda 分页参数
   */
  public static <TModel> LambdaPageParam<TModel> of(
      Repository<TModel> repository,
      int pageNo,
      int pageSize) {
    LambdaPageParam<TModel> param = new LambdaPageParam<>(repository);
    param.setPageNo(pageNo);
    param.setPageSize(pageSize);
    return param;
  }

  public LambdaPageParam<TModel> sort(SFunction<TModel, ?> column, OrderByEnum method)
      throws PersistenceException {
    super.sort(columnName(column), method);
    return this;
  }

  public LambdaPageParam<TModel> asc(SFunction<TModel, ?> column) throws PersistenceException {
    return sort(column, OrderByEnum.ASC);
  }

  public LambdaPageParam<TModel> desc(SFunction<TModel, ?> column) throws PersistenceException {
    return sort(column, OrderByEnum.DESCEND);
  }

  public LambdaPageParam<TModel> seekColumn(SFunction<TModel, ?> column)
      throws PersistenceException {
    super.seekColumn(columnName(column));
    return this;
  }

  @Override
  public LambdaPageParam<TModel> orderBy(String orderBy) {
    super.orderBy(orderBy);
    return this;
  }

  @Override
  public LambdaPageParam<TModel> lastValue(Object lastValue) {
    super.lastValue(lastValue);
    return this;
  }

  @Override
  public LambdaPageParam<TModel> seekAsc() {
    super.seekAsc();
    return this;
  }

  @Override
  public LambdaPageParam<TModel> seekDesc() {
    super.seekDesc();
    return this;
  }

  private String columnName(SFunction<TModel, ?> column) throws PersistenceException {
    return repository.columnName(LambdaProperty.name(column));
  }
}
