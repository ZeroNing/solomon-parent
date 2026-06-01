package com.steven.solomon.security.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.security.filter.TenantAuthenticationFilter;
import com.steven.solomon.security.handler.JsonAccessDeniedHandler;
import com.steven.solomon.security.handler.JsonAuthenticationEntryPoint;
import com.steven.solomon.security.properties.SecurityJwtProperties;
import com.steven.solomon.security.properties.SecurityTenantProperties;
import com.steven.solomon.security.properties.SecurityWebProperties;
import com.steven.solomon.security.service.TenantAccessValidator;
import com.steven.solomon.security.service.TokenAccessValidator;
import com.steven.solomon.security.utils.SecurityTokenUtils;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 多租户安全自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "security", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@EnableMethodSecurity
@EnableConfigurationProperties({
    SecurityJwtProperties.class,
    SecurityTenantProperties.class,
    SecurityWebProperties.class
})
public class SecurityAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public SecurityTokenUtils securityTokenUtils(SecurityJwtProperties properties) {
    return new SecurityTokenUtils(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public TenantAccessValidator tenantAccessValidator() {
    return user -> true;
  }

  @Bean
  @ConditionalOnMissingBean
  public TokenAccessValidator tokenAccessValidator() {
    return (token, user) -> true;
  }

  @Bean
  @ConditionalOnMissingBean
  public JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint() {
    return new JsonAuthenticationEntryPoint();
  }

  @Bean
  @ConditionalOnMissingBean
  public JsonAccessDeniedHandler jsonAccessDeniedHandler() {
    return new JsonAccessDeniedHandler();
  }

  @Bean
  @ConditionalOnMissingBean
  public TenantAuthenticationFilter tenantAuthenticationFilter(
      SecurityTokenUtils tokenUtils,
      SecurityTenantProperties tenantProperties,
      TenantAccessValidator tenantAccessValidator,
      TokenAccessValidator tokenAccessValidator,
      JsonAuthenticationEntryPoint authenticationEntryPoint) {
    return new TenantAuthenticationFilter(
        tokenUtils,
        tenantProperties,
        tenantAccessValidator,
        tokenAccessValidator,
        authenticationEntryPoint);
  }

  /**
   * 过滤器只交给 Spring Security 管理，避免 Servlet 容器重复注册。
   */
  @Bean
  public FilterRegistrationBean<TenantAuthenticationFilter> tenantAuthenticationFilterRegistration(
      TenantAuthenticationFilter authenticationFilter) {
    FilterRegistrationBean<TenantAuthenticationFilter> registration =
        new FilterRegistrationBean<>(authenticationFilter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  @ConditionalOnMissingBean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @ConditionalOnMissingBean(SecurityFilterChain.class)
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      SecurityWebProperties webProperties,
      TenantAuthenticationFilter authenticationFilter,
      JsonAuthenticationEntryPoint authenticationEntryPoint,
      JsonAccessDeniedHandler accessDeniedHandler) throws Exception {
    List<String> permitAllPaths =
        ObjectUtil.defaultIfNull(webProperties.getPermitAll(), List.of());
    String[] permitAll = permitAllPaths.stream()
        .filter(StrUtil::isNotBlank)
        .toArray(String[]::new);
    return http
        .csrf(csrf -> csrf.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .logout(logout -> logout.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(permitAll).permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
