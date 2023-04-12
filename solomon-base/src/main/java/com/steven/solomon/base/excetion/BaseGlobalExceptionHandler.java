package com.steven.solomon.base.excetion;

import com.steven.solomon.constant.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.utils.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;
import com.steven.solomon.vo.BaseExceptionVO;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

public class BaseGlobalExceptionHandler {

    public static BaseExceptionVO handler(Exception ex, Integer httpStatus, String serverId, Locale locale) {
        String exceptionSimpleName = ex.getClass().getSimpleName();

        BaseExceptionVO baseExceptionVO = ExceptionUtil.getBaseExceptionVO(exceptionSimpleName,ex);

        baseExceptionVO.setServerId(serverId);
        baseExceptionVO.setLocale(locale);
        baseExceptionVO.setMessage(ValidateUtils.isEmpty(baseExceptionVO.getMessage()) ? ValidateUtils.isEmpty(locale) ? I18nUtils
            .getErrorMessage(baseExceptionVO.getCode(),locale,baseExceptionVO.getArg()) : I18nUtils.getErrorMessage(baseExceptionVO.getCode(),baseExceptionVO.getArg()) : baseExceptionVO.getMessage());
        return baseExceptionVO;
    }

    public static Map<String,Object> handlerMap(Exception ex, Integer httpStatus, String serverId, Locale locale) {
        Map<String, Object> result = new HashMap<>(4);
        BaseExceptionVO baseExceptionVO = handler(ex, httpStatus, serverId, locale);
        result.put(BaseCode.HTTP_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.put(BaseCode.ERROR_CODE, baseExceptionVO.getCode());
        result.put(BaseCode.MESSAGE, baseExceptionVO.getMessage());
        result.put(BaseCode.SERVER_ID, serverId);
        return result;
    }
}
