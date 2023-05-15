package com.steven.solomon.exception.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;
import com.steven.solomon.base.exception.BaseGlobalExceptionHandler;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.LocaleUtils;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

public class SentinelGatewayBlockExceptionHandler implements WebExceptionHandler {

  private List<ViewResolver>         viewResolvers;
  private List<HttpMessageWriter<?>> messageWriters;

  @Value("${spring.application.id:default}")
  private String serverId;

  public SentinelGatewayBlockExceptionHandler(List<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer) {
    this.viewResolvers  = viewResolvers;
    this.messageWriters = serverCodecConfigurer.getWriters();
  }

  private Mono<Void> writeResponse(ServerResponse response, ServerWebExchange exchange, Throwable ex) {

    Exception exception = (Exception) ex;

    ServerHttpResponse resp = exchange.getResponse();
    resp.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
    String     json   = JackJsonUtils.formatJsonByFilter(
        BaseGlobalExceptionHandler.handlerMap(exception,null,serverId,new LocaleUtils().getLocale(exchange)));
    DataBuffer buffer = resp.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
    return resp.writeWith(Mono.just(buffer));
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (exchange.getResponse().isCommitted() || !BlockException.isBlockException(ex)) {
      return Mono.error(ex);
    }
    return handleBlockedRequest(exchange, ex).flatMap(response -> writeResponse(response, exchange,ex));
  }

  private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
    return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
  }

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
