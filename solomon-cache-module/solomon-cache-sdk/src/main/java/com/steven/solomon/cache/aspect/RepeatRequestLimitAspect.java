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
 * 重复请求限制切面。
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RepeatRequestLimitAspect {

  private final CacheService cacheService;

  private final RequestFingerprintBuilder fingerprintBuilder;

  public RepeatRequestLimitAspect(
      CacheService cacheService,
      RequestFingerprintBuilder fingerprintBuilder) {
    this.cacheService = cacheService;
    this.fingerprintBuilder = fingerprintBuilder;
  }

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
