package com.steven.solomon.exception.handler;

import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;
import com.steven.solomon.base.exception.BaseGlobalExceptionHandler;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.utils.LocaleUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.Context;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * Sentinel 网关限流异常处理器
 * 专门处理Sentinel触发的限流、熔断、降级等流量控制异常
 * 实现WebFlux的WebExceptionHandler接口，集成到网关异常处理链中
 *
 * @author steven
 * @since 1.0.0
 */
public class SentinelGatewayBlockExceptionHandler implements WebExceptionHandler {

  /**
   * 视图解析器列表，用于异常页面渲染
   */
  private List<ViewResolver> viewResolvers;
  
  /**
   * HTTP消息写入器列表，用于响应序列化
   */
  private List<HttpMessageWriter<?>> messageWriters;

  /**
   * 服务ID，用于异常响应标识是哪个服务抛出的异常
   */
  @Value("${spring.application.id:default}")
  private String serverId;

  /**
   * 构造方法注入必要依赖
   *
   * @param viewResolvers 视图解析器列表
   * @param serverCodecConfigurer HTTP消息编解码器配置
   */
  public SentinelGatewayBlockExceptionHandler(List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
    this.viewResolvers = viewResolvers;
    this.messageWriters = serverCodecConfigurer.getWriters();
  }

  /**
   * 写入异常响应
   * 将Sentinel限流异常转换为标准化JSON响应返回
   *
   * @param response 原始响应对象
   * @param exchange 当前请求上下文
   * @param ex 异常对象
   * @return 响应完成信号Mono
   */
  private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange, Throwable ex) {
    Exception exception = (Exception) ex;

    ServerHttpResponse resp = exchange.getResponse();
    // 设置响应Content-Type为JSON
    resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
    
    // 使用全局异常处理器统一处理异常，构造标准化响应
    Map<String, Object> responseMap = BaseGlobalExceptionHandler.handlerMap(
        exception, 
        serverId, 
        new LocaleUtils().getLocale(exchange) // 根据请求头获取国际化语言
    );
    
    // 设置HTTP状态码
    Integer httpStatus = (Integer) responseMap.getOrDefault(
        BaseCode.HTTP_STATUS, 
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );
    resp.setStatusCode(HttpStatus.valueOf(httpStatus));
    
    // 序列化响应为JSON并写入返回
    String json = JSONUtil.toJsonStr(responseMap);
    DataBuffer buffer = resp.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
    return resp.writeWith(Mono.just(buffer));
  }

  /**
   * 处理异常
   * 只处理Sentinel的BlockException类型异常，其他异常继续传递
   *
   * @param exchange 当前请求上下文
   * @param ex 异常对象
   * @return 处理结果Mono
   */
  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    // 如果响应已经提交或者不是Sentinel的限流异常，直接传递异常
    if (exchange.getResponse().isCommitted() || !BlockException.isBlockException(ex)) {
      return Mono.error(ex);
    }
    // 处理限流请求，然后写入响应
    return handleBlockedRequest(exchange, ex).flatMap(response -> writeResponse(response, exchange, ex));
  }

  /**
   * 处理被Sentinel拦截的请求
   * 调用Sentinel网关回调管理器的阻塞处理器处理请求
   *
   * @param exchange 当前请求上下文
   * @param throwable 限流异常对象
   * @return 响应对象Mono
   */
  private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
    return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
  }

  /**
   * 上下文提供者，提供消息写入器和视图解析器
   */
  private final Supplier<Context> contextSupplier = () -> new Context() {

    @Override
    public List<HttpMessageWriter<?>> messageWriters() {
      return SentinelGatewayBlockExceptionHandler.this.messageWriters;
    }

    @Override
    public List<ViewResolver> viewResolvers() {
      return SentinelGatewayBlockExceptionHandler.this.viewResolvers;
    }
  };
}
