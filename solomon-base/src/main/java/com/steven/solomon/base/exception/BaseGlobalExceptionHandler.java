package com.steven.solomon.base.exception;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

public class BaseGlobalExceptionHandler {

    private static BaseExceptionVO handler(Throwable ex, String serverId, Locale locale) {
        String exceptionSimpleName = ex.getClass().getSimpleName();

        BaseExceptionVO baseExceptionVO = ExceptionUtil.getBaseExceptionVO(exceptionSimpleName,ex);
        String requestId = ExceptionUtil.requestId.get();
        requestId = ValidateUtils.getOrDefault(requestId,"0");
        baseExceptionVO.setServerId(serverId);
        baseExceptionVO.setLocale(locale);
        baseExceptionVO.setRequestId(requestId);
        baseExceptionVO.setMessage(ValidateUtils.isEmpty(baseExceptionVO.getMessage()) ? ValidateUtils.isEmpty(locale) ? I18nUtils
            .getErrorMessage(baseExceptionVO.getCode(),locale,baseExceptionVO.getArg()) : I18nUtils.getErrorMessage(baseExceptionVO.getCode(),baseExceptionVO.getArg()) : baseExceptionVO.getMessage());
        ExceptionUtil.requestId.remove();
        return baseExceptionVO;
    }

    public static Map<String,Object> handlerMap(Throwable ex, String serverId, Locale locale) {
        Map<String, Object> result = new HashMap<>(4);
        BaseExceptionVO baseExceptionVO = handler(ex, serverId, locale);
        result.put(BaseCode.HTTP_STATUS, ValidateUtils.getOrDefault(baseExceptionVO.getStatusCode(),HttpStatus.INTERNAL_SERVER_ERROR.value()));
        result.put(BaseCode.ERROR_CODE, baseExceptionVO.getCode());
        result.put(BaseCode.MESSAGE, baseExceptionVO.getMessage());
        result.put(BaseCode.SERVER_ID, serverId);
        result.put(BaseCode.REQUEST_ID, baseExceptionVO.getRequestId());
        return result;
    }
    public static Map<String,Object> handlerMap(Throwable ex, String serverId, Locale locale, HttpServletResponse response) {
        Map<String,Object> map = handlerMap(ex,serverId,locale);
        response.setStatus((Integer) map.getOrDefault(BaseCode.HTTP_STATUS,HttpStatus.INTERNAL_SERVER_ERROR.value()));
        return map;
    }
}
