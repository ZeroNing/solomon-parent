package com.steven.solomon.gateway.autoconfigure;

import com.steven.solomon.gateway.core.GatewayResponseWriter;
import com.steven.solomon.security.core.JwtTokenService;
import com.steven.solomon.gateway.filter.GatewaySecurityFilter;
import com.steven.solomon.gateway.filter.GrayReleaseFilter;
import com.steven.solomon.gateway.gray.GrayReleaseSelector;
import com.steven.solomon.security.core.permission.InMemoryPermissionStore;
import com.steven.solomon.security.core.permission.PermissionScanner;
import com.steven.solomon.security.core.permission.PermissionStore;
import com.steven.solomon.gateway.properties.GatewayGrayProperties;
import com.steven.solomon.security.core.JwtTokenProperties;
import com.steven.solomon.gateway.properties.GatewaySecurityProperties;
import com.steven.solomon.gateway.properties.GatewaySwaggerProperties;
import com.steven.solomon.gateway.properties.GatewayTenantProperties;
import com.steven.solomon.security.core.spi.AnonymousPathProvider;
import com.steven.solomon.gateway.spi.GatewayAccessValidator;
import com.steven.solomon.gateway.spi.GatewayTenantValidator;
import com.steven.solomon.gateway.swagger.SwaggerResourceProvider;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 网关安全 SDK 自动配置。
 *
 * <p>当 classpath 存在 Spring Cloud Gateway 且 {@code gateway.enabled=true}（默认开启）时生效。
 * 自动装配以下组件：</p>
 *
 * <ul>
 *   <li>{@link JwtTokenService}：JWT 令牌签发/解密</li>
 *   <li>{@link GatewaySecurityFilter}：鉴权/授权/租户核心过滤器</li>
 *   <li>{@link GrayReleaseFilter} + {@link GrayReleaseSelector}：灰度发布</li>
 *   <li>{@link PermissionScanner} + {@link PermissionStore}：权限扫描与存储</li>
 *   <li>{@link SwaggerResourceProvider}：Swagger 文档聚合</li>
 * </ul>
 *
 * <p>SPI 接口（{@link GatewayAccessValidator}/{@link GatewayTenantValidator}/{@link AnonymousPathProvider}）
 * 由客户实现并注册为 Bean，SDK 不提供默认实现（安全优先：未实现即拒绝）。
 * {@link PermissionStore} 默认提供 {@link InMemoryPermissionStore} 内存实现。</p>
 *
 * @author steven
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.gateway.filter.GlobalFilter")
@ConditionalOnProperty(name = "gateway.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
        GatewaySecurityProperties.class,
        JwtTokenProperties.class,
        GatewayTenantProperties.class,
        GatewayGrayProperties.class,
        GatewaySwaggerProperties.class
})
public class GatewayAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(GatewayAutoConfiguration.class);

    /**
     * JWT 令牌服务。
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtTokenService jwtTokenService(JwtTokenProperties properties) {
        return new JwtTokenService(properties);
    }

    /**
     * 错误响应写入器。
     */
    @Bean
    @ConditionalOnMissingBean
    public GatewayResponseWriter gatewayResponseWriter() {
        return new GatewayResponseWriter();
    }

    /**
     * 权限存储默认实现（内存）。
     *
     * <p>客户可声明自定义 {@link PermissionStore} Bean 覆盖。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public PermissionStore permissionStore() {
        logger.info("注册默认内存权限存储");
        return new InMemoryPermissionStore();
    }

    /**
     * 权限扫描器。
     */
    @Bean
    @ConditionalOnMissingBean
    public PermissionScanner permissionScanner(ApplicationContext applicationContext,
                                               PermissionStore permissionStore) {
        return new PermissionScanner(applicationContext, permissionStore);
    }

    /**
     * 匿名路径提供者：从权限存储读取匿名接口路径。
     *
     * <p>客户可声明自定义实现从权限中心加载匿名路径。</p>
     */
    @Bean
    @ConditionalOnMissingBean(AnonymousPathProvider.class)
    public AnonymousPathProvider defaultAnonymousPathProvider(PermissionStore permissionStore) {
        return () -> permissionStore.findAnonymousPaths();
    }

    /**
     * 网关安全核心过滤器。
     *
     * <p>SPI 接口（accessValidator/tenantValidator）由容器注入，客户未实现时为 null（默认拒绝）。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public GatewaySecurityFilter gatewaySecurityFilter(
            JwtTokenService jwtTokenService,
            GatewayTenantProperties tenantProperties,
            GatewaySecurityProperties securityProperties,
            GatewayResponseWriter responseWriter,
            org.springframework.beans.factory.ObjectProvider<GatewayAccessValidator> accessValidatorProvider,
            org.springframework.beans.factory.ObjectProvider<GatewayTenantValidator> tenantValidatorProvider,
            AnonymousPathProvider anonymousPathProvider) {
        tenantProperties.validate();
        return new GatewaySecurityFilter(jwtTokenService, tenantProperties, securityProperties,
                responseWriter, accessValidatorProvider.getIfAvailable(),
                tenantValidatorProvider.getIfAvailable(), anonymousPathProvider);
    }

    /**
     * 灰度版本选择器。
     */
    @Bean
    @ConditionalOnMissingBean
    public GrayReleaseSelector grayReleaseSelector(GatewayGrayProperties properties) {
        return new GrayReleaseSelector(properties);
    }

    /**
     * 灰度发布全局过滤器。
     */
    @Bean
    @ConditionalOnMissingBean
    public GrayReleaseFilter grayReleaseFilter(GatewayGrayProperties properties,
                                               GrayReleaseSelector selector) {
        return new GrayReleaseFilter(properties, selector);
    }

    /**
     * Swagger 文档资源提供者。
     */
    @Bean
    @ConditionalOnMissingBean
    public SwaggerResourceProvider swaggerResourceProvider(
            org.springframework.beans.factory.ObjectProvider<RouteDefinitionLocator> locatorProvider,
            GatewaySwaggerProperties properties) {
        return new SwaggerResourceProvider(locatorProvider.getIfAvailable(), properties);
    }

    /**
     * Spring Security 响应式安全过滤器链。
     *
     * <p>禁用 Spring Security 的默认认证授权拦截（CSRF、表单登录、默认 HTTP Basic），
     * 让网关的 {@link GatewaySecurityFilter} 全面接管鉴权与授权。
     * 所有请求都允许通过 Spring Security 层（permitAll），实际的安全校验由
     * {@code GatewaySecurityFilter} 在更高优先级执行。</p>
     *
     * <p>客户若需要自定义 Spring Security 行为（如添加额外的 WebFilter），
     * 可声明自己的 {@code SecurityWebFilterChain} Bean 覆盖本默认实现。</p>
     *
     * @param http ServerHttpSecurity 配置器
     * @return 安全过滤器链
     */
    @Bean
    @ConditionalOnMissingBean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnClass(
            name = "org.springframework.security.config.web.server.ServerHttpSecurity")
    public org.springframework.security.web.server.SecurityWebFilterChain securityWebFilterChain(
            org.springframework.security.config.web.server.ServerHttpSecurity http) {
        logger.info("配置 Spring Security 响应式过滤链: 禁用默认拦截，由 GatewaySecurityFilter 接管鉴权授权");
        http
                // 禁用 CSRF（网关是无状态的 API 网关，不渲染表单）
                .csrf(org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec::disable)
                // 禁用默认表单登录和 HTTP Basic（由 JWT Token 替代）
                .formLogin(org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec::disable)
                // 禁用默认登出页
                .logout(org.springframework.security.config.web.server.ServerHttpSecurity.LogoutSpec::disable)
                // 所有请求放行，实际鉴权授权由 GatewaySecurityFilter 执行
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
        return http.build();
    }
}
