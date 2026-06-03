package com.steven.solomon.datasource.sql;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import com.steven.solomon.datasource.sql.dialect.SqlDialect;
import com.steven.solomon.pojo.enums.OrderByEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL语句对象。
 *
 * <p>同时支持手写SQL和结构化构建SQL。手写SQL适合复杂报表或历史代码，结构化构建SQL适合常规查询。
 * 所有值参数都会进入命名参数集合，表名、字段名、排序、分组等不能参数化的位置会经过
 * {@link SqlInjectionGuard} 白名单校验。</p>
 */
public class Sql {

  private final StringBuilder text;

  private final Map<String, Object> params = new LinkedHashMap<>();

  private final List<String> selects = new ArrayList<>();

  private final List<JoinSegment> joins = new ArrayList<>();

  private final List<String> wheres = new ArrayList<>();

  private final List<String> groupBys = new ArrayList<>();

  private final List<String> havings = new ArrayList<>();

  private boolean selectMode;

  private boolean distinct;

  private String table;

  private String alias;

  private String orderBy;

  private int paramIndex;

  private JoinSegment lastJoin;

  /**
   * 创建空SQL对象。
   */
  public Sql() {
    this.text = new StringBuilder();
  }

  /**
   * 创建手写SQL对象。
   *
   * @param text SQL文本；为空时按空字符串处理
   */
  public Sql(String text) {
    SqlInjectionGuard.validateRawSql(text, "sqlText");
    this.text = new StringBuilder(StrUtil.nullToEmpty(text));
  }

  /**
   * 创建命名参数SQL对象。
   *
   * @param text SQL文本，参数占位符使用 {@code :paramName}
   * @param params 命名参数集合，key为不带冒号的参数名，value为参数值
   */
  public Sql(String text, Map<String, Object> params) {
    this(text);
    if (ObjectUtil.isNotEmpty(params)) {
      params.forEach(this::param);
    }
  }

  /**
   * 创建兼容问号占位符的SQL对象。
   *
   * @param text SQL文本，支持 {@code ?} 占位符
   * @param params 按顺序绑定的参数集合，会转换为 {@code :p1}、{@code :p2}
   */
  public Sql(String text, Collection<?> params) {
    SqlInjectionGuard.validateRawSql(text, "sqlText");
    this.text = new StringBuilder(StrUtil.nullToEmpty(text));
    if (ObjectUtil.isNotEmpty(params)) {
      int index = 1;
      for (Object value : params) {
        String paramName = "p" + index;
        int questionIndex = this.text.indexOf("?");
        if (questionIndex >= 0) {
          this.text.replace(questionIndex, questionIndex + 1, ":" + paramName);
        }
        this.params.put(paramName, value);
        index++;
      }
      this.paramIndex = index - 1;
    }
  }

  private Sql(Sql source) {
    this.text = new StringBuilder(source.text);
    this.params.putAll(source.params);
    this.selects.addAll(source.selects);
    for (JoinSegment join : source.joins) {
      JoinSegment copiedJoin = join.copy();
      this.joins.add(copiedJoin);
      if (join == source.lastJoin) {
        this.lastJoin = copiedJoin;
      }
    }
    this.wheres.addAll(source.wheres);
    this.groupBys.addAll(source.groupBys);
    this.havings.addAll(source.havings);
    this.selectMode = source.selectMode;
    this.distinct = source.distinct;
    this.table = source.table;
    this.alias = source.alias;
    this.orderBy = source.orderBy;
    this.paramIndex = source.paramIndex;
  }

  /**
   * 创建SQL对象，保留历史项目中 {@code Sql.New(...)} 的写法。
   *
   * @param text SQL文本
   * @return SQL对象
   */
  public static Sql New(String text) {
    return new Sql(text);
  }

  /**
   * 创建手写SQL对象。
   *
   * @param text SQL文本
   * @return SQL对象
   */
  public static Sql of(String text) {
    return new Sql(text);
  }

  /**
   * 创建命名参数SQL对象。
   *
   * @param text SQL文本，参数占位符使用 {@code :paramName}
   * @param params 命名参数集合
   * @return SQL对象
   */
  public static Sql of(String text, Map<String, Object> params) {
    return new Sql(text, params);
  }

  /**
   * 创建SELECT构建器。
   *
   * @param columns 查询字段表达式；为空时最终生成 {@code SELECT *}
   * @return SQL对象
   */
  public static Sql select(String... columns) {
    Sql sql = new Sql();
    sql.selectMode = true;
    return sql.columns(columns);
  }

  /**
   * 复制当前SQL对象。
   *
   * @return 独立副本；修改副本不会影响当前对象
   */
  public Sql copy() {
    return new Sql(this);
  }

  /**
   * 复制当前SQL并清除结构化构建器中的ORDER BY。
   *
   * <p>主要用于分页统计总数，避免数据库在子查询COUNT时执行无意义排序。手写SQL无法可靠解析，
   * 因此只处理结构化构建模式中的排序字段。</p>
   *
   * @return 清除排序后的SQL副本
   */
  public Sql withoutOrderBy() {
    Sql copy = copy();
    copy.orderBy = null;
    return copy;
  }

  /**
   * 拼接SQL片段。
   *
   * @param sql SQL片段，会按原样追加，不自动补空格
   * @return 当前SQL对象
   */
  public Sql append(String sql) {
    SqlInjectionGuard.validateRawSql(sql, "append");
    this.text.append(sql);
    return this;
  }

  /**
   * 拼接SQL片段并追加换行。
   *
   * @param sql SQL片段
   * @return 当前SQL对象
   */
  public Sql appendLine(String sql) {
    SqlInjectionGuard.validateRawSql(sql, "appendLine");
    this.text.append(sql).append(System.lineSeparator());
    return this;
  }

  /**
   * 开启DISTINCT去重查询。
   *
   * @return 当前SQL对象
   */
  public Sql distinct() {
    this.distinct = true;
    this.selectMode = true;
    return this;
  }

  /**
   * 追加单个查询字段。
   *
   * @param column 查询字段表达式，例如 {@code "u.id"}、{@code "COUNT(1) AS total"}
   * @return 当前SQL对象
   */
  public Sql column(String column) {
    if (StrUtil.isNotBlank(column)) {
      SqlInjectionGuard.validateExpression(column, "selectColumn");
      this.selects.add(column);
      this.selectMode = true;
    }
    return this;
  }

  /**
   * 批量追加查询字段。
   *
   * @param columns 查询字段表达式数组
   * @return 当前SQL对象
   */
  public Sql columns(String... columns) {
    if (ObjectUtil.isNotEmpty(columns)) {
      for (String column : columns) {
        column(column);
      }
    }
    return this;
  }

  /**
   * 设置主表。
   *
   * @param table 表名或子查询表表达式
   * @return 当前SQL对象
   */
  public Sql from(String table) {
    SqlInjectionGuard.validateTableExpression(table, "fromTable");
    this.table = table;
    this.selectMode = true;
    return this;
  }

  /**
   * 设置主表和表别名。
   *
   * @param table 表名或子查询表表达式
   * @param alias 表别名；为空时不拼接别名
   * @return 当前SQL对象
   */
  public Sql from(String table, String alias) {
    SqlInjectionGuard.validateTableExpression(table, "fromTable");
    SqlInjectionGuard.validateIdentifier(alias, "fromAlias");
    this.table = table;
    this.alias = alias;
    this.selectMode = true;
    return this;
  }

  /**
   * 设置主表，并使用实体表名的驼峰形式作为默认别名。
   *
   * <p>例如实体表名 {@code demo_user} 会生成 {@code FROM demo_user demoUser}。</p>
   *
   * @param entityClass 实体类型
   * @return 当前SQL对象
   */
  public Sql from(Class<?> entityClass) {
    return from(SqlLambdaMetadata.tableName(entityClass), SqlLambdaMetadata.tableAlias(entityClass));
  }

  /**
   * 追加INNER JOIN。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @param on ON条件表达式
   * @return 当前SQL对象
   */
  public Sql join(String table, String alias, String on) {
    return join(JoinType.JOIN, table, alias, on);
  }

  /**
   * 追加INNER JOIN，并使用实体表名的驼峰形式作为默认别名。
   *
   * @param entityClass 关联实体类型
   * @param on ON条件表达式
   * @return 当前SQL对象
   */
  public Sql join(Class<?> entityClass, String on) {
    return join(entityClass).on(on);
  }

  /**
   * 追加INNER JOIN，并允许后续通过 {@link #on(String)} 继续追加多个ON条件。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @return 当前SQL对象
   */
  public Sql join(String table, String alias) {
    return join(JoinType.JOIN, table, alias);
  }

  /**
   * 追加INNER JOIN，并使用实体表名的驼峰形式作为默认别名。
   *
   * @param entityClass 关联实体类型
   * @return 当前SQL对象
   */
  public Sql join(Class<?> entityClass) {
    return join(JoinType.JOIN, entityClass);
  }

  /**
   * 追加LEFT JOIN。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @param on ON条件表达式
   * @return 当前SQL对象
   */
  public Sql leftJoin(String table, String alias, String on) {
    return join(JoinType.LEFT_JOIN, table, alias, on);
  }

  /**
   * 追加LEFT JOIN，并使用实体表名的驼峰形式作为默认别名。
   *
   * @param entityClass 关联实体类型
   * @param on ON条件表达式
   * @return 当前SQL对象
   */
  public Sql leftJoin(Class<?> entityClass, String on) {
    return leftJoin(entityClass).on(on);
  }

  /**
   * 追加LEFT JOIN，并允许后续通过 {@link #on(String)} 继续追加多个ON条件。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @return 当前SQL对象
   */
  public Sql leftJoin(String table, String alias) {
    return join(JoinType.LEFT_JOIN, table, alias);
  }

  /**
   * 追加LEFT JOIN，并使用实体表名的驼峰形式作为默认别名。
   *
   * @param entityClass 关联实体类型
   * @return 当前SQL对象
   */
  public Sql leftJoin(Class<?> entityClass) {
    return join(JoinType.LEFT_JOIN, entityClass);
  }

  /**
   * 追加RIGHT JOIN。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @param on ON条件表达式
   * @return 当前SQL对象
   */
  public Sql rightJoin(String table, String alias, String on) {
    return join(JoinType.RIGHT_JOIN, table, alias, on);
  }

  public Sql rightJoin(Class<?> entityClass, String on) {
    return rightJoin(entityClass).on(on);
  }

  /**
   * 追加RIGHT JOIN，并允许后续通过 {@link #on(String)} 继续追加多个ON条件。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @return 当前SQL对象
   */
  public Sql rightJoin(String table, String alias) {
    return join(JoinType.RIGHT_JOIN, table, alias);
  }

  public Sql rightJoin(Class<?> entityClass) {
    return join(JoinType.RIGHT_JOIN, entityClass);
  }

  /**
   * 追加FULL JOIN。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @param on ON条件表达式
   * @return 当前SQL对象
   */
  public Sql fullJoin(String table, String alias, String on) {
    return join(JoinType.FULL_JOIN, table, alias, on);
  }

  public Sql fullJoin(Class<?> entityClass, String on) {
    return fullJoin(entityClass).on(on);
  }

  /**
   * 追加FULL JOIN，并允许后续通过 {@link #on(String)} 继续追加多个ON条件。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @return 当前SQL对象
   */
  public Sql fullJoin(String table, String alias) {
    return join(JoinType.FULL_JOIN, table, alias);
  }

  public Sql fullJoin(Class<?> entityClass) {
    return join(JoinType.FULL_JOIN, entityClass);
  }

  /**
   * 追加CROSS JOIN。
   *
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @return 当前SQL对象
   */
  public Sql crossJoin(String table, String alias) {
    SqlInjectionGuard.validateTableExpression(table, "joinTable");
    SqlInjectionGuard.validateIdentifier(alias, "joinAlias");
    JoinSegment join = new JoinSegment(JoinType.CROSS_JOIN, table, alias);
    this.joins.add(join);
    this.lastJoin = join;
    this.selectMode = true;
    return this;
  }

  public Sql crossJoin(Class<?> entityClass) {
    return crossJoin(SqlLambdaMetadata.tableName(entityClass),
        SqlLambdaMetadata.tableAlias(entityClass));
  }

  /**
   * 按指定关联类型追加JOIN。
   *
   * @param joinType 关联类型
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @param on ON条件表达式
   * @return 当前SQL对象
   */
  public Sql join(JoinType joinType, String table, String alias, String on) {
    return join(joinType, table, alias).on(on);
  }

  public Sql join(JoinType joinType, Class<?> entityClass, String on) {
    return join(joinType, entityClass).on(on);
  }

  /**
   * 按指定关联类型追加JOIN，并允许后续继续追加多个ON条件。
   *
   * @param joinType 关联类型
   * @param table 关联表名或子查询
   * @param alias 关联表别名；为空时不拼接别名
   * @return 当前SQL对象
   */
  public Sql join(JoinType joinType, String table, String alias) {
    SqlInjectionGuard.validateTableExpression(table, "joinTable");
    SqlInjectionGuard.validateIdentifier(alias, "joinAlias");
    JoinSegment join = new JoinSegment(joinType, table, alias);
    this.joins.add(join);
    this.lastJoin = join;
    this.selectMode = true;
    return this;
  }

  public Sql join(JoinType joinType, Class<?> entityClass) {
    return join(joinType, SqlLambdaMetadata.tableName(entityClass),
        SqlLambdaMetadata.tableAlias(entityClass));
  }

  /**
   * 给最近一次JOIN追加AND ON条件。
   *
   * <p>适合一个关联表需要多个ON条件的场景，例如
   * {@code leftJoin("sys_order", "o").on("o.user_id = u.id").on("o.status = 1")}。</p>
   *
   * @param condition ON条件表达式，不要包含 {@code ON} 关键字
   * @return 当前SQL对象
   */
  public Sql on(String condition) {
    return on("AND", condition);
  }

  /**
   * 给最近一次JOIN追加AND ON条件。
   *
   * <p>支持 {@code Cond.eq("u", User::getDeptId, "d", Dept::getId)} 这类Lambda字段写法。</p>
   *
   * @param condition ON条件对象
   * @return 当前SQL对象
   */
  public Sql on(Cond condition) {
    return on("AND", condition);
  }

  /**
   * 给最近一次JOIN追加OR ON条件。
   *
   * @param condition ON条件表达式，不要包含 {@code ON} 关键字
   * @return 当前SQL对象
   */
  public Sql orOn(String condition) {
    return on("OR", condition);
  }

  /**
   * 给最近一次JOIN追加OR ON条件。
   *
   * @param condition ON条件对象
   * @return 当前SQL对象
   */
  public Sql orOn(Cond condition) {
    return on("OR", condition);
  }

  /**
   * 追加默认WHERE占位，主要用于手写SQL链式拼接。
   *
   * @return 当前SQL对象
   */
  public Sql where() {
    if (!selectMode) {
      this.text.append(" WHERE 1=1 ");
    }
    return this;
  }

  /**
   * 追加原始WHERE条件。
   *
   * @param condition WHERE条件片段，不要包含 {@code WHERE} 关键字
   * @return 当前SQL对象
   */
  public Sql where(String condition) {
    if (StrUtil.isBlank(condition)) {
      return this;
    }
    SqlInjectionGuard.validateRawSql(condition, "where");
    if (selectMode) {
      this.wheres.add(condition);
    } else {
      this.text.append(" WHERE ").append(condition);
    }
    return this;
  }

  /**
   * 条件成立时追加原始WHERE条件。
   *
   * @param condition 是否追加
   * @param where WHERE条件片段，不包含WHERE关键字
   * @return 当前SQL对象
   */
  public Sql whereIf(boolean condition, String where) {
    return condition ? where(where) : this;
  }

  /**
   * 追加带命名参数的原始WHERE条件。
   *
   * <p>SQL片段只负责表达式，参数值统一进入命名参数集合，避免业务侧把值拼进SQL。</p>
   *
   * @param condition WHERE条件片段，不包含WHERE关键字
   * @param params 命名参数集合
   * @return 当前SQL对象
   */
  public Sql where(String condition, Map<String, Object> params) {
    if (StrUtil.isBlank(condition)) {
      return this;
    }
    where(condition);
    return params(params);
  }

  /**
   * 条件成立时追加带命名参数的原始WHERE条件。
   *
   * @param matched 是否追加
   * @param condition WHERE条件片段，不包含WHERE关键字
   * @param params 命名参数集合
   * @return 当前SQL对象
   */
  public Sql whereIf(boolean matched, String condition, Map<String, Object> params) {
    return matched ? where(condition, params) : this;
  }

  /**
   * 追加结构化WHERE条件。
   *
   * @param cond 条件对象；为空时忽略
   * @return 当前SQL对象
   */
  public Sql where(Cond cond) {
    return appendBuilderCond(wheres, cond);
  }

  /**
   * 追加AND条件。
   *
   * @param cond 条件对象；为空时忽略
   * @return 当前SQL对象
   */
  public Sql and(Cond cond) {
    return and(cond, false);
  }

  /**
   * 追加AND条件。
   *
   * @param cond 条件对象；为空时忽略
   * @param isMoreCond 是否使用括号包裹条件
   * @return 当前SQL对象
   */
  public Sql and(Cond cond, boolean isMoreCond) {
    if (selectMode) {
      return appendBuilderCond(wheres, cond);
    }
    if (ObjectUtil.isEmpty(cond) || StrUtil.isBlank(cond.getText())) {
      return this;
    }
    this.text.append(" AND ");
    appendRawCond(cond, isMoreCond);
    return this;
  }

  /**
   * 追加OR条件。
   *
   * @param cond 条件对象；为空时忽略
   * @return 当前SQL对象
   */
  public Sql or(Cond cond) {
    return or(cond, false);
  }

  /**
   * 追加OR条件。
   *
   * @param cond 条件对象；为空时忽略
   * @param isMoreCond 是否使用括号包裹条件
   * @return 当前SQL对象
   */
  public Sql or(Cond cond, boolean isMoreCond) {
    if (selectMode) {
      if (ObjectUtil.isEmpty(cond) || StrUtil.isBlank(cond.getText())) {
        return this;
      }
      SqlInjectionGuard.validateRawSql(cond.getText(), "condition");
      if (ObjectUtil.isEmpty(wheres)) {
        wheres.add(cond.getText());
      } else {
        int lastIndex = wheres.size() - 1;
        wheres.set(lastIndex, "(" + wheres.get(lastIndex) + " OR " + cond.getText() + ")");
      }
      params.putAll(cond.getParams());
      return this;
    }
    if (ObjectUtil.isEmpty(cond) || StrUtil.isBlank(cond.getText())) {
      return this;
    }
    this.text.append(" OR ");
    appendRawCond(cond, isMoreCond);
    return this;
  }

  public Sql eq(String column, Object value) {
    return condition(column, "=", value);
  }

  public Sql eqIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, "=", value);
  }

  public Sql ne(String column, Object value) {
    return condition(column, "<>", value);
  }

  public Sql neIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, "<>", value);
  }

  public Sql gt(String column, Object value) {
    return condition(column, ">", value);
  }

  public Sql gtIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, ">", value);
  }

  public Sql ge(String column, Object value) {
    return condition(column, ">=", value);
  }

  public Sql geIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, ">=", value);
  }

  public Sql lt(String column, Object value) {
    return condition(column, "<", value);
  }

  public Sql ltIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, "<", value);
  }

  public Sql le(String column, Object value) {
    return condition(column, "<=", value);
  }

  public Sql leIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, "<=", value);
  }

  public Sql like(String column, Object value) {
    return condition(column, "LIKE", value);
  }

  public Sql likeIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, "LIKE", value);
  }

  public Sql notLike(String column, Object value) {
    return condition(column, "NOT LIKE", value);
  }

  public Sql notLikeIf(boolean condition, String column, Object value) {
    return conditionIf(condition, column, "NOT LIKE", value);
  }

  public Sql in(String column, Collection<?> values) {
    return collectionCondition(column, "IN", values);
  }

  public Sql inIf(boolean condition, String column, Collection<?> values) {
    return collectionConditionIf(condition, column, "IN", values);
  }

  public Sql notIn(String column, Collection<?> values) {
    return collectionCondition(column, "NOT IN", values);
  }

  public Sql notInIf(boolean condition, String column, Collection<?> values) {
    return collectionConditionIf(condition, column, "NOT IN", values);
  }

  public Sql between(String column, Object start, Object end) {
    SqlInjectionGuard.validateExpression(column, "whereColumn");
    String startParam = nextParamName();
    String endParam = nextParamName();
    this.wheres.add(column + " BETWEEN :" + startParam + " AND :" + endParam);
    this.params.put(startParam, start);
    this.params.put(endParam, end);
    this.selectMode = true;
    return this;
  }

  public Sql betweenIf(boolean condition, String column, Object start, Object end) {
    return condition ? between(column, start, end) : this;
  }

  public Sql isNull(String column) {
    SqlInjectionGuard.validateExpression(column, "whereColumn");
    this.wheres.add(column + " IS NULL");
    this.selectMode = true;
    return this;
  }

  public Sql isNullIf(boolean condition, String column) {
    return condition ? isNull(column) : this;
  }

  public Sql isNotNull(String column) {
    SqlInjectionGuard.validateExpression(column, "whereColumn");
    this.wheres.add(column + " IS NOT NULL");
    this.selectMode = true;
    return this;
  }

  public Sql isNotNullIf(boolean condition, String column) {
    return condition ? isNotNull(column) : this;
  }

  public Sql exists(String sql) {
    if (StrUtil.isNotBlank(sql)) {
      SqlInjectionGuard.validateRawSql(sql, "exists");
      this.wheres.add("EXISTS (" + sql + ")");
      this.selectMode = true;
    }
    return this;
  }

  public Sql notExists(String sql) {
    if (StrUtil.isNotBlank(sql)) {
      SqlInjectionGuard.validateRawSql(sql, "notExists");
      this.wheres.add("NOT EXISTS (" + sql + ")");
      this.selectMode = true;
    }
    return this;
  }

  public Sql condition(String column, String operator, Object value) {
    SqlInjectionGuard.validateExpression(column, "whereColumn");
    String paramName = nextParamName();
    this.wheres.add(column + " " + operator + " :" + paramName);
    this.params.put(paramName, value);
    this.selectMode = true;
    return this;
  }

  /**
   * 追加命名参数。
   *
   * @param name 参数名，不带冒号
   * @param value 参数值
   * @return 当前SQL对象
   */
  public Sql param(String name, Object value) {
    SqlInjectionGuard.validateIdentifier(name, "paramName");
    this.params.put(name, value);
    return this;
  }

  /**
   * 追加多个命名参数。
   *
   * @param params 命名参数集合
   * @return 当前SQL对象
   */
  public Sql params(Map<String, Object> params) {
    if (ObjectUtil.isNotEmpty(params)) {
      params.forEach(this::param);
    }
    return this;
  }

  /**
   * 追加ORDER BY排序。
   *
   * @param orderBy 排序表达式，不要包含 {@code ORDER BY} 关键字
   * @return 当前SQL对象
   */
  public Sql orderBy(String orderBy) {
    if (StrUtil.isBlank(orderBy)) {
      return this;
    }
    SqlInjectionGuard.validateOrderBy(orderBy, "orderBy");
    if (selectMode) {
      this.orderBy = orderBy;
    } else {
      this.text.append(" ORDER BY ").append(orderBy);
    }
    return this;
  }

  public Sql orderByIf(boolean condition, String orderBy) {
    return condition ? orderBy(orderBy) : this;
  }

  /**
   * 追加GROUP BY分组。
   *
   * @param groupBy 分组字段或表达式，多个字段建议使用可变参数重载
   * @return 当前SQL对象
   */
  public Sql groupBy(String groupBy) {
    if (StrUtil.isBlank(groupBy)) {
      return this;
    }
    SqlInjectionGuard.validateExpression(groupBy, "groupBy");
    if (selectMode) {
      this.groupBys.add(groupBy);
    } else {
      this.text.append(" GROUP BY ").append(groupBy);
    }
    return this;
  }

  public Sql groupByIf(boolean condition, String groupBy) {
    return condition ? groupBy(groupBy) : this;
  }

  public Sql groupBy(String... groupBy) {
    if (ObjectUtil.isNotEmpty(groupBy)) {
      for (String item : groupBy) {
        groupBy(item);
      }
    }
    return this;
  }

  /**
   * 追加结构化HAVING条件。
   *
   * @param cond HAVING条件对象；为空时忽略
   * @return 当前SQL对象
   */
  public Sql having(Cond cond) {
    if (selectMode) {
      return appendBuilderCond(havings, cond);
    }
    if (ObjectUtil.isEmpty(cond) || StrUtil.isBlank(cond.getText())) {
      return this;
    }
    this.text.append(" HAVING ");
    appendRawCond(cond, false);
    return this;
  }

  /**
   * 追加原始HAVING条件。
   *
   * @param having HAVING条件片段，不要包含 {@code HAVING} 关键字
   * @return 当前SQL对象
   */
  public Sql having(String having) {
    if (StrUtil.isBlank(having)) {
      return this;
    }
    SqlInjectionGuard.validateRawSql(having, "having");
    if (selectMode) {
      this.havings.add(having);
    } else {
      this.text.append(" HAVING ").append(having);
    }
    return this;
  }

  public Sql having(String having, Map<String, Object> params) {
    if (StrUtil.isBlank(having)) {
      return this;
    }
    having(having);
    return params(params);
  }

  public Sql havingIf(boolean condition, String having) {
    return condition ? having(having) : this;
  }

  public Sql havingIf(boolean condition, String having, Map<String, Object> params) {
    return condition ? having(having, params) : this;
  }

  public Sql havingEq(String column, Object value) {
    return havingCondition(column, "=", value);
  }

  public Sql havingNe(String column, Object value) {
    return havingCondition(column, "<>", value);
  }

  public Sql havingGt(String column, Object value) {
    return havingCondition(column, ">", value);
  }

  public Sql havingGe(String column, Object value) {
    return havingCondition(column, ">=", value);
  }

  public Sql havingLt(String column, Object value) {
    return havingCondition(column, "<", value);
  }

  public Sql havingLe(String column, Object value) {
    return havingCondition(column, "<=", value);
  }

  public Sql havingBetween(String column, Object start, Object end) {
    SqlInjectionGuard.validateExpression(column, "havingColumn");
    String startParam = nextParamName();
    String endParam = nextParamName();
    this.havings.add(column + " BETWEEN :" + startParam + " AND :" + endParam);
    this.params.put(startParam, start);
    this.params.put(endParam, end);
    this.selectMode = true;
    return this;
  }

  /**
   * 追加ORDER BY排序。
   *
   * @param fields 排序字段或表达式
   * @param orderByEnum 排序方向枚举
   * @return 当前SQL对象
   */
  public Sql orderBy(String fields, OrderByEnum orderByEnum) {
    if (StrUtil.isNotBlank(fields)) {
      OrderByEnum method = ObjectUtil.defaultIfNull(orderByEnum, OrderByEnum.DESCEND);
      return orderBy(fields + " " + method.label());
    }
    return this;
  }

  public Sql orderByAsc(String column) {
    return orderBy(column + " ASC");
  }

  public Sql orderByDesc(String column) {
    return orderBy(column + " DESC");
  }

  /**
   * 将构建器渲染为固定SQL。
   *
   * @return 当前SQL对象
   */
  public Sql build() {
    RenderedSql renderedSql = render(null, null, null);
    this.text.setLength(0);
    this.text.append(renderedSql.text());
    this.params.clear();
    this.params.putAll(renderedSql.params());
    this.selectMode = false;
    return this;
  }

  /**
   * 按指定数据库方言生成分页SQL。
   *
   * @param databaseType 数据库类型
   * @param pageNo 页码，从1开始
   * @param pageSize 每页条数
   * @return 新的分页SQL对象
   */
  public Sql buildPage(DataBaseTypeEnum databaseType, int pageNo, int pageSize) {
    RenderedSql renderedSql = render(null, null, null);
    String pageSql = SqlDialectFactory.getDialect(databaseType)
        .pageSql(renderedSql.text(), pageNo, pageSize);
    return Sql.of(pageSql, renderedSql.params());
  }

  /**
   * 按指定数据库方言生成游标分页SQL。
   *
   * @param databaseType 数据库类型
   * @param seekColumn 游标字段
   * @param lastValue 上一页最后一条记录的游标值，第一页传null
   * @param asc 是否升序
   * @param pageSize 每页条数
   * @return 新的游标分页SQL对象
   */
  public Sql buildSeekPage(
      DataBaseTypeEnum databaseType,
      String seekColumn,
      Object lastValue,
      boolean asc,
      int pageSize) {
    return buildSeekPage(SqlDialectFactory.getDialect(databaseType), seekColumn, lastValue, asc,
        pageSize);
  }

  /**
   * 按指定SQL方言生成游标分页SQL。
   *
   * @param dialect SQL方言实现
   * @param seekColumn 游标字段
   * @param lastValue 上一页最后一条记录的游标值，第一页传null
   * @param asc 是否升序
   * @param pageSize 每页条数
   * @return 新的游标分页SQL对象
   */
  public Sql buildSeekPage(
      SqlDialect dialect,
      String seekColumn,
      Object lastValue,
      boolean asc,
      int pageSize) {
    String paramName = "__seekValue";
    SqlInjectionGuard.validateExpression(seekColumn, "seekColumn");
    String operator = asc ? ">" : "<";
    Map<String, Object> seekParams = new LinkedHashMap<>();
    String seekWhere = null;
    if (ObjectUtil.isNotNull(lastValue)) {
      seekWhere = seekColumn + " " + operator + " :" + paramName;
      seekParams.put(paramName, lastValue);
    }
    String seekOrderBy = seekColumn + (asc ? " ASC" : " DESC");
    RenderedSql renderedSql = render(seekWhere, seekParams, seekOrderBy);
    return Sql.of(dialect.limitSql(renderedSql.text(), pageSize), renderedSql.params());
  }

  /**
   * 获取SQL文本。
   *
   * @return SQL文本
   */
  public String getText() {
    return render(null, null, null).text();
  }

  /**
   * 获取命名参数集合。
   *
   * @return 不可变参数集合，key为不带冒号的参数名
   */
  public Map<String, Object> getParams() {
    return Collections.unmodifiableMap(render(null, null, null).params());
  }

  @Override
  public String toString() {
    return getText();
  }

  private RenderedSql render(
      String extraWhere,
      Map<String, Object> extraParams,
      String orderByOverride) {
    Map<String, Object> renderedParams = new LinkedHashMap<>(params);
    if (ObjectUtil.isNotEmpty(extraParams)) {
      renderedParams.putAll(extraParams);
    }
    if (!selectMode) {
      return new RenderedSql(text.toString(), renderedParams);
    }

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT ");
    if (distinct) {
      sql.append("DISTINCT ");
    }
    sql.append(ObjectUtil.isEmpty(selects) ? "*" : String.join(", ", selects));
    sql.append(" FROM ").append(table);
    if (StrUtil.isNotBlank(alias)) {
      sql.append(" ").append(alias);
    }
    for (JoinSegment join : joins) {
      sql.append(join.toSql());
    }

    List<String> allWheres = new ArrayList<>(wheres);
    if (StrUtil.isNotBlank(extraWhere)) {
      allWheres.add(extraWhere);
    }
    if (ObjectUtil.isNotEmpty(allWheres)) {
      sql.append(" WHERE ").append(String.join(" AND ", allWheres));
    }
    if (ObjectUtil.isNotEmpty(groupBys)) {
      sql.append(" GROUP BY ").append(String.join(", ", groupBys));
    }
    if (ObjectUtil.isNotEmpty(havings)) {
      sql.append(" HAVING ").append(String.join(" AND ", havings));
    }
    String finalOrderBy = StrUtil.isNotBlank(orderByOverride) ? orderByOverride : orderBy;
    if (StrUtil.isNotBlank(finalOrderBy)) {
      sql.append(" ORDER BY ").append(finalOrderBy);
    }
    return new RenderedSql(sql.toString(), renderedParams);
  }

  private void appendRawCond(Cond cond, boolean isMoreCond) {
    SqlInjectionGuard.validateRawSql(cond.getText(), "condition");
    if (isMoreCond) {
      this.text.append("(").append(cond.getText()).append(")");
    } else {
      this.text.append(cond.getText());
    }
    this.params.putAll(cond.getParams());
  }

  private Sql appendBuilderCond(List<String> target, Cond cond) {
    if (ObjectUtil.isNotEmpty(cond) && StrUtil.isNotBlank(cond.getText())) {
      SqlInjectionGuard.validateRawSql(cond.getText(), "condition");
      target.add(cond.getText());
      params.putAll(cond.getParams());
      selectMode = true;
    }
    return this;
  }

  private Sql collectionCondition(String column, String operator, Collection<?> values) {
    SqlInjectionGuard.validateExpression(column, "whereColumn");
    String paramName = nextParamName();
    this.wheres.add(column + " " + operator + " (:" + paramName + ")");
    this.params.put(paramName, values);
    this.selectMode = true;
    return this;
  }

  private Sql conditionIf(boolean matched, String column, String operator, Object value) {
    return matched ? condition(column, operator, value) : this;
  }

  private Sql collectionConditionIf(
      boolean matched,
      String column,
      String operator,
      Collection<?> values) {
    return matched ? collectionCondition(column, operator, values) : this;
  }

  private Sql havingCondition(String column, String operator, Object value) {
    SqlInjectionGuard.validateExpression(column, "havingColumn");
    String paramName = nextParamName();
    this.havings.add(column + " " + operator + " :" + paramName);
    this.params.put(paramName, value);
    this.selectMode = true;
    return this;
  }

  private String nextParamName() {
    paramIndex++;
    return "p" + paramIndex;
  }

  private Sql on(String keyword, String condition) {
    if (ObjectUtil.isEmpty(lastJoin) || StrUtil.isBlank(condition)) {
      return this;
    }
    SqlInjectionGuard.validateRawSql(condition, "joinOn");
    lastJoin.addOn(keyword, condition);
    return this;
  }

  private Sql on(String keyword, Cond condition) {
    if (ObjectUtil.isEmpty(condition) || StrUtil.isBlank(condition.getText())) {
      return this;
    }
    on(keyword, condition.getText());
    params.putAll(condition.getParams());
    return this;
  }

}
