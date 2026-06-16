package com.steven.solomon.security.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.security.core.JwtTokenService;
import com.steven.solomon.security.core.TokenClaims;
import com.steven.solomon.security.properties.SecurityProperties;
import com.steven.solomon.security.spi.AccessValidator;
import com.steven.solomon.security.spi.AnonymousPathProvider;
import com.steven.solomon.utils.logger.LoggerUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 安全认证授权过滤器（Servlet MVC 版）。
 *
 * <p>基于 {@link OncePerRequestFilter}，每个请求只执行一次。处理流程：</p>
 * <ol>
 *   <li><b>忽略路径</b>：健康检查/Swagger 等直接放行。</li>
 *   <li><b>匿名路径</b>：扫描到的 anonymous 接口放行。</li>
 *   <li><b>Token 鉴权</b>：解析 Authorization 头的 Bearer Token，无 Token 返回 401，无效返回 401。</li>
 *   <li><b>写身份到请求属性</b>：将 {@link TokenClaims} 存入 request 属性，供下游使用。</li>
 *   <li><b>授权校验</b>：调 {@link AccessValidator}（客户实现），失败返回 403。</li>
 *   <li><b>绑定租户上下文</b>：将租户编码写入 {@code X-Tenant-Code} 请求属性，
 *       供 {@code TenantRequestBinder} 按租户切换数据源/缓存等资源。</li>
 * </ol>
 *
 * <p><b>默认拒绝原则</b>：客户未实现 {@link AccessValidator} 时，受保护接口一律 403。</p>
 *
 * <p>本过滤器面向普通 Spring Boot Servlet MVC 服务（非网关）。
 * 网关场景请使用 {@code solomon-gateway-security} 的响应式 {@code GatewaySecurityFilter}。</p>
 *
 * @author steven
 */
public class SecurityAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerUtils.logger(SecurityAuthenticationFilter.class);

    /** request 属性中存储令牌声明的键。 */
    public static final String CLAIMS_ATTRIBUTE = "security.token.claims";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtTokenService jwtTokenService;
    private final SecurityProperties properties;
    private final AccessValidator accessValidator;
    private final AnonymousPathProvider anonymousPathProvider;

    public SecurityAuthenticationFilter(JwtTokenService jwtTokenService,
                                        SecurityProperties properties,
                                        AccessValidator accessValidator,
                                        AnonymousPathProvider anonymousPathProvider) {
        this.jwtTokenService = jwtTokenService;
        this.properties = properties;
        this.accessValidator = accessValidator;
        this.anonymousPathProvider = anonymousPathProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // 1. 忽略路径：健康检查/Swagger 等
        if (matchesAny(properties.getIgnoredPaths(), path)) {
            logger.debug("忽略路径放行: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 匿名路径：扫描到的 anonymous 接口
        List<String> anonymousPaths = anonymousPathProvider != null
                ? anonymousPathProvider.getAnonymousPaths() : List.of();
        if (matchesAny(anonymousPaths, path)) {
            logger.debug("匿名路径放行: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Token 鉴权：从 Authorization 头提取 Bearer Token
        String token = jwtTokenService.resolveBearerToken(request.getHeader("Authorization"));
        if (StrUtil.isBlank(token)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "TOKEN_REQUIRED", "缺少访问令牌，请先登录");
            return;
        }
        TokenClaims claims = jwtTokenService.parseToken(token);
        if (claims == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "访问令牌无效或已过期，请重新登录");
            return;
        }

        // 4. 写身份到请求属性，供下游和租户绑定使用
        request.setAttribute(CLAIMS_ATTRIBUTE, claims);
        request.setAttribute(BaseCode.TENANT_CODE, claims.tenantCode());

        // 5. 授权校验（客户实现，默认拒绝）
        if (accessValidator == null) {
            writeError(response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "未配置授权校验器，请求被拒绝");
            return;
        }
        try {
            if (!accessValidator.validate(claims, request)) {
                writeError(response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "无权访问该接口");
                return;
            }
        } catch (Exception e) {
            logger.error("授权校验异常, 用户={}, 路径={}", claims.userId(), path, e);
            writeError(response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "授权校验失败，请求被拒绝");
            return;
        }

        // 6. 放行，下游可通过 request 属性获取身份和租户
        filterChain.doFilter(request, response);
    }

    /**
     * 写入 JSON 错误响应。
     */
    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        logger.warn("安全过滤拒绝请求: 状态={}, 错误码={}, 提示={}", status.value(), code, message);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = "{\"errorCode\":\"" + code + "\",\"message\":\"" + message + "\",\"status\":" + status.value() + "}";
        response.getWriter().write(json);
    }

    private boolean matchesAny(List<String> patterns, String path) {
        if (CollUtil.isEmpty(patterns)) {
            return false;
        }
        return patterns.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }
}
