package com.steven.solomon.exception;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.Locale;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

public class ExceptionUtil {

  private static final Logger logger = LoggerUtils.logger(ExceptionUtil.class);

  public static ThreadLocal<String> requestId = new ThreadLocal<>();

  public static AbstractExceptionHandler getExceptionHandler(String exceptionSimpleName){
    if (ValidateUtils.isEmpty(AbstractExceptionHandler.exceptionHandlerMap)) {
      AbstractExceptionHandler.exceptionHandlerMap = SpringUtil.getBeansOfType(
          AbstractExceptionHandler.class);
    }
    String exceptionHandlerName = exceptionSimpleName + AbstractExceptionHandler.HANDLER_NAME;
    return AbstractExceptionHandler.exceptionHandlerMap.get(exceptionHandlerName);
  }

  public static BaseExceptionVO getBaseExceptionVO(String exceptionSimpleName,Exception ex){
    BaseExceptionVO baseExceptionVO = new BaseExceptionVO(BaseExceptionCode.BASE_EXCEPTION_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());

    AbstractExceptionHandler abstractExceptionHandler = getExceptionHandler(exceptionSimpleName);
    if (ValidateUtils.isEmpty(abstractExceptionHandler)) {
      logger.info("BaseGlobalExceptionHandler 处理异常遇到未知异常抛出默认的系统异常 S9999,未知异常为:{}", exceptionSimpleName);
    } else {
      baseExceptionVO = abstractExceptionHandler.handleBaseException(ex);
    }
    return baseExceptionVO;
  }

  public static String getMessage(String exceptionSimpleName,Exception ex, Locale locale){
    BaseExceptionVO baseExceptionVO = getBaseExceptionVO(exceptionSimpleName, ex);
    return ValidateUtils.isEmpty(baseExceptionVO.getMessage()) ? I18nUtils
        .getErrorMessage(baseExceptionVO.getCode(),locale,baseExceptionVO.getArg()) : baseExceptionVO.getMessage();
  }
}
