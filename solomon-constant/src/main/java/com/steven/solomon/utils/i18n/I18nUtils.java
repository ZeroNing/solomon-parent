package com.steven.solomon.utils.i18n;

import com.steven.solomon.code.BaseCode;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Configuration(proxyBeanMethods=false)
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
  public static String getErrorMessage(String code, Object... args) {
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
  public static String getErrorMessage(String code,Locale locale, Object... args) {
    if(code == null || code.length() == 0){
      return null;
    }
    return getMessage(BaseCode.BASE_EXCEPTION_CODE + code,locale, args);
  }

  /**
   * 获取枚举参数信息
   *
   * @param code 枚举编码
   * @param enumClazz 枚举Class
   * @return
   */
  public static String getEnumMessage(String code, Class enumClazz) {
    if(code == null || code.length() == 0){
      return null;
    }
    return getEnumMessage(enumClazz.getSimpleName()+"."+code);
  }

  /**
   * 获取枚举参数信息
   *
   * @param enumCode 枚举名
   * @param enumClazz 枚举Class
   * @return
   */
  public static String getEnumMessage(Enum enumCode, Class enumClazz) {
    if(enumCode == null){
      return null;
    }
    return getEnumMessage(enumClazz.getSimpleName()+"."+enumCode.name());
  }

  public static String getEnumMessage(String code) {
    if(code == null || code.length() == 0){
      return null;
    }
    return getMessage(code, null);
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
  public static String getMessage(String code, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return getMessage(code,locale,args);
  }

  private static String getMessage(String code,Locale locale, Object... args) {
    try {
      return messageSource.getMessage(code, args, locale);
    }catch (Throwable e) {
      return null;
    }
  }
}
