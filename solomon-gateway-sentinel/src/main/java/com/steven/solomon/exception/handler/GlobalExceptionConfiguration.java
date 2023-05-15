package com.steven.solomon.exception.handler;

import com.steven.solomon.base.exception.BaseGlobalExceptionHandler;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.LocaleUtils;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

@Order(Integer.MIN_VALUE)
@AutoConfigureBefore(WebFluxAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
public class GlobalExceptionConfiguration extends DefaultErrorWebExceptionHandler {

  private final ObjectProvider<ViewResolver> viewResolvers;
  private final ServerCodecConfigurer        serverCodecConfigurer;

  @Value("${spring.application.id:default}")
  private String serverId;

  public GlobalExceptionConfiguration(ErrorAttributes errorAttributes, WebProperties.Resources resources,
      ServerProperties serverProperties, ApplicationContext applicationContext,
      ObjectProvider<ViewResolver> viewResolvers,
      ServerCodecConfigurer serverCodecConfigurer) {
    super(errorAttributes, resources, serverProperties.getError(), applicationContext);
    this.viewResolvers         = viewResolvers;
    this.serverCodecConfigurer = serverCodecConfigurer;
    setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
    setMessageReaders(serverCodecConfigurer.getReaders());
    setMessageWriters(serverCodecConfigurer.getWriters());
  }

  @Override
  protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    // 返回码
    int status;
    // 原始的异常信息可以用getError方法取得
    Throwable throwable = getError(request);
//    status = getHttpStatus(getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL)));
    status = HttpStatus.INTERNAL_SERVER_ERROR.value();
    String json = JackJsonUtils.formatJsonByFilter(
        BaseGlobalExceptionHandler.handlerMap(getException(throwable),status,serverId,new LocaleUtils().getLocale(request)));

    return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(json));
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  private Exception getException(Throwable throwable){
    return (Exception) throwable;
  }
}
