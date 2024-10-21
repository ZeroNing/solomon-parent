package com.steven.solomon.aspect;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

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
      + "execution(* com.steven.solomon.service.impl.RedisService.*(..)) || "
      + "execution(* org.springframework.data.repository.Repository.*(..)) ||"
      + "execution(* org.springframework.data.repository.CrudRepository.*(..)) ||"
      + "execution(* org.springframework.data.repository.PagingAndSortingRepository.*(..))")
  void cutPoint() {}

  @Around("cutPoint()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    boolean isSwitch = ValidateUtils.equals(mode, SwitchModeEnum.SWITCH_DB.toString());
    String tenantCode = isSwitch ? RequestHeaderHolder.getTenantCode() : BaseCode.DEFAULT;
    String msg = isSwitch ? "Redis切换数据源,租户编码为: " + tenantCode : "Redis不需要切换数据源,使用默认数据源";
    logger.info(msg);
    context.setFactory(tenantCode);
    try {
      return point.proceed();
    } finally {
      context.removeFactory();
    }
  }

}
