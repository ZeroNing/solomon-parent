package com.steven.solomon.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库主键字段标记注解�?
 *
 * <p>该注解只用于标记当前字段是主键，不负责配置数据库列名。主键列名优先读取同字段上的
 * {@link Column#value()}，如果没有配�?{@link Column}，则使用Java字段名�?/p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {
}
