package com.steven.solomon.datasource.sql.converter;

import com.steven.solomon.datasource.exception.DataSourceException;

/**
 * SQL值转换器。
 *
 * <p>业务侧可以实现该接口，并通过 {@link SqlTypeConverterRegistry#addConverter(SqlValueConverter)}
 * 注册到转换链中。注册顺序越靠前，优先级越高。</p>
 */
public interface SqlValueConverter {

  /**
   * 判断当前转换器是否支持把数据库值转换为指定Java类型。
   *
   * @param value 数据库返回的原始值，可能是Timestamp、BigDecimal、String等
   * @param targetType 实体字段或单列查询的目标Java类型
   * @return true表示当前转换器可以处理该转换
   */
  boolean supportsJava(Object value, Class<?> targetType);

  /**
   * 把数据库值转换为指定Java类型。
   *
   * @param value 数据库返回的原始值
   * @param targetType 目标Java类型
   * @return 转换后的Java值
   * @throws DataSourceException 转换失败时抛出数据源模块异常
   */
  Object convertForJava(Object value, Class<?> targetType) throws DataSourceException;

  /**
   * 判断当前转换器是否支持把Java参数转换为JDBC参数。
   *
   * @param value Java原始参数值
   * @return true表示当前转换器可以处理该参数
   */
  boolean supportsJdbc(Object value);

  /**
   * 把Java参数转换为JDBC参数。
   *
   * @param value Java原始参数值
   * @return 转换后的JDBC参数
   */
  Object convertForJdbc(Object value);
}
