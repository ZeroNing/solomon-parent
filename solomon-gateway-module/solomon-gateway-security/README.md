# solomon-gateway-security

提供一套可直接使用、可按业务扩展的多租户接口鉴权能力。

## 接口鉴权规则

- 标记 `@ApiPermission` 的接口会在应用启动时登记，并且强制校验 Token、租户和接口权限。
- 未标记注解的接口允许匿名访问。请求携带有效 Token 时，仍会写入 `AuthContext`。
- Token 与 `X-Tenant-Code` 请求头同时存在时，租户编码必须一致。
- 鉴权失败统一抛出 `BaseException`，错误文案复用项目原有 I18n。
- Swagger 文档会为受保护接口增加 Bearer JWT 标识和 `x-permission-code` 扩展字段。

## 配置

```yaml
gateway:
  security:
    enabled: true
    secret: 请配置足够长的安全密钥
    issuer: gateway-security
    expire-seconds: 7200
    swagger:
      enabled: true
```

## 使用注解

```java
@ApiPermission(value = "order:query", name = "查询订单")
@GetMapping("/orders/{id}")
public OrderVO query(@PathVariable Long id) {
  return orderService.query(id);
}
```

## 持久化与业务校验

默认使用内存保存扫描到的接口权限。业务系统可声明以下 Bean 覆盖默认实现：

- `PermissionRepository`：将接口权限写入数据库、Redis 或其他存储。
- `PermissionVerifier`：从数据库、缓存或 Token 权限集合判断用户是否有权访问接口。
- `TenantVerifier`：判断租户是否启用、是否过期以及是否允许访问当前服务。
