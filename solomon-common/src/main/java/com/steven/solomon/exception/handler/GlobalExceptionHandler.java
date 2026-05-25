package com.steven.solomon.exception.handler;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.base.exception.BaseGlobalExceptionHandler;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Spring MVC 全局异常处理器。
 *
 * <p>该处理器负责兜底捕获 Controller 抛出的异常，并委托
 * {@link BaseGlobalExceptionHandler} 生成统一响应结构。异常日志会带上 requestId 和请求体，
 * 方便排查线上问题。</p>
 */
@AutoConfiguration
@RestControllerAdvice
@ConditionalOnProperty(prefix = "solomon.web.exception-handler", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class GlobalExceptionHandler {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Value("${spring.application.id:default}")
  private String serverId;

  /**
   * 捕获所有未被业务自行处理的异常。
   */
  @ExceptionHandler(value = {Exception.class})
  @ResponseBody
  public Map<String, Object> handleException(HttpServletRequest request,
      HttpServletResponse response, Exception ex, Locale locale) {
    String requestParameter = getRequestBody(request);
    logger.error(
        "GlobalExceptionHandler handle exception, requestId={}, requestBody={}",
        ExceptionUtil.requestId.get(), JSONUtil.toJsonStr(requestParameter), ex);
    return BaseGlobalExceptionHandler.handlerMap(ex, serverId, locale, response);
  }

  /**
   * 从缓存请求包装器中读取请求体。
   */
  private String getRequestBody(HttpServletRequest request) {
    if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
      return null;
    }
    Charset charset = getCharset(wrapper);
    return new String(wrapper.getContentAsByteArray(), charset);
  }

  /**
   * 获取请求编码；编码缺失或非法时回退到 UTF-8。
   */
  private Charset getCharset(HttpServletRequest request) {
    try {
      String encoding = request.getCharacterEncoding();
      return encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
    } catch (Exception ex) {
      return StandardCharsets.UTF_8;
    }
  }
}
