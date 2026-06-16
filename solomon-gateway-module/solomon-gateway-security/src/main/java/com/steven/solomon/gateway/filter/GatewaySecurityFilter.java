package com.steven.solomon.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.gateway.core.GatewayResponseWriter;
import com.steven.solomon.gateway.core.JwtTokenService;
import com.steven.solomon.gateway.core.TokenClaims;
import com.steven.solomon.gateway.properties.GatewaySecurityProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.gateway.spi.AnonymousPathProvider;
import com.steven.solomon.gateway.spi.GatewayAccessValidator;
import com.steven.solomon.gateway.spi.GatewayTenantValidator;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关安全核心过滤器（全局过滤器）。
 *
 * <p>整合鉴权（Authentication）、授权（Authorization）和租户校验，按固定顺序执行：</p>
 * <ol>
 *   <li><b>忽略路径</b>：健康检查/Swagger 等完全跳过，清理不可信头。</li>
 *   <li><b>匿名路径</b>：扫描到的 anonymous 接口跳过 Token 校验，清理头。</li>
 *   <li><b>公开租户路径</b>：登录等无 Token 但需校验租户的路径。</li>
 *   <li><b>Token 鉴权</b>：解析 JWT 得到身份，无 Token 返回 401，无效返回 401。</li>
 *   <li><b>租户校验</b>：调 {@link GatewayTenantValidator} 校验租户合法性。</li>
 *   <li><b>授权校验</b>：调 {@link GatewayAccessValidator} 校验接口权限。</li>
 *   <li><b>写可信头</b>：微服务模式写 X-Tenant-Code/X-User-Id，单机模式只存 attribute。</li>
 * </ol>
 *
 * <p><b>安全原则</b>：清理所有外部伪造的身份头（X-Tenant-Code 等），只写入网关权威计算的值。
 * SPI 接口未实现时默认拒绝（返回 403）。</p>
 *
 * @author steven
 */
public class GatewaySecurityFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerUtils.logger(GatewaySecurityFilter.class);

    /** 过滤器优先级，确保在路由转发前执行。 */
    public static final int ORDER = -200;

    /** exchange attribute 中存储令牌声明的键。 */
    public static final String CLAIMS_ATTRIBUTE = "gateway.token.claims";

    /** exchange attribute 中存储租户编码的键。 */
    public static final String TENANT_ATTRIBUTE = "gateway.tenant.code";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String ERR_TOKEN_REQUIRED = "TOKEN_REQUIRED";
    private static final String ERR_TOKEN_INVALID = "TOKEN_INVALID";
    private static final String ERR_ACCESS_DENIED = "ACCESS_DENIED";
    private static final String ERR_TENANT_INVALID = "TENANT_INVALID";

    private final JwtTokenService jwtTokenService;
    private final GatewayTenantProperties tenantProperties;
    private final GatewaySecurityProperties securityProperties;
    private final GatewayResponseWriter responseWriter;
    private final GatewayAccessValidator accessValidator;
    private final GatewayTenantValidator tenantValidator;
    private final AnonymousPathProvider anonymousPathProvider;

    /**
     * 全参构造，由 {@code GatewayAutoConfiguration} 注入。
     *
     * @param jwtTokenService      JWT 令牌服务
     * @param tenantProperties     租户配置
     * @param securityProperties   安全配置
     * @param responseWriter       错误响应写入器
     * @param accessValidator      授权校验器（客户实现，可为 null 表示默认拒绝）
     * @param tenantValidator      租户校验器（客户实现，可为 null 表示默认拒绝）
     * @param anonymousPathProvider 匿名路径提供者
     */
    public GatewaySecurityFilter(JwtTokenService jwtTokenService,
                                 GatewayTenantProperties tenantProperties,
                                 GatewaySecurityProperties securityProperties,
                                 GatewayResponseWriter responseWriter,
                                 GatewayAccessValidator accessValidator,
                                 GatewayTenantValidator tenantValidator,
                                 AnonymousPathProvider anonymousPathProvider) {
        this.jwtTokenService = jwtTokenService;
        this.tenantProperties = tenantProperties;
        this.securityProperties = securityProperties;
        this.responseWriter = responseWriter;
        this.accessValidator = accessValidator;
        this.tenantValidator = tenantValidator;
        this.anonymousPathProvider = anonymousPathProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. 忽略路径：健康检查/Swagger 等，清理不可信头后直接放行
        if (matchesAny(tenantProperties.getIgnoredPaths(), path)) {
            logger.debug("忽略路径放行: {}", path);
            return chain.filter(cleanUntrustedHeaders(exchange));
        }

        // 2. 匿名路径：扫描到的 anonymous 接口，清理头后放行
        List<String> anonymousPaths = anonymousPathProvider != null
                ? anonymousPathProvider.getAnonymousPaths() : List.of();
        if (matchesAny(anonymousPaths, path)) {
            logger.debug("匿名路径放行: {}", path);
            return chain.filter(cleanUntrustedHeaders(exchange));
        }

        // 3. 公开租户路径：登录等，无 Token 但需校验租户
        if (matchesAny(tenantProperties.getPublicTenantPaths(), path)) {
            return handlePublicTenantPath(exchange, chain);
        }

        // 4. Token 鉴权：解析 JWT
        String authorization = exchange.getRequest().getHeaders().getFirst(GatewayHeaders.AUTHORIZATION);
        String token = jwtTokenService.resolveBearerToken(authorization);
        if (StrUtil.isBlank(token)) {
            return responseWriter.writeError(exchange, HttpStatus.UNAUTHORIZED,
                    ERR_TOKEN_REQUIRED, "缺少访问令牌，请先登录");
        }
        TokenClaims claims = jwtTokenService.parseToken(token);
        if (claims == null) {
            return responseWriter.writeError(exchange, HttpStatus.UNAUTHORIZED,
                    ERR_TOKEN_INVALID, "访问令牌无效或已过期，请重新登录");
        }

        // 保存声明到 exchange，供灰度过滤器复用
        exchange.getAttributes().put(CLAIMS_ATTRIBUTE, claims);

        // 5. 授权校验：客户实现，默认拒绝
        final TokenClaims finalClaims = claims;
        if (accessValidator == null) {
            return responseWriter.writeError(exchange, HttpStatus.FORBIDDEN,
                    ERR_ACCESS_DENIED, "未配置授权校验器，请求被拒绝");
        }
        return accessValidator.validate(finalClaims, exchange)
                .flatMap(allowed -> {
                    if (Boolean.FALSE.equals(allowed)) {
                        return responseWriter.writeError(exchange, HttpStatus.FORBIDDEN,
                                ERR_ACCESS_DENIED, "无权访问该接口");
                    }
                    // 6. 写可信头，用新 exchange 继续链路（exchange 不可变，不能丢弃返回值）
                    ServerWebExchange securedExchange = writeTrustedHeaders(exchange, finalClaims);
                    return chain.filter(securedExchange);
                })
                .onErrorResume(e -> {
                    logger.error("授权校验异常, 用户={}, 路径={}", finalClaims.userId(), path, e);
                    return responseWriter.writeError(exchange, HttpStatus.FORBIDDEN,
                            ERR_ACCESS_DENIED, "授权校验失败，请求被拒绝");
                });
    }

    /**
     * 处理公开租户路径（登录等）：校验租户合法性后放行。
     */
    private Mono<Void> handlePublicTenantPath(ServerWebExchange exchange, GatewayFilterChain chain) {
        String tenantCode = exchange.getRequest().getHeaders().getFirst(GatewayHeaders.TENANT_CODE);
        // 校验租户编码格式（防注入）
        if (StrUtil.isNotBlank(tenantCode) && !jwtTokenService.isSafeIdentity(tenantCode.trim())) {
            return responseWriter.writeError(exchange, HttpStatus.FORBIDDEN,
                    ERR_TENANT_INVALID, "租户编码格式不合法");
        }
        tenantCode = StrUtil.trim(tenantCode);

        // 调租户校验器（客户实现，默认拒绝）
        if (tenantValidator == null) {
            return responseWriter.writeError(exchange, HttpStatus.FORBIDDEN,
                    ERR_TENANT_INVALID, "未配置租户校验器，请求被拒绝");
        }
        String finalTenantCode = tenantCode;
        return tenantValidator.validate(tenantCode, exchange)
                .flatMap(valid -> {
                    if (Boolean.FALSE.equals(valid)) {
                        return responseWriter.writeError(exchange, HttpStatus.FORBIDDEN,
                                ERR_TENANT_INVALID, "租户不存在或未启用");
                    }
                    // 公开路径先清理不可信头，再写入校验过的租户编码
                    ServerWebExchange cleaned = cleanUntrustedHeaders(exchange);
                    if (StrUtil.isNotBlank(finalTenantCode)) {
                        cleaned.getAttributes().put(TENANT_ATTRIBUTE, finalTenantCode);
                    }
                    if (securityProperties.shouldForwardTrustedHeaders() && StrUtil.isNotBlank(finalTenantCode)) {
                        ServerHttpRequest mutated = cleaned.getRequest().mutate()
                                .header(GatewayHeaders.TENANT_CODE, finalTenantCode)
                                .build();
                        return chain.filter(cleaned.mutate().request(mutated).build());
                    }
                    return chain.filter(cleaned);
                })
                .onErrorResume(e -> {
                    logger.error("租户校验异常, 租户={}", finalTenantCode, e);
                    return responseWriter.writeError(exchange, HttpStatus.FORBIDDEN,
                            ERR_TENANT_INVALID, "租户校验失败，请求被拒绝");
                });
    }

    /**
     * 写入网关权威计算的信任头，返回修改后的新 exchange。
     *
     * <p>先清理所有外部伪造的身份头，再写入网关计算出的可信值。
     * 微服务模式写入请求头供下游读取；单机模式只存 exchange attribute。
     * 返回的新 exchange 必须在调用方继续传递，不能丢弃（exchange 不可变）。</p>
     *
     * @param exchange 原始 exchange
     * @param claims   令牌声明
     * @return 写入可信头后的新 exchange
     */
    private ServerWebExchange writeTrustedHeaders(ServerWebExchange exchange, TokenClaims claims) {
        ServerWebExchange cleaned = cleanUntrustedHeaders(exchange);
        cleaned.getAttributes().put(TENANT_ATTRIBUTE, claims.tenantCode());
        if (securityProperties.shouldForwardTrustedHeaders()) {
            ServerHttpRequest mutated = cleaned.getRequest().mutate()
                    .header(GatewayHeaders.TENANT_CODE, claims.tenantCode())
                    .header(GatewayHeaders.USER_ID, claims.userId())
                    .build();
            return cleaned.mutate().request(mutated).build();
        }
        return cleaned;
    }

    /**
     * 清理外部伪造的身份头，返回修改后的新 exchange。
     *
     * <p>{@link ServerWebExchange} 是不可变对象，mutate().build() 返回新实例，
     * 原始 exchange 不受影响。调用方必须使用返回的新 exchange。</p>
     *
     * @param exchange 原始 exchange
     * @return 清理身份头后的新 exchange
     */
    private ServerWebExchange cleanUntrustedHeaders(ServerWebExchange exchange) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(GatewayHeaders.TENANT_CODE);
                    headers.remove(GatewayHeaders.USER_ID);
                    headers.remove(GatewayHeaders.TENANT_ID);
                    headers.remove(GatewayHeaders.TENANT_NAME);
                })
                .build();
        return exchange.mutate().request(mutated).build();
    }

    /**
     * 判断路径是否匹配任一模式。
     */
    private boolean matchesAny(List<String> patterns, String path) {
        if (CollUtil.isEmpty(patterns)) {
            return false;
        }
        return patterns.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
