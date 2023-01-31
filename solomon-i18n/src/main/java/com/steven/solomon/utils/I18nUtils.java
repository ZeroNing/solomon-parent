package com.steven.solomon.utils;

import com.steven.solomon.constant.code.BaseCode;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class I18nUtils {

  private static MessageSource messageSource;

  I18nUtils(MessageSource messageSource) {
    I18nUtils.messageSource = messageSource;
  }

  /**
   * 获取报错信息
   *
   * @param code 异常编码
   * @param args 可替换信息参数
   * @return
   */
  public static String getErrorMessage(String code, String... args) {
    if(code == null || code.length() == 0){
      return null;
    }
    return getMessage(BaseCode.BASE_EXCEPTION_CODE + code, args);
  }

  /**
   * 获取报错信息
   *
   * @param code 异常编码
   * @param args 可替换信息参数
   * @return
   */
  public static String getErrorMessage(String code,Locale locale, String... args) {
    if(code == null || code.length() == 0){
      return null;
    }
    return getMessage(BaseCode.BASE_EXCEPTION_CODE + code,locale, args);
  }

  /**
   * 获取枚举参数信息
   *
   * @param code 枚举编码
   * @param args 可替换参数
   * @return
   */
  public static String getEnumMessage(String code, String... args) {
    if(code == null || code.length() == 0){
      return null;
    }
    return getMessage(BaseCode.BASE_ENUM_CODE + code, args);
  }

  /**
   * 获取报错信息
   *
   * @param code 异常编码
   * @return
   */
  public static String getErrorMessage(String code) {
    return getErrorMessage(code, null);
  }

  /**
   * 获取消息
   *
   * @param code 编码
   * @param args 参数
   * @return
   */
  private static String getMessage(String code, String... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return getMessage(code,locale,args);
  }

  private static String getMessage(String code,Locale locale, String... args) {
    try {
      return messageSource.getMessage(code, args, locale);
    }catch (Exception e) {
      return null;
    }
  }
}
