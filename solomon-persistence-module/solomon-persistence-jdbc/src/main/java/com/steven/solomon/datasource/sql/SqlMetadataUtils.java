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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL元数据工具。
 */
public class SqlMetadataUtils {

  private static final Map<Class<?>, SqlEntityMetadata> CACHE = new ConcurrentHashMap<>();

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
    String tableName = metadata(entityClass).getTableName();
    if (StrUtil.isBlank(tableName)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_TABLE_NOT_FOUND,
          entityClass.getName());
    }
    return tableName;
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
    ColumnField primaryKey = metadata(entityClass).getPrimaryKey();
    if (ObjectUtil.isNotEmpty(primaryKey)) {
      return primaryKey;
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

  /**
   * 将 Java 字段名解析为数据库列名。
   *
   * @param entityClass 实体类型
   * @param fieldName Java 字段名，也允许传入数据库列名
   * @return 数据库列名
   * @throws DataSourceException 字段未在实体元数据中声明时抛出
   */
  public static String columnName(Class<?> entityClass, String fieldName)
      throws DataSourceException {
    if (StrUtil.isBlank(fieldName)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND,
          entityClass.getName());
    }
    for (ColumnField field : metadata(entityClass).getFields()) {
      if (StrUtil.equals(field.getFieldName(), fieldName)
          || StrUtil.equals(field.getColumnName(), fieldName)) {
        return field.getColumnName();
      }
    }
    throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND,
        entityClass.getName(), fieldName);
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
    for (ColumnField field : metadata(entityClass).getFields()) {
      Column column = field.getField().getAnnotation(Column.class);
      if (!insert && field.isPrimaryKey()) {
        continue;
      }
      if (ObjectUtil.isNotEmpty(column) && insert && !column.insertable()) {
        continue;
      }
      if (ObjectUtil.isNotEmpty(column) && !insert && !column.updatable()) {
        continue;
      }
      if (ObjectUtil.isNotEmpty(includeFieldSet)
          && !includeFieldSet.contains(field.getFieldName())
          && !includeFieldSet.contains(field.getColumnName())) {
        continue;
      }
      fields.add(field);
    }
    return fields;
  }

  private static SqlEntityMetadata metadata(Class<?> entityClass) {
    return CACHE.computeIfAbsent(entityClass, SqlMetadataUtils::parseMetadata);
  }

  private static SqlEntityMetadata parseMetadata(Class<?> entityClass) {
    String tableName = null;
    Table table = entityClass.getAnnotation(Table.class);
    if (ObjectUtil.isNotEmpty(table)) {
      if (StrUtil.isNotBlank(table.value())) {
        tableName = SqlInjectionGuard.validateTableExpression(table.value(), "tableAnnotation");
      } else if (StrUtil.isNotBlank(table.name())) {
        tableName = SqlInjectionGuard.validateTableExpression(table.name(), "tableAnnotation");
      } else {
        tableName = SqlInjectionGuard.validateTableExpression(entityClass.getSimpleName(),
            "tableAnnotation");
      }
    }

    List<ColumnField> fields = new ArrayList<>();
    ColumnField primaryKey = null;
    for (Field field : entityClass.getDeclaredFields()) {
      Column column = field.getAnnotation(Column.class);
      PrimaryKey primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
      if (ObjectUtil.isNull(column) && ObjectUtil.isNull(primaryKeyAnnotation)) {
        continue;
      }
      field.setAccessible(true);
      ColumnField columnField = new ColumnField(field, resolveColumnName(field, column),
          ObjectUtil.isNotEmpty(primaryKeyAnnotation));
      fields.add(columnField);
      if (columnField.isPrimaryKey()) {
        primaryKey = columnField;
      }
    }
    return new SqlEntityMetadata(tableName, List.copyOf(fields), primaryKey);
  }

  private static String resolveColumnName(Field field, Column column) {
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
