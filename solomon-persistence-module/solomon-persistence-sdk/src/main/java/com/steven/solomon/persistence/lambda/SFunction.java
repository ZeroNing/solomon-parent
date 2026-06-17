package com.steven.solomon.persistence.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 可序列化字段函数。
 *
 * <p>用于接收 {@code User::getName} 这类方法引用，框架可以从中解析 Java 属性名，
 * 再映射为数据库列名，避免业务代码手写字段字符串。</p>
 *
 * @param <T> 实体类型
 * @param <R> 字段返回类型
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
