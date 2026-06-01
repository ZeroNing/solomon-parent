package com.steven.solomon.gatewaysecurity.swagger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;
import com.steven.solomon.gatewaysecurity.support.PermissionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;

class PermissionOpenApiCustomizerTest {

  @Test
  void addBearerAuthAndPermissionCode() {
    PermissionRegistry registry = new PermissionRegistry();
    registry.replaceAll(
        java.util.List.of(new PermissionDefinition("order:query", "查询订单", "", "/orders", "GET")));
    OpenAPI openApi = new OpenAPI().paths(
        new Paths().addPathItem("/orders", new PathItem().get(new Operation())));

    new PermissionOpenApiCustomizer(registry).customise(openApi);

    Operation operation = openApi.getPaths().get("/orders").getGet();
    assertNotNull(openApi.getComponents().getSecuritySchemes()
        .get(PermissionOpenApiCustomizer.SECURITY_SCHEME));
    assertEquals("order:query",
        operation.getExtensions().get(PermissionOpenApiCustomizer.PERMISSION_EXTENSION));
    assertEquals(PermissionOpenApiCustomizer.SECURITY_SCHEME,
        operation.getSecurity().getFirst().keySet().iterator().next());
  }
}
