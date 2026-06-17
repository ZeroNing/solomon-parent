package com.steven.solomon.persistence.sql.converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * SQL 结果映射元数据。
 *
 * <p>缓存结果类型的无参构造器和字段映射表，避免每次创建行映射器都重复反射扫描。</p>
 */
class SqlResultMetadata<T> {

  private final Constructor<T> constructor;
  private final Map<String, Field> fields;

  SqlResultMetadata(Constructor<T> constructor, Map<String, Field> fields) {
    this.constructor = constructor;
    this.fields = fields;
  }

  Constructor<T> getConstructor() {
    return constructor;
  }

  Map<String, Field> getFields() {
    return fields;
  }
}
