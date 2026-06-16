package com.steven.solomon.cloud.dubbo.filter;

import cn.hutool.core.util.StrUtil;

import com.steven.solomon.code.BaseCode;
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
 * Dubbo 多租户上下文透传 Filter。
 *
 * <p>Dubbo RPC 调用不经过 HTTP 头，因此需要通过 RPC 隐式参数（attachment）传递租户编码。
 * 本 Filter 同时在消费端和提供端生效（{@code @Activate(group={PROVIDER,CONSUMER})}）：</p>
 * <ul>
 *   <li>消费端：发起调用前，把当前线程的租户编码写入 attachment，随调用传递到提供端。</li>
 *   <li>提供端：收到调用后，从 attachment 读取租户编码并绑定到 {@link RequestHeaderHolder}，
 *       使提供端业务代码能按租户切换资源（数据源、缓存等）。调用结束后清理上下文，
 *       避免线程池复用导致租户串号。</li>
 * </ul>
 *
 * <p>与 Feign 的 {@code TenantHeaderInterceptor} 互补：无论业务用 Feign 还是 Dubbo，
 * 租户上下文都能正确透传，配合 Gateway 的 JWT 解析形成完整的多租户调用链。</p>
 *
 * @author steven
 */
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class DubboTenantFilter implements Filter {

    private static final Logger logger = LoggerUtils.logger(DubboTenantFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 通过 RpcContext 判断当前是消费端还是提供端
        boolean isConsumerSide = RpcContext.getServiceContext().isConsumerSide();

        if (isConsumerSide) {
            // 消费端：发起调用前，把当前租户编码写入 RPC 隐式参数，随调用透传到提供端
            String tenantCode = RequestHeaderHolder.getTenantCode();
            if (StrUtil.isNotBlank(tenantCode)) {
                invocation.setAttachment(BaseCode.TENANT_CODE, tenantCode);
                logger.debug("Dubbo 消费端写入租户编码到 attachment: {}", tenantCode);
            }
        } else {
            // 提供端：从 attachment 读取消费端透传的租户编码，绑定到当前线程上下文
            String tenantCode = invocation.getAttachment(BaseCode.TENANT_CODE);
            if (StrUtil.isNotBlank(tenantCode)) {
                RequestHeaderHolder.setTenantCode(tenantCode);
                logger.debug("Dubbo 提供端从 attachment 绑定租户编码: {}", tenantCode);
            }
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            // 提供端调用结束后清理租户上下文，避免线程池复用串号
            if (!isConsumerSide) {
                RequestHeaderHolder.remove();
            }
        }
    }
}
