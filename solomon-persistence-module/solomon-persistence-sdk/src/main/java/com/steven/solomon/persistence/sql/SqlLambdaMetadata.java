package com.steven.solomon.persistence.sql;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.persistence.annotation.Column;
import com.steven.solomon.persistence.annotation.Table;
import com.steven.solomon.persistence.code.PersistenceErrorCode;
import com.steven.solomon.persistence.exception.PersistenceException;
import java.lang.reflect.Field;

/**
 * 实体 SQL 元数据工具�? *
 * <p>集中处理实体表名、表别名和字段名解析，避�?Lambda 条件、JOIN 构建器重复写反射逻辑�?/p>
 */
final class SqlLambdaMetadata {

  private SqlLambdaMetadata() {
  }

  /**
   * 解析实体表名�?   *
   * @param entityClass 实体类型
   * @return 表名；优先读�?{@link Table}，未配置时使用类名下划线形式
   */
  static String tableName(Class<?> entityClass) {
    if (ObjectUtil.isEmpty(entityClass)) {
      throw risk(PersistenceErrorCode.DATA_SOURCE_TABLE_NOT_FOUND, "entityClass");
    }
    Table table = entityClass.getAnnotation(Table.class);
    if (ObjectUtil.isNotEmpty(table)) {
      String tableName = StrUtil.blankToDefault(table.value(), table.name());
      if (StrUtil.isNotBlank(tableName)) {
        return SqlInjectionGuard.validateQualifiedIdentifier(tableName, "entityTable");
      }
    }
    return SqlInjectionGuard.validateQualifiedIdentifier(
        camelToUnderline(entityClass.getSimpleName()), "entityTable");
  }

  /**
   * 使用实体表名生成默认别名�?   *
   * <p>例如 {@code demo_user -> demoUser}、{@code sys_order_item -> sysOrderItem}�?/p>
   *
   * @param entityClass 实体类型
   * @return 默认表别�?   */
  static String tableAlias(Class<?> entityClass) {
    return SqlInjectionGuard.validateIdentifier(underlineToCamel(tableName(entityClass)),
        "entityAlias");
  }

  /**
   * 解析实体字段对应的数据库列名�?   *
   * @param owner 字段所属实体类�?   * @param fieldName Java 字段�?   * @return 数据库列�?   */
  static String columnName(Class<?> owner, String fieldName) {
    Field field = findField(owner, fieldName);
    Column column = field.getAnnotation(Column.class);
    String columnName = ObjectUtil.isNotEmpty(column) && StrUtil.isNotBlank(column.value())
        ? column.value()
        : camelToUnderline(field.getName());
    return SqlInjectionGuard.validateQualifiedIdentifier(columnName, "conditionField");
  }

  /**
   * 解析带默认表别名的数据库列名�?   *
   * @param owner 字段所属实体类�?   * @param fieldName Java 字段�?   * @return 默认别名加列名，例如 {@code demoUser.id}
   */
  static String qualifiedColumnName(Class<?> owner, String fieldName) {
    return tableAlias(owner) + "." + columnName(owner, fieldName);
  }

  private static Field findField(Class<?> owner, String fieldName) {
    Class<?> current = owner;
    while (ObjectUtil.isNotEmpty(current) && current != Object.class) {
      try {
        Field field = current.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    throw risk(PersistenceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, owner.getName(), fieldName);
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

  private static String underlineToCamel(String value) {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    StringBuilder builder = new StringBuilder(value.length());
    boolean upperNext = false;
    for (int i = 0; i < value.length(); i++) {
      char current = value.charAt(i);
      if (current == '_') {
        upperNext = builder.length() > 0;
        continue;
      }
      builder.append(upperNext ? Character.toUpperCase(current) : current);
      upperNext = false;
    }
    return builder.toString();
  }

  private static RuntimeException risk(String errorCode, Object... args) {
    return sneakyThrow(new PersistenceException(errorCode, args));
  }

  @SuppressWarnings("unchecked")
  private static <TException extends Throwable> RuntimeException sneakyThrow(Throwable throwable)
      throws TException {
    throw (TException) throwable;
  }
}
