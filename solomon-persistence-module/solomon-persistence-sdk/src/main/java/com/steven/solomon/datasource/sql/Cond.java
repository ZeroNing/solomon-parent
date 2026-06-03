package com.steven.solomon.datasource.sql;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.lambda.LambdaProperty;
import com.steven.solomon.datasource.lambda.SFunction;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SQL条件对象。
 *
 * <p>所有field参数都表示数据库字段名或SQL表达式，例如 {@code "u.id"}、{@code "COUNT(1)"}；
 * value参数会自动绑定成命名参数；isRequired为false时，空值条件会被忽略并返回null。</p>
 */
public class Cond {

  private static final AtomicInteger PARAM_INDEX = new AtomicInteger();

  private final String text;

  private final Map<String, Object> params;

  private Cond(String text, Map<String, Object> params) {
    this.text = text;
    this.params = params;
  }

  /**
   * 创建等于条件。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond eq(String field, Object value, boolean isRequired) {
    return compare(field, "=", value, isRequired);
  }

  /**
   * 创建等于条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @return 条件对象，可能为null
   */
  public static Cond eq(String field, Object value) {
    return eq(field, value, false);
  }

  public static <T> Cond eq(SFunction<T, ?> field, Object value) {
    return eq(columnName(field), value);
  }

  public static <T> Cond eq(SFunction<T, ?> field, Object value, boolean isRequired) {
    return eq(columnName(field), value, isRequired);
  }

  public static <T> Cond eq(String alias, SFunction<T, ?> field, Object value) {
    return eq(columnName(alias, field), value);
  }

  public static <T> Cond eq(
      String alias,
      SFunction<T, ?> field,
      Object value,
      boolean isRequired) {
    return eq(columnName(alias, field), value, isRequired);
  }

  public static <TLeft, TRight> Cond eq(
      String leftAlias,
      SFunction<TLeft, ?> leftField,
      String rightAlias,
      SFunction<TRight, ?> rightField) {
    return compareColumns(columnName(leftAlias, leftField), "=", columnName(rightAlias, rightField));
  }

  public static <TLeft, TRight> Cond eq(
      SFunction<TLeft, ?> leftField,
      SFunction<TRight, ?> rightField) {
    return compareColumns(defaultAliasColumnName(leftField), "=", defaultAliasColumnName(rightField));
  }

  /**
   * 创建不等于条件。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond ne(String field, Object value, boolean isRequired) {
    return compare(field, "<>", value, isRequired);
  }

  /**
   * 创建不等于条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @return 条件对象，可能为null
   */
  public static Cond ne(String field, Object value) {
    return ne(field, value, false);
  }

  public static <T> Cond ne(SFunction<T, ?> field, Object value) {
    return ne(columnName(field), value);
  }

  public static <T> Cond ne(String alias, SFunction<T, ?> field, Object value) {
    return ne(columnName(alias, field), value);
  }

  public static <TLeft, TRight> Cond ne(
      String leftAlias,
      SFunction<TLeft, ?> leftField,
      String rightAlias,
      SFunction<TRight, ?> rightField) {
    return compareColumns(columnName(leftAlias, leftField), "<>",
        columnName(rightAlias, rightField));
  }

  public static <TLeft, TRight> Cond ne(
      SFunction<TLeft, ?> leftField,
      SFunction<TRight, ?> rightField) {
    return compareColumns(defaultAliasColumnName(leftField), "<>",
        defaultAliasColumnName(rightField));
  }

  /**
   * 创建大于条件。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond gt(String field, Object value, boolean isRequired) {
    return compare(field, ">", value, isRequired);
  }

  /**
   * 创建大于条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @return 条件对象，可能为null
   */
  public static Cond gt(String field, Object value) {
    return gt(field, value, false);
  }

  public static <T> Cond gt(SFunction<T, ?> field, Object value) {
    return gt(columnName(field), value);
  }

  public static <T> Cond gt(String alias, SFunction<T, ?> field, Object value) {
    return gt(columnName(alias, field), value);
  }

  /**
   * 创建大于等于条件。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond ge(String field, Object value, boolean isRequired) {
    return compare(field, ">=", value, isRequired);
  }

  /**
   * 创建大于等于条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @return 条件对象，可能为null
   */
  public static Cond ge(String field, Object value) {
    return ge(field, value, false);
  }

  public static <T> Cond ge(SFunction<T, ?> field, Object value) {
    return ge(columnName(field), value);
  }

  public static <T> Cond ge(String alias, SFunction<T, ?> field, Object value) {
    return ge(columnName(alias, field), value);
  }

  /**
   * 创建小于条件。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond lt(String field, Object value, boolean isRequired) {
    return compare(field, "<", value, isRequired);
  }

  /**
   * 创建小于条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @return 条件对象，可能为null
   */
  public static Cond lt(String field, Object value) {
    return lt(field, value, false);
  }

  public static <T> Cond lt(SFunction<T, ?> field, Object value) {
    return lt(columnName(field), value);
  }

  public static <T> Cond lt(String alias, SFunction<T, ?> field, Object value) {
    return lt(columnName(alias, field), value);
  }

  /**
   * 创建小于等于条件。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond le(String field, Object value, boolean isRequired) {
    return compare(field, "<=", value, isRequired);
  }

  /**
   * 创建小于等于条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value 条件值
   * @return 条件对象，可能为null
   */
  public static Cond le(String field, Object value) {
    return le(field, value, false);
  }

  public static <T> Cond le(SFunction<T, ?> field, Object value) {
    return le(columnName(field), value);
  }

  public static <T> Cond le(String alias, SFunction<T, ?> field, Object value) {
    return le(columnName(alias, field), value);
  }

  /**
   * 创建LIKE条件。
   *
   * @param field 字段名或SQL表达式
   * @param value LIKE匹配值；调用方自行决定是否携带 {@code %}
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond like(String field, Object value, boolean isRequired) {
    return compare(field, "LIKE", value, isRequired);
  }

  /**
   * 创建LIKE条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value LIKE匹配值；调用方自行决定是否携带 {@code %}
   * @return 条件对象，可能为null
   */
  public static Cond like(String field, Object value) {
    return like(field, value, false);
  }

  public static <T> Cond like(SFunction<T, ?> field, Object value) {
    return like(columnName(field), value);
  }

  public static <T> Cond like(String alias, SFunction<T, ?> field, Object value) {
    return like(columnName(alias, field), value);
  }

  /**
   * 创建NOT LIKE条件。
   *
   * @param field 字段名或SQL表达式
   * @param value NOT LIKE匹配值；调用方自行决定是否携带 {@code %}
   * @param isRequired 是否强制生成条件；false时空值返回null
   * @return 条件对象，可能为null
   */
  public static Cond notLike(String field, Object value, boolean isRequired) {
    return compare(field, "NOT LIKE", value, isRequired);
  }

  /**
   * 创建NOT LIKE条件，空值时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param value NOT LIKE匹配值；调用方自行决定是否携带 {@code %}
   * @return 条件对象，可能为null
   */
  public static Cond notLike(String field, Object value) {
    return notLike(field, value, false);
  }

  public static <T> Cond notLike(SFunction<T, ?> field, Object value) {
    return notLike(columnName(field), value);
  }

  public static <T> Cond notLike(String alias, SFunction<T, ?> field, Object value) {
    return notLike(columnName(alias, field), value);
  }

  /**
   * 创建IN条件。
   *
   * @param field 字段名或SQL表达式
   * @param values IN集合值，会由NamedParameterJdbcTemplate展开
   * @param isRequired 是否强制生成条件；false时空集合返回null
   * @return 条件对象，可能为null
   */
  public static Cond in(String field, Collection<?> values, boolean isRequired) {
    return inOrNotIn(field, values, isRequired, false);
  }

  /**
   * 创建IN条件，空集合时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param values IN集合值
   * @return 条件对象，可能为null
   */
  public static Cond in(String field, Collection<?> values) {
    return in(field, values, false);
  }

  public static <T> Cond in(SFunction<T, ?> field, Collection<?> values) {
    return in(columnName(field), values);
  }

  public static <T> Cond in(String alias, SFunction<T, ?> field, Collection<?> values) {
    return in(columnName(alias, field), values);
  }

  /**
   * 创建NOT IN条件。
   *
   * @param field 字段名或SQL表达式
   * @param values NOT IN集合值，会由NamedParameterJdbcTemplate展开
   * @param isRequired 是否强制生成条件；false时空集合返回null
   * @return 条件对象，可能为null
   */
  public static Cond notIn(String field, Collection<?> values, boolean isRequired) {
    return inOrNotIn(field, values, isRequired, true);
  }

  /**
   * 创建NOT IN条件，空集合时忽略。
   *
   * @param field 字段名或SQL表达式
   * @param values NOT IN集合值
   * @return 条件对象，可能为null
   */
  public static Cond notIn(String field, Collection<?> values) {
    return notIn(field, values, false);
  }

  public static <T> Cond notIn(SFunction<T, ?> field, Collection<?> values) {
    return notIn(columnName(field), values);
  }

  public static <T> Cond notIn(String alias, SFunction<T, ?> field, Collection<?> values) {
    return notIn(columnName(alias, field), values);
  }

  /**
   * 创建EXISTS条件。
   *
   * @param sql 子查询SQL，不需要包含外层括号
   * @return 条件对象，可能为null
   */
  public static Cond exists(String sql) {
    SqlInjectionGuard.validateRawSql(sql, "exists");
    return raw("EXISTS (" + sql + ")");
  }

  /**
   * 创建NOT EXISTS条件。
   *
   * @param sql 子查询SQL，不需要包含外层括号
   * @return 条件对象，可能为null
   */
  public static Cond notExists(String sql) {
    SqlInjectionGuard.validateRawSql(sql, "notExists");
    return raw("NOT EXISTS (" + sql + ")");
  }

  /**
   * 创建原始SQL条件。
   *
   * @param text 条件SQL片段，不包含WHERE/AND/OR关键字
   * @return 条件对象，可能为null
   */
  public static Cond raw(String text) {
    if (StrUtil.isBlank(text)) {
      return null;
    }
    SqlInjectionGuard.validateRawSql(text, "condition");
    return new Cond(text, new LinkedHashMap<>());
  }

  /**
   * 创建带命名参数的原始SQL条件。
   *
   * @param text 条件SQL片段，参数使用 {@code :paramName}
   * @param params 命名参数集合；key为不带冒号的参数名
   * @return 条件对象，可能为null
   */
  public static Cond raw(String text, Map<String, Object> params) {
    if (StrUtil.isBlank(text)) {
      return null;
    }
    SqlInjectionGuard.validateRawSql(text, "condition");
    return new Cond(text,
        ObjectUtil.isEmpty(params) ? new LinkedHashMap<>() : new LinkedHashMap<>(params));
  }

  /**
   * 创建BETWEEN条件。
   *
   * @param field 字段名或SQL表达式
   * @param start 起始值；为空且end不为空时自动降级为小于等于条件
   * @param end 结束值；为空且start不为空时自动降级为大于等于条件
   * @return 条件对象，start和end都为空时返回null
   */
  public static Cond between(String field, Object start, Object end) {
    if (isEmpty(start) && isEmpty(end)) {
      return null;
    }
    if (!isEmpty(start) && isEmpty(end)) {
      return ge(field, start, true);
    }
    if (isEmpty(start)) {
      return le(field, end, true);
    }
    String startParam = nextParamName();
    String endParam = nextParamName();
    SqlInjectionGuard.validateExpression(field, "conditionField");
    Map<String, Object> params = new LinkedHashMap<>();
    params.put(startParam, start);
    params.put(endParam, end);
    return new Cond(field + " BETWEEN :" + startParam + " AND :" + endParam, params);
  }

  public static <T> Cond between(SFunction<T, ?> field, Object start, Object end) {
    return between(columnName(field), start, end);
  }

  public static <T> Cond between(
      String alias,
      SFunction<T, ?> field,
      Object start,
      Object end) {
    return between(columnName(alias, field), start, end);
  }

  /**
   * 创建IS NULL条件。
   *
   * @param field 字段名或SQL表达式
   * @return 条件对象
   */
  public static Cond isNull(String field) {
    SqlInjectionGuard.validateExpression(field, "conditionField");
    return new Cond(field + " IS NULL", new LinkedHashMap<>());
  }

  public static <T> Cond isNull(SFunction<T, ?> field) {
    return isNull(columnName(field));
  }

  public static <T> Cond isNull(String alias, SFunction<T, ?> field) {
    return isNull(columnName(alias, field));
  }

  /**
   * 创建IS NOT NULL条件。
   *
   * @param field 字段名或SQL表达式
   * @return 条件对象
   */
  public static Cond isNotNull(String field) {
    SqlInjectionGuard.validateExpression(field, "conditionField");
    return new Cond(field + " IS NOT NULL", new LinkedHashMap<>());
  }

  public static <T> Cond isNotNull(SFunction<T, ?> field) {
    return isNotNull(columnName(field));
  }

  public static <T> Cond isNotNull(String alias, SFunction<T, ?> field) {
    return isNotNull(columnName(alias, field));
  }

  /**
   * 和另一个条件组合为AND条件。
   *
   * @param cond 需要组合的条件；为空时返回当前条件
   * @return 组合后的条件对象
   */
  public Cond and(Cond cond) {
    return join("AND", cond);
  }

  /**
   * 和另一个条件组合为OR条件。
   *
   * @param cond 需要组合的条件；为空时返回当前条件
   * @return 组合后的条件对象
   */
  public Cond or(Cond cond) {
    return join("OR", cond);
  }

  /**
   * 获取条件SQL片段。
   *
   * @return 条件SQL片段
   */
  public String getText() {
    return text;
  }

  /**
   * 获取条件绑定的命名参数。
   *
   * @return 命名参数集合
   */
  public Map<String, Object> getParams() {
    return params;
  }

  private Cond join(String keyword, Cond cond) {
    if (ObjectUtil.isEmpty(cond) || StrUtil.isBlank(cond.getText())) {
      return this;
    }
    Map<String, Object> joinParams = new LinkedHashMap<>(this.params);
    joinParams.putAll(cond.getParams());
    return new Cond("(" + this.text + " " + keyword + " " + cond.getText() + ")", joinParams);
  }

  private static Cond compare(String field, String operator, Object value, boolean isRequired) {
    if (!isRequired && isEmpty(value)) {
      return null;
    }
    SqlInjectionGuard.validateExpression(field, "conditionField");
    String paramName = nextParamName();
    Map<String, Object> params = new LinkedHashMap<>();
    params.put(paramName, value);
    return new Cond(field + " " + operator + " :" + paramName, params);
  }

  private static Cond compareColumns(String leftField, String operator, String rightField) {
    SqlInjectionGuard.validateExpression(leftField, "conditionField");
    SqlInjectionGuard.validateExpression(rightField, "conditionField");
    return new Cond(leftField + " " + operator + " " + rightField, new LinkedHashMap<>());
  }

  private static Cond inOrNotIn(
      String field,
      Collection<?> values,
      boolean isRequired,
      boolean notIn) {
    if (!isRequired && ObjectUtil.isEmpty(values)) {
      return null;
    }
    SqlInjectionGuard.validateExpression(field, "conditionField");
    String paramName = nextParamName();
    Map<String, Object> params = new LinkedHashMap<>();
    params.put(paramName, values);
    return new Cond(field + (notIn ? " NOT IN " : " IN ") + "(:" + paramName + ")", params);
  }

  private static String nextParamName() {
    return "c" + PARAM_INDEX.incrementAndGet();
  }

  private static <T> String columnName(SFunction<T, ?> field) {
    try {
      String fieldName = LambdaProperty.name(field);
      Class<?> owner = LambdaProperty.owner(field);
      return SqlLambdaMetadata.columnName(owner, fieldName);
    } catch (DataSourceException e) {
      throw sneakyThrow(e);
    }
  }

  private static <T> String columnName(String alias, SFunction<T, ?> field) {
    SqlInjectionGuard.validateIdentifier(alias, "conditionAlias");
    String columnName = columnName(field);
    return StrUtil.isBlank(alias) ? columnName : alias + "." + columnName;
  }

  private static <T> String defaultAliasColumnName(SFunction<T, ?> field) {
    try {
      String fieldName = LambdaProperty.name(field);
      Class<?> owner = LambdaProperty.owner(field);
      return SqlLambdaMetadata.qualifiedColumnName(owner, fieldName);
    } catch (DataSourceException e) {
      throw sneakyThrow(e);
    }
  }

  private static boolean isEmpty(Object value) {
    if (ObjectUtil.isEmpty(value)) {
      return true;
    }
    if (value instanceof String str) {
      return StrUtil.isBlank(str);
    }
    if (value instanceof Collection<?> collection) {
      return ObjectUtil.isEmpty(collection);
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private static <TException extends Throwable> RuntimeException sneakyThrow(Throwable throwable)
      throws TException {
    throw (TException) throwable;
  }
}
