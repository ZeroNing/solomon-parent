package com.steven.solomon.cloud;

import cn.hutool.core.util.StrUtil;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.utils.logger.LoggerUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;

/**
 * Feign 跨服务调用租户上下文透传拦截器。
 *
 * <p>微服务场景下，下游服务发起远程调用时（通过 OpenFeign），需要把当前请求的租户编码
 * {@code X-Tenant-Code} 透传给被调服务，使被调服务能绑定正确的租户资源（数据源、缓存等）。</p>
 *
 * <p><b>租户编码来源链路</b>：Gateway 完成 JWT 鉴权后，从 Token 解析出租户编码并写入可信头
 * {@code X-Tenant-Code}，下游服务（引入 solomon-common）通过
 * {@link HeaderTenantSourceResolver} 从头中读取并绑定到 {@link RequestHeaderHolder}。
 * 本拦截器在 Feign 发起调用前，从 {@link RequestHeaderHolder} 读取当前租户编码写入请求头，
 * 保证租户上下文在整条服务调用链上不丢失。</p>
 *
 * <p><b>未来扩展</b>：当下游服务不经过 Gateway 直连调用、租户编码需从 Token 实时解析时，
 * 可实现自定义 {@link TenantSourceResolver} 替代 {@link HeaderTenantSourceResolver}，
 * 业务代码无需改动。</p>
 *
 * @author steven
 */
public class TenantHeaderInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerUtils.logger(TenantHeaderInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        // 从当前请求上下文读取租户编码（已由上游 Gateway 从 Token 解析并透传）
        String tenantCode = RequestHeaderHolder.getTenantCode();
        if (StrUtil.isNotBlank(tenantCode)) {
            template.header(BaseCode.TENANT_CODE, tenantCode);
            logger.debug("Feign 调用透传租户编码: {}", tenantCode);
        }
    }
}
