package com.steven.solomon.aspect;

import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.enums.CacheModeEnum;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.holder.HeardHolder;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.utils.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;
import javax.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redis 租户切换的AOP实现类
 */
@Aspect
@Component
public class RedisAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private RedisTenantContext context;

  @Value("${cache.mode}")
  private String mode;

  @Pointcut("@annotation(org.springframework.cache.annotation.CachePut) ||"
      + "@annotation(org.springframework.cache.annotation.CacheEvict) ||"
      + "@annotation(org.springframework.cache.annotation.Cacheable) || "
      + "execution(* com.steven.solomon.service.impl.RedisService.*(..)) ")
  void cutPoint() {}

  @Around("cutPoint()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    boolean isSwitch = ValidateUtils.equals(mode, CacheModeEnum.SWITCH_DB.toString());
    try {
      if(isSwitch){
        logger.info("redis切换数据源,租户编码为:{}",HeardHolder.getTenantCode());
        String tenantId = HeardHolder.getTenantCode();
        if(ValidateUtils.isNotEmpty(tenantId)){
          context.setFactory(tenantId);
          Object result = point.proceed();
          return result;
        } else {
          throw new BaseException(I18nUtils.getErrorMessage(BaseExceptionCode.FAILED_TO_SWITCH_DATA_SOURCE));
        }
      } else {
        Object result = point.proceed();
        return result;
      }
    } finally {
      if(isSwitch){
        context.removeFactory();
      }
    }
  }

}
