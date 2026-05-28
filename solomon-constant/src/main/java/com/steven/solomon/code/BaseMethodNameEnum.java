package com.steven.solomon.code;

/**
 * 枚举方法名常量接口，定义枚举国际化与取值的方法名后缀。
 *
 * <p>配合枚举工具类使用，通过反射调用 {@code xxxDesc()} 获取国际化描述，
 * {@code xxxValue()} 获取数据库存储值。</p>
 */
public interface BaseMethodNameEnum {

  /**
   * 获取国际化值方法名
   */
  String DESCRIPTION = "Desc";
  /**
   * 获取数据库保存的值方法名
   */
  String VALUE = "Value";
}
