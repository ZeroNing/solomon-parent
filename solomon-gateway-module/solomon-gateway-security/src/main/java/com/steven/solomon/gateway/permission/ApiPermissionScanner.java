package com.steven.solomon.gateway.permission;

import cn.hutool.core.util.StrUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 启动时扫描接口权限注解，并同步到可替换的存储实现。 */
public class ApiPermissionScanner implements ApplicationRunner {

  private final ApplicationContext applicationContext;
  private final ApiPermissionStore permissionStore;

  public ApiPermissionScanner(ApplicationContext applicationContext,
      ApiPermissionStore permissionStore) {
    this.applicationContext = applicationContext;
    this.permissionStore = permissionStore;
  }

  @Override
  public void run(ApplicationArguments args) {
    permissionStore.saveAll(scan());
  }

  public List<ApiPermissionMetadata> scan() {
    List<ApiPermissionMetadata> permissions = new ArrayList<>();
    for (Object bean : controllerBeans()) {
      Class<?> type = AopUtils.getTargetClass(bean);
      RequestMapping classMapping =
          AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
      for (Method method : ReflectionUtils.getUniqueDeclaredMethods(type)) {
        ApiPermission permission =
            AnnotatedElementUtils.findMergedAnnotation(method, ApiPermission.class);
        if (permission == null) {
          continue;
        }
        RequestMapping methodMapping =
            AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
        if (methodMapping == null) {
          throw new IllegalStateException("@ApiPermission 必须标记在请求接口上: " + method);
        }
        for (String path : combinePaths(paths(classMapping), paths(methodMapping))) {
          permissions.add(new ApiPermissionMetadata(ApiPermissionCodeGenerator.fromPath(path),
              resolveName(method), path, resolveHttpMethod(methodMapping), permission.anonymous()));
        }
      }
    }
    return permissions;
  }

  private Collection<Object> controllerBeans() {
    Set<Object> beans = new LinkedHashSet<>();
    beans.addAll(applicationContext.getBeansWithAnnotation(RestController.class).values());
    beans.addAll(applicationContext.getBeansWithAnnotation(Controller.class).values());
    return beans;
  }

  private List<String> paths(RequestMapping mapping) {
    if (mapping == null) {
      return List.of("");
    }
    String[] paths = mapping.path().length > 0 ? mapping.path() : mapping.value();
    return paths.length == 0 ? List.of("") : Arrays.asList(paths);
  }

  private List<String> combinePaths(List<String> classPaths, List<String> methodPaths) {
    List<String> paths = new ArrayList<>();
    for (String classPath : classPaths) {
      for (String methodPath : methodPaths) {
        paths.add("/" + StrUtil.strip(StrUtil.join("/",
            StrUtil.strip(classPath, "/"), StrUtil.strip(methodPath, "/")), "/"));
      }
    }
    return paths;
  }

  private String resolveHttpMethod(RequestMapping mapping) {
    return mapping.method().length == 0 ? "ALL"
        : Arrays.stream(mapping.method()).map(Enum::name).sorted().collect(java.util.stream.Collectors.joining(","));
  }

  private String resolveName(Method method) {
    for (Annotation annotation : method.getAnnotations()) {
      String annotationName = annotation.annotationType().getName();
      if ("io.swagger.v3.oas.annotations.Operation".equals(annotationName)) {
        return annotationValue(annotation, "summary", method.getName());
      }
      if ("io.swagger.annotations.ApiOperation".equals(annotationName)) {
        return annotationValue(annotation, "value", method.getName());
      }
    }
    return method.getName();
  }

  private String annotationValue(Annotation annotation, String attribute, String defaultValue) {
    try {
      String value = String.valueOf(annotation.annotationType().getMethod(attribute).invoke(annotation));
      return StrUtil.isBlank(value) ? defaultValue : value;
    } catch (ReflectiveOperationException ex) {
      return defaultValue;
    }
  }
}
