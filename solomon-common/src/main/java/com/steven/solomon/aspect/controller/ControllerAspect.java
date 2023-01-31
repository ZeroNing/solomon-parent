package com.steven.solomon.aspect.controller;

import cn.hutool.core.date.StopWatch;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerAspect {

  private static final Logger logger = LoggerUtils.logger(ControllerAspect.class);

  @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || "
      + "@annotation(org.springframework.web.bind.annotation.GetMapping) || "
      + "@annotation(org.springframework.web.bind.annotation.PutMapping) ||"
      + "@annotation(org.springframework.web.bind.annotation.DeleteMapping) ||"
      + "@annotation(org.springframework.web.bind.annotation.RequestMapping) ||"
      + "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
  private void pointCutMethodService() {

  }

  @Around("pointCutMethodService()")
  public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {
    StopWatch stopWatch = new StopWatch();
    Object    obj       = null;
    try {
      stopWatch.start();
      obj = pjp.proceed();
    } catch (Throwable e) {
      throw e;
    } finally {
      saveLog(pjp,stopWatch);
    }
    return obj;
  }

  private void saveLog(ProceedingJoinPoint pjp, StopWatch stopWatch) {
    String proceedingJoinPoint = pjp.getSignature().toString();
    stopWatch.stop();
    Long   millisecond = stopWatch.getLastTaskTimeMillis();
    Double second      = Double.parseDouble(String.valueOf(millisecond)) / 1000;
    logger.info("调用controller方法:{},执行耗时:{}毫秒,耗时:{}秒", proceedingJoinPoint, millisecond, second);
  }
}
