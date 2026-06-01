package com.steven.solomon.gateway.swagger;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.gateway.model.GatewaySwaggerResource;
import com.steven.solomon.gateway.properties.GatewaySwaggerProperties;
import java.util.Comparator;
import java.util.List;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.core.publisher.Mono;

/**
 * 网关 Swagger 资源聚合器。
 *
 * <p>根据 Gateway 动态路由生成下游 OpenAPI 地址，调用方可以直接用于 Swagger UI 或 Knife4j 聚合接口。</p>
 */
public class GatewaySwaggerResourceProvider {

  private final RouteLocator routeLocator;
  private final GatewaySwaggerProperties properties;

  public GatewaySwaggerResourceProvider(
      RouteLocator routeLocator,
      GatewaySwaggerProperties properties) {
    this.routeLocator = routeLocator;
    this.properties = properties;
  }

  /**
   * 获取全部可用的下游 OpenAPI 文档资源。
   */
  public Mono<List<GatewaySwaggerResource>> getResources() {
    return routeLocator.getRoutes()
        .filter(route -> StrUtil.isNotBlank(route.getId()))
        .filter(route -> ObjectUtil.isEmpty(properties.getIgnoredRouteIds())
            || !properties.getIgnoredRouteIds().contains(route.getId()))
        .map(route -> new GatewaySwaggerResource(route.getId(), buildApiDocsUrl(route.getId())))
        .sort(Comparator.comparing(GatewaySwaggerResource::name))
        .collectList();
  }

  /**
   * 根据路由 ID 构造经过网关转发的 OpenAPI 地址。
   */
  public String buildApiDocsUrl(String routeId) {
    String normalizedRouteId = StrUtil.removePrefix(StrUtil.trim(routeId), "/");
    String apiDocsPath = StrUtil.addPrefixIfNot(properties.getApiDocsPath(), "/");
    return "/" + normalizedRouteId + apiDocsPath;
  }
}
