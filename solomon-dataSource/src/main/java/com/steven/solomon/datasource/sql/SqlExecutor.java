package com.steven.solomon.datasource.sql;

import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.SingleDataSourceProperties;
import com.steven.solomon.datasource.routing.DataSourceTenantContext;
import com.steven.solomon.datasource.sql.converter.SqlResultRowMapper;
import com.steven.solomon.datasource.sql.converter.SqlTypeConverterRegistry;
import com.steven.solomon.datasource.sql.dialect.SqlDialect;
import com.steven.solomon.datasource.sql.param.DataSourcePageParam;
import com.steven.solomon.datasource.sql.result.PageResult;
import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

/**
 * SQL执行器。
 *
 * <p>统一执行命名参数SQL，支持普通查询、分页查询、游标分页和常用聚合函数。底层使用
 * {@link NamedParameterJdbcTemplate}，数据源切换由动态数据源路由和租户上下文完成。</p>
 */
public class SqlExecutor {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  private final SolomonDataSourceProperties properties;

  private final DataSourceTenantContext context;

  private final SqlTypeConverterRegistry converterRegistry;

  /**
   * 构造SQL执行器。
   *
   * @param jdbcTemplate Spring命名参数JDBC模板，用于执行SQL和绑定命名参数
   * @param properties 动态数据源配置，用于解析当前租户的数据类型和SQL方言
   * @param context 数据源租户上下文，用于获取当前线程中的租户编码
   */
  public SqlExecutor(
      NamedParameterJdbcTemplate jdbcTemplate,
      SolomonDataSourceProperties properties,
      DataSourceTenantContext context) {
    this(jdbcTemplate, properties, context, SqlTypeConverterRegistry.defaultRegistry());
  }

  /**
   * 构造SQL执行器。
   *
   * @param jdbcTemplate Spring命名参数JDBC模板，用于执行SQL和绑定命名参数
   * @param properties 动态数据源配置，用于解析当前租户的数据库类型和SQL方言
   * @param context 数据源租户上下文，用于获取当前线程中的租户编码
   * @param converterRegistry SQL类型转换器注册器，用于查询结果和写入参数转换
   */
  public SqlExecutor(
      NamedParameterJdbcTemplate jdbcTemplate,
      SolomonDataSourceProperties properties,
      DataSourceTenantContext context,
      SqlTypeConverterRegistry converterRegistry) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
    this.context = context;
    this.converterRegistry = converterRegistry == null
        ? SqlTypeConverterRegistry.defaultRegistry()
        : converterRegistry;
  }

  /**
   * 执行增删改SQL。
   *
   * @param sql SQL对象，包含SQL文本和命名参数，参数占位符格式为 {@code :paramName}
   * @return 受影响的行数
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public int update(Sql sql) throws DataSourceException {
    try {
      return jdbcTemplate.update(sql.getText(), convertParams(sql.getParams()));
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e,
          sql.getText());
    }
  }

  /**
   * 批量执行命名参数SQL。
   *
   * @param sql SQL文本，参数占位符格式为 {@code :paramName}
   * @param params 批量参数数组，每个Map表示一条SQL执行所需的命名参数
   * @return 每条SQL对应的受影响行数
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public int[] batchUpdate(String sql, Map<String, ?>[] params) throws DataSourceException {
    try {
      SqlInjectionGuard.validateRawSql(sql, "batchUpdate");
      return jdbcTemplate.batchUpdate(sql, convertBatchParams(params));
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e, sql);
    }
  }

  /**
   * 查询实体列表。
   *
   * @param sql SQL对象，包含查询SQL和命名参数
   * @param resultType 返回结果类型，字段按 {@link BeanPropertyRowMapper} 规则映射
   * @param <T> 返回对象类型
   * @return 查询结果列表
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> List<T> query(Sql sql, Class<T> resultType) throws DataSourceException {
    if (converterRegistry.isSimpleValueType(resultType)) {
      return query(sql, (rs, rowNum) -> {
        try {
          return converterRegistry.convertForJava(rs.getObject(1), resultType);
        } catch (DataSourceException e) {
          throw new SQLException(e);
        }
      });
    }
    return query(sql, SqlResultRowMapper.of(resultType, converterRegistry));
  }

  /**
   * 使用自定义行映射器查询列表。
   *
   * @param sql SQL对象，包含查询SQL和命名参数
   * @param rowMapper 自定义行映射器，用于将ResultSet行转换为业务对象
   * @param <T> 返回对象类型
   * @return 查询结果列表
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> List<T> query(Sql sql, RowMapper<T> rowMapper) throws DataSourceException {
    try {
      return jdbcTemplate.query(sql.getText(), convertParams(sql.getParams()), rowMapper);
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e,
          sql.getText());
    }
  }

  /**
   * 查询Map列表。
   *
   * @param sql SQL对象，包含查询SQL和命名参数
   * @return 查询结果列表，每行数据以列名为key
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public List<Map<String, Object>> queryForList(Sql sql) throws DataSourceException {
    try {
      return jdbcTemplate.queryForList(sql.getText(), convertParams(sql.getParams()));
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e,
          sql.getText());
    }
  }

  /**
   * 查询单个对象。
   *
   * @param sql SQL对象，包含查询SQL和命名参数
   * @param resultType 返回结果类型，适合单列值或Spring可直接转换的类型
   * @param <T> 返回对象类型
   * @return 查询到的单个对象
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T queryForObject(Sql sql, Class<T> resultType) throws DataSourceException {
    try {
      List<T> records = query(sql, resultType);
      return records.isEmpty() ? null : records.get(0);
    } catch (Exception e) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e,
          sql.getText());
    }
  }

  /**
   * 分页查询实体列表。
   *
   * @param sql 原始查询SQL对象，不能包含数据库分页语句
   * @param pageNo 页码，从1开始，小于1时按1处理
   * @param pageSize 每页条数，小于1时按1处理
   * @param resultType 返回结果类型，字段按 {@link BeanPropertyRowMapper} 规则映射
   * @param <T> 返回对象类型
   * @return 分页结果，包含总数、当前页数据和是否有下一页
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> PageResult<T> page(Sql sql, int pageNo, int pageSize, Class<T> resultType)
      throws DataSourceException {
    int currentPageNo = normalizePageNo(pageNo);
    int currentPageSize = normalizePageSize(pageSize);
    long total = count(sql);
    String pageSql = resolveDialect().pageSql(sql.getText(), currentPageNo, currentPageSize);
    List<T> records = query(Sql.of(pageSql, sql.getParams()), resultType);
    return new PageResult<>(records, total, currentPageNo, currentPageSize);
  }

  /**
   * 使用分页参数对象分页查询实体列表。
   *
   * @param sql 原始查询SQL对象，支持 {@link Sql#select(String...)} 和 {@link Sql#New(String)} 两种写法
   * @param param 分页参数对象，读取pageNo、pageSize和排序表达式
   * @param resultType 返回结果类型
   * @param <T> 返回对象类型
   * @return 分页结果，包含总数、当前页数据和是否有下一页
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> PageResult<T> page(Sql sql, DataSourcePageParam param, Class<T> resultType)
      throws DataSourceException {
    param = normalizePageParam(param);
    Sql pageSql = applyOrderBy(sql, param);
    if (!param.getPage()) {
      List<T> records = query(pageSql, resultType);
      return new PageResult<>(records, records.size(), 1, Math.max(records.size(), 1));
    }
    if (shouldUseSeekPage(param)) {
      return seekPageResult(pageSql, param, resultType);
    }
    return page(pageSql, param.getPageNo(), param.getPageSize(), resultType);
  }

  /**
   * 分页查询Map列表。
   *
   * @param sql 原始查询SQL对象，不能包含数据库分页语句
   * @param pageNo 页码，从1开始，小于1时按1处理
   * @param pageSize 每页条数，小于1时按1处理
   * @return Map分页结果，每行数据以列名为key
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<Map<String, Object>> page(Sql sql, int pageNo, int pageSize)
      throws DataSourceException {
    int currentPageNo = normalizePageNo(pageNo);
    int currentPageSize = normalizePageSize(pageSize);
    long total = count(sql);
    String pageSql = resolveDialect().pageSql(sql.getText(), currentPageNo, currentPageSize);
    List<Map<String, Object>> records = queryForList(Sql.of(pageSql, sql.getParams()));
    return new PageResult<>(records, total, currentPageNo, currentPageSize);
  }

  /**
   * 使用分页参数对象分页查询Map列表。
   *
   * @param sql 原始查询SQL对象，支持手写SQL和结构化SQL构造
   * @param param 分页参数对象，读取pageNo、pageSize和排序表达式
   * @return Map分页结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public PageResult<Map<String, Object>> page(Sql sql, DataSourcePageParam param)
      throws DataSourceException {
    param = normalizePageParam(param);
    Sql pageSql = applyOrderBy(sql, param);
    if (!param.getPage()) {
      List<Map<String, Object>> records = queryForList(pageSql);
      return new PageResult<>(records, records.size(), 1, Math.max(records.size(), 1));
    }
    if (shouldUseSeekPage(param)) {
      return seekPageResult(pageSql, param);
    }
    return page(pageSql, param.getPageNo(), param.getPageSize());
  }

  private <T> PageResult<T> seekPageResult(
      Sql sql,
      DataSourcePageParam param,
      Class<T> resultType) throws DataSourceException {
    int currentPageNo = normalizePageNo(param.getPageNo());
    int currentPageSize = normalizePageSize(param.getPageSize());
    String seekColumn = resolveSeekColumn(param);
    Object lastValue = resolveSeekStartValue(sql, param, seekColumn, currentPageNo,
        currentPageSize);
    long total = count(sql);
    if (currentPageNo > 1 && lastValue == null) {
      return new PageResult<>(List.of(), total, currentPageNo, currentPageSize, true, null);
    }
    Sql seekSql = sql.buildSeekPage(resolveDialect(), seekColumn, lastValue, param.getAsc(),
        currentPageSize + 1);
    List<T> records = new ArrayList<>(query(seekSql, resultType));
    boolean hasNext = records.size() > currentPageSize;
    if (hasNext) {
      records.remove(records.size() - 1);
    }
    return new PageResult<>(records, total, currentPageNo, currentPageSize, true,
        hasNext ? resolveNextSeekValue(records, seekColumn) : null);
  }

  private PageResult<Map<String, Object>> seekPageResult(
      Sql sql,
      DataSourcePageParam param) throws DataSourceException {
    int currentPageNo = normalizePageNo(param.getPageNo());
    int currentPageSize = normalizePageSize(param.getPageSize());
    String seekColumn = resolveSeekColumn(param);
    Object lastValue = resolveSeekStartValue(sql, param, seekColumn, currentPageNo,
        currentPageSize);
    long total = count(sql);
    if (currentPageNo > 1 && lastValue == null) {
      return new PageResult<>(List.of(), total, currentPageNo, currentPageSize, true, null);
    }
    Sql seekSql = sql.buildSeekPage(resolveDialect(), seekColumn, lastValue, param.getAsc(),
        currentPageSize + 1);
    List<Map<String, Object>> records = new ArrayList<>(queryForList(seekSql));
    boolean hasNext = records.size() > currentPageSize;
    if (hasNext) {
      records.remove(records.size() - 1);
    }
    return new PageResult<>(records, total, currentPageNo, currentPageSize, true,
        hasNext ? resolveNextSeekValue(records, seekColumn) : null);
  }

  /**
   * 对任意查询SQL统计总数。
   *
   * @param sql 原始查询SQL对象，会被包装为 {@code SELECT COUNT(1) FROM (...)}
   * @return 总行数
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public long count(Sql sql) throws DataSourceException {
    Sql countSql = sql.withoutOrderBy();
    Long value = queryForObject(Sql.of(resolveDialect().countSql(countSql.getText()),
            countSql.getParams()),
        Long.class);
    return value == null ? 0 : value;
  }

  /**
   * 按实体表统计总数。
   *
   * @param entityClass 实体类型，实体类需要标注 {@code @Table}
   * @return 总行数
   * @throws DataSourceException 表注解缺失或SQL执行失败时抛出的国际化业务异常
   */
  public long count(Class<?> entityClass) throws DataSourceException {
    return count(Sql.of("SELECT * FROM " + SqlMetadataUtils.tableName(entityClass)));
  }

  /**
   * 按表统计总数。
   *
   * @param tableName 表名，会直接拼接到SQL中，调用方需要保证来源可信
   * @return 总行数
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public long count(String tableName) throws DataSourceException {
    SqlInjectionGuard.validateTableExpression(tableName, "countTable");
    Long value = aggregate(tableName, "COUNT", "1", Long.class);
    return value == null ? 0 : value;
  }

  /**
   * 对指定表字段求和。
   *
   * @param tableName 表名，会直接拼接到SQL中，调用方需要保证来源可信
   * @param columnName 求和字段名或表达式
   * @return 求和结果，数据库返回null时保持null
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public BigDecimal sum(String tableName, String columnName) throws DataSourceException {
    SqlInjectionGuard.validateTableExpression(tableName, "aggregateTable");
    SqlInjectionGuard.validateExpression(columnName, "aggregateColumn");
    return aggregate(tableName, "SUM", columnName, BigDecimal.class);
  }

  /**
   * 对指定表字段求最大值。
   *
   * @param tableName 表名，会直接拼接到SQL中，调用方需要保证来源可信
   * @param columnName 求最大值字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回对象类型
   * @return 最大值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T max(String tableName, String columnName, Class<T> resultType)
      throws DataSourceException {
    SqlInjectionGuard.validateTableExpression(tableName, "aggregateTable");
    SqlInjectionGuard.validateExpression(columnName, "aggregateColumn");
    return aggregate(tableName, "MAX", columnName, resultType);
  }

  /**
   * 对指定表字段求最小值。
   *
   * @param tableName 表名，会直接拼接到SQL中，调用方需要保证来源可信
   * @param columnName 求最小值字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回对象类型
   * @return 最小值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T min(String tableName, String columnName, Class<T> resultType)
      throws DataSourceException {
    SqlInjectionGuard.validateTableExpression(tableName, "aggregateTable");
    SqlInjectionGuard.validateExpression(columnName, "aggregateColumn");
    return aggregate(tableName, "MIN", columnName, resultType);
  }

  /**
   * 对指定表字段求平均值。
   *
   * @param tableName 表名，会直接拼接到SQL中，调用方需要保证来源可信
   * @param columnName 求平均值字段名或表达式
   * @return 平均值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public BigDecimal avg(String tableName, String columnName) throws DataSourceException {
    SqlInjectionGuard.validateTableExpression(tableName, "aggregateTable");
    SqlInjectionGuard.validateExpression(columnName, "aggregateColumn");
    return aggregate(tableName, "AVG", columnName, BigDecimal.class);
  }

  /**
   * 执行通用聚合函数。
   *
   * @param tableName 表名，会直接拼接到SQL中，调用方需要保证来源可信
   * @param functionName 聚合函数名，例如 {@code COUNT}、{@code SUM}、{@code MAX}
   * @param columnName 聚合字段名或表达式，例如 {@code "amount"}、{@code "1"}
   * @param resultType 返回值类型
   * @param <T> 返回对象类型
   * @return 聚合结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T aggregate(String tableName, String functionName, String columnName, Class<T> resultType)
      throws DataSourceException {
    SqlInjectionGuard.validateTableExpression(tableName, "aggregateTable");
    SqlInjectionGuard.validateAggregateFunction(functionName);
    SqlInjectionGuard.validateExpression(columnName, "aggregateColumn");
    String sql = "SELECT " + functionName + "(" + columnName + ") FROM " + tableName;
    return queryForObject(Sql.of(sql), resultType);
  }

  /**
   * 对SQL查询结果执行聚合函数，适合连表查询后的聚合。
   *
   * @param sql SQL构造对象，会先构建为子查询
   * @param functionName 聚合函数名，例如 {@code COUNT}、{@code SUM}、{@code MAX}
   * @param columnName 聚合字段名或表达式，基于子查询别名 {@code t} 的可见字段
   * @param resultType 返回值类型
   * @param <T> 返回对象类型
   * @return 聚合结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T aggregate(Sql sql, String functionName, String columnName, Class<T> resultType)
      throws DataSourceException {
    SqlInjectionGuard.validateAggregateFunction(functionName);
    SqlInjectionGuard.validateExpression(columnName, "aggregateColumn");
    Sql baseSql = sql.build();
    String aggregateSql = "SELECT " + functionName + "(" + columnName + ") FROM ("
        + baseSql.getText() + ") t";
    return queryForObject(Sql.of(aggregateSql, baseSql.getParams()), resultType);
  }

  /**
   * 对SQL查询结果求和。
   *
   * @param sql SQL构造对象，会先构建为子查询
   * @param columnName 求和字段名或表达式
   * @return 求和结果
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public BigDecimal sum(Sql sql, String columnName) throws DataSourceException {
    return aggregate(sql, "SUM", columnName, BigDecimal.class);
  }

  /**
   * 对SQL查询结果求最大值。
   *
   * @param sql SQL构造对象，会先构建为子查询
   * @param columnName 求最大值字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回对象类型
   * @return 最大值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T max(Sql sql, String columnName, Class<T> resultType)
      throws DataSourceException {
    return aggregate(sql, "MAX", columnName, resultType);
  }

  /**
   * 对SQL查询结果求最小值。
   *
   * @param sql SQL构造对象，会先构建为子查询
   * @param columnName 求最小值字段名或表达式
   * @param resultType 返回值类型
   * @param <T> 返回对象类型
   * @return 最小值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public <T> T min(Sql sql, String columnName, Class<T> resultType)
      throws DataSourceException {
    return aggregate(sql, "MIN", columnName, resultType);
  }

  /**
   * 对SQL查询结果求平均值。
   *
   * @param sql SQL构造对象，会先构建为子查询
   * @param columnName 求平均值字段名或表达式
   * @return 平均值
   * @throws DataSourceException SQL执行失败时抛出的国际化业务异常
   */
  public BigDecimal avg(Sql sql, String columnName) throws DataSourceException {
    return aggregate(sql, "AVG", columnName, BigDecimal.class);
  }

  /**
   * 转换命名SQL参数。
   *
   * @param params 原始命名参数，key为参数名，value为Java参数值
   * @return 转换后的JDBC参数，LocalDateTime、Date、枚举、集合等会被统一处理
   */
  private Map<String, Object> convertParams(Map<String, ?> params) {
    Map<String, Object> converted = new LinkedHashMap<>();
    if (params == null || params.isEmpty()) {
      return converted;
    }
    for (Map.Entry<String, ?> entry : params.entrySet()) {
      converted.put(entry.getKey(), converterRegistry.convertForJdbc(entry.getValue()));
    }
    return converted;
  }

  /**
   * 转换批量SQL参数。
   *
   * @param params 原始批量参数数组，每个Map对应一次SQL执行
   * @return 转换后的批量参数数组
   */
  @SuppressWarnings("unchecked")
  private Map<String, ?>[] convertBatchParams(Map<String, ?>[] params) {
    if (params == null || params.length == 0) {
      return new Map[0];
    }
    Map<String, ?>[] converted = new Map[params.length];
    for (int i = 0; i < params.length; i++) {
      converted[i] = convertParams(params[i]);
    }
    return converted;
  }

  private DataBaseTypeEnum resolveDatabaseType(SingleDataSourceProperties tenantProperties) {
    return tenantProperties == null ? DataBaseTypeEnum.MYSQL : tenantProperties.getDatabaseType();
  }

  private SqlDialect resolveDialect() {
    SingleDataSourceProperties tenantProperties = resolveTenantProperties();
    return SqlDialectFactory.getDialect(
        resolveDatabaseType(tenantProperties),
        tenantProperties == null ? null : tenantProperties.getSqlServerVersion());
  }

  private SingleDataSourceProperties resolveTenantProperties() {
    String tenantCode = context.getCurrentTenantCode();
    if (!StringUtils.hasText(tenantCode)) {
      tenantCode = properties.getDefaultTenant();
    }
    SingleDataSourceProperties tenantProperties = properties.getTenants().get(tenantCode);
    if (tenantProperties == null && !properties.getTenants().isEmpty()) {
      tenantProperties = properties.getTenants().values().iterator().next();
    }
    return tenantProperties;
  }

  private Object resolveNextSeekValue(List<?> records, String seekColumn) {
    if (records.isEmpty()) {
      return null;
    }
    Object lastRecord = records.get(records.size() - 1);
    String seekColumnName = normalizeSeekColumnName(seekColumn);
    if (lastRecord instanceof Map<?, ?> map) {
      Object value = map.get(seekColumn);
      if (value == null) {
        value = map.get(seekColumnName);
      }
      return value;
    }
    String fieldName = normalizeSeekFieldName(seekColumn);
    try {
      String getterName = "get" + Character.toUpperCase(fieldName.charAt(0))
          + fieldName.substring(1);
      Method method = lastRecord.getClass().getMethod(getterName);
      return method.invoke(lastRecord);
    } catch (Exception ignored) {
      try {
        Field field = lastRecord.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(lastRecord);
      } catch (Exception ignoredAgain) {
        return null;
      }
    }
  }

  private boolean shouldUseSeekPage(DataSourcePageParam param) {
    SolomonDataSourceProperties.Page page = properties.getPage();
    if (page == null || !page.isAutoSeekEnabled() || param == null || !param.getSeek()) {
      return false;
    }
    int currentPageNo = normalizePageNo(param.getPageNo());
    int currentPageSize = normalizePageSize(param.getPageSize());
    return currentPageNo >= Math.max(page.getSeekPageNo(), 1)
        && currentPageSize >= Math.max(page.getSeekPageSize(), 1)
        && StringUtils.hasText(resolveSeekColumn(param));
  }

  private String resolveSeekColumn(DataSourcePageParam param) {
    if (param != null && StringUtils.hasText(param.getSeekColumn())) {
      return param.getSeekColumn();
    }
    SolomonDataSourceProperties.Page page = properties.getPage();
    return page == null ? null : page.getDefaultSeekColumn();
  }

  private Object resolveSeekStartValue(
      Sql sql,
      DataSourcePageParam param,
      String seekColumn,
      int pageNo,
      int pageSize) throws DataSourceException {
    if (param.getLastValue() != null || pageNo <= 1) {
      return param.getLastValue();
    }
    long anchorPageNo = ((long) pageNo - 1) * pageSize;
    if (anchorPageNo <= 0 || anchorPageNo > Integer.MAX_VALUE) {
      return null;
    }
    String seekColumnName = normalizeSeekColumnName(seekColumn);
    SqlInjectionGuard.validateQualifiedIdentifier(seekColumnName, "seekColumn");
    String orderedSql = "SELECT t." + seekColumnName + " FROM ("
        + sql.withoutOrderBy().getText() + ") t ORDER BY t." + seekColumnName
        + (param.getAsc() ? " ASC" : " DESC");
    Sql anchorSql = Sql.of(resolveDialect().pageSql(orderedSql, (int) anchorPageNo, 1),
        sql.getParams());
    List<Map<String, Object>> rows = queryForList(anchorSql);
    if (rows.isEmpty()) {
      return null;
    }
    Map<String, Object> row = rows.get(0);
    Object value = row.get(seekColumnName);
    return value == null && !row.isEmpty() ? row.values().iterator().next() : value;
  }

  private String normalizeSeekFieldName(String seekColumn) {
    String fieldName = normalizeSeekColumnName(seekColumn);
    StringBuilder builder = new StringBuilder(fieldName.length());
    boolean upperNext = false;
    for (int i = 0; i < fieldName.length(); i++) {
      char current = fieldName.charAt(i);
      if (current == '_') {
        upperNext = true;
        continue;
      }
      if (upperNext) {
        builder.append(Character.toUpperCase(current));
        upperNext = false;
      } else {
        builder.append(current);
      }
    }
    return builder.toString();
  }

  private String normalizeSeekColumnName(String seekColumn) {
    if (seekColumn.contains(".")) {
      return seekColumn.substring(seekColumn.lastIndexOf('.') + 1);
    }
    return seekColumn;
  }

  private Sql applyOrderBy(Sql sql, DataSourcePageParam param) throws DataSourceException {
    if (param == null || !StringUtils.hasText(param.orderBy())) {
      return sql;
    }
    SqlInjectionGuard.validateOrderBy(param.orderBy(), "pageOrderBy");
    return sql.copy().orderBy(param.orderBy());
  }

  private DataSourcePageParam normalizePageParam(DataSourcePageParam param) {
    return param == null ? new DataSourcePageParam() : param;
  }

  private int normalizePageNo(int pageNo) {
    return Math.max(pageNo, 1);
  }

  private int normalizePageSize(int pageSize) {
    return Math.max(pageSize, 1);
  }
}
