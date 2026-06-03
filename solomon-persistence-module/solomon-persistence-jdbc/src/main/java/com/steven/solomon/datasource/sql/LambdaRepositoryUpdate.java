package com.steven.solomon.datasource.sql;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.lambda.SFunction;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lambda 仓储更新器。
 *
 * <p>用于以 {@code User::getName} 这类方法引用指定更新字段和条件字段，字段名统一走实体元数据映射。
 * 执行前会强制要求存在 WHERE 条件，避免误执行全表更新。</p>
 *
 * @param <TModel> 当前 Repository 绑定的实体类型
 */
public class LambdaRepositoryUpdate<TModel>
    extends LambdaConditionSupport<LambdaRepositoryUpdate<TModel>, TModel> {

  private final Map<String, Object> setValues = new LinkedHashMap<>();

  /**
   * 构造 Lambda 更新器。
   *
   * @param repository 当前仓储
   */
  public LambdaRepositoryUpdate(Repository<TModel> repository) {
    super(repository);
  }

  public LambdaRepositoryUpdate<TModel> set(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    setValues.put(columnName(column), value);
    return this;
  }

  public LambdaRepositoryUpdate<TModel> setIf(
      boolean condition,
      SFunction<TModel, ?> column,
      Object value) throws DataSourceException {
    return condition ? set(column, value) : this;
  }

  public LambdaRepositoryUpdate<TModel> setIfNotEmpty(SFunction<TModel, ?> column, Object value)
      throws DataSourceException {
    if (ObjectUtil.isNotEmpty(value)) {
      set(column, value);
    }
    return this;
  }

  /**
   * 执行更新。
   *
   * @return 受影响行数
   * @throws DataSourceException 更新字段为空或条件为空时抛出
   */
  public int execute() throws DataSourceException {
    validateBeforeBuild();
    return repository.update(buildSql());
  }

  public Sql sql() throws DataSourceException {
    validateBeforeBuild();
    return buildSql();
  }

  private void validateBeforeBuild() throws DataSourceException {
    if (ObjectUtil.isEmpty(setValues)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND,
          repository.modelClass.getName());
    }
    if (!hasConditions()) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED,
          "lambdaUpdateWhereRequired", repository.modelClass.getName());
    }
  }

  @Override
  protected LambdaRepositoryUpdate<TModel> self() {
    return this;
  }

  private Sql buildSql() throws DataSourceException {
    Map<String, Object> params = new LinkedHashMap<>();
    StringBuilder builder = new StringBuilder("UPDATE ")
        .append(SqlMetadataUtils.tableName(repository.modelClass))
        .append(" SET ");
    int index = 1;
    for (Map.Entry<String, Object> entry : setValues.entrySet()) {
      String paramName = "set" + index;
      if (index > 1) {
        builder.append(", ");
      }
      builder.append(entry.getKey()).append(" = :").append(paramName);
      params.put(paramName, entry.getValue());
      index++;
    }
    appendWhere(builder, params);
    return Sql.of(builder.toString(), params);
  }
}
