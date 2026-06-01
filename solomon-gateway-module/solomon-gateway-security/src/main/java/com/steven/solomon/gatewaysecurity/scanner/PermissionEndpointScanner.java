package com.steven.solomon.gatewaysecurity.scanner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gatewaysecurity.annotation.ApiPermission;
import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;
import com.steven.solomon.gatewaysecurity.service.PermissionRepository;
import com.steven.solomon.gatewaysecurity.support.PermissionRegistry;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** 扫描并登记带有 ApiPermission 注解的接口。 */
public class PermissionEndpointScanner implements SmartInitializingSingleton {

  private final ObjectProvider<RequestMappingHandlerMapping> handlerMappings;
  private final PermissionRepository permissionRepository;
  private final PermissionRegistry permissionRegistry;

  public PermissionEndpointScanner(ObjectProvider<RequestMappingHandlerMapping> handlerMappings,
      PermissionRepository permissionRepository, PermissionRegistry permissionRegistry) {
    this.handlerMappings = handlerMappings;
    this.permissionRepository = permissionRepository;
    this.permissionRegistry = permissionRegistry;
  }

  @Override
  public void afterSingletonsInstantiated() {
    Set<PermissionDefinition> permissions = new LinkedHashSet<>();
    handlerMappings.orderedStream().forEach(handlerMapping ->
        handlerMapping.getHandlerMethods().forEach((mapping, handlerMethod) ->
            addPermissions(permissions, mapping, handlerMethod)));
    permissionRegistry.replaceAll(permissions);
    permissionRepository.saveAll(permissions);
  }

  private void addPermissions(Set<PermissionDefinition> permissions, RequestMappingInfo mapping,
      HandlerMethod handlerMethod) {
    ApiPermission annotation = resolveAnnotation(handlerMethod);
    if (annotation == null || StrUtil.isBlank(annotation.value())) {
      return;
    }
    Set<String> paths = mapping.getPatternValues();
    if (CollUtil.isEmpty(paths)) {
      paths = Set.of("/");
    }
    Set<RequestMethod> methods = mapping.getMethodsCondition().getMethods();
    if (CollUtil.isEmpty(methods)) {
      paths.forEach(path -> permissions.add(toDefinition(annotation, path, "*")));
      return;
    }
    paths.forEach(path -> methods.forEach(method ->
        permissions.add(toDefinition(annotation, path, method.name()))));
  }

  private ApiPermission resolveAnnotation(HandlerMethod handlerMethod) {
    ApiPermission annotation =
        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), ApiPermission.class);
    return annotation != null ? annotation
        : AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), ApiPermission.class);
  }

  private PermissionDefinition toDefinition(ApiPermission annotation, String path, String method) {
    return new PermissionDefinition(annotation.value(), annotation.name(), annotation.description(),
        path, method);
  }
}
