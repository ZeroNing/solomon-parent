package com.steven.solomon.persistence.sql;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.persistence.code.PersistenceErrorCode;
import com.steven.solomon.persistence.exception.PersistenceException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * SQL注入防护工具。
 *
 * <p>值参数必须通过命名参数绑定；表名、列名、排序、聚合函数名等不能参数化的位置，
 * 统一在这里做白名单和危险片段校验。</p>
 */
public final class SqlInjectionGuard {

  private static final Pattern IDENTIFIER =
      Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

  private static final Pattern QUALIFIED_IDENTIFIER =
      Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*$");

  private static final Pattern SAFE_EXPRESSION =
      Pattern.compile("^[A-Za-z0-9_.$`\"'\\[\\]\\s,()*+\\-/<>=%]+$");

  private static final Pattern AGGREGATE_FUNCTION =
      Pattern.compile("^(COUNT|SUM|MAX|MIN|AVG)$", Pattern.CASE_INSENSITIVE);

  private static final Pattern QUERY_START =
      Pattern.compile("^(SELECT|WITH|SHOW|DESC|DESCRIBE|EXPLAIN)\\b", Pattern.CASE_INSENSITIVE);

  private static final Pattern WRITE_KEYWORD =
      Pattern.compile("\\b(INSERT|UPDATE|DELETE|MERGE|DROP|ALTER|TRUNCATE|CREATE|GRANT|REVOKE|CALL|EXEC|EXECUTE)\\b",
          Pattern.CASE_INSENSITIVE);

  private SqlInjectionGuard() {
  }

  /**
   * 校验原始SQL片段。
   *
   * @param value SQL文本或片段
   * @param scene 使用场景，用于异常提示
   * @return 原始SQL片段
   */
  public static String validateRawSql(String value, String scene) {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    String text = value.trim();
    rejectDangerousToken(text, scene);
    return value;
  }

  /**
   * 校验只读查询SQL。
   *
   * <p>适用于自定义报表等只允许读取数据的入口，会拒绝写操作、DDL和存储过程调用。</p>
   *
   * @param value SQL文本
   * @param scene 使用场景，用于异常提示
   * @return 原始SQL文本
   */
  public static String validateQuerySql(String value, String scene) {
    validateRawSql(value, scene);
    if (StrUtil.isBlank(value)) {
      return value;
    }
    String text = trimTrailingSemicolon(value.trim());
    if (!QUERY_START.matcher(text).find() || WRITE_KEYWORD.matcher(text).find()) {
      throw risk(scene, value);
    }
    return value;
  }

  /**
   * 校验数据库标识符。
   *
   * @param value 表名、列名或别名
   * @param scene 使用场景，用于异常提示
   * @return 原始标识符
   */
  public static String validateIdentifier(String value, String scene) {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    String text = trimQuote(value.trim());
    if (!IDENTIFIER.matcher(text).matches()) {
      throw risk(scene, value);
    }
    return value;
  }

  /**
   * 校验带表别名或schema前缀的字段名。
   *
   * @param value 字段名，例如 {@code id}、{@code u.id}
   * @param scene 使用场景，用于异常提示
   * @return 原始字段名
   */
  public static String validateQualifiedIdentifier(String value, String scene)
      {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    String text = normalizeIdentifierQuote(value.trim());
    if (!QUALIFIED_IDENTIFIER.matcher(text).matches()) {
      throw risk(scene, value);
    }
    return value;
  }

  /**
   * 校验SQL表达式。
   *
   * @param value 字段、函数或简单表达式，例如 {@code COUNT(1)}、{@code SUM(o.amount)}
   * @param scene 使用场景，用于异常提示
   * @return 原始表达式
   */
  public static String validateExpression(String value, String scene) {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    String text = value.trim();
    rejectDangerousToken(text, scene);
    if (!SAFE_EXPRESSION.matcher(text).matches()) {
      throw risk(scene, value);
    }
    return value;
  }

  /**
   * 校验表表达式。
   *
   * @param value 表名或安全子查询表达式
   * @param scene 使用场景，用于异常提示
   * @return 原始表表达式
   */
  public static String validateTableExpression(String value, String scene)
      {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    String text = value.trim();
    rejectDangerousToken(text, scene);
    if (text.startsWith("(")) {
      return validateRawSql(text, scene);
    }
    return validateQualifiedIdentifier(value, scene);
  }

  /**
   * 校验ORDER BY表达式。
   *
   * @param value ORDER BY后面的排序表达式，不包含ORDER BY关键字
   * @param scene 使用场景，用于异常提示
   * @return 原始排序表达式
   */
  public static String validateOrderBy(String value, String scene) {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    rejectDangerousToken(value, scene);
    for (String item : value.split(",")) {
      String orderItem = item.trim();
      if (StrUtil.isBlank(orderItem)) {
        throw risk(scene, value);
      }
      String[] parts = orderItem.split("\\s+");
      if (parts.length > 2) {
        throw risk(scene, value);
      }
      validateExpression(parts[0], scene);
      if (parts.length == 2) {
        String direction = parts[1].toUpperCase(Locale.ROOT);
        if (!"ASC".equals(direction) && !"DESC".equals(direction)) {
          throw risk(scene, value);
        }
      }
    }
    return value;
  }

  /**
   * 校验聚合函数名。
   *
   * @param value 聚合函数名，例如COUNT、SUM、MAX、MIN、AVG
   * @return 原始聚合函数名
   */
  public static String validateAggregateFunction(String value) {
    if (StrUtil.isBlank(value) || !AGGREGATE_FUNCTION.matcher(value.trim()).matches()) {
      throw risk("aggregateFunction", value);
    }
    return value;
  }

  private static void rejectDangerousToken(String value, String scene) {
    String lower = value.toLowerCase(Locale.ROOT);
    if (lower.contains("--")
        || lower.contains("/*")
        || lower.contains("*/")
        || hasMultiStatement(value)
        || lower.contains("\u0000")) {
      throw risk(scene, value);
    }
  }

  private static boolean hasMultiStatement(String value) {
    String text = value.trim();
    int index = text.indexOf(';');
    return index >= 0 && index != text.length() - 1;
  }

  private static String trimTrailingSemicolon(String value) {
    String text = value;
    while (text.endsWith(";")) {
      text = text.substring(0, text.length() - 1).trim();
    }
    return text;
  }

  private static String trimQuote(String value) {
    if ((value.startsWith("`") && value.endsWith("`"))
        || (value.startsWith("\"") && value.endsWith("\""))
        || (value.startsWith("[") && value.endsWith("]"))) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  private static String normalizeIdentifierQuote(String value) {
    String[] parts = value.split("\\.");
    StringBuilder builder = new StringBuilder();
    for (String part : parts) {
      if (ObjectUtil.isNotEmpty(builder)) {
        builder.append(".");
      }
      builder.append(trimQuote(part));
    }
    return builder.toString();
  }

  private static RuntimeException risk(String scene, Object value) {
    return sneakyThrow(new PersistenceException(PersistenceErrorCode.DATA_SOURCE_SQL_INJECTION_RISK,
        scene, value));
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws T {
    throw (T) throwable;
  }
}
