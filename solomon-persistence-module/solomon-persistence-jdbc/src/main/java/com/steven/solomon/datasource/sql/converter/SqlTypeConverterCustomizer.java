package com.steven.solomon.datasource.sql.converter;

/**
 * SQL类型转换器注册器自定义回调。
 *
 * <p>业务模块可以声明该接口的Spring Bean，在回调中调用
 * {@link SqlTypeConverterRegistry#addConverter(SqlValueConverter)} 添加自定义转换器。</p>
 */
@FunctionalInterface
public interface SqlTypeConverterCustomizer {

  /**
   * 自定义SQL类型转换器注册器。
   *
   * @param registry SQL类型转换器注册器，已经带有默认转换器
   */
  void customize(SqlTypeConverterRegistry registry);
}
