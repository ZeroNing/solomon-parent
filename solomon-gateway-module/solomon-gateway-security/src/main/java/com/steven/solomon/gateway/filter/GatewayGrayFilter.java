package com.steven.solomon.gateway.filter;

import com.steven.solomon.gateway.gray.GatewayGraySelector;
import com.steven.solomon.gateway.properties.GatewayGrayProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 清理外部灰度标记，并向下游写入网关计算出的可信版本。 */
public class GatewayGrayFilter implements GlobalFilter, Ordered {

  public static final String VERSION_ATTRIBUTE = GatewayGrayFilter.class.getName() + ".version";

  private final GatewayGrayProperties properties;
  private final GatewayGraySelector selector;

  public GatewayGrayFilter(GatewayGrayProperties properties, GatewayGraySelector selector) {
    this.properties = properties;
    this.selector = selector;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerWebExchange trustedExchange = exchange.mutate().request(request ->
        request.headers(headers -> headers.remove(properties.getHeaderName()))).build();
    if (!properties.isEnabled()) {
      return chain.filter(trustedExchange);
    }
    String version = selector.select(trustedExchange);
    ServerWebExchange grayExchange = trustedExchange.mutate().request(request ->
        request.header(properties.getHeaderName(), version)).build();
    grayExchange.getAttributes().put(VERSION_ATTRIBUTE, version);
    return chain.filter(grayExchange);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 200;
  }
}
