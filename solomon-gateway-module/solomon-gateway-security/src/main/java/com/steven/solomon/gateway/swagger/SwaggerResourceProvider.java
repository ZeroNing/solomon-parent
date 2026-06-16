package com.steven.solomon.gateway.swagger;

import cn.hutool.core.collection.CollUtil;

import com.steven.solomon.gateway.properties.GatewaySwaggerProperties;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Swagger 文档资源提供者。
 *
 * <p>根据网关路由定义聚合各微服务的 Swagger 文档地址，按服务名排序，
 * 供 Knife4j/SpringDoc 统一展示。</p>
 *
 * @author steven
 */
public class SwaggerResourceProvider {

    private static final Logger logger = LoggerUtils.logger(SwaggerResourceProvider.class);

    private final RouteDefinitionLocator routeDefinitionLocator;
    private final GatewaySwaggerProperties properties;

    public SwaggerResourceProvider(RouteDefinitionLocator routeDefinitionLocator,
                                   GatewaySwaggerProperties properties) {
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.properties = properties;
    }

    /**
     * 获取所有网关路由对应的 Swagger 文档资源。
     *
     * @return 文档资源 Flux
     */
    public Mono<List<SwaggerResourceInfo>> getResources() {
        Flux<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions();
        return routes.collectList().map(this::buildResources);
    }

    /**
     * 根据路由定义构建 Swagger 资源列表。
     */
    private List<SwaggerResourceInfo> buildResources(List<RouteDefinition> routeDefinitions) {
        if (CollUtil.isEmpty(routeDefinitions)) {
            return List.of();
        }
        List<SwaggerResourceInfo> resources = new ArrayList<>();
        for (RouteDefinition route : routeDefinitions) {
            String serviceId = extractServiceId(route);
            if (serviceId == null) {
                continue;
            }
            String url = "/" + serviceId + properties.getApiDocsPath();
            resources.add(new SwaggerResourceInfo(serviceId, url));
        }
        // 按名称排序，保证展示稳定
        resources.sort((a, b) -> a.name().compareTo(b.name()));
        logger.info("Swagger 聚合完成, 共 {} 个文档资源", resources.size());
        return resources;
    }

    /**
     * 从路由定义中提取服务ID。
     *
     * <p>lb://service-name 格式的 URI 提取 service-name 作为服务标识。</p>
     */
    private String extractServiceId(RouteDefinition route) {
        if (route.getUri() == null) {
            return null;
        }
        String uri = route.getUri().toString();
        if (uri.startsWith("lb://")) {
            return uri.substring(5);
        }
        return route.getId();
    }
}
