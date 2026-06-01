# solomon-gateway

`solomon-gateway` 提供网关层 JWT、租户解析、可信租户透传、Swagger 聚合与 I18n 工具。

## 基础配置

```yaml
gateway:
  sdk:
    enabled: true
  jwt:
    secret: 请使用足够长的安全密钥
    issuer: gateway
    expire-seconds: 7200
  tenant:
    enabled: true
    required: true
    header-name: X-Tenant-Code
    header-fallback-enabled: true
    validate-header: true
    reject-invalid-token: true
    ignored-paths:
      - /auth/**
      - /actuator/health
      - /v3/api-docs/**
      - /swagger-ui/**
      - /swagger-ui.html
```

`gateway.jwt.secret` 必须显式配置。租户编码优先从有效 Token 中读取；未携带 Token 时，可按配置从请求头读取。Token 与请求头同时存在时，两者必须一致。

## 租户校验扩展

默认允许已解析的租户访问网关。业务系统可声明 `GatewayTenantAccessValidator` Bean，集中校验租户启用状态、套餐有效期和路由权限。

```java
@Bean
public GatewayTenantAccessValidator gatewayTenantAccessValidator() {
  return (tenantCode, exchange) -> tenantService.canAccess(tenantCode, exchange.getRequest().getPath().value());
}
```

过滤器会移除外部传入的租户请求头，只向下游写入解析和校验后的可信租户编码。忽略路径会直接放行，同时移除外部租户请求头。
