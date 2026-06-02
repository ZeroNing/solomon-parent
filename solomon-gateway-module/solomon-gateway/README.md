# solomon-gateway

基于 Spring Cloud Gateway 的响应式多租户网关模块。

## 能力

- 生成和解析 JWT，校验签名、签发方和过期时间。
- 从 Token 解析用户和租户编码，并向下游透传可信请求头。
- 登录等公开路径可按配置透传经过租户校验的租户编码，支持登录前切换租户数据源。
- Token 仅携带用户标识和租户编码，角色与权限由引入模块动态查询。
- 支持忽略登录、健康检查和 Swagger 等公开路径。
- 支持 I18n 错误响应和 Swagger 路由资源聚合。

## 配置

```yaml
gateway:
  enabled: true
  jwt:
    secret: 请配置足够长的安全密钥
    issuer: gateway
    expire-seconds: 7200
  tenant:
    enabled: true
    ignored-paths:
      - /auth/**
      - /actuator/health
      - /v3/api-docs/**
      - /swagger-ui/**
      - /swagger-resources/**
    public-tenant-paths:
      - /auth/**
  swagger:
    enabled: true
    api-docs-path: /v3/api-docs
```

## 外部权限校验

引入模块声明 `GatewayAccessValidator` Bean，即可校验租户状态、角色、权限和路由访问范围：

```java
@Bean
public GatewayAccessValidator gatewayAccessValidator() {
  return (claims, exchange) -> permissionService.validate(
      claims.tenantCode(), claims.userId(),
      exchange.getRequest().getPath().value());
}
```

引入模块还可以声明 `GatewayTenantValidator` Bean，校验登录前后的租户是否存在、是否启用。

## 下游动态数据源切换

下游服务引入 `solomon-common` 和 `solomon-datasource` 后，请求入口会读取网关透传的
`X-Tenant-Code`，自动绑定当前请求的数据源，结束后自动清理。其他租户资源模块可实现
`TenantRequestBinder`，复用同一套请求生命周期。

过滤器会清除外部传入的租户和用户请求头，只向下游写入 Token 中已校验的数据：

- `X-Tenant-Code`
- `X-User-Id`
