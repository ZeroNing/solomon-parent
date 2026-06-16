# solomon-gateway-security

基于 Spring Cloud Gateway + Spring Security WebFlux 的多租户安全网关 SDK。

## 定位

**底层安全网关 SDK**：只提供框架、抽象接口、默认实现和自动装配。用户认证、权限数据源、
租户校验等业务逻辑全部以 SPI 接口暴露，交由客户实现。

## 能力

- **JWT 生成/解密**：`JwtTokenService` 基于 Hutool HS256，校验签名、签发方、过期时间，密钥不少于 32 字节。
- **单/多租户**：复用 `TenantModeResolver`，SINGLE 模式空租户回退 default，MULTI 模式必须传。
- **鉴权（Authentication）**：`GatewaySecurityFilter` 解析 Token 得到身份。
- **授权（Authorization）**：SPI 接口 `GatewayAccessValidator`，客户实现后按租户+用户+路径校验。
- **租户校验**：SPI 接口 `GatewayTenantValidator`，登录等公开路径校验租户合法性。
- **灰度发布**：`GrayReleaseFilter` 按租户+用户+路径稳定分桶，写可信版本头。
- **权限扫描**：`@RequirePermission` 注解 + `PermissionScanner`，启动扫描 Controller 自动生成权限目录。
- **权限存储**：默认 `InMemoryPermissionStore`，可替换为 DB/配置中心实现。
- **Swagger 聚合**：`SwaggerResourceProvider` 按网关路由聚合文档。
- **可信头透传**：MICROSERVICE 模式写 X-Tenant-Code/X-User-Id；STANDALONE 只存上下文。
- **安全原则**：清理外部伪造身份头，只写网关权威值；SPI 未实现时默认拒绝（403）。

## SDK 提供 vs 客户实现

| 能力 | SDK 提供 | 客户实现（SPI） |
| --- | --- | --- |
| JWT 签发/解密 | ✅ `JwtTokenService` | — |
| 单/多租户模式 | ✅ `GatewaySecurityFilter` | — |
| 鉴权 | ✅ `GatewaySecurityFilter` | — |
| 授权 | 框架+默认拒绝 | `GatewayAccessValidator` |
| 租户合法性校验 | 框架+默认拒绝 | `GatewayTenantValidator` |
| 灰度发布 | ✅ `GrayReleaseFilter` | — |
| 权限扫描 | ✅ `PermissionScanner` | — |
| 权限存储 | ✅ 默认内存实现 | `PermissionStore`（可替换） |
| 匿名路径 | ✅ 从扫描结果收集 | `AnonymousPathProvider` |

## 配置

```yaml
gateway:
  enabled: true
  security:
    mode: MICROSERVICE          # MICROSERVICE 或 STANDALONE
  jwt:
    secret: 请配置至少32字节的安全密钥
    issuer: gateway
    expire-seconds: 7200
  tenant:
    enabled: true
    ignored-paths:              # 完全跳过鉴权
      - /actuator/health
      - /v3/api-docs/**
      - /swagger-ui/**
    public-tenant-paths:        # 无Token但需校验租户（必须同时在ignored-paths中）
      - /auth/login
  gray:
    enabled: false
    header-name: X-Gray-Version
    stable-version: stable
    candidate-version: gray
    candidate-weight: 10
  swagger:
    enabled: true
    api-docs-path: /v3/api-docs
```

## 客户实现示例

```java
// 授权校验器
@Component
public class MyAccessValidator implements GatewayAccessValidator {
    @Override
    public Mono<Boolean> validate(TokenClaims claims, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        return permissionService.hasPermission(claims.userId(), claims.tenantCode(), path);
    }
}

// 租户校验器
@Component
public class MyTenantValidator implements GatewayTenantValidator {
    @Override
    public Mono<Boolean> validate(String tenantCode, ServerWebExchange exchange) {
        return tenantService.isActive(tenantCode);
    }
}
```

## 权限注解

```java
@Operation(summary = "订单详情")
@RequirePermission                              // 自动生成权限码 CORE:ORDERS:ID
@GetMapping("/api/core/orders/{id}")
public OrderVO detail(@PathVariable String id) { ... }

@RequirePermission(anonymous = true)            // 匿名接口，无需Token
@PostMapping("/api/auth/login")
public TokenVO login(@RequestBody LoginDTO dto) { ... }
```

## 核心流程

1. 忽略路径 → 清理不可信头，放行
2. 匿名路径 → 清理头，放行
3. Token 鉴权 → 解析 JWT，无 Token/无效返回 401
4. 授权校验 → 调 `GatewayAccessValidator`，失败 403
5. 租户校验（公开路径）→ 调 `GatewayTenantValidator`，失败 403
6. 写可信头 → MICROSERVICE 写 X-Tenant-Code/X-User-Id
7. 灰度过滤器 → 写版本头
