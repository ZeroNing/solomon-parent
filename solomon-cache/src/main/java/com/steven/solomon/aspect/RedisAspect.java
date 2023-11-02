package com.steven.solomon.aspect;

import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.verification.ValidateUtils;
import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 租户切换的AOP实现类
 */
@Aspect
@Configuration
public class RedisAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private RedisTenantContext context;

  @Value("${spring.cache.mode:NORMAL}")
  private String mode;

  @Pointcut("@annotation(org.springframework.cache.annotation.CachePut) ||"
      + "@annotation(org.springframework.cache.annotation.CacheEvict) ||"
      + "@annotation(org.springframework.cache.annotation.Cacheable) || "
      + "execution(* com.steven.solomon.service.impl.RedisService.*(..)) ")
  void cutPoint() {}

  @Around("cutPoint()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    boolean isSwitch = ValidateUtils.equals(mode, SwitchModeEnum.SWITCH_DB.toString());
    try {
      if(isSwitch){
        logger.info("redis切换数据源,租户编码为:{}", RequestHeaderHolder.getTenantCode());
        String tenantCode = RequestHeaderHolder.getTenantCode();
        context.setFactory(tenantCode);
      }
      return point.proceed();
    } finally {
      if(isSwitch){
        context.removeFactory();
      }
    }
  }

}
