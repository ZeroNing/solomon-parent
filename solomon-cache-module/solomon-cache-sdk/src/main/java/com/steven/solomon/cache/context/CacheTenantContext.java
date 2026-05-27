package com.steven.solomon.cache.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 缓存多租户资源上下文。
 *
 * @param <R> 租户资源类型，例如 RedisConnectionFactory。
 */
public class CacheTenantContext<R> {

  private final ThreadLocal<String> currentTenant = new ThreadLocal<>();

  private final ThreadLocal<R> currentResource = new ThreadLocal<>();

  private final Map<String, R> resourceMap = new ConcurrentHashMap<>();

  public R getResource() {
    return currentResource.get();
  }

  public String getCurrentTenant() {
    return currentTenant.get();
  }

  public R getResource(String tenantCode) {
    return resourceMap.get(tenantCode);
  }

  public Map<String, R> getResourceMap() {
    return Map.copyOf(resourceMap);
  }

  public void register(String tenantCode, R resource) {
    requireText(tenantCode, "tenantCode 不能为空");
    if (resource == null) {
      throw new IllegalArgumentException("resource 不能为空");
    }
    resourceMap.put(tenantCode, resource);
  }

  public R unregister(String tenantCode) {
    return resourceMap.remove(tenantCode);
  }

  public void switchTo(String tenantCode) {
    R resource = resourceMap.get(tenantCode);
    if (resource == null) {
      throw new IllegalStateException("未找到租户缓存资源: " + tenantCode);
    }
    currentTenant.set(tenantCode);
    currentResource.set(resource);
  }

  public void clear() {
    currentTenant.remove();
    currentResource.remove();
  }

  public void run(String tenantCode, Runnable task) {
    execute(tenantCode, () -> {
      task.run();
      return null;
    });
  }

  public <T> T execute(String tenantCode, Supplier<T> supplier) {
    try {
      switchTo(tenantCode);
      return supplier.get();
    } finally {
      clear();
    }
  }

  private void requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
