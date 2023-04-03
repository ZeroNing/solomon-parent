package com.steven.solomon.swagger;

import com.steven.solomon.lambda.Lambda;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Component
@Primary
public class SwaggerProvider implements SwaggerResourcesProvider {

  /**
   * 接口地址
   */
  public static final String API_URI = "/v2/api-docs";

  private final GatewayProperties properties;

  /**
   * 网关应用名称
   */
  @Value("${spring.application.name}")
  private String applicationName;

  public SwaggerProvider(GatewayProperties properties) {this.properties = properties;}

  @Override
  public List<SwaggerResource> get() {
    List<SwaggerResource> resources =new ArrayList<>();
    resources.add(createResource(applicationName, API_URI));
    resources.addAll(Lambda.toList(properties.getRoutes(), route -> createResource(route.getId(), getRouteLocation(route))));
    //添加gateway
    return resources;
  }

  // You will certainly need to edit this
  private String getRouteLocation(RouteDefinition route) {
    return Optional.ofNullable(route.getPredicates().get(0).getArgs().values().toArray()[0])
        .map(String::valueOf)
        .map(s -> s.replace("/**", API_URI))
        .orElse(null);
  }

  private SwaggerResource createResource(String name, String location) {
    SwaggerResource swaggerResource = new SwaggerResource();
    swaggerResource.setName(name);
    swaggerResource.setLocation(location);
    swaggerResource.setSwaggerVersion("2.0");
    return swaggerResource;
  }

}
