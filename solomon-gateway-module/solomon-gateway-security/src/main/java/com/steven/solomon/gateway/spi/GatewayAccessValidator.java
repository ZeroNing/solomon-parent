package com.steven.solomon.gateway.spi;

import com.steven.solomon.gateway.core.TokenClaims;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关授权校验器（SPI 接口，由客户实现）。
 *
 * <p>网关在完成 Token 鉴权（确认身份合法）后，调用本接口校验当前用户是否有权访问
 * 目标接口。客户实现内可查询角色、权限等业务数据源（DB/缓存/配置中心）。</p>
 *
 * <p><b>默认拒绝原则</b>：客户未声明本接口 Bean 时，网关对所有受保护接口一律返回 403，
 * 确保安全。客户必须显式实现并注册本接口才能放行请求。</p>
 *
 * <pre>{@code
 * @Component
 * public class MyAccessValidator implements GatewayAccessValidator {
 *   public Mono<Boolean> validate(TokenClaims claims, ServerWebExchange exchange) {
 *     String path = exchange.getRequest().getPath().value();
 *     return permissionService.hasPermission(claims.userId(), claims.tenantCode(), path)
 *         .map(ok -> ok ? true : false);
 *   }
 * }
 * }</pre>
 *
 * @author steven
 */
public interface GatewayAccessValidator {

    /**
     * 校验用户是否有权访问当前请求的接口。
     *
     * @param claims   从 Token 解析出的身份声明（用户标识、租户编码）
     * @param exchange 当前请求交换对象，可获取路径、方法、头等
     * @return Mono&lt;true&gt; 表示允许访问；Mono&lt;false&gt; 或抛异常表示拒绝（403）
     */
    Mono<Boolean> validate(TokenClaims claims, ServerWebExchange exchange);
}
