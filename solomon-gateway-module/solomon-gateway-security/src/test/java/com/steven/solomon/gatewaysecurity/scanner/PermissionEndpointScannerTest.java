package com.steven.solomon.gatewaysecurity.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.steven.solomon.gatewaysecurity.annotation.ApiPermission;
import com.steven.solomon.gatewaysecurity.support.InMemoryPermissionRepository;
import com.steven.solomon.gatewaysecurity.support.PermissionRegistry;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class PermissionEndpointScannerTest {

  @Test
  void scanAndSaveAnnotatedEndpoint() throws NoSuchMethodException {
    RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
    Method method = TestController.class.getDeclaredMethod("orders");
    mapping.registerMapping(RequestMappingInfo.paths("/orders").methods(RequestMethod.GET).build(),
        new TestController(), method);
    StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
    beanFactory.addBean("requestMappingHandlerMapping", mapping);
    InMemoryPermissionRepository repository = new InMemoryPermissionRepository();
    PermissionRegistry registry = new PermissionRegistry();

    new PermissionEndpointScanner(beanFactory.getBeanProvider(RequestMappingHandlerMapping.class),
        repository, registry).afterSingletonsInstantiated();

    assertEquals(1, repository.findAll().size());
    assertEquals("order:query", repository.findAll().getFirst().code());
    assertEquals("/orders", registry.findAll().getFirst().path());
    assertFalse(repository.findAll().getFirst().anonymous());
  }

  @Test
  void autoGenerateCodeFromPath() throws NoSuchMethodException {
    RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
    Method method = AutoCodeController.class.getDeclaredMethod("getOrder");
    mapping.registerMapping(
        RequestMappingInfo.paths("/api/core/orders/{id}").methods(RequestMethod.GET).build(),
        new AutoCodeController(), method);
    StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
    beanFactory.addBean("requestMappingHandlerMapping", mapping);
    InMemoryPermissionRepository repository = new InMemoryPermissionRepository();
    PermissionRegistry registry = new PermissionRegistry();

    new PermissionEndpointScanner(beanFactory.getBeanProvider(RequestMappingHandlerMapping.class),
        repository, registry).afterSingletonsInstantiated();

    assertEquals(1, repository.findAll().size());
    assertEquals("CORE:ORDERS:ID", repository.findAll().getFirst().code());
    assertEquals("获取订单", repository.findAll().getFirst().name());
  }

  private static class TestController {

    @ApiPermission(value = "order:query", name = "查询订单")
    public void orders() {
    }
  }

  private static class AutoCodeController {

    @ApiPermission
    @io.swagger.v3.oas.annotations.Operation(summary = "获取订单")
    @RequestMapping("/api/core/orders/{id}")
    public void getOrder() {
    }
  }
}
