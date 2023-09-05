package com.steven.solomon.exception.handler;

import com.steven.solomon.base.exception.BaseGlobalExceptionHandler;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.json.FastJsonUtils;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

  private Logger logger = LoggerUtils.logger(getClass());

  @Value("${spring.application.id:default}")
  private String serverId;

  /**
   * 捕获全局异常
   */
  @ExceptionHandler(value = {Throwable.class})
  @ResponseBody
  public Map<String, Object> handleException(HttpServletRequest request, HttpServletResponse response, Throwable ex, Locale locale) throws IOException {
    //获取异常名字
    String requestParameter = null;
    if(request instanceof ContentCachingRequestWrapper){
      ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
      requestParameter = StringUtils.toEncodedString(wrapper.getContentAsByteArray(), Charset.forName(wrapper.getCharacterEncoding()));
    }
    logger.info("GlobalExceptionHandler处理全局异常,请求ID:{},请求参数:{}当前异常是:", ExceptionUtil.requestId.get(), FastJsonUtils.conversionJsonArray(requestParameter), ex);
    BaseExceptionVO baseExceptionVO = BaseGlobalExceptionHandler.handler(ex, null, serverId, locale);
    response.setStatus(baseExceptionVO.getStatusCode());
    return handlerMap(baseExceptionVO);
  }

  public static Map<String, Object> handlerMap(BaseExceptionVO baseExceptionVO) {
    Map<String, Object> result = new HashMap<>(4);
    result.put(BaseCode.HTTP_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
    result.put(BaseCode.ERROR_CODE, baseExceptionVO.getCode());
    result.put(BaseCode.MESSAGE, baseExceptionVO.getMessage());
    result.put(BaseCode.SERVER_ID, baseExceptionVO.getServerId());
    result.put(BaseCode.REQUEST_ID, baseExceptionVO.getRequestId());
    return result;
  }
}
