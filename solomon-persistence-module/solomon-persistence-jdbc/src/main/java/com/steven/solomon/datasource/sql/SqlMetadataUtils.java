package com.steven.solomon.datasource.sql;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.datasource.annotation.Column;
import com.steven.solomon.datasource.annotation.PrimaryKey;
import com.steven.solomon.datasource.annotation.Table;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SQL元数据工具。
 */
public class SqlMetadataUtils {

  private SqlMetadataUtils() {
  }

  /**
   * 解析实体对应的表名。
   *
   * @param entityClass 实体类型，必须标注 {@link Table}
   * @return 数据库表名
   * @throws DataSourceException 未标注表注解时抛出
   */
  public static String tableName(Class<?> entityClass) throws DataSourceException {
    Table table = entityClass.getAnnotation(Table.class);
    if (ObjectUtil.isNull(table)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_TABLE_NOT_FOUND,
          entityClass.getName());
    }
    if (StrUtil.isNotBlank(table.value())) {
      return SqlInjectionGuard.validateTableExpression(table.value(), "tableAnnotation");
    }
    if (StrUtil.isNotBlank(table.name())) {
      return SqlInjectionGuard.validateTableExpression(table.name(), "tableAnnotation");
    }
    return SqlInjectionGuard.validateTableExpression(entityClass.getSimpleName(),
        "tableAnnotation");
  }

  /**
   * 解析实体主键列名。
   *
   * <p>{@link PrimaryKey} 只负责标记主键，列名优先使用同字段上的 {@link Column#value()}，
   * 未填写列名时使用Java字段名的下划线格式，例如 {@code userId -> user_id}。</p>
   *
   * @param entityClass 实体类型，必须存在 {@link PrimaryKey} 字段
   * @return 数据库主键列名
   * @throws DataSourceException 未找到主键注解时抛出
   */
  public static String primaryKeyName(Class<?> entityClass) throws DataSourceException {
    return primaryKeyField(entityClass).getColumnName();
  }

  /**
   * 解析实体主键字段元数据。
   *
   * <p>{@link PrimaryKey} 只负责标记主键，列名优先使用同字段上的 {@link Column#value()}，
   * 未填写列名时使用Java字段名的下划线格式，例如 {@code userId -> user_id}。</p>
   *
   * @param entityClass 实体类型，必须存在 {@link PrimaryKey} 字段
   * @return 主键字段映射信息
   * @throws DataSourceException 未找到主键注解时抛出
   */
  public static ColumnField primaryKeyField(Class<?> entityClass) throws DataSourceException {
    for (Field field : entityClass.getDeclaredFields()) {
      PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
      if (ObjectUtil.isNull(primaryKey)) {
        continue;
      }
      field.setAccessible(true);
      return new ColumnField(field, resolveColumnName(field, field.getAnnotation(Column.class)),
          true);
    }
    throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_PRIMARY_KEY_NOT_FOUND,
        entityClass.getName());
  }

  /**
   * 解析实体可插入字段。
   *
   * @param entityClass 实体类型
   * @return 可插入字段映射列表
   * @throws DataSourceException 未找到可插入字段时抛出
   */
  public static List<ColumnField> insertFields(Class<?> entityClass) throws DataSourceException {
    List<ColumnField> fields = columnFields(entityClass, null, true);
    if (ObjectUtil.isEmpty(fields)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND,
          entityClass.getName());
    }
    return fields;
  }

  /**
   * 解析实体可更新字段。
   *
   * @param entityClass 实体类型
   * @param includeFields 指定更新字段；支持Java字段名或数据库列名，空时返回所有可更新字段
   * @return 可更新字段映射列表，不包含主键字段
   * @throws DataSourceException 未找到可更新字段时抛出
   */
  public static List<ColumnField> updateFields(Class<?> entityClass, String... includeFields)
      throws DataSourceException {
    List<ColumnField> fields = columnFields(entityClass, includeFields, false);
    if (ObjectUtil.isEmpty(fields)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND,
          entityClass.getName());
    }
    return fields;
  }

  private static List<ColumnField> columnFields(
      Class<?> entityClass,
      String[] includeFields,
      boolean insert) throws DataSourceException {
    Set<String> includeFieldSet = new HashSet<>();
    if (ObjectUtil.isNotEmpty(includeFields)) {
      for (String field : includeFields) {
        if (StrUtil.isNotBlank(field)) {
          includeFieldSet.add(field);
        }
      }
    }

    List<ColumnField> fields = new ArrayList<>();
    for (Field field : entityClass.getDeclaredFields()) {
      Column column = field.getAnnotation(Column.class);
      PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
      if (ObjectUtil.isNull(column) && ObjectUtil.isNull(primaryKey)) {
        continue;
      }
      String columnName = resolveColumnName(field, column);
      if (!insert && ObjectUtil.isNotEmpty(primaryKey)) {
        continue;
      }
      if (ObjectUtil.isNotEmpty(column) && insert && !column.insertable()) {
        continue;
      }
      if (ObjectUtil.isNotEmpty(column) && !insert && !column.updatable()) {
        continue;
      }
      if (ObjectUtil.isNotEmpty(includeFieldSet)
          && !includeFieldSet.contains(field.getName())
          && !includeFieldSet.contains(columnName)) {
        continue;
      }
      field.setAccessible(true);
      fields.add(new ColumnField(field, columnName, ObjectUtil.isNotEmpty(primaryKey)));
    }
    return fields;
  }

  private static String resolveColumnName(Field field, Column column) throws DataSourceException {
    if (ObjectUtil.isNotEmpty(column) && StrUtil.isNotBlank(column.value())) {
      return SqlInjectionGuard.validateQualifiedIdentifier(column.value(), "columnAnnotation");
    }
    return SqlInjectionGuard.validateQualifiedIdentifier(camelToUnderline(field.getName()),
        "columnAnnotation");
  }

  private static String camelToUnderline(String value) {
    if (StrUtil.isBlank(value)) {
      return value;
    }
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

}
