package com.steven.solomon.persistence.sql.converter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.persistence.annotation.Column;
import com.steven.solomon.persistence.code.PersistenceErrorCode;
import com.steven.solomon.persistence.exception.PersistenceException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.core.RowMapper;

/**
 * SQL结果行映射器。
 *
 * <p>支持按字段名、下划线字段名、{@link Column#value()} 映射实体属性，并在赋值前调用
 * {@link SqlTypeConverter} 做类型转换，解决LocalDateTime、Date、Long等类型在不同数据库
 * 驱动下返回类型不一致的问题。</p>
 *
 * @param <T> 查询结果实体类型
 */
public class SqlResultRowMapper<T> implements RowMapper<T> {

  private static final Map<Class<?>, SqlResultMetadata<?>> METADATA_CACHE =
      new ConcurrentHashMap<>();

  private final Class<T> resultType;

  private final SqlResultMetadata<T> metadata;

  private final SqlTypeConverterRegistry converterRegistry;

  /**
   * 构造结果映射器。
   *
   * @param resultType 查询结果实体类型，必须存在无参构造方法
   */
  public SqlResultRowMapper(Class<T> resultType) {
    this(resultType, SqlTypeConverterRegistry.defaultRegistry());
  }

  /**
   * 构造结果映射器。
   *
   * @param resultType 查询结果实体类型，必须存在无参构造方法
   * @param converterRegistry SQL类型转换器注册器，用于字段赋值前的类型转换
   */
  public SqlResultRowMapper(Class<T> resultType, SqlTypeConverterRegistry converterRegistry) {
    this.resultType = resultType;
    this.metadata = metadata(resultType);
    this.converterRegistry = ObjectUtil.defaultIfNull(
        converterRegistry,
        SqlTypeConverterRegistry.defaultRegistry());
  }

  /**
   * 创建结果映射器。
   *
   * @param resultType 查询结果实体类型
   * @param <T> 查询结果泛型类型
   * @return SQL结果行映射器
   */
  public static <T> SqlResultRowMapper<T> of(Class<T> resultType) {
    return new SqlResultRowMapper<>(resultType);
  }

  /**
   * 创建结果映射器。
   *
   * @param resultType 查询结果实体类型
   * @param converterRegistry SQL类型转换器注册器
   * @param <T> 查询结果泛型类型
   * @return SQL结果行映射器
   */
  public static <T> SqlResultRowMapper<T> of(
      Class<T> resultType,
      SqlTypeConverterRegistry converterRegistry) {
    return new SqlResultRowMapper<>(resultType, converterRegistry);
  }

  /**
   * 将ResultSet当前行转换为实体对象。
   *
   * @param rs JDBC结果集，调用方保证游标已经定位到当前行
   * @param rowNum 当前行号，从0开始
   * @return 映射后的实体对象
   * @throws SQLException JDBC读取失败时抛出
   */
  @Override
  public T mapRow(ResultSet rs, int rowNum) throws SQLException {
    T target = newInstance();
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    for (int i = 1; i <= columnCount; i++) {
      String columnLabel = metaData.getColumnLabel(i);
      Field field = findField(columnLabel);
      if (ObjectUtil.isNull(field)) {
        continue;
      }
      Object rawValue = rs.getObject(i);
      Object value;
      try {
        value = converterRegistry.convertForJava(rawValue, field.getType());
      } catch (PersistenceException e) {
        throw new SQLException(e);
      }
      if (ObjectUtil.isNull(value) && field.getType().isPrimitive()) {
        continue;
      }
      setValue(target, field, value);
    }
    return target;
  }

  private T newInstance() throws SQLException {
    try {
      return metadata.getConstructor().newInstance();
    } catch (Exception e) {
      throw new SQLException(new PersistenceException(
          PersistenceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, e, resultType.getName()));
    }
  }

  private void setValue(T target, Field field, Object value) throws SQLException {
    try {
      field.set(target, value);
    } catch (Exception e) {
      throw new SQLException(new PersistenceException(
          PersistenceErrorCode.DATA_SOURCE_TYPE_CONVERT_FAILED, e,
          ObjectUtil.isNull(value) ? "null" : value.getClass().getName(), field.getType().getName(),
          field.getName()));
    }
  }

  private Field findField(String columnLabel) {
    if (StrUtil.isBlank(columnLabel)) {
      return null;
    }
    Field field = metadata.getFields().get(normalize(columnLabel));
    if (ObjectUtil.isNotNull(field)) {
      return field;
    }
    return metadata.getFields().get(normalize(underlineToCamel(columnLabel)));
  }

  @SuppressWarnings("unchecked")
  private SqlResultMetadata<T> metadata(Class<T> type) {
    return (SqlResultMetadata<T>) METADATA_CACHE.computeIfAbsent(type, this::resolveMetadata);
  }

  @SuppressWarnings("unchecked")
  private SqlResultMetadata<T> resolveMetadata(Class<?> type) {
    try {
      Constructor<?> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return new SqlResultMetadata<>((Constructor<T>) constructor, resolveFields(type));
    } catch (Exception e) {
      throw risk(e, type.getName());
    }
  }

  private Map<String, Field> resolveFields(Class<?> type) {
    Map<String, Field> mapping = new LinkedHashMap<>();
    Class<?> current = type;
    while (ObjectUtil.isNotEmpty(current) && current != Object.class) {
      for (Field field : current.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
          continue;
        }
        field.setAccessible(true);
        mapping.putIfAbsent(normalize(field.getName()), field);
        mapping.putIfAbsent(normalize(camelToUnderline(field.getName())), field);
        Column column = field.getAnnotation(Column.class);
        if (ObjectUtil.isNotEmpty(column) && StrUtil.isNotBlank(column.value())) {
          mapping.putIfAbsent(normalize(column.value()), field);
        }
      }
      current = current.getSuperclass();
    }
    return Map.copyOf(mapping);
  }

  private String normalize(String value) {
    return ObjectUtil.isNull(value) ? "" : value.replace("`", "")
        .replace("\"", "")
        .replace("[", "")
        .replace("]", "")
        .toLowerCase(Locale.ROOT);
  }

  private String camelToUnderline(String value) {
    StringBuilder builder = new StringBuilder(value.length() + 8);
    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      if (Character.isUpperCase(current)) {
        if (i > 0) {
          builder.append('_');
        }
        builder.append(Character.toLowerCase(current));
      } else {
        builder.append(current);
      }
    }
    return builder.toString();
  }

  private String underlineToCamel(String value) {
    StringBuilder builder = new StringBuilder(value.length());
    boolean upperNext = false;
    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      if (current == '_') {
        upperNext = true;
        continue;
      }
      if (upperNext) {
        builder.append(Character.toUpperCase(current));
        upperNext = false;
      } else {
        builder.append(current);
      }
    }
    return builder.toString();
  }

  private RuntimeException risk(Throwable throwable, Object... args) {
    return sneakyThrow(new PersistenceException(
        PersistenceErrorCode.DATA_SOURCE_SQL_EXECUTE_FAILED, throwable, args));
  }

  @SuppressWarnings("unchecked")
  private <TException extends Throwable> RuntimeException sneakyThrow(Throwable throwable)
      throws TException {
    throw (TException) throwable;
  }
}
