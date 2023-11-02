package com.steven.solomon.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.steven.solomon.exception.handler.SentinelGatewayBlockExceptionHandler;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

@Configuration
public class SentinelConfig {

  private final List<ViewResolver>    views;
  private final ServerCodecConfigurer configurer;

  public SentinelConfig(ObjectProvider<List<ViewResolver>> views, ServerCodecConfigurer config) {
    this.views      = views.getIfAvailable(Collections::emptyList);
    this.configurer = config;
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
    return new SentinelGatewayBlockExceptionHandler(views, configurer);
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public GlobalFilter sentinelGatewayFilter() {
      return new SentinelGatewayFilter();
  }

}
