# solomon-gateway-security

基于 Spring Cloud Gateway 与 Spring Security WebFlux 的多租户安全网关模块。

## 能力

- JWT 仅保存用户标识和租户编码，并校验签名、签发方和过期时间。
- 支持 `SINGLE` 单租户模式和 `MULTI` 多租户模式。
- 支持 `MICROSERVICE` 微服务模式和 `STANDALONE` 单机模式。
- 登录等公开路径可以在鉴权前校验租户，Swagger 和健康检查路径可以直接放行。
- 默认拒绝缺少业务校验实现的请求，角色与接口权限交由引入模块动态查询。
- 清理外部伪造的租户、用户和灰度请求头，只向下游写入网关计算出的可信值。
- 支持 I18n 错误响应、Swagger 路由资源聚合和稳定分桶灰度发布。

## 配置

```yaml
tenant:
  mode: SINGLE # SINGLE 或 MULTI
  default-code: default
  require-code-in-multi-mode: true

gateway:
  enabled: true
  security:
    mode: MICROSERVICE # MICROSERVICE 或 STANDALONE
    # forward-trusted-headers: true # 可选，默认按部署模式决定
  jwt:
    secret: 请配置至少32字节的安全密钥
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
  gray:
    enabled: false
    header-name: X-Gray-Version
    stable-version: stable
    candidate-version: gray
    candidate-weight: 10
```

`SINGLE` 模式下，请求或 Token 没有租户编码时自动使用 `tenant.default-code`。
`MULTI` 模式下，默认要求请求和 Token 明确携带租户编码。

`MICROSERVICE` 模式向下游写入可信的 `X-Tenant-Code` 和 `X-User-Id`。
`STANDALONE` 模式默认只在网关交换上下文中保留身份，不继续外发身份头。

`public-tenant-paths` 必须同时配置在 `ignored-paths` 中。租户编码和用户标识仅允许字母、
数字、`.`、`_`、`:`、`@`、`-`，最大长度为 `128`。

## 外部校验

引入模块必须声明 `GatewayAccessValidator` Bean，按租户、用户和路由校验角色与接口权限。
未声明时默认拒绝受保护接口。

```java
@Bean
public GatewayAccessValidator gatewayAccessValidator() {
  return (claims, exchange) -> permissionService.validate(
      claims.tenantCode(), claims.userId(),
      exchange.getRequest().getPath().value());
}
```

引入模块还必须声明 `GatewayTenantValidator` Bean，用于校验租户是否存在、是否启用。
未声明时默认拒绝租户预校验。

## 权限注解

接口使用 `@ApiPermission` 标记后，启动时会同步到 `ApiPermissionStore`。默认实现是单机内存目录；
微服务可以替换为数据库或配置中心实现。权限编码根据接口路径自动生成，例如：

```java
@Operation(summary = "订单详情")
@ApiPermission
@GetMapping("/api/core/orders/{id}")
public OrderVO detail(@PathVariable String id) {
  return orderService.detail(id);
}
```

该接口生成的权限编码为 `CORE:ORDERS:ID`，名称取 Swagger `@Operation` 的 `summary`。
匿名接口使用 `@ApiPermission(anonymous = true)`。微服务网关需要实现
`GatewayAnonymousPathProvider`，从权限中心加载已同步的匿名路径。

## 灰度发布

灰度过滤器始终删除客户端传入的 `gateway.gray.header-name`，避免客户端自行选择版本。
开启灰度后，网关按可信租户、用户和请求路径稳定分桶，并写入可信版本头。下游服务发现、
负载均衡或路由规则可基于该请求头选择实例。

## 下游租户切换

下游服务引入 `solomon-common` 后，请求入口会读取网关透传的 `X-Tenant-Code`，
并通过 `TenantRequestBinder` 在请求结束前绑定租户资源，结束后统一清理。

当前可以复用该生命周期的模块：

| 模块 | 切换方式 |
| --- | --- |
| `solomon-cache-redis` | 请求进入时切换 Redis 连接工厂 |
| MQTT 模块 | 消费消息时按消息租户绑定资源；单租户消息可以省略租户编码 |
| `solomon-cache-caffeine` | 使用 `TENANT_PREFIX` 隔离缓存键，不支持 `TENANT_SWITCH` |

数据库模块暂不在本轮改造范围内。
