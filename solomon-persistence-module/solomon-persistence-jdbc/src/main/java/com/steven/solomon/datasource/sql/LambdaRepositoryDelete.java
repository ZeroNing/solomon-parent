package com.steven.solomon.datasource.sql;

import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lambda 仓储删除器。
 *
 * <p>删除条件通过 Getter 方法引用指定，执行前强制要求存在 WHERE 条件，避免误删全表。</p>
 *
 * @param <TModel> 当前 Repository 绑定的实体类型
 */
public class LambdaRepositoryDelete<TModel>
    extends LambdaConditionSupport<LambdaRepositoryDelete<TModel>, TModel> {

  /**
   * 构造 Lambda 删除器。
   *
   * @param repository 当前仓储
   */
  public LambdaRepositoryDelete(Repository<TModel> repository) {
    super(repository);
  }

  /**
   * 执行删除。
   *
   * @return 受影响行数
   * @throws DataSourceException 条件为空时抛出
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
    if (!hasConditions()) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED,
          "lambdaDeleteWhereRequired", repository.modelClass.getName());
    }
  }

  @Override
  protected LambdaRepositoryDelete<TModel> self() {
    return this;
  }

  private Sql buildSql() throws DataSourceException {
    Map<String, Object> params = new LinkedHashMap<>();
    StringBuilder builder = new StringBuilder("DELETE FROM ")
        .append(SqlMetadataUtils.tableName(repository.modelClass));
    appendWhere(builder, params);
    return Sql.of(builder.toString(), params);
  }
}
