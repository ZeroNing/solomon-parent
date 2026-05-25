package com.steven.solomon.utils.i18n;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化消息工具。
 *
 * <p>错误码和国际化 key 保持一致，例如错误码 {@code S9999} 会直接查找
 * {@code messages_*.properties} 中的 {@code S9999}。历史版本曾自动拼接固定前缀，
 * 现在已经移除，避免错误码和配置 key 不一致。</p>
 */
@Configuration(proxyBeanMethods = false)
public class I18nUtils {

  private static MessageSource messageSource;

  I18nUtils(MessageSource messageSource) {
    I18nUtils.messageSource = messageSource;
  }

  /**
   * 根据错误码获取当前请求语言环境下的错误消息。
   *
   * @param code 错误码，同时也是国际化 key
   * @param args 占位参数
   * @return 国际化消息；未找到时返回 null
   */
  public static String getErrorMessage(String code, Object... args) {
    return getMessage(code, currentLocale(), args);
  }

  /**
   * 根据错误码获取指定语言环境下的错误消息。
   *
   * @param code 错误码，同时也是国际化 key
   * @param locale 指定语言环境
   * @param args 占位参数
   * @return 国际化消息；未找到时返回 null
   */
  public static String getErrorMessage(String code, Locale locale, Object... args) {
    return getMessage(code, locale, args);
  }

  /**
   * 根据枚举类和枚举值获取国际化文案。
   */
  public static String getEnumMessage(String code, Class<?> enumClazz) {
    if (isBlank(code) || enumClazz == null) {
      return null;
    }
    return getEnumMessage(enumClazz.getSimpleName() + "." + code);
  }

  /**
   * 根据枚举对象和枚举类获取国际化文案。
   */
  public static String getEnumMessage(Enum<?> enumCode, Class<?> enumClazz) {
    if (enumCode == null || enumClazz == null) {
      return null;
    }
    return getEnumMessage(enumClazz.getSimpleName() + "." + enumCode.name());
  }

  /**
   * 直接按枚举国际化 key 获取文案。
   */
  public static String getEnumMessage(String code) {
    return getMessage(code, currentLocale(), "");
  }

  /**
   * 根据 key 获取当前请求语言环境下的普通消息。
   */
  public static String getMessage(String code, Object... args) {
    return getMessage(code, currentLocale(), args);
  }

  private static String getMessage(String code, Locale locale, Object... args) {
    if (isBlank(code) || messageSource == null) {
      return null;
    }
    try {
      return messageSource.getMessage(code, args, locale);
    } catch (Throwable ex) {
      return null;
    }
  }

  private static Locale currentLocale() {
    return LocaleContextHolder.getLocale();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isEmpty();
  }
}
