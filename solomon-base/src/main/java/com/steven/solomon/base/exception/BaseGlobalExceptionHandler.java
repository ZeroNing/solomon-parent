package com.steven.solomon.base.exception;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class BaseGlobalExceptionHandler {

  private BaseGlobalExceptionHandler() {
  }

  private static BaseExceptionVO handler(Throwable ex, String serverId, Locale locale) {
    String exceptionSimpleName = ex.getClass().getSimpleName();
    BaseExceptionVO baseExceptionVO = ExceptionUtil.getBaseExceptionVO(exceptionSimpleName, ex);
    String requestId = ValidateUtils.getOrDefault(ExceptionUtil.requestId.get(), "0");

    baseExceptionVO.setServerId(serverId);
    baseExceptionVO.setLocale(locale);
    baseExceptionVO.setRequestId(requestId);
    if (ValidateUtils.isEmpty(baseExceptionVO.getMessage())) {
      baseExceptionVO.setMessage(resolveMessage(baseExceptionVO, locale));
    }
    ExceptionUtil.requestId.remove();
    return baseExceptionVO;
  }

  public static Map<String, Object> handlerMap(Throwable ex, String serverId, Locale locale) {
    Map<String, Object> result = new HashMap<>(5);
    BaseExceptionVO baseExceptionVO = handler(ex, serverId, locale);
    result.put(BaseCode.HTTP_STATUS,
        ValidateUtils.getOrDefault(baseExceptionVO.getStatusCode(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()));
    result.put(BaseCode.ERROR_CODE, baseExceptionVO.getCode());
    result.put(BaseCode.MESSAGE, baseExceptionVO.getMessage());
    result.put(BaseCode.SERVER_ID, serverId);
    result.put(BaseCode.REQUEST_ID, baseExceptionVO.getRequestId());
    return result;
  }

  public static Map<String, Object> handlerMap(Throwable ex, String serverId, Locale locale,
      HttpServletResponse response) {
    Map<String, Object> map = handlerMap(ex, serverId, locale);
    response.setStatus(
        (Integer) map.getOrDefault(BaseCode.HTTP_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    return map;
  }

  private static String resolveMessage(BaseExceptionVO baseExceptionVO, Locale locale) {
    if (ValidateUtils.isNotEmpty(locale)) {
      return I18nUtils.getErrorMessage(baseExceptionVO.getCode(), locale, baseExceptionVO.getArg());
    }
    return I18nUtils.getErrorMessage(baseExceptionVO.getCode(), baseExceptionVO.getArg());
  }
}
