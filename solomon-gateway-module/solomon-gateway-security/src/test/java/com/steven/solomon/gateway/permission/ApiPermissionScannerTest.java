package com.steven.solomon.gateway.permission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class ApiPermissionScannerTest {

  @Test
  void scanAnnotatedController() {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.registerBean(TestController.class);
      context.refresh();

      List<ApiPermissionMetadata> permissions =
          new ApiPermissionScanner(context, values -> {
          }).scan();

      assertEquals(List.of(new ApiPermissionMetadata(
          "CORE:ORDERS:ID", "detail", "/api/core/orders/{id}", "GET", true)), permissions);
    }
  }

  @RestController
  @RequestMapping("/api/core")
  static class TestController {

    @ApiPermission(anonymous = true)
    @GetMapping("/orders/{id}")
    String detail() {
      return "ok";
    }
  }
}
