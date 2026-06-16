package com.steven.solomon.pojo.enums;
import com.steven.solomon.utils.i18n.I18nUtils;

/**
 * 枚举基础接口，提供国际化描述和数据库存储值的统一取值方式。
 *
 * <p>所有业务枚举都应实现该接口，配合 {@link I18nUtils} 实现枚举值的国际化展示。
 * {@code key()} 返回国际化键后缀，{@code label()} 返回数据库存储值。
 * {@code Desc()} 方法自动拼接枚举类名和键后缀来查找国际化消息。</p>
 *
 * @param <T> 枚举存储值类型，通常为 String 或 Integer
 */
public interface BaseEnum<T> {

  /**
   * 获取I8N国际化key
   */
  String key();

  /**
   * 获取存入数据库的值
   */
  T label();

  /**
   * 获取描述
   */
  String getDesc();

  /**
   * 获取I18N国际化信息
   *
   * @return 国际化信息
   */
  default String Desc() {
    return I18nUtils.getEnumMessage(getClass().getSimpleName()+"."+key());
  }
}
