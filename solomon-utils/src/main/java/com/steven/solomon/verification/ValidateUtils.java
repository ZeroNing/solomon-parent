package com.steven.solomon.verification;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

/**
 * 通用校验工具。
 *
 * <p>历史代码中大量使用本类的静态方法，因此这里保留原有方法签名。
 * 带 {@code errorCode} 的方法会在条件命中时抛出 {@link BaseException}，
 * 调用方可以通过统一异常处理返回标准错误响应。</p>
 */
public final class ValidateUtils {

  private static final Pattern IS_NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
  private static final Pattern EL_PATTERN = Pattern.compile("\\$\\{[^}]+}|#\\{[^}]+}");
  private static final Pattern EL_PROPERTY_PATTERN = Pattern.compile("\\$\\{([^:}]+)(?::[^}]+)?}");
  private static final Logger logger = LoggerUtils.logger(ValidateUtils.class);

  private ValidateUtils() {}

  /**
   * 数字为空时返回默认字符串。
   */
  public static String valueOf(Number target, String def) {
    return isEmpty(target) ? def : String.valueOf(target);
  }

  /**
   * 字符串为空时返回默认 Long，否则转换为 Long。
   */
  public static Long valueOf(String target, Long def) {
    return isEmpty(target) ? def : Long.valueOf(target);
  }

  /**
   * 字符串为空时返回默认 Integer，否则转换为 Integer。
   */
  public static Integer valueOf(String target, Integer def) {
    return isEmpty(target) ? def : Integer.valueOf(target);
  }

  /**
   * 字符串为空时返回默认 Double，否则转换为 Double。
   */
  public static Double valueOf(String target, Double def) {
    return isEmpty(target) ? def : Double.valueOf(target);
  }

  /**
   * 字符串为空时返回默认 Short，否则转换为 Short。
   */
  public static Short valueOf(String target, Short def) {
    return isEmpty(target) ? def : Short.valueOf(target);
  }

  /**
   * 字符串为空时返回默认 Float，否则转换为 Float。
   */
  public static Float valueOf(String target, Float def) {
    return isEmpty(target) ? def : Float.valueOf(target);
  }

  /**
   * 对象为空时返回默认值。
   */
  public static <T> T getOrDefault(T obj, T def) {
    return isEmpty(obj) ? def : obj;
  }

  public static boolean equals(String contrast, String var) {
    return ObjectUtil.equals(contrast, var);
  }

  /**
   * 两个字符串相等时抛出异常。
   */
  public static void equals(String contrast, String var, String errorCode) throws BaseException {
    check(equals(contrast, var), new BaseException(errorCode));
  }

  public static void equals(String contrast, String var, String errorCode, Object... args)
      throws BaseException {
    check(equals(contrast, var), new BaseException(errorCode, args));
  }

  public static boolean notEquals(String contrast, String var) {
    return !equals(contrast, var);
  }

  /**
   * 两个字符串不相等时抛出异常。
   */
  public static void notEquals(String contrast, String var, String errorCode) throws BaseException {
    check(notEquals(contrast, var), new BaseException(errorCode));
  }

  public static void notEquals(String contrast, String var, String errorCode, Object... args)
      throws BaseException {
    check(notEquals(contrast, var), new BaseException(errorCode, args));
  }

  public static boolean equalsIgnoreCase(String contrast, String var) {
    if (isEmpty(contrast) || isEmpty(var)) {
      return false;
    }
    return contrast.equalsIgnoreCase(var);
  }

  public static void equalsIgnoreCase(String contrast, String var, String errorCode)
      throws BaseException {
    check(equalsIgnoreCase(contrast, var), new BaseException(errorCode));
  }

  public static void equalsIgnoreCase(String contrast, String var, String errorCode, Object... args)
      throws BaseException {
    check(equalsIgnoreCase(contrast, var), new BaseException(errorCode, args));
  }

  public static boolean notEqualsIgnoreCase(String contrast, String var) {
    return !equalsIgnoreCase(contrast, var);
  }

  public static void notEqualsIgnoreCase(String contrast, String var, String errorCode)
      throws BaseException {
    check(notEqualsIgnoreCase(contrast, var), new BaseException(errorCode));
  }

  public static void notEqualsIgnoreCase(String contrast, String var, String errorCode,
      Object... args) throws BaseException {
    check(notEqualsIgnoreCase(contrast, var), new BaseException(errorCode, args));
  }

  public static boolean equals(Number contrast, Number var) {
    return ObjectUtil.equals(contrast, var);
  }

  public static void equals(Number contrast, Number var, String errorCode) throws BaseException {
    check(equals(contrast, var), new BaseException(errorCode));
  }

  public static void equals(Number contrast, Number var, String errorCode, Object... args)
      throws BaseException {
    check(equals(contrast, var), new BaseException(errorCode, args));
  }

  public static boolean notEquals(Number contrast, Number var) {
    return !equals(contrast, var);
  }

  public static void notEquals(Number contrast, Number var, String errorCode) throws BaseException {
    check(notEquals(contrast, var), new BaseException(errorCode));
  }

  public static void notEquals(Number contrast, Number var, String errorCode, Object... args)
      throws BaseException {
    check(notEquals(contrast, var), new BaseException(errorCode, args));
  }

  public static boolean equals(Boolean contrast, Boolean var) {
    return ObjectUtil.equals(contrast, var);
  }

  public static void equals(Boolean contrast, Boolean var, String errorCode) throws BaseException {
    check(equals(contrast, var), new BaseException(errorCode));
  }

  public static void equals(Boolean contrast, Boolean var, String errorCode, Object... args)
      throws BaseException {
    check(equals(contrast, var), new BaseException(errorCode, args));
  }

  public static boolean notEquals(Boolean contrast, Boolean var) {
    return !equals(contrast, var);
  }

  public static void notEquals(Boolean contrast, Boolean var, String errorCode)
      throws BaseException {
    check(notEquals(contrast, var), new BaseException(errorCode));
  }

  public static void notEquals(Boolean contrast, Boolean var, String errorCode, Object... args)
      throws BaseException {
    check(notEquals(contrast, var), new BaseException(errorCode, args));
  }

  /**
   * 判断数字是否为空或等于 0。
   */
  public static boolean isZero(Object number) {
    if (isEmpty(number)) {
      return true;
    }
    String str = String.valueOf(number);
    return "0".equals(str) || "0.0".equals(str);
  }

  public static boolean isNumber(String str) {
    return regular(IS_NUMBER_PATTERN, str);
  }

  private static void check(boolean flag, BaseException ex) throws BaseException {
    if (flag) {
      throw ex;
    }
  }

  /**
   * 正则完全匹配校验。
   */
  public static boolean regular(Pattern pattern, Object object) {
    if (pattern == null || isEmpty(object)) {
      return false;
    }
    try {
      return pattern.matcher(object.toString()).matches();
    } catch (Throwable ex) {
      logger.error("正则校验失败: value={}", object, ex);
      return false;
    }
  }

  /**
   * 下划线命名转小驼峰命名。
   */
  public static String camelName(String name) {
    if (name == null || name.isEmpty()) {
      return "";
    }
    if (!name.contains("_")) {
      return name.substring(0, 1).toLowerCase() + name.substring(1).toLowerCase();
    }
    StringBuilder result = new StringBuilder();
    for (String part : name.split("_")) {
      if (part.isEmpty()) {
        continue;
      }
      if (result.length() == 0) {
        result.append(part.toLowerCase());
      } else {
        result.append(part.substring(0, 1).toUpperCase())
            .append(part.substring(1).toLowerCase());
      }
    }
    return result.toString();
  }

  public static Boolean isEmpty(Object obj) {
    return ObjectUtil.isEmpty(obj);
  }

  /**
   * 对象为空时抛出异常。
   */
  public static <T> T isEmpty(T obj, String errorCode) throws BaseException {
    return isEmpty(obj, errorCode, (Object) null);
  }

  public static <T> T isEmpty(T obj, String errorCode, Object... args) throws BaseException {
    if (isEmpty(obj)) {
      throw new BaseException(errorCode, args);
    }
    return obj;
  }

  public static boolean isNotEmpty(Object obj) {
    return !isEmpty(obj);
  }

  /**
   * 对象非空时抛出异常。
   */
  public static <T> T isNotEmpty(T obj, String errorCode) throws BaseException {
    return isNotEmpty(obj, errorCode, (Object) null);
  }

  public static <T> T isNotEmpty(T obj, String code, Object... args) throws BaseException {
    if (!isEmpty(obj)) {
      throw new BaseException(code, args);
    }
    return obj;
  }

  /**
   * 判断枚举值是否无效。
   *
   * @param value 枚举名称，大小写敏感
   * @param clazz 枚举类型
   * @return true 表示为空或不存在于枚举中
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static boolean checkEnumValueIsEmpty(String value, Class<? extends Enum> clazz) {
    if (isEmpty(value) || clazz == null) {
      return true;
    }
    try {
      Enum.valueOf(clazz, value);
      return false;
    } catch (IllegalArgumentException ex) {
      return true;
    }
  }

  public static void checkEnumValueIsEmpty(String value, Class<? extends Enum> clazz,
      String errorCode, Object[] args) throws BaseException {
    if (checkEnumValueIsEmpty(value, clazz)) {
      throw new BaseException(errorCode, args);
    }
  }

  public static void checkEnumValueIsEmpty(String value, Class<? extends Enum> clazz,
      String errorCode) throws BaseException {
    if (checkEnumValueIsEmpty(value, clazz)) {
      throw new BaseException(errorCode, (String) null);
    }
  }

  /**
   * 判断枚举值是否有效。
   */
  public static boolean checkEnumValueIsNotEmpty(String value, Class<? extends Enum> clazz) {
    return !checkEnumValueIsEmpty(value, clazz);
  }

  public static void checkEnumValueIsNotEmpty(String value, Class<? extends Enum> clazz,
      String errorCode, Object[] args) throws BaseException {
    if (checkEnumValueIsNotEmpty(value, clazz)) {
      throw new BaseException(errorCode, args);
    }
  }

  public static void checkEnumValueIsNotEmpty(String value, Class<? extends Enum> clazz,
      String errorCode) throws BaseException {
    if (checkEnumValueIsNotEmpty(value, clazz)) {
      throw new BaseException(errorCode, (String) null);
    }
  }

  /**
   * 判断字符串是否为 Spring 占位符表达式。
   */
  public static boolean isELExpression(String expression) {
    return expression != null && EL_PATTERN.matcher(expression).matches();
  }

  /**
   * 从 ${name:default} 中提取 name。
   */
  public static String extractPropertyName(String expression) {
    if (expression == null) {
      return null;
    }
    Matcher matcher = EL_PROPERTY_PATTERN.matcher(expression);
    return matcher.find() ? matcher.group(1) : null;
  }

  /**
   * 从 ${name:default} 中提取 default。
   */
  public static String getElDefaultValue(String expression) {
    if (expression == null || !expression.contains(":")) {
      return null;
    }
    return expression.substring(expression.indexOf(":") + 1, expression.indexOf("}"));
  }
}
