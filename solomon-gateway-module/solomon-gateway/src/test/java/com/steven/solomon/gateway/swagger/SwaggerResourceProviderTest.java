package com.steven.solomon.gateway.swagger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.steven.solomon.gateway.properties.GatewaySwaggerProperties;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.RouteDefinition;
import reactor.core.publisher.Flux;

class SwaggerResourceProviderTest {

  @Test
  void createSwaggerResourcesFromRoutes() {
    RouteDefinition orders = route("orders-service");
    RouteDefinition users = route("users-service");
    SwaggerResourceProvider provider = new SwaggerResourceProvider(
        () -> Flux.just(users, orders), new GatewaySwaggerProperties());

    List<com.steven.solomon.gateway.model.SwaggerResource> resources =
        provider.getResources().block();

    assertEquals("orders-service", resources.getFirst().name());
    assertEquals("/orders-service/v3/api-docs", resources.getFirst().url());
  }

  private RouteDefinition route(String id) {
    RouteDefinition route = new RouteDefinition();
    route.setId(id);
    route.setUri(URI.create("lb://" + id));
    return route;
  }
}
