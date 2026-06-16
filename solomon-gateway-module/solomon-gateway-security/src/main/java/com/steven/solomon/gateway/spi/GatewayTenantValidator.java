package com.steven.solomon.gateway.spi;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关租户合法性校验器（SPI 接口，由客户实现）。
 *
 * <p>在登录等公开路径上，网关需要在没有 Token 的情况下校验租户编码是否合法
 * （存在、已启用、未过期）。客户实现内可查询租户注册表（DB/配置中心）。</p>
 *
 * <p><b>默认拒绝原则</b>：客户未声明本接口 Bean 时，公开租户路径的请求一律返回 403。
 * 客户必须显式实现并注册本接口才能放行公开路径的租户请求。</p>
 *
 * @author steven
 */
public interface GatewayTenantValidator {

    /**
     * 校验租户编码是否合法。
     *
     * @param tenantCode 租户编码
     * @param exchange   当前请求交换对象
     * @return Mono&lt;true&gt; 表示租户合法；Mono&lt;false&gt; 或抛异常表示拒绝（403）
     */
    Mono<Boolean> validate(String tenantCode, ServerWebExchange exchange);
}
