package com.steven.solomon.cache.aspect;

import com.steven.solomon.cache.annotation.CacheRemove;
import com.steven.solomon.cache.annotation.CacheResult;
import com.steven.solomon.cache.model.CacheNullValue;
import com.steven.solomon.cache.service.CacheService;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 缓存注解切面实现。
 *
 * <p>拦截 {@link CacheResult} 和 {@link CacheRemove} 注解，
 * 分别实现方法结果缓存读取/写入和缓存删除功能。
 * 支持 SpEL 表达式解析、条件缓存、同步加载防击穿等特性。</p>
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public class CacheAspect {

  /** 缓存服务实例。 */
  private final CacheService cacheService;

  /** SpEL 表达式解析器。 */
  private final CacheExpressionResolver expressionResolver;

  /** 缓存 key 的同步锁池，用于 sync 模式下的缓存击穿防护。 */
  private final Map<String, Object> keyLocks = new ConcurrentHashMap<>();

  public CacheAspect(CacheService cacheService, CacheExpressionResolver expressionResolver) {
    this.cacheService = cacheService;
    this.expressionResolver = expressionResolver;
  }

  /**
   * {@link CacheResult} 注解处理切面。
   *
   * <p>先尝试从缓存获取结果，命中则直接返回；未命中则执行方法体，
   * 然后将结果写入缓存。支持通过 {@code sync} 参数启用同步加载模式。</p>
   */
  @Around("@annotation(cache)")
  public Object cache(ProceedingJoinPoint joinPoint, CacheResult cache) throws Throwable {
    if (!expressionResolver.resolveCondition(joinPoint, cache.condition(), null, true)) {
      return joinPoint.proceed();
    }
    String key = expressionResolver.resolveKey(joinPoint, cache.key());
    if (cache.sync()) {
      return syncLoad(joinPoint, cache, key);
    }
    return load(joinPoint, cache, key);
  }

  /**
   * 同步加载模式：对相同 key 使用 synchronized 块加锁，
   * 防止高并发下多个线程同时穿透到方法层。
   */
  private Object syncLoad(ProceedingJoinPoint joinPoint, CacheResult cache, String key)
      throws Throwable {
    String lockKey = cache.group() + ":" + key;
    Object lock = keyLocks.computeIfAbsent(lockKey, value -> new Object());
    try {
      synchronized (lock) {
        return load(joinPoint, cache, key);
      }
    } finally {
      keyLocks.remove(lockKey, lock);
    }
  }

  /**
   * 加载缓存逻辑：查缓存 → 未命中则执行方法 → 按 unless 条件决定是否写入缓存。
   */
  private Object load(ProceedingJoinPoint joinPoint, CacheResult cache, String key)
      throws Throwable {
    Object cachedValue = cacheService.get(cache.group(), key);
    if (cachedValue != null) {
      if (cachedValue instanceof CacheNullValue) {
        return null;
      }
      return cachedValue;
    }
    Object result = joinPoint.proceed();
    if (result == null && !cache.cacheNull()) {
      return null;
    }
    if (expressionResolver.resolveCondition(joinPoint, cache.unless(), result, false)) {
      return result;
    }
    Object cacheValue = result == null ? new CacheNullValue() : result;
    if (cache.expireSeconds() > 0) {
      cacheService.set(cache.group(), key, cacheValue, cache.expireSeconds());
    } else {
      cacheService.set(cache.group(), key, cacheValue);
    }
    return result;
  }

  /**
   * {@link CacheRemove} 注解处理切面。
   *
   * <p>支持 beforeInvocation 控制删除时机，支持按 key 列表、pattern 匹配
   * 或清空整个分组等方式删除缓存。</p>
   */
  @Around("@annotation(evict)")
  public Object evict(ProceedingJoinPoint joinPoint, CacheRemove evict) throws Throwable {
    if (evict.beforeInvocation()) {
      delete(joinPoint, evict, null);
      return joinPoint.proceed();
    }
    Object result = joinPoint.proceed();
    delete(joinPoint, evict, result);
    return result;
  }

  /**
   * 执行缓存删除操作，按配置依次判断：清空分组 → pattern 匹配 → key 列表达式。
   */
  private void delete(ProceedingJoinPoint joinPoint, CacheRemove evict, Object result) {
    if (evict.allEntries()) {
      cacheService.deleteGroup(evict.group());
      return;
    }
    if (hasText(evict.pattern())) {
      cacheService.deleteByPattern(
          evict.group(), expressionResolver.resolveKey(joinPoint, evict.pattern(), result));
      return;
    }
    if (evict.keys().length == 0) {
      cacheService.delete(evict.group(), expressionResolver.resolveKey(joinPoint, "", result));
      return;
    }
    String[] keys = Arrays.stream(evict.keys())
        .map(key -> expressionResolver.resolveKey(joinPoint, key, result))
        .filter(this::hasText)
        .toArray(String[]::new);
    if (keys.length > 0) {
      cacheService.delete(evict.group(), keys);
    }
  }

  /** 判断字符串是否为非空白。 */
  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
