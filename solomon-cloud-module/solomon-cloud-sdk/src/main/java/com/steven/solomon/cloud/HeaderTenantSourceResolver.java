package com.steven.solomon.cloud;

import cn.hutool.core.util.StrUtil;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.utils.logger.LoggerUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从 HTTP 请求头解析租户编码的默认实现。
 *
 * <p>标准微服务链路：客户端请求 → Gateway 鉴权 → Gateway 从 JWT Token 解析租户编码 →
 * 写入可信的 {@code X-Tenant-Code} 头 → 透传给下游服务 → 下游服务（引入 solomon-common）
 * 通过本解析器从头中读取租户编码。</p>
 *
 * <p>Gateway 会清理外部伪造的租户头，只写入自身鉴权计算出的可信值，
 * 因此下游服务可以安全地信任该头。非 HTTP 场景（如 MQ 消费）没有 Servlet 请求，
 * 本方法返回 {@code null}，由调用方（如 MQ 消费者）改从消息体 {@code BaseMq.tenantCode} 解析。</p>
 *
 * @author steven
 */
public class HeaderTenantSourceResolver implements TenantSourceResolver {

    private static final Logger logger = LoggerUtils.logger(HeaderTenantSourceResolver.class);

    @Override
    public String resolve() {
        // 从当前线程绑定的 Servlet 请求属性获取 HTTP 请求
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 非 HTTP 上下文（如定时任务、MQ 消费），无请求头可读
            logger.debug("当前非 HTTP 请求上下文，无法从请求头解析租户编码");
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String tenantCode = request.getHeader(BaseCode.TENANT_CODE);
        if (StrUtil.isBlank(tenantCode)) {
            logger.debug("请求头未携带租户编码 {}", BaseCode.TENANT_CODE);
            return null;
        }
        return tenantCode.trim();
    }
}
