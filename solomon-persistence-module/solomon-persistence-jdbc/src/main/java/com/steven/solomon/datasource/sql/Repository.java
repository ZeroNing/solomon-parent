package com.steven.solomon.datasource.sql;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.lambda.LambdaProperty;
import com.steven.solomon.datasource.lambda.SFunction;
import com.steven.solomon.datasource.sql.param.DataSourcePageParam;
import com.steven.solomon.datasource.sql.result.PageResult;
import com.steven.solomon.pojo.param.BasePageParam;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用仓储基类。
 *
 * <p>Repository直接承载查询、分页、执行SQL、实体插入和实体更新能力，不再继承Query。</p>
 *
 * @param <TModel> 当前仓储绑定的实体类型
 */
public class Repository<TModel> {

  /** SQL执行器，负责底层JDBC执行、租户数据源切换和数据库方言分页。 */
  protected final SqlExecutor sqlExecutor;

  /** 当前仓储绑定的实体类型。 */
  protected final Class<TModel> modelClass;

  /**
   * 构造通用仓储。
   *
   * @param sqlExecutor SQL执行器，用于执行查询、分页、更新和批量SQL
   */
  @SuppressWarnings("unchecked")
  public Repository(SqlExecutor sqlExecutor) {
    this.sqlExecutor = sqlExecutor;
    this.modelClass = (Class<TModel>) resolveModelClass(getClass());
  }

  /**
   * 创建当前实体表的基础查询SQL。
   *
   * @return {@code SELECT * FROM 当前实体表} 查询对象
   * @throws DataSourceException 实体未配置 {@code @Table} 时抛出
   */
  public Sql sql() throws DataSourceException {
    return Sql.select("*").from(SqlMetadataUtils.tableName(modelClass));
  }

  /**
   * 创建指定查询列的基础SQL。
   *
   * <p>查询列通过实体 Getter 方法引用传入，会先解析为 Java 字段，再按实体元数据映射为数据库列名。</p>
   *
   * @param columns 查询列；为空时查询全部字段
   * @return 当前实体表的查询SQL
   * @throws DataSourceException 实体元数据缺失时抛出
   */
  @SafeVarargs
  public final Sql sql(SFunction<TModel, ?>... columns) throws DataSourceException {
    String[] columnNames = lambdaColumnNames(columns);
    if (ObjectUtil.isEmpty(columnNames)) {
      return sql();
    }
    return Sql.select(columnNames)
        .from(SqlMetadataUtils.tableName(modelClass));
  }

  /**
   * 创建当前实体表的查询执行器。
   *
   * @return Repository查询执行器，可继续调用 {@code list()}、{@code page(param)}
   * @throws DataSourceException 实体未配置 {@code @Table} 时抛出
   */
  public RepositoryQuery<TModel> query() throws DataSourceException {
    return query(sql());
  }

  /**
   * 基于指定SQL创建查询执行器。
   *
   * @param sql SQL对象，包含SQL文本和命名参数
   * @return Repository查询执行器
   */
  public RepositoryQuery<TModel> query(Sql sql) {
    return new RepositoryQuery<>(this, sql);
  }

  /**
   * 创建 Lambda 分页参数。
   *
   * <p>排序字段和深分页游标字段可以使用 Getter 方法引用指定。</p>
   *
   * @param pageNo 页码
   * @param pageSize 每页条数
   * @return Lambda 分页参数
   */
  public LambdaPageParam<TModel> pageParam(int pageNo, int pageSize) {
    return LambdaPageParam.of(this, pageNo, pageSize);
  }

  /**
   * 按主键查询单条实体。
   *
   * @param id 主键值；为空时直接返回null
   * @return 实体对象；无结果时返回null
   * @throws DataSourceException 主键元数据缺失或SQL执行失败时抛出
   */
  public TModel getById(Object id) throws DataSourceException {
    if (ObjectUtil.isEmpty(id)) {
      return null;
    }
    ColumnField primaryKey = SqlMetadataUtils.primaryKeyField(modelClass);
    return get(sql().eq(primaryKey.getColumnName(), id));
  }

  /**
   * 按主键查询单条实体，保留更贴近日常命名的别名。
   *
   * @param id 主键值；为空时直接返回null
   * @return 实体对象；无结果时返回null
   * @throws DataSourceException 主键元数据缺失或SQL执行失败时抛出
   */
  public TModel findById(Object id) throws DataSourceException {
    return getById(id);
  }

  /**
   * 按主键判断记录是否存在。
   *
   * @param id 主键值；为空时直接返回false
   * @return 存在返回true
   * @throws DataSourceException 主键元数据缺失或SQL执行失败时抛出
   */
  public boolean existsById(Object id) throws DataSourceException {
    if (ObjectUtil.isEmpty(id)) {
      return false;
    }
    ColumnField primaryKey = SqlMetadataUtils.primaryKeyField(modelClass);
    return exists(sql().eq(primaryKey.getColumnName(), id));
  }

  /**
   * 查询当前实体表全部数据。
   *
   * @return 实体列表
   * @throws DataSourceException SQL执行失败时抛出
   */
  public List<TModel> findAll() throws DataSourceException {
    return find(sql());
  }

  /**
   * 按字段查询实体列表。
   *
   * @param field 数据库字段名或字段表达式
   * @param value 字段值；为空时不会自动忽略
   * @return 实体列表
   * @throws DataSourceException SQL执行失败时抛出
   */
  public List<TModel> findByField(String field, Object value) throws DataSourceException {
    return find(sql().eq(field, value));
  }

  /**
   * 按 Getter 字段判断记录是否存在。
   *
   * @param column 实体字段 Getter
   * @param value 字段值
   * @return 存在返回true
   * @throws DataSourceException 字段元数据缺失或SQL执行失败时抛出
   */
  public boolean exists(SFunction<TModel, ?> column, Object value) throws DataSourceException {
    return exists(sql().eq(columnName(LambdaProperty.name(column)), value));
  }

  /**
   * 按字段查询单条实体。
   *
   * @param field 数据库字段名或字段表达式
   * @param value 字段值；为空时不会自动忽略
   * @return 第一条实体记录；无结果时返回null
   * @throws DataSourceException SQL执行失败时抛出
   */
  public TModel getByField(String field, Object value) throws DataSourceException {
    return get(sql().eq(field, value));
  }

  /**
   * 查询当前实体类型的单条记录。
   *
   * @param sql SQL对象，包含查询SQL和命名参数
   * @return 第一条实体记录；无结果时返回null
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public TModel get(Sql sql) throws DataSourceException {
    List<TModel> list = find(sql);
    return ObjectUtil.isEmpty(list) ? null : list.get(0);
  }

  /**
   * 使用问号占位符SQL查询当前实体类型的单条记录。
   *
   * @param sql SQL文本，支持 {@code ?} 占位符
   * @param params 按顺序绑定的参数集合
   * @return 第一条实体记录；无结果时返回null
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public TModel get(String sql, Collection<?> params) throws DataSourceException {
    return get(new Sql(sql, params));
  }

  /**
   * 查询当前实体类型列表。
   *
   * @param sql SQL对象，包含查询SQL和命名参数
   * @return 实体列表
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public List<TModel> find(Sql sql) throws DataSourceException {
    return sqlExecutor.query(sql, modelClass);
  }

  /**
   * 按指定结果类型查询列表。
   *
   * <p>适合 DTO/VO 投影查询，结果字段由 {@code SqlResultRowMapper} 按字段名、下划线字段名和
   * {@code @Column} 做映射。</p>
   *
   * @param sql SQL对象
   * @param resultType 返回结果类型
   * @param <TResult> 返回结果泛型
   * @return 查询结果列表
   * @throws DataSourceException SQL执行失败时抛出
   */
  public <TResult> List<TResult> findAs(Sql sql, Class<TResult> resultType)
      throws DataSourceException {
    return sqlExecutor.query(sql, resultType);
  }

  /**
   * 按指定结果类型查询单条记录。
   *
   * @param sql SQL对象
   * @param resultType 返回结果类型
   * @param <TResult> 返回结果泛型
   * @return 第一条记录；无结果时返回null
   * @throws DataSourceException SQL执行失败时抛出
   */
  public <TResult> TResult getAs(Sql sql, Class<TResult> resultType)
      throws DataSourceException {
    List<TResult> list = findAs(sql, resultType);
    return ObjectUtil.isEmpty(list) ? null : list.get(0);
  }

  /**
   * 查询当前实体类型列表，作为 {@link #find(Sql)} 的简单别名。
   *
   * @param sql SQL对象，包含SQL文本和命名参数
   * @return 实体列表
   * @throws DataSourceException SQL执行失败时抛出
   */
  public List<TModel> list(Sql sql) throws DataSourceException {
    return find(sql);
  }

  /**
   * 使用问号占位符SQL查询当前实体类型列表。
   *
   * @param sql SQL文本，支持 {@code ?} 占位符
   * @param params 按顺序绑定的参数集合
   * @return 实体列表
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public List<TModel> find(String sql, Collection<?> params) throws DataSourceException {
    return find(new Sql(sql, params));
  }

  /**
   * 查询Map列表。
   *
   * @param sql SQL对象，包含查询SQL和命名参数
   * @return 查询结果列表，每行数据以列名为key
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public List<Map<String, Object>> findMapList(Sql sql) throws DataSourceException {
    return sqlExecutor.queryForList(sql);
  }

  /**
   * 查询当前实体类型分页。
   *
   * @param sql 原始查询SQL对象
   * @param param dataSource分页参数，包含页码、页大小和排序表达式
   * @return 实体分页结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<TModel> findPage(Sql sql, DataSourcePageParam param)
      throws DataSourceException {
    return sqlExecutor.page(sql, param, modelClass);
  }

  /**
   * 按指定结果类型分页查询。
   *
   * @param sql SQL对象
   * @param param 分页参数
   * @param resultType 返回结果类型
   * @param <TResult> 返回结果泛型
   * @return 分页结果
   * @throws DataSourceException SQL执行失败时抛出
   */
  public <TResult> PageResult<TResult> findPageAs(
      Sql sql,
      DataSourcePageParam param,
      Class<TResult> resultType) throws DataSourceException {
    return sqlExecutor.page(sql, param, resultType);
  }

  /**
   * 查询当前实体类型分页，作为 {@link #findPage(Sql, DataSourcePageParam)} 的简单别名。
   *
   * @param sql 原始查询SQL对象
   * @param param 分页参数；{@code page=false} 时返回不分页列表包装结果
   * @return 实体分页结果
   * @throws DataSourceException SQL执行失败时抛出
   */
  public PageResult<TModel> page(Sql sql, DataSourcePageParam param)
      throws DataSourceException {
    return findPage(sql, param);
  }

  /**
   * 兼容项目通用分页参数，查询当前实体类型分页。
   *
   * @param sql 原始查询SQL对象
   * @param param 通用分页参数，读取pageNo和pageSize
   * @return 实体分页结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<TModel> findPageLite(Sql sql, BasePageParam param)
      throws DataSourceException {
    return sqlExecutor.page(sql, param.getPageNo(), param.getPageSize(), modelClass);
  }

  /**
   * 查询Map分页。
   *
   * @param sql 原始查询SQL对象
   * @param param dataSource分页参数，包含页码、页大小和排序表达式
   * @return Map分页结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<Map<String, Object>> findMapPage(Sql sql, DataSourcePageParam param)
      throws DataSourceException {
    return sqlExecutor.page(sql, param);
  }

  /**
   * 使用项目通用分页参数查询Map分页。
   *
   * @param sql 原始查询SQL对象
   * @param param 通用分页参数，读取pageNo和pageSize
   * @return Map分页结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<Map<String, Object>> findPageLiteMap(Sql sql, BasePageParam param)
      throws DataSourceException {
    return sqlExecutor.page(sql, param.getPageNo(), param.getPageSize());
  }

  /**
   * 执行增删改SQL。
   *
   * @param sql SQL对象，包含SQL文本和命名参数
   * @return 受影响行数
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public int execute(Sql sql) throws DataSourceException {
    return sqlExecutor.update(sql);
  }

  /**
   * 执行增删改SQL，作为 {@link #execute(Sql)} 的简单别名。
   *
   * @param sql SQL对象，包含SQL文本和命名参数
   * @return 受影响行数
   * @throws DataSourceException SQL执行失败时抛出
   */
  public int update(Sql sql) throws DataSourceException {
    return execute(sql);
  }

  /**
   * 使用问号占位符SQL执行增删改。
   *
   * @param sql SQL文本，支持 {@code ?} 占位符
   * @param params 按顺序绑定的参数集合
   * @return 受影响行数
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public int execute(String sql, Collection<?> params) throws DataSourceException {
    return execute(new Sql(sql, params));
  }

  /**
   * 插入单个实体。
   *
   * @param entity 实体对象，为null时不执行SQL
   * @return 受影响行数；entity为null时返回0
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出的国际化业务异常
   */
  public int create(TModel entity) throws DataSourceException {
    int[] results = create(toModelList(entity));
    return results.length == 0 ? 0 : results[0];
  }

  /**
   * 插入单个实体，作为 {@link #create(Object)} 的简单别名。
   *
   * @param entity 实体对象；为null时不执行SQL
   * @return 受影响行数
   * @throws DataSourceException SQL执行失败时抛出
   */
  public int insert(TModel entity) throws DataSourceException {
    return create(entity);
  }

  /**
   * 批量插入实体集合。
   *
   * @param entities 实体集合，集合中的null元素会被忽略
   * @return 每条插入SQL的受影响行数
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出的国际化业务异常
   */
  public int[] create(Collection<TModel> entities) throws DataSourceException {
    List<TModel> entityList = toModelList(entities);
    if (ObjectUtil.isEmpty(entityList)) {
      return new int[0];
    }
    try {
      List<ColumnField> fields = SqlMetadataUtils.insertFields(modelClass);
      StringBuilder columns = new StringBuilder();
      StringBuilder values = new StringBuilder();
      for (ColumnField field : fields) {
        columns.append(field.getColumnName()).append(", ");
        values.append(":").append(field.getFieldName()).append(", ");
      }
      removeLastComma(columns);
      removeLastComma(values);

      String sql = "INSERT INTO " + SqlMetadataUtils.tableName(modelClass)
          + " (" + columns + ") VALUES (" + values + ")";
      return sqlExecutor.batchUpdate(sql, toParamArray(entityList, fields));
    } catch (DataSourceException e) {
      throw e;
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e,
          modelClass.getName());
    }
  }

  /**
   * 批量插入实体集合，作为 {@link #create(Collection)} 的简单别名。
   *
   * @param entities 实体集合，集合中的null元素会被忽略
   * @return 每条插入SQL的受影响行数
   * @throws DataSourceException SQL执行失败时抛出
   */
  public int[] insert(Collection<TModel> entities) throws DataSourceException {
    return create(entities);
  }

  /**
   * 批量插入实体数组。
   *
   * @param entities 实体数组，数组中的null元素会被忽略
   * @return 每条插入SQL的受影响行数
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出的国际化业务异常
   */
  public int[] create(TModel[] entities) throws DataSourceException {
    return create(toModelList(entities));
  }

  /**
   * 按主键更新单个实体。
   *
   * @param entity 实体对象，为null时不执行SQL
   * @return 受影响行数；entity为null时返回0
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出的国际化业务异常
   */
  public int update(TModel entity) throws DataSourceException {
    return update(entity, (String[]) null);
  }

  /**
   * 按主键更新单个实体的指定字段。
   *
   * @param entity 实体对象，为null时不执行SQL
   * @param columns 需要更新的字段名，支持Java属性名或数据库列名；为null时更新全部可更新字段
   * @return 受影响行数；entity为null时返回0
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出的国际化业务异常
   */
  public int update(TModel entity, String[] columns) throws DataSourceException {
    int[] results = update(toModelList(entity), columns);
    return results.length == 0 ? 0 : results[0];
  }

  /**
   * 按主键更新单个实体的指定字段。
   *
   * @param entity 实体对象；为null时不执行SQL
   * @param columns 需要更新的字段名集合，支持Java属性名或数据库列名
   * @return 受影响行数
   * @throws DataSourceException SQL执行失败时抛出
   */
  public int update(TModel entity, Collection<String> columns) throws DataSourceException {
    return update(entity, toStringArray(columns));
  }

  /**
   * 按主键批量更新实体集合的指定字段。
   *
   * @param entities 实体集合，集合中的null元素会被忽略
   * @param columns 需要更新的字段名，支持Java属性名或数据库列名；为null时更新全部可更新字段
   * @return 每条更新SQL的受影响行数
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出的国际化业务异常
   */
  public int[] update(Collection<TModel> entities, String[] columns) throws DataSourceException {
    List<TModel> entityList = toModelList(entities);
    if (ObjectUtil.isEmpty(entityList)) {
      return new int[0];
    }
    try {
      ColumnField primaryKey = SqlMetadataUtils.primaryKeyField(modelClass);
      List<ColumnField> fields = SqlMetadataUtils.updateFields(
          modelClass, columns);
      StringBuilder setSql = new StringBuilder();
      for (ColumnField field : fields) {
        setSql.append(field.getColumnName())
            .append(" = :")
            .append(field.getFieldName())
            .append(", ");
      }
      removeLastComma(setSql);

      String sql = "UPDATE " + SqlMetadataUtils.tableName(modelClass)
          + " SET " + setSql
          + " WHERE " + primaryKey.getColumnName() + " = :" + primaryKey.getFieldName();
      List<ColumnField> paramFields = new ArrayList<>(fields);
      paramFields.add(primaryKey);
      return sqlExecutor.batchUpdate(sql, toParamArray(entityList, paramFields));
    } catch (DataSourceException e) {
      throw e;
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e,
          modelClass.getName());
    }
  }

  /**
   * 按主键批量更新实体集合的指定字段。
   *
   * @param entities 实体集合，集合中的null元素会被忽略
   * @param columns 需要更新的字段名集合，支持Java属性名或数据库列名
   * @return 每条更新SQL的受影响行数
   * @throws DataSourceException SQL执行失败时抛出
   */
  public int[] update(Collection<TModel> entities, Collection<String> columns)
      throws DataSourceException {
    return update(entities, toStringArray(columns));
  }

  /**
   * 按主键批量更新实体数组的指定字段。
   *
   * @param entities 实体数组，数组中的null元素会被忽略
   * @param columns 需要更新的字段名，支持Java属性名或数据库列名；为null时更新全部可更新字段
   * @return 每条更新SQL的受影响行数
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出的国际化业务异常
   */
  public int[] update(TModel[] entities, String[] columns) throws DataSourceException {
    return update(toModelList(entities), columns);
  }

  /**
   * 按主键更新单个实体的指定 Getter 字段。
   *
   * @param entity 实体对象；为null时不执行SQL
   * @param columns 需要更新的字段 Getter；为空时更新全部可更新字段
   * @return 受影响行数
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出
   */
  @SafeVarargs
  public final int updateColumns(TModel entity, SFunction<TModel, ?>... columns)
      throws DataSourceException {
    return update(entity, lambdaColumnNames(columns));
  }

  /**
   * 按主键批量更新实体的指定 Getter 字段。
   *
   * @param entities 实体集合；集合中的null元素会被忽略
   * @param columns 需要更新的字段 Getter；为空时更新全部可更新字段
   * @return 每条更新SQL的受影响行数
   * @throws DataSourceException 元数据缺失或SQL执行失败时抛出
   */
  @SafeVarargs
  public final int[] updateColumns(Collection<TModel> entities, SFunction<TModel, ?>... columns)
      throws DataSourceException {
    return update(entities, lambdaColumnNames(columns));
  }

  /**
   * 按主键删除单条实体。
   *
   * @param entity 实体对象；为null时不执行SQL
   * @return 受影响行数
   * @throws DataSourceException 主键元数据缺失或SQL执行失败时抛出
   */
  public int delete(TModel entity) throws DataSourceException {
    if (ObjectUtil.isEmpty(entity)) {
      return 0;
    }
    try {
      ColumnField primaryKey = SqlMetadataUtils.primaryKeyField(modelClass);
      return deleteById(primaryKey.getValue(entity));
    } catch (DataSourceException e) {
      throw e;
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e,
          modelClass.getName());
    }
  }

  /**
   * 按主键删除记录。
   *
   * @param id 主键值；为空时不执行SQL
   * @return 受影响行数
   * @throws DataSourceException 主键元数据缺失或SQL执行失败时抛出
   */
  public int deleteById(Object id) throws DataSourceException {
    if (ObjectUtil.isEmpty(id)) {
      return 0;
    }
    ColumnField primaryKey = SqlMetadataUtils.primaryKeyField(modelClass);
    Sql sql = Sql.of("DELETE FROM " + SqlMetadataUtils.tableName(modelClass)
        + " WHERE " + primaryKey.getColumnName() + " = :id")
        .param("id", id);
    return execute(sql);
  }

  /**
   * 按主键批量删除记录。
   *
   * <p>主键集合通过命名参数绑定，不会把值拼进SQL文本；集合为空时直接返回0。</p>
   *
   * @param ids 主键集合
   * @return 受影响行数
   * @throws DataSourceException 主键元数据缺失或SQL执行失败时抛出
   */
  public int deleteByIds(Collection<?> ids) throws DataSourceException {
    if (ObjectUtil.isEmpty(ids)) {
      return 0;
    }
    ColumnField primaryKey = SqlMetadataUtils.primaryKeyField(modelClass);
    Sql sql = Sql.of("DELETE FROM " + SqlMetadataUtils.tableName(modelClass)
        + " WHERE " + primaryKey.getColumnName() + " IN (:ids)")
        .param("ids", ids);
    return execute(sql);
  }

  /**
   * 统计当前实体表总数。
   *
   * @return 当前实体表总行数
   * @throws DataSourceException SQL执行失败时抛出
   */
  public long count() throws DataSourceException {
    return sqlExecutor.count(modelClass);
  }

  /**
   * 统计指定SQL结果总数。
   *
   * @param sql SQL对象，通常为查询SQL
   * @return 总行数
   * @throws DataSourceException SQL执行失败时抛出
   */
  public long count(Sql sql) throws DataSourceException {
    return sqlExecutor.count(sql);
  }

  /**
   * 判断指定查询是否存在数据。
   *
   * @param sql 查询SQL
   * @return 至少存在一行返回true
   * @throws DataSourceException SQL执行失败时抛出
   */
  public boolean exists(Sql sql) throws DataSourceException {
    return count(sql) > 0;
  }

  /**
   * 对当前实体表字段求和。
   *
   * @param columnName 字段名或表达式
   * @return 求和结果
   * @throws DataSourceException SQL执行失败时抛出
   */
  public BigDecimal sum(String columnName) throws DataSourceException {
    return sqlExecutor.sum(SqlMetadataUtils.tableName(modelClass), columnName);
  }

  /**
   * 对指定SQL结果字段求和。
   *
   * @param sql SQL对象，通常为查询SQL
   * @param columnName 聚合字段名或表达式
   * @return 求和结果
   * @throws DataSourceException SQL执行失败时抛出
   */
  public BigDecimal sum(Sql sql, String columnName) throws DataSourceException {
    return sqlExecutor.sum(sql, columnName);
  }

  /**
   * 对当前实体表字段求最大值。
   *
   * @param columnName 字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回值泛型
   * @return 最大值
   * @throws DataSourceException SQL执行失败时抛出
   */
  public <T> T max(String columnName, Class<T> resultType) throws DataSourceException {
    return sqlExecutor.max(SqlMetadataUtils.tableName(modelClass), columnName, resultType);
  }

  /**
   * 对指定SQL结果字段求最大值。
   *
   * @param sql SQL对象，通常为查询SQL
   * @param columnName 聚合字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回值泛型
   * @return 最大值
   * @throws DataSourceException SQL执行失败时抛出
   */
  public <T> T max(Sql sql, String columnName, Class<T> resultType)
      throws DataSourceException {
    return sqlExecutor.max(sql, columnName, resultType);
  }

  /**
   * 对当前实体表字段求最小值。
   *
   * @param columnName 字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回值泛型
   * @return 最小值
   * @throws DataSourceException SQL执行失败时抛出
   */
  public <T> T min(String columnName, Class<T> resultType) throws DataSourceException {
    return sqlExecutor.min(SqlMetadataUtils.tableName(modelClass), columnName, resultType);
  }

  /**
   * 对指定SQL结果字段求最小值。
   *
   * @param sql SQL对象，通常为查询SQL
   * @param columnName 聚合字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回值泛型
   * @return 最小值
   * @throws DataSourceException SQL执行失败时抛出
   */
  public <T> T min(Sql sql, String columnName, Class<T> resultType)
      throws DataSourceException {
    return sqlExecutor.min(sql, columnName, resultType);
  }

  /**
   * 对当前实体表字段求平均值。
   *
   * @param columnName 字段名或表达式
   * @return 平均值
   * @throws DataSourceException SQL执行失败时抛出
   */
  public BigDecimal avg(String columnName) throws DataSourceException {
    return sqlExecutor.avg(SqlMetadataUtils.tableName(modelClass), columnName);
  }

  /**
   * 对指定SQL结果字段求平均值。
   *
   * @param sql SQL对象，通常为查询SQL
   * @param columnName 聚合字段名或表达式
   * @return 平均值
   * @throws DataSourceException SQL执行失败时抛出
   */
  public BigDecimal avg(Sql sql, String columnName) throws DataSourceException {
    return sqlExecutor.avg(sql, columnName);
  }

  String columnName(String fieldName) throws DataSourceException {
    return SqlMetadataUtils.columnName(modelClass, fieldName);
  }

  private String[] toStringArray(Collection<String> values) {
    if (ObjectUtil.isEmpty(values)) {
      return null;
    }
    List<String> list = new ArrayList<>();
    for (String value : values) {
      if (ObjectUtil.isNotEmpty(value)) {
        list.add(value);
      }
    }
    return list.toArray(new String[0]);
  }

  @SafeVarargs
  private final String[] lambdaColumnNames(SFunction<TModel, ?>... columns)
      throws DataSourceException {
    if (ObjectUtil.isEmpty(columns)) {
      return null;
    }
    List<String> columnNames = new ArrayList<>(columns.length);
    for (SFunction<TModel, ?> column : columns) {
      columnNames.add(columnName(LambdaProperty.name(column)));
    }
    return columnNames.toArray(new String[0]);
  }

  private List<TModel> toModelList(TModel entity) {
    if (ObjectUtil.isEmpty(entity)) {
      return List.of();
    }
    return List.of(entity);
  }

  private List<TModel> toModelList(TModel[] entities) {
    if (ObjectUtil.isEmpty(entities)) {
      return List.of();
    }
    List<TModel> list = new ArrayList<>(entities.length);
    for (TModel entity : entities) {
      if (ObjectUtil.isNotEmpty(entity)) {
        list.add(entity);
      }
    }
    return list;
  }

  private List<TModel> toModelList(Collection<TModel> entities) {
    if (ObjectUtil.isEmpty(entities)) {
      return List.of();
    }
    List<TModel> list = new ArrayList<>(entities.size());
    for (TModel entity : entities) {
      if (ObjectUtil.isNotEmpty(entity)) {
        list.add(entity);
      }
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  private Map<String, ?>[] toParamArray(
      List<TModel> entities,
      List<ColumnField> fields) throws IllegalAccessException {
    Map<String, ?>[] params = new Map[entities.size()];
    for (int i = 0; i < entities.size(); i++) {
      Map<String, Object> param = new LinkedHashMap<>();
      TModel entity = entities.get(i);
      for (ColumnField field : fields) {
        param.put(field.getFieldName(), field.getValue(entity));
      }
      params[i] = param;
    }
    return params;
  }

  private void removeLastComma(StringBuilder builder) {
    int index = builder.lastIndexOf(", ");
    if (index >= 0) {
      builder.delete(index, index + 2);
    }
  }

  private Class<?> resolveModelClass(Class<?> clazz) {
    Type type = clazz.getGenericSuperclass();
    while (ObjectUtil.isNotEmpty(type)) {
      if (type instanceof ParameterizedType parameterizedType) {
        Type actualType = parameterizedType.getActualTypeArguments()[0];
        if (actualType instanceof Class<?> actualClass) {
          return actualClass;
        }
      }
      clazz = clazz.getSuperclass();
      type = ObjectUtil.isEmpty(clazz) ? null : clazz.getGenericSuperclass();
    }
    return Object.class;
  }
}
