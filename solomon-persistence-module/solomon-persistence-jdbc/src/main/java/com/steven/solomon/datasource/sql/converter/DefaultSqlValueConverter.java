package com.steven.solomon.datasource.sql.converter;

import com.steven.solomon.datasource.exception.DataSourceException;

/**
 * 默认 SQL 类型转换器。
 */
final class DefaultSqlValueConverter implements SqlValueConverter {

  @Override
  public boolean supportsJava(Object value, Class<?> targetType) {
    return true;
  }

  @Override
  public Object convertForJava(Object value, Class<?> targetType) throws DataSourceException {
    return SqlTypeConverter.convertForJava(value, targetType);
  }

  @Override
  public boolean supportsJdbc(Object value) {
    return true;
  }

  @Override
  public Object convertForJdbc(Object value) {
    return SqlTypeConverter.convertForJdbc(value);
  }
}
