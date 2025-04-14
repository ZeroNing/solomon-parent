package com.steven.solomon.aspect.controller;

import cn.hutool.core.date.StopWatch;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.utils.date.DateTimeUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Configuration
public class ControllerAspect {

  private static final Logger logger = LoggerUtils.logger(ControllerAspect.class);

  @Value("${i18n.language}")
  public Locale DEFAULT_LOCALE;

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
    stopWatch.start();
    Object             obj     = null;
    Throwable ex = null;
    String startTime = DateTimeUtils.getLocalDateTimeString(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    try {
      obj = pjp.proceed();
    } catch (Throwable e) {
      ex = e;
      throw e;
    } finally {
      saveLog(pjp,stopWatch,ex,ExceptionUtil.requestId.get(),obj,startTime);
    }
    return obj;
  }

  protected void saveLog(ProceedingJoinPoint pjp, StopWatch stopWatch,Throwable ex,String uuid,Object obj,String startTime) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String url = request.getRequestURL().toString();
//    String proceedingJoinPoint = pjp.getSignature().toString();

    //获取请求参数
    String targetMethodParams= JSONUtil.toJsonStr(pjp.getArgs());

    stopWatch.stop();
    Long   millisecond = stopWatch.getLastTaskTimeMillis();
    Double second      = Double.parseDouble(String.valueOf(millisecond)) / 1000;
    StringBuilder sb = new StringBuilder();
    sb.append("===========================================").append(System.lineSeparator());
    sb.append("请求时间:").append(startTime).append(System.lineSeparator());
    sb.append("请求ID:").append(uuid).append(System.lineSeparator());
    sb.append("请求URL:").append(url).append(System.lineSeparator());
    sb.append("请求参数:").append(targetMethodParams).append(System.lineSeparator());
//    sb.append("调用controller方法::"+proceedingJoinPoint+System.lineSeparator());
    sb.append("执行耗时:").append(millisecond).append("毫秒").append(System.lineSeparator());
    sb.append("执行耗时:").append(second).append("秒").append(System.lineSeparator());
    sb.append("响应数据:").append(JSONUtil.toJsonStr(obj)).append(System.lineSeparator());
    if(ValidateUtils.isNotEmpty(ex)){
      String message = ExceptionUtil.getMessage(ex.getClass().getSimpleName(),ex,ValidateUtils.isNotEmpty(request.getLocale()) ? request.getLocale() : DEFAULT_LOCALE);
      sb.append("异常为:").append(message).append(System.lineSeparator());
    }
    sb.append("请求结束时间:").append(DateTimeUtils.getLocalDateTimeString(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))).append(System.lineSeparator());
    sb.append("===========================================");
    logger.info("{}{}", System.lineSeparator(),sb);

  }
}
