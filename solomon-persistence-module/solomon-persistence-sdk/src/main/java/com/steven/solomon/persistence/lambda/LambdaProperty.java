package com.steven.solomon.persistence.lambda;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.persistence.code.PersistenceErrorCode;
import com.steven.solomon.persistence.exception.PersistenceException;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda 属性解析工具�? *
 * <p>只解�?Getter 方法引用，例�?{@code User::getName}、{@code User::isEnabled}�? * 解析结果会缓存，减少重复反射开销�?/p>
 */
public final class LambdaProperty {

  private static final Map<Class<?>, String> CACHE = new ConcurrentHashMap<>();

  private LambdaProperty() {
  }

  /**
   * �?Getter 方法引用中解�?Java 属性名�?   *
   * @param function 字段方法引用
   * @param <T> 实体类型
   * @return Java 属性名
   * @throws PersistenceException Lambda 不是 Getter 方法引用时抛�?   */
  public static <T> String name(SFunction<T, ?> function) throws PersistenceException {
    if (ObjectUtil.isEmpty(function)) {
      throw new PersistenceException(PersistenceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, "lambda");
    }
    return CACHE.computeIfAbsent(function.getClass(), key -> resolveName(function));
  }

  /**
   * �?Getter 方法引用中解析实体类型�?   *
   * @param function 字段方法引用
   * @param <T> 实体类型
   * @return Getter 所属实体类�?   * @throws PersistenceException Lambda 解析失败时抛�?   */
  public static <T> Class<?> owner(SFunction<T, ?> function) throws PersistenceException {
    if (ObjectUtil.isEmpty(function)) {
      throw new PersistenceException(PersistenceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, "lambda");
    }
    SerializedLambda lambda = serializedLambda(function);
    try {
      return Class.forName(lambda.getImplClass().replace('/', '.'));
    } catch (ClassNotFoundException e) {
      throw risk(e, lambda.getImplClass());
    }
  }

  private static String resolveName(SFunction<?, ?> function) {
    SerializedLambda lambda = serializedLambda(function);
    return toPropertyName(lambda.getImplMethodName());
  }

  private static SerializedLambda serializedLambda(SFunction<?, ?> function) {
    try {
      Class<?> lambdaClass = function.getClass();
      Method method = lambdaClass.getDeclaredMethod("writeReplace");
      method.setAccessible(true);
      return (SerializedLambda) method.invoke(function);
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
    PersistenceException exception = ObjectUtil.isEmpty(throwable)
        ? new PersistenceException(PersistenceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, args)
        : new PersistenceException(PersistenceErrorCode.DATA_SOURCE_COLUMN_NOT_FOUND, throwable,
            args);
    return sneakyThrow(exception);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws T {
    throw (T) throwable;
  }
}
