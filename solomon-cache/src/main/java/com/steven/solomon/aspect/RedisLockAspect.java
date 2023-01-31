package com.steven.solomon.aspect;

import cn.hutool.crypto.digest.MD5;
import com.steven.solomon.annotation.Lock;
import com.steven.solomon.constant.code.BaseICacheCode;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.rsa.Md5Utils;
import com.steven.solomon.rsa.RSAUtils;
import com.steven.solomon.service.ICacheService;
import com.steven.solomon.verification.ValidateUtils;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Redis Lock的AOP实现类
 */
@Aspect
@Component
public class RedisLockAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  @Resource(name="redisService")
  private ICacheService iCacheService;

  /**
   * 切点，拦截带有@RedisLock的注解方法
   */
  @Pointcut("@annotation(com.steven.solomon.annotation.Lock)")
  void cutPoint() {}

  @Around("cutPoint()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    Signature       signature       = point.getSignature();
    MethodSignature methodSignature = (MethodSignature) signature;
    Method          targetMethod    = methodSignature.getMethod();
    //获取方法上的注解
    Lock lock = targetMethod.getAnnotation(Lock.class);

    if (ValidateUtils.isEmpty(lock)) {
      Object result = point.proceed();
      return result;
    }
    String key = new StringBuilder(signature.getDeclaringType().getName()).append(":").append(signature.getName()).append("_").append(Md5Utils.digest(JackJsonUtils.formatJsonByFilter(point.getArgs()))).toString();

    //通过setnx设置值，如果值不存在，则获得该锁
    boolean flag = iCacheService.lockSet(BaseICacheCode.REDIS_LOCK,key, 0, lock.expire());
    if (flag) {
      try {
        Object result = point.proceed();
        return result;
      } finally {
        iCacheService.deleteLock(BaseICacheCode.REDIS_LOCK,key);
      }
    } else {
      //查找错误处理器
      throw new BaseException(lock.errorCode());
    }

  }
}
