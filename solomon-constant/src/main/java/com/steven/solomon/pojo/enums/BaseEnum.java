package com.steven.solomon.pojo.enums;
import com.steven.solomon.utils.i18n.I18nUtils;

public interface BaseEnum<T> {

  /**
   * 获取I8N国际化key
   *
   * @return code
   */
  String key();

  /**
   * 获取存入数据库的值
   *
   * @return label
   */
  T label();

  /**
   * 获取I18N国际化信息
   *
   * @return 国际化信息
   */
  default String Desc() {
    return I18nUtils.getEnumMessage(getClass().getSimpleName()+"."+key());
  }

  /**
   * 获取存入数据库的值
   *
   * @return label
   */
  default T Value() {
    return label();
  }
}
