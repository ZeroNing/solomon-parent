package com.steven.solomon.enums;
import com.steven.solomon.utils.I18nUtils;

public interface BaseEnum {

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
  String label();

  /**
   * 获取I18N国际化信息
   *
   * @return 国际化信息
   */
  default String Desc() {
    return I18nUtils.getEnumMessage(key());
  }

  /**
   * 获取存入数据库的值
   *
   * @return label
   */
  default String Value() {
    return label();
  }
}
