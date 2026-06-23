package com.steven.solomon.cloud;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.utils.logger.LoggerUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;

/**
 * Propagates the current request context to downstream Feign calls.
 */
public class TenantHeaderInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerUtils.logger(TenantHeaderInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        setHeader(template, BaseCode.REQUEST_ID, ExceptionUtil.requestId.get());
        setHeader(template, BaseCode.TIMEZONE, RequestHeaderHolder.getTimeZone());
        setHeader(template, BaseCode.TENANT_CODE, RequestHeaderHolder.getTenantCode());
        setHeader(template, BaseCode.TENANT_ID, RequestHeaderHolder.getTenantId());
        setHeader(template, BaseCode.TENANT_NAME, RequestHeaderHolder.getTenantName());
        logger.debug("Feign propagated request context, requestId={}, tenantCode={}",
            ExceptionUtil.requestId.get(), RequestHeaderHolder.getTenantCode());
    }

    private void setHeader(RequestTemplate template, String name, String value) {
        if (StrUtil.isNotBlank(value)) {
            template.header(name, value);
        }
    }
}
