package com.steven.solomon.security.autoconfigure;

import com.steven.solomon.security.core.JwtTokenService;
import com.steven.solomon.security.core.InMemoryTokenRevocationStore;
import com.steven.solomon.security.core.TokenRevocationStore;
import com.steven.solomon.security.filter.SecurityAuthenticationFilter;
import com.steven.solomon.security.core.spi.AnonymousPathProvider;
import com.steven.solomon.security.core.permission.InMemoryPermissionStore;
import com.steven.solomon.security.core.permission.PermissionScanner;
import com.steven.solomon.security.core.permission.PermissionStore;
import com.steven.solomon.security.properties.SecurityGrayProperties;
import com.steven.solomon.security.core.JwtTokenProperties;
import com.steven.solomon.security.properties.SecurityProperties;
import com.steven.solomon.security.spi.AccessValidator;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * 通用安全 SDK 自动配置。
 *
 * <p>当 classpath 存在 Servlet API 且 {@code security.enabled=true}（默认开启）时生效。
 * 自动装配 JWT 服务、安全过滤器、权限扫描器和权限存储。</p>
 *
 * <p>SPI 接口 {@link AccessValidator} 和 {@link AnonymousPathProvider} 由客户实现并注册为 Bean，
 * SDK 不提供默认实现（安全优先：未实现即拒绝）。
 * {@link PermissionStore} 默认提供 {@link InMemoryPermissionStore}。</p>
 *
 * @author steven
 */
@AutoConfiguration
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
@ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
        SecurityProperties.class,
        JwtTokenProperties.class,
        SecurityGrayProperties.class
})
public class SecurityAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(SecurityAutoConfiguration.class);

    /**
     * JWT 令牌服务。
     */
    @Bean
    @ConditionalOnMissingBean
    public TokenRevocationStore tokenRevocationStore() {
        return new InMemoryTokenRevocationStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenService jwtTokenService(
            JwtTokenProperties properties,
            TokenRevocationStore tokenRevocationStore) {
        return new JwtTokenService(properties, new com.steven.solomon.context.TenantModeResolver(
                new com.steven.solomon.context.TenantModeProperties()), tokenRevocationStore);
    }

    /**
     * 权限存储默认实现（内存）。
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
     */
    @Bean
    @ConditionalOnMissingBean(AnonymousPathProvider.class)
    public AnonymousPathProvider defaultAnonymousPathProvider(PermissionStore permissionStore) {
        return () -> permissionStore.findAnonymousPaths();
    }

    /**
     * 注册安全认证授权过滤器。
     *
     * <p>SPI 接口（accessValidator）由容器注入，客户未实现时为 null（默认拒绝）。
     * 过滤器优先级设为最高优先级，确保在业务过滤器之前执行。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<SecurityAuthenticationFilter> securityFilterRegistration(
            JwtTokenService jwtTokenService,
            SecurityProperties properties,
            org.springframework.beans.factory.ObjectProvider<AccessValidator> accessValidatorProvider,
            AnonymousPathProvider anonymousPathProvider) {
        SecurityAuthenticationFilter filter = new SecurityAuthenticationFilter(
                jwtTokenService, properties, accessValidatorProvider.getIfAvailable(), anonymousPathProvider);
        FilterRegistrationBean<SecurityAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        // 最高优先级，确保最先执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        registration.addUrlPatterns("/*");
        logger.info("注册安全认证授权过滤器");
        return registration;
    }

    /**
     * Spring Security Servlet 安全过滤器链。
     *
     * <p>禁用 Spring Security 的默认认证授权拦截（CSRF、表单登录、默认 HTTP Basic），
     * 让服务的 {@link SecurityAuthenticationFilter} 全面接管鉴权与授权。
     * 所有请求都允许通过 Spring Security 层（permitAll），实际的安全校验由
     * {@code SecurityAuthenticationFilter} 在更高优先级执行。</p>
     *
     * <p>客户若需要自定义 Spring Security 行为，可声明自己的
     * {@code SecurityFilterChain} Bean 覆盖本默认实现。</p>
     *
     * @param http HttpSecurity 配置器
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    @ConditionalOnMissingBean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnClass(
            name = "org.springframework.security.config.annotation.web.builders.HttpSecurity")
    public org.springframework.security.web.SecurityFilterChain securityFilterChain(
            org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        logger.info("配置 Spring Security Servlet 过滤链: 禁用默认拦截，由 SecurityAuthenticationFilter 接管鉴权授权");
        http
                // 禁用 CSRF（RESTful API 服务不需要 CSRF 保护）
                .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                // 禁用默认表单登录和 HTTP Basic（由 JWT Token 替代）
                .formLogin(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                .httpBasic(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                // 所有请求放行，实际鉴权授权由 SecurityAuthenticationFilter 执行
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
