package com.steven.solomon.aspect.controller;

import cn.hutool.core.date.StopWatch;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.date.DateTimeUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Configuration(proxyBeanMethods = false)
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
    Exception ex = null;
    String startTime = DateTimeUtils.getLocalDateTimeString(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    try {
      obj = pjp.proceed();
    } catch (Exception e) {
      ex = e;
      throw e;
    } finally {
      saveLog(pjp,stopWatch,ex,ExceptionUtil.requestId.get(),obj,startTime);
    }
    return obj;
  }

  private void saveLog(ProceedingJoinPoint pjp, StopWatch stopWatch,Exception ex,String uuid,Object obj,String startTime) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String url = request.getRequestURL().toString();
//    String proceedingJoinPoint = pjp.getSignature().toString();

    //获取请求参数
    String targetMethodParams= JackJsonUtils.formatJsonByFilter(pjp.getArgs());

    stopWatch.stop();
    Long   millisecond = stopWatch.getLastTaskTimeMillis();
    Double second      = Double.parseDouble(String.valueOf(millisecond)) / 1000;
    StringBuilder sb = new StringBuilder();
    sb.append("==========================================="+System.lineSeparator());
    sb.append("开始时间:"+startTime+System.lineSeparator());
    sb.append("请求ID:"+uuid+System.lineSeparator());
    sb.append("请求URL:"+url+System.lineSeparator());
    sb.append("请求参数:"+targetMethodParams+System.lineSeparator());
//    sb.append("调用controller方法::"+proceedingJoinPoint+System.lineSeparator());
    sb.append("执行耗时:"+millisecond+"毫秒"+System.lineSeparator());
    sb.append("执行耗时:"+second+"秒"+System.lineSeparator());
    sb.append("响应数据:"+JackJsonUtils.formatJsonByFilter(obj)+System.lineSeparator());
    if(ValidateUtils.isNotEmpty(ex)){
      String message = ExceptionUtil.getMessage(ex.getClass().getSimpleName(),ex,ValidateUtils.isNotEmpty(request.getLocale()) ? request.getLocale() : DEFAULT_LOCALE);
      sb.append("异常为:"+message+System.lineSeparator());
    }
    sb.append("结束时间:"+DateTimeUtils.getLocalDateTimeString(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))+System.lineSeparator());
    sb.append("===========================================");
    logger.info(System.lineSeparator()+sb.toString());

  }
}
