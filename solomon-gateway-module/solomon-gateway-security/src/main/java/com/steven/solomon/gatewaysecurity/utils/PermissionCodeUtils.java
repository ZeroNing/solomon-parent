package com.steven.solomon.gatewaysecurity.utils;

import cn.hutool.core.util.StrUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * 权限相关工具方法。
 *
 * <p>提供路径转权限编码、Swagger 注解提取等能力。</p>
 */
public final class PermissionCodeUtils {

  /** Swagger @Operation 注解的全限定类名（可选依赖）。 */
  private static final String OPERATION_ANNOTATION =
      "io.swagger.v3.oas.annotations.Operation";

  /** 缓存 Operation 注解类是否可用，null 表示未检测。 */
  private static volatile Boolean operationClassPresent;

  private PermissionCodeUtils() {
  }

  /**
   * 根据接口路径自动生成权限编码。
   *
   * <p>转换规则：移除前导斜杠和 /api 前缀，将路径段用冒号拼接并转大写，
   * 路径参数的花括号会被去除。例如：</p>
   * <ul>
   *   <li>/api/core/orders/{id} → CORE:ORDERS:ID</li>
   *   <li>/orders → ORDERS</li>
   *   <li>/api/user/profile → USER:PROFILE</li>
   * </ul>
   *
   * @param path 接口路径
   * @return 权限编码
   */
  public static String pathToCode(String path) {
    if (StrUtil.isBlank(path)) {
      return "";
    }
    String normalized = path;
    // 移除 /api 前缀
    if (normalized.startsWith("/api/")) {
      normalized = normalized.substring("/api".length());
    }
    return Arrays.stream(normalized.split("/"))
        .filter(StrUtil::isNotBlank)
        .map(segment -> segment.replaceAll("[{}]", ""))
        .map(String::toUpperCase)
        .collect(Collectors.joining(":"));
  }

  /**
   * 从方法的 Swagger @Operation 注解中提取 summary 作为权限名称。
   * 若 springdoc 不在 classpath 上则静默返回空字符串。
   *
   * @param method 接口方法
   * @return summary 值，未标注或 springdoc 不可用则返回空字符串
   */
  public static String resolveOperationName(java.lang.reflect.Method method) {
    return resolveSwaggerSummary(method);
  }

  /**
   * 从类的 Swagger @Operation 注解中提取 summary 作为权限名称（类级别回退）。
   * 若 springdoc 不在 classpath 上则静默返回空字符串。
   *
   * @param clazz 控制器类
   * @return summary 值，未标注或 springdoc 不可用则返回空字符串
   */
  public static String resolveOperationName(Class<?> clazz) {
    return resolveSwaggerSummary(clazz);
  }

  /**
   * 安全地从 @Operation 注解中提取 summary。
   * 使用反射避免对可选 springdoc 依赖的直接类引用。
   */
  @SuppressWarnings("unchecked")
  private static String resolveSwaggerSummary(AnnotatedElement element) {
    if (!isOperationClassPresent()) {
      return "";
    }
    try {
      Class<? extends Annotation> operationClass =
          (Class<? extends Annotation>) Class.forName(OPERATION_ANNOTATION);
      Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(element, operationClass);
      if (annotation == null) {
        return "";
      }
      String summary = (String) annotation.getClass().getMethod("summary").invoke(annotation);
      return StrUtil.blankToDefault(summary, "");
    } catch (Exception e) {
      return "";
    }
  }

  private static boolean isOperationClassPresent() {
    if (operationClassPresent == null) {
      try {
        Class.forName(OPERATION_ANNOTATION);
        operationClassPresent = Boolean.TRUE;
      } catch (ClassNotFoundException e) {
        operationClassPresent = Boolean.FALSE;
      }
    }
    return operationClassPresent;
  }
}
