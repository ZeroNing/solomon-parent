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
 * 缓存注解切面。
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public class CacheAspect {

  private final CacheService cacheService;

  private final CacheExpressionResolver expressionResolver;

  private final Map<String, Object> keyLocks = new ConcurrentHashMap<>();

  public CacheAspect(CacheService cacheService, CacheExpressionResolver expressionResolver) {
    this.cacheService = cacheService;
    this.expressionResolver = expressionResolver;
  }

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

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
