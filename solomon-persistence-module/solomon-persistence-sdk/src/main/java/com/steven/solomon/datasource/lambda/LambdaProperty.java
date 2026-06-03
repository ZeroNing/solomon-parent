package com.steven.solomon.datasource.lambda;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda 属性解析工具。
 *
 * <p>只解析 Getter 方法引用，例如 {@code User::getName}、{@code User::isEnabled}。
 * 解析结果会缓存，减少重复反射开销。</p>
 */
public final class LambdaProperty {

  private static final Map<Class<?>, String> CACHE = new ConcurrentHashMap<>();

  private LambdaProperty() {
  }

  /**
   * 从 Getter 方法引用中解析 Java 属性名。
   *
   * @param function 字段方法引用
   * @param <T> 实体类型
   * @return Java 属性名
   * @throws DataSourceException Lambda 不是 Getter 方法引用时抛出
   */
  public static <T> String name(SFunction<T, ?> function) throws DataSourceException {
    if (ObjectUtil.isEmpty(function)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, "lambda");
    }
    return CACHE.computeIfAbsent(function.getClass(), key -> resolveName(function));
  }

  private static String resolveName(SFunction<?, ?> function) {
    try {
      Class<?> lambdaClass = function.getClass();
      Method method = lambdaClass.getDeclaredMethod("writeReplace");
      method.setAccessible(true);
      SerializedLambda lambda = (SerializedLambda) method.invoke(function);
      return toPropertyName(lambda.getImplMethodName());
    } catch (Exception e) {
      throw risk(e, function.getClass().getName());
    }
  }

  private static String toPropertyName(String methodName) {
    if (StrUtil.startWith(methodName, "get") && methodName.length() > 3) {
      return lowerFirst(methodName.substring(3));
    }
    if (StrUtil.startWith(methodName, "is") && methodName.length() > 2) {
      return lowerFirst(methodName.substring(2));
    }
    throw risk(null, methodName);
  }

  private static String lowerFirst(String value) {
    if (StrUtil.isBlank(value)) {
      return value;
    }
    return Character.toLowerCase(value.charAt(0)) + value.substring(1);
  }

  private static RuntimeException risk(Throwable throwable, Object... args) {
    DataSourceException exception = ObjectUtil.isEmpty(throwable)
        ? new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, args)
        : new DataSourceException(DataSourceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, throwable,
            args);
    return sneakyThrow(exception);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws T {
    throw (T) throwable;
  }
}
