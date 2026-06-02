package com.steven.solomon.gateway.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ApiPermissionCodeGeneratorTest {

  @Test
  void generateCodeFromApiPath() {
    assertEquals("CORE:ORDERS:ID",
        ApiPermissionCodeGenerator.fromPath("/api/core/orders/{id}"));
  }

  @Test
  void rejectBlankPath() {
    assertThrows(IllegalArgumentException.class,
        () -> ApiPermissionCodeGenerator.fromPath(" "));
  }

  @Test
  void catalogProvidesOnlyAnonymousPaths() {
    ApiPermissionCatalog catalog = new ApiPermissionCatalog();
    catalog.saveAll(List.of(
        new ApiPermissionMetadata("AUTH:LOGIN", "登录", "/api/auth/login", "POST", true),
        new ApiPermissionMetadata("CORE:ORDERS", "订单列表", "/api/core/orders", "GET", false)));

    assertEquals(List.of("/api/auth/login"), catalog.paths());
  }
}
