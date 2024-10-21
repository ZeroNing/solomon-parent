package com.steven.solomon.aspect;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.config.MongoTenantsContext;
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
 * mongodb 租户切换的AOP实现类
 */
@Aspect
@Configuration
public class MongoAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private MongoTenantsContext context;

  @Value("${spring.data.mongodb.mode}")
  private String mode;

  @Pointcut("execution(* org.springframework.data.mongodb.core.MongoTemplate.*(..)) ||"
      + "execution(* org.springframework.data.mongodb.core.MongoOperations.*(..)) ||"
      + "execution(* org.springframework.data.mongodb.repository.MongoRepository.*(..)) ||"
      + "execution(* org.springframework.data.mongodb.repository.support.SimpleMongoRepository.*(..)) ||"
      + "execution(* org.springframework.data.mongodb.repository.support.SpringDataMongodbQuery.*(..))")
  void cutPoint() {}

  @Around("cutPoint()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    boolean isSwitch = ValidateUtils.equals(mode, SwitchModeEnum.SWITCH_DB.toString());
    try {
      String tenantCode = isSwitch ? RequestHeaderHolder.getTenantCode() : BaseCode.DEFAULT;
      String msg = isSwitch ? "Mongodb切换数据源,租户编码为: " + tenantCode : "Mongodb不需要切换数据源,使用默认数据源";
      logger.info(msg);
      context.setFactory(tenantCode);
      return point.proceed();
    } finally {
      context.removeFactory();
    }
  }

}
