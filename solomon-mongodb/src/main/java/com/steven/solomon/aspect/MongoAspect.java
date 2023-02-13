package com.steven.solomon.aspect;

import com.steven.solomon.config.MongoTenantsContext;
import com.steven.solomon.constant.code.BaseExceptionCode;
import com.steven.solomon.enums.MongoModeEnum;
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
public class MongoAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private MongoTenantsContext context;

  @Value("${mongo.mode}")
  private String mode;

  @Pointcut("execution(* org.springframework.data.mongodb.core.MongoTemplate.*(..)) ||"
      + "execution(* org.springframework.data.mongodb.core.MongoOperations.*(..))")
  void cutPoint() {}

  @Around("cutPoint()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    boolean isSwitch = ValidateUtils.equals(mode, MongoModeEnum.SWITCH_DB.toString());
    try {
      if(isSwitch){
        logger.info("mongo,租户id为:{}",HeardHolder.getTenantId());
        String tenantId = HeardHolder.getTenantId();
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
