package com.steven.solomon.cache.aspect;

import com.steven.solomon.cache.annotation.RepeatRequestLimit;
import com.steven.solomon.cache.service.CacheService;
import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.exception.BaseException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 重复请求限制切面实现。
 *
 * <p>拦截 {@link com.steven.solomon.cache.annotation.RepeatRequestLimit} 注解，
 * 利用缓存实现请求幂等控制。基于请求方法、URL 和 token 生成指纹，
 * 在锁定时间内相同指纹的重复请求直接拒绝。</p>
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RepeatRequestLimitAspect {

  /** 缓存服务实例。 */
  private final CacheService cacheService;

  /** 请求指纹生成器。 */
  private final RequestFingerprintBuilder fingerprintBuilder;

  public RepeatRequestLimitAspect(
      CacheService cacheService,
      RequestFingerprintBuilder fingerprintBuilder) {
    this.cacheService = cacheService;
    this.fingerprintBuilder = fingerprintBuilder;
  }

  /**
   * 请求限制处理逻辑：生成请求指纹 → 尝试写入缓存（setIfAbsent）
   * → 写入失败表示重复请求，抛出异常。
   */
  @Around("@annotation(limit)")
  public Object limit(ProceedingJoinPoint joinPoint, RepeatRequestLimit limit) throws Throwable {
    String key = fingerprintBuilder.build(joinPoint.getSignature().toLongString());
    Boolean locked = cacheService.setIfAbsent(
        limit.group(), key, Boolean.TRUE, Math.max(1, limit.lockSeconds()));
    if (!Boolean.TRUE.equals(locked)) {
      throw new BaseException(BaseExceptionCode.ACCESS_EXCEPTION_CODE);
    }
    return joinPoint.proceed();
  }
}
