package com.steven.solomon.verification;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.exception.BaseException;
import java.util.regex.Pattern;

/**
 * Project-specific validation helpers that are not provided directly by Hutool.
 */
public final class ValidateUtils {

  private static final Pattern EL_PATTERN = Pattern.compile("\\$\\{[^}]+}|#\\{[^}]+}");

  private ValidateUtils() {
  }

  /**
   * Checks whether an enum name is missing or invalid.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static boolean checkEnumValueIsEmpty(String value, Class<? extends Enum> clazz) {
    if (StrUtil.isBlank(value) || clazz == null) {
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
   * Checks whether an enum name exists.
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
   * Checks whether a value is a Spring placeholder expression.
   */
  public static boolean isELExpression(String expression) {
    return StrUtil.isNotEmpty(expression) && EL_PATTERN.matcher(expression).matches();
  }
}
