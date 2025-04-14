package com.steven.solomon.exception.handler;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.base.exception.BaseGlobalExceptionHandler;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * 全局异常处理类
 *
 * @author huangweihua
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Value("${spring.application.id:default}")
  private String serverId;

  /**
   * 捕获全局异常
   */
  @ExceptionHandler(value = {Throwable.class})
  @ResponseBody
  public Map<String, Object> handleException(HttpServletRequest request, HttpServletResponse response, Throwable ex, Locale locale) {
    //获取异常名字
    String requestParameter = null;
    if(request instanceof ContentCachingRequestWrapper){
      ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
      requestParameter = new String(wrapper.getContentAsByteArray(), Charset.forName(wrapper.getCharacterEncoding()));
    }
    logger.error("GlobalExceptionHandler处理全局异常,请求ID:{},请求参数:{}当前异常是:", ExceptionUtil.requestId.get(), JSONUtil.toJsonStr(requestParameter), ex);
    return BaseGlobalExceptionHandler.handlerMap(ex,serverId,locale,response);
  }

}
