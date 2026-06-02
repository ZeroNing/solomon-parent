package com.steven.solomon.gateway.swagger;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gateway.model.SwaggerResource;
import com.steven.solomon.gateway.properties.GatewaySwaggerProperties;
import java.util.Comparator;
import java.util.List;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Mono;

/** 根据网关路由生成下游 Swagger 聚合资源。 */
public class SwaggerResourceProvider {

  private final RouteDefinitionLocator routeDefinitionLocator;
  private final GatewaySwaggerProperties properties;

  public SwaggerResourceProvider(RouteDefinitionLocator routeDefinitionLocator,
      GatewaySwaggerProperties properties) {
    this.routeDefinitionLocator = routeDefinitionLocator;
    this.properties = properties;
  }

  /** 返回所有有效路由的 OpenAPI 文档地址。 */
  public Mono<List<SwaggerResource>> getResources() {
    return routeDefinitionLocator.getRouteDefinitions()
        .filter(route -> StrUtil.isNotBlank(route.getId()))
        .map(route -> new SwaggerResource(route.getId(),
            "/" + route.getId() + normalizePath(properties.getApiDocsPath())))
        .sort(Comparator.comparing(SwaggerResource::name))
        .collectList();
  }

  private String normalizePath(String path) {
    if (StrUtil.isBlank(path)) {
      return "/v3/api-docs";
    }
    return path.startsWith("/") ? path : "/" + path;
  }
}
