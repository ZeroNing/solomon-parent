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

/**
 * Sentinel 网关限流配置类
 * 为Spring Cloud Gateway集成Sentinel流量控制能力
 *
 * @author steven
 * @since 1.0.0
 */
@Configuration
public class SentinelConfig {

  /**
   * 视图解析器列表，用于异常页面渲染
   */
  private final List<ViewResolver> views;
  
  /**
   * HTTP消息编解码器配置，用于异常响应序列化
   */
  private final ServerCodecConfigurer configurer;

  /**
   * 构造方法注入必要依赖
   *
   * @param views 视图解析器提供器
   * @param config HTTP消息编解码器配置
   */
  public SentinelConfig(ObjectProvider<List<ViewResolver>> views, ServerCodecConfigurer config) {
    this.views = views.getIfAvailable(Collections::emptyList);
    this.configurer = config;
  }

  /**
   * 注册Sentinel网关限流异常处理器
   * 当请求被Sentinel限流/熔断时，统一返回友好的JSON响应
   *
   * @return Sentinel网关限流异常处理器
   */
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，优先处理限流异常
  public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
    return new SentinelGatewayBlockExceptionHandler(views, configurer);
  }

  /**
   * 注册Sentinel网关全局过滤器
   * 对所有经过网关的请求进行流量控制、熔断降级等检查
   *
   * @return Sentinel网关全局过滤器
   */
  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，最先执行限流检查
  public GlobalFilter sentinelGatewayFilter() {
      return new SentinelGatewayFilter();
  }

}
