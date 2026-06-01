package com.steven.solomon.gatewaysecurity.swagger;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;
import com.steven.solomon.gatewaysecurity.support.PermissionRegistry;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Locale;
import org.springdoc.core.customizers.OpenApiCustomizer;

/** 在 Swagger 文档中标识需要鉴权的接口。 */
public class PermissionOpenApiCustomizer implements OpenApiCustomizer {

  public static final String SECURITY_SCHEME = "bearerAuth";
  public static final String PERMISSION_EXTENSION = "x-permission-code";

  private final PermissionRegistry permissionRegistry;

  public PermissionOpenApiCustomizer(PermissionRegistry permissionRegistry) {
    this.permissionRegistry = permissionRegistry;
  }

  @Override
  public void customise(OpenAPI openApi) {
    Components components = openApi.getComponents() == null ? new Components() : openApi.getComponents();
    components.addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT"));
    openApi.setComponents(components);
    permissionRegistry.findAll().forEach(permission -> addPermission(openApi, permission));
  }

  private void addPermission(OpenAPI openApi, PermissionDefinition permission) {
    if (openApi.getPaths() == null || StrUtil.isBlank(permission.path())) {
      return;
    }
    PathItem pathItem = openApi.getPaths().get(permission.path());
    if (pathItem == null) {
      return;
    }
    if ("*".equals(permission.httpMethod())) {
      pathItem.readOperations().forEach(operation -> configure(operation, permission));
      return;
    }
    PathItem.HttpMethod method =
        PathItem.HttpMethod.valueOf(permission.httpMethod().toUpperCase(Locale.ROOT));
    Operation operation = pathItem.readOperationsMap().get(method);
    if (operation != null) {
      configure(operation, permission);
    }
  }

  private void configure(Operation operation, PermissionDefinition permission) {
    operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    operation.addExtension(PERMISSION_EXTENSION, permission.code());
  }
}
