package com.steven.solomon.exception.handler;

import com.steven.solomon.base.exception.BaseGlobalExceptionHandler;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.LocaleUtils;
import java.util.Map;
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

/**
 * 网关全局异常处理器
 * 统一处理WebFlux网关层的所有异常，返回标准化JSON响应
 * 优先级最高，优先于其他异常处理器执行
 *
 * @author steven
 * @since 1.0.0
 */
@Order(Integer.MIN_VALUE) // 最高优先级，确保最先处理异常
@AutoConfigureBefore(WebFluxAutoConfiguration.class) // 在WebFlux自动配置前加载
@Configuration
public class GlobalExceptionConfiguration extends DefaultErrorWebExceptionHandler {

  /**
   * 视图解析器提供器，用于异常页面渲染
   */
  private final ObjectProvider<ViewResolver> viewResolvers;
  
  /**
   * HTTP消息编解码器配置，用于响应序列化
   */
  private final ServerCodecConfigurer serverCodecConfigurer;

  /**
   * 服务ID，用于异常响应标识是哪个服务抛出的异常
   */
  @Value("${spring.application.id:default}")
  private String serverId;

  /**
   * 构造方法注入必要依赖并初始化父类
   *
   * @param errorAttributes 错误属性管理器
   * @param webProperties Web配置属性
   * @param serverProperties 服务配置属性
   * @param applicationContext Spring应用上下文
   * @param viewResolvers 视图解析器提供器
   * @param serverCodecConfigurer HTTP消息编解码器配置
   */
  public GlobalExceptionConfiguration(ErrorAttributes errorAttributes, WebProperties webProperties,
      ServerProperties serverProperties, ApplicationContext applicationContext,
      ObjectProvider<ViewResolver> viewResolvers,
      ServerCodecConfigurer serverCodecConfigurer) {
    super(errorAttributes, webProperties.getResources(), serverProperties.getError(), applicationContext);
    this.viewResolvers = viewResolvers;
    this.serverCodecConfigurer = serverCodecConfigurer;
    
    // 初始化父类的视图解析器和消息编解码器
    setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
    setMessageReaders(serverCodecConfigurer.getReaders());
    setMessageWriters(serverCodecConfigurer.getWriters());
  }

  /**
   * 渲染异常响应
   * 将所有异常统一处理为标准化JSON格式返回
   *
   * @param request 当前请求对象
   * @return 异常响应Mono流
   */
  @Override
  protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    // 获取原始异常对象
    Throwable throwable = getError(request);
    
    // 使用全局异常处理器统一处理异常，构造标准化响应
    Map<String, Object> responseMap = BaseGlobalExceptionHandler.handlerMap(
        getException(throwable), 
        serverId, 
        new LocaleUtils().getLocale(request) // 根据请求头获取国际化语言
    );
    
    // 将响应对象序列化为JSON字符串
    String json = JackJsonUtils.formatJsonByFilter(responseMap);
    
    // 构建HTTP响应，设置状态码、Content-Type和响应体
    Integer httpStatus = (Integer) responseMap.getOrDefault(BaseCode.HTTP_STATUS, 500);
    return ServerResponse.status(httpStatus)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(json));
  }

  /**
   * 配置路由函数，拦截所有请求的异常
   *
   * @param errorAttributes 错误属性管理器
   * @return 路由函数
   */
  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    // 拦截所有请求，统一使用renderErrorResponse方法处理异常
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  /**
   * 将Throwable转换为Exception类型
   *
   * @param throwable 原始异常对象
   * @return 转换后的Exception对象
   */
  private Exception getException(Throwable throwable) {
    return (Exception) throwable;
  }
}
