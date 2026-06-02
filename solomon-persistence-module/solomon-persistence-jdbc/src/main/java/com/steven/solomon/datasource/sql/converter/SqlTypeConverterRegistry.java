package com.steven.solomon.datasource.sql.converter;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.datasource.exception.DataSourceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SQL类型转换器注册器。
 *
 * <p>注册器默认包含 {@link SqlTypeConverter} 提供的内置转换能力；业务侧可以通过
 * {@link #addConverter(SqlValueConverter)} 添加自定义转换器，自定义转换器会优先于默认转换器执行。</p>
 */
public class SqlTypeConverterRegistry {

  private final List<SqlValueConverter> customConverters = new ArrayList<>();

  private final SqlValueConverter defaultConverter = new DefaultSqlValueConverter();

  /**
   * 创建带默认转换器的注册器。
   *
   * @return SQL类型转换器注册器
   */
  public static SqlTypeConverterRegistry defaultRegistry() {
    return new SqlTypeConverterRegistry();
  }

  /**
   * 添加自定义转换器。
   *
   * <p>新添加的转换器会按添加顺序优先执行；如果所有自定义转换器都不支持当前值，
   * 会继续走默认转换器。</p>
   *
   * @param converter 自定义SQL值转换器；为null时忽略
   * @return 当前注册器，方便链式调用
   */
  public SqlTypeConverterRegistry addConverter(SqlValueConverter converter) {
    if (ObjectUtil.isNotEmpty(converter)) {
      customConverters.add(converter);
    }
    return this;
  }

  /**
   * 判断目标类型是否可以按单列简单值读取。
   *
   * @param type 目标Java类型
   * @return true表示可以直接按单列值读取并转换
   */
  public boolean isSimpleValueType(Class<?> type) {
    return SqlTypeConverter.isSimpleValueType(type);
  }

  /**
   * 将数据库返回值转换为指定Java类型。
   *
   * @param value 数据库原始值
   * @param targetType 目标Java类型
   * @param <T> 目标泛型类型
   * @return 转换后的Java值
   * @throws DataSourceException 转换失败时抛出数据源异常
   */
  @SuppressWarnings("unchecked")
  public <T> T convertForJava(Object value, Class<T> targetType) throws DataSourceException {
    if (ObjectUtil.isNull(value)) {
      return null;
    }
    for (SqlValueConverter converter : customConverters) {
      if (converter.supportsJava(value, targetType)) {
        return (T) converter.convertForJava(value, targetType);
      }
    }
    return (T) defaultConverter.convertForJava(value, targetType);
  }

  /**
   * 将Java参数转换为JDBC参数。
   *
   * @param value Java原始参数值
   * @return 转换后的JDBC参数
   */
  public Object convertForJdbc(Object value) {
    if (ObjectUtil.isNull(value)) {
      return null;
    }
    if (value instanceof Collection<?> collection) {
      List<Object> converted = new ArrayList<>(collection.size());
      for (Object item : collection) {
        converted.add(convertForJdbc(item));
      }
      return converted;
    }
    for (SqlValueConverter converter : customConverters) {
      if (converter.supportsJdbc(value)) {
        return converter.convertForJdbc(value);
      }
    }
    return defaultConverter.convertForJdbc(value);
  }

}
