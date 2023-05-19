package com.steven.solomon.aspect;

import com.steven.solomon.config.MongoTenantsContext;
import com.steven.solomon.holder.HeardHolder;
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
import org.springframework.stereotype.Component;

/**
 * Redis 租户切换的AOP实现类
 */
@Aspect
@Configuration(proxyBeanMethods = false)
public class MongoAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private MongoTenantsContext context;

  @Value("${spring.data.mongodb.mode}")
  private String mode;

  @Pointcut("execution(* org.springframework.data.mongodb.core.MongoTemplate.*(..)) ||"
      + "execution(* org.springframework.data.mongodb.core.MongoOperations.*(..)) ")
  void cutPoint() {}

  @Around("cutPoint()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    boolean isSwitch = ValidateUtils.equals(mode, SwitchModeEnum.SWITCH_DB.toString());
    try {
      if(isSwitch){
        logger.info("mongo切换数据源,租户编码为:{}",HeardHolder.getTenantCode());
        String tenantCode = HeardHolder.getTenantCode();
        context.setFactory(tenantCode);
      }
      Object result = point.proceed();
      return result;
    } finally {
      if(isSwitch){
        context.removeFactory();
      }
    }
  }

}
