# solomon-security

`solomon-security` 是独立的 Spring Security 多租户 JWT 鉴权模块，不依赖网关模块。

`security.jwt.secret` 没有默认值，接入模块时必须显式配置。

## 能力

- JWT 签发、签名校验、过期校验和签发者校验。
- 从 Token 恢复用户、租户和权限信息。
- 自动写入 Spring Security 上下文与 `RequestHeaderHolder`。
- 请求结束后自动清理上下文，避免线程池复用导致串租户。
- Token 租户与 `X-Tenant-Code` 请求头冲突时拒绝访问。
- 支持 `@PreAuthorize("hasAuthority('order:read')")` 方法级权限校验。
- 提供租户状态和 Token 状态扩展点。

## 配置

```yaml
security:
  enabled: true
  jwt:
    secret: replace-with-production-secret
    expire-seconds: 7200
    issuer: security
  tenant:
    required: true
    header-name: X-Tenant-Code
    validate-header: true
  web:
    permit-all:
      - /auth/**
      - /actuator/health
      - /v3/api-docs/**
      - /swagger-ui/**
```

## 签发 Token

```java
String token = securityTokenUtils.generateToken(new SecurityTokenClaims(
    userId,
    tenantCode,
    tenantId,
    tenantName,
    Set.of("order:read", "order:write"),
    Map.of()));
```

## 扩展租户校验

业务项目可以覆盖默认 Bean：

```java
@Bean
public TenantAccessValidator tenantAccessValidator() {
  return user -> tenantService.isEnabled(user.tenantCode())
      && tenantService.containsUser(user.tenantCode(), user.getUsername());
}
```

## 扩展 Token 状态校验

```java
@Bean
public TokenAccessValidator tokenAccessValidator() {
  return (token, user) -> !tokenBlacklistService.contains(token);
}
```
