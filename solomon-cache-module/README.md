# solomon-cache-module

`solomon-cache-module` 提供统一缓存能力，当前包含缓存基础 SDK 与 Redis 实现。模块默认支持不处理 key、增加前缀、多租户 key 前缀、多租户连接切换四种模式。

## 模块说明

| 模块 | 说明 |
| --- | --- |
| `solomon-cache-sdk` | 缓存基础接口、通用缓存注解、通用缓存切面、租户切换上下文、缓存 key 构建工具。 |
| `solomon-cache-redis` | Redis 缓存实现，提供单机/多租户连接工厂、`RedisTemplate` 与 `CacheService` 自动装配。 |
| `solomon-cache-caffeine` | Caffeine 本地缓存实现，适合单机应用、测试环境和轻量热点缓存。 |

## 引入依赖

```xml
<dependency>
    <groupId>com.steven</groupId>
    <artifactId>solomon-cache-redis</artifactId>
</dependency>
```

本地缓存：

```xml
<dependency>
    <groupId>com.steven</groupId>
    <artifactId>solomon-cache-caffeine</artifactId>
</dependency>
```

只需要基础工具时，可以单独引入：

```xml
<dependency>
    <groupId>com.steven</groupId>
    <artifactId>solomon-cache-sdk</artifactId>
</dependency>
```

## 配置示例

单机 Redis：

```yaml
cache:
  aspect:
    enabled: true
  repeat-request:
    enabled: true
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    key:
      mode: PREFIX
      global-prefix: app
```

多租户 key 前缀：

```yaml
cache:
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    key:
      mode: TENANT_PREFIX
      global-prefix: app
      fail-when-tenant-missing: true
```

多租户 Redis 连接切换：

```yaml
cache:
  redis:
    default-tenant: tenant-a
    key:
      mode: TENANT_SWITCH
      global-prefix: app
    tenants:
      tenant-a:
        host: 127.0.0.1
        port: 6379
        database: 0
      tenant-b:
        host: 127.0.0.1
        port: 6380
        database: 1
```

Caffeine 本地缓存：

```yaml
cache:
  caffeine:
    enabled: true
    maximum-size: 10000
    default-expire: 30m
    key:
      mode: PREFIX
      global-prefix: app
```

## 使用方式

```java
@Service
public class UserCacheService {

  private final CacheService cacheService;

  public UserCacheService(CacheService cacheService) {
    this.cacheService = cacheService;
  }

  public UserDTO cacheUser(UserDTO user) {
    return cacheService.set("user", user.getId(), user, 3600);
  }

  public UserDTO getUser(String userId) {
    return cacheService.get("user", userId);
  }
}
```

租户连接切换：

```java
redisCacheTenantSwitcher.run("tenant-a", () -> {
  cacheService.set("user", "10001", user, 3600);
});
```

单独使用 key 工具：

```java
String key = CacheKeys.prefix("app").build("user", "10001");
String tenantKey = CacheKeys.tenantPrefix("app", true).build("user", "10001");
String rawKey = CacheKeys.none().build("user", "10001");
String switchKey = CacheKeys.tenantSwitch("app").build("user", "10001");
```

注解缓存：

```java
@Service
public class UserQueryService {

  @CacheResult(
      group = "user",
      key = "#userId",
      expireSeconds = 3600,
      cacheNull = true,
      condition = "#userId != null",
      unless = "#result == null",
      sync = true
  )
  public UserDTO getUser(String userId) {
    return queryUserFromDatabase(userId);
  }

  @CacheRemove(group = "user", keys = {"#user.id"})
  public void updateUser(UserDTO user) {
    saveUser(user);
  }
}
```

删除整个分组或按 pattern 删除：

```java
@CacheRemove(group = "user", allEntries = true)
public void reloadAllUsers() {
  reload();
}

@CacheRemove(group = "user", pattern = "'profile:*'")
public void removeUserProfiles() {
  refreshProfiles();
}
```

重复请求限制：

```java
@PostMapping("/orders")
@RepeatRequestLimit(lockSeconds = 5)
public OrderVO createOrder(@RequestBody OrderCreateParam param) {
  return orderService.create(param);
}
```

`RepeatRequestLimit` 使用当前请求 `URL + token` 生成唯一指纹，并通过 Redis 原子 `setIfAbsent` 控制重复提交。token 读取顺序为 `Authorization`、`token`、`access_token`。

## 设计约定

- `NONE`：不处理 key，直接使用业务传入的 key。
- `PREFIX`：只使用全局前缀、分组和业务 key，适合单 Redis 实例。
- `TENANT_PREFIX`：在 key 中追加当前请求租户编码，适合共享 Redis 实例。
- `TENANT_SWITCH`：通过 `CacheTenantSwitcher` 切换租户资源，Redis 实现中会切换 Redis 连接，key 本身不写入租户。
- `@CacheResult`：方法返回值进入缓存，`key`、`condition`、`unless` 支持 SpEL 表达式。
- `@CacheRemove`：方法执行前后删除缓存，支持明确 key、整个 group、pattern 删除。
- `@RepeatRequestLimit`：按当前 URL 和 token 限制重复请求，默认锁定 3 秒。
- Caffeine 默认不启用，需要配置 `cache.caffeine.enabled=true`，避免和 Redis 实现同时抢占 `CacheService`。
- `CacheService` 只暴露常用缓存能力，复杂 Redis 命令建议直接注入 `redisTemplate`。
