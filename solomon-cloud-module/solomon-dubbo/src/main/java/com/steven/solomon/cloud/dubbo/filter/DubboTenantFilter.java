package com.steven.solomon.cloud.dubbo.filter;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeader;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;

/**
 * Propagates request context through Dubbo attachments.
 */
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class DubboTenantFilter implements Filter {

    private static final Logger logger = LoggerUtils.logger(DubboTenantFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        boolean consumerSide = isConsumerSide();
        String previousRequestId = ExceptionUtil.requestId.get();
        RequestHeader previousHeader = RequestHeaderHolder.snapshot();

        if (consumerSide) {
            writeAttachment(invocation, BaseCode.REQUEST_ID, ExceptionUtil.requestId.get());
            writeAttachment(invocation, BaseCode.TIMEZONE, RequestHeaderHolder.getTimeZone());
            writeAttachment(invocation, BaseCode.TENANT_CODE, RequestHeaderHolder.getTenantCode());
            writeAttachment(invocation, BaseCode.TENANT_ID, RequestHeaderHolder.getTenantId());
            writeAttachment(invocation, BaseCode.TENANT_NAME, RequestHeaderHolder.getTenantName());
            logger.debug("Dubbo consumer propagated request context, requestId={}, tenantCode={}",
                ExceptionUtil.requestId.get(), RequestHeaderHolder.getTenantCode());
        } else {
            bindProviderContext(invocation);
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            if (!consumerSide) {
                restore(previousRequestId, previousHeader);
            }
        }
    }

    private void bindProviderContext(Invocation invocation) {
        String requestId = invocation.getAttachment(BaseCode.REQUEST_ID);
        if (StrUtil.isNotBlank(requestId)) {
            ExceptionUtil.requestId.set(requestId);
        }
        RequestHeaderHolder.setTimeZone(invocation.getAttachment(BaseCode.TIMEZONE));
        RequestHeaderHolder.setTenantCode(invocation.getAttachment(BaseCode.TENANT_CODE));
        RequestHeaderHolder.setTenantId(invocation.getAttachment(BaseCode.TENANT_ID));
        RequestHeaderHolder.setTenantName(invocation.getAttachment(BaseCode.TENANT_NAME));
        logger.debug("Dubbo provider bound request context, requestId={}, tenantCode={}",
            requestId, RequestHeaderHolder.getTenantCode());
    }

    private void writeAttachment(Invocation invocation, String key, String value) {
        if (StrUtil.isNotBlank(value)) {
            invocation.setAttachment(key, value);
        }
    }

    private boolean isConsumerSide() {
        try {
            return RpcContext.getServiceContext().isConsumerSide();
        } catch (RuntimeException ex) {
            logger.debug("Dubbo side is unknown, treating request as provider side", ex);
            return false;
        }
    }

    private void restore(String requestId, RequestHeader requestHeader) {
        if (requestId == null) {
            ExceptionUtil.requestId.remove();
        } else {
            ExceptionUtil.requestId.set(requestId);
        }
        RequestHeaderHolder.restore(requestHeader);
    }
}
