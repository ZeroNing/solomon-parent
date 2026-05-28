package com.steven.solomon.aspect.controller;

import cn.hutool.core.date.StopWatch;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.utils.date.DateTimeUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Controller 请求日志切面。
 *
 * <p>切面只记录 Spring MVC 映射方法的入参、响应、耗时和异常摘要。
 * 日志序列化采用容错方式处理，避免因为参数中包含流、文件等不可序列化对象而影响真实业务请求。</p>
 */
@Aspect
@AutoConfiguration
@ConditionalOnProperty(prefix = "solomon.web.log", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class ControllerAspect {

  /** 日志记录器。 */
  private static final Logger logger = LoggerUtils.logger(ControllerAspect.class);
  /** 日志时间格式化器。 */
  private static final DateTimeFormatter LOG_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

  /** 默认语言区域，当请求未指定 Accept-Language 时使用。 */
  @Value("${i18n.language:zh}")
  private Locale defaultLocale;

  /**
   * 切点定义：匹配所有 Spring MVC 映射注解标注的方法。
   */
  @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || "
      + "@annotation(org.springframework.web.bind.annotation.GetMapping) || "
      + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
      + "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || "
      + "@annotation(org.springframework.web.bind.annotation.RequestMapping) || "
      + "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
  private void pointCutMethodService() {
  }

  /**
   * 包裹 Controller 方法执行过程，用 finally 保证成功和异常场景都能记录日志。
   *
   * @param pjp AOP 连接点
   * @return Controller 方法的原始返回值
   * @throws Throwable Controller 方法抛出的原始异常
   */
  @Around("pointCutMethodService()")
  public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Object result = null;
    Throwable error = null;
    String startTime = DateTimeUtils.getLocalDateTimeString(LOG_TIME_FORMATTER);
    try {
      result = pjp.proceed();
      return result;
    } catch (Exception ex) {
      error = ex;
      throw ex;
    } finally {
      saveLog(pjp, stopWatch, error, ExceptionUtil.requestId.get(), result, startTime);
    }
  }

  /**
   * 组装并输出请求日志。
   *
   * <p>异步线程或非 Web 调用可能没有 ServletRequestAttributes，此时直接跳过日志。</p>
   *
   * @param pjp        AOP 连接点
   * @param stopWatch  计时器
   * @param error      捕获的异常（无异常时为 null）
   * @param requestId  请求ID
   * @param result     Controller 返回值
   * @param startTime  请求开始时间字符串
   */
  protected void saveLog(ProceedingJoinPoint pjp, StopWatch stopWatch, Throwable error,
      String requestId, Object result, String startTime) {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (ValidateUtils.isEmpty(attributes)) {
      stopWatch.stop();
      return;
    }

    HttpServletRequest request = attributes.getRequest();
    String url = request.getRequestURL().toString();
    String params = safeToJson(pjp.getArgs());

    stopWatch.stop();
    long millisecond = stopWatch.getLastTaskTimeMillis();
    double second = millisecond / 1000D;

    StringBuilder sb = new StringBuilder();
    sb.append("===========================================").append(System.lineSeparator());
    sb.append("requestTime:").append(startTime).append(System.lineSeparator());
    sb.append("requestId:").append(requestId).append(System.lineSeparator());
    sb.append("requestUrl:").append(url).append(System.lineSeparator());
    sb.append("requestParams:").append(params).append(System.lineSeparator());
    sb.append("elapsedMillis:").append(millisecond).append(System.lineSeparator());
    sb.append("elapsedSeconds:").append(second).append(System.lineSeparator());
    sb.append("response:").append(safeToJson(result)).append(System.lineSeparator());
    if (ValidateUtils.isNotEmpty(error)) {
      Locale locale = ValidateUtils.isNotEmpty(request.getLocale()) ? request.getLocale() : defaultLocale;
      String message = ExceptionUtil.getMessage(error.getClass().getSimpleName(), error, locale);
      sb.append("exception:").append(message).append(System.lineSeparator());
    }
    sb.append("endTime:").append(DateTimeUtils.getLocalDateTimeString(LOG_TIME_FORMATTER))
        .append(System.lineSeparator());
    sb.append("===========================================");
    logger.info("{}{}", System.lineSeparator(), sb);
  }

  /**
   * 安全序列化日志对象，容错处理不可序列化的参数。
   *
   * @param value 待序列化的对象
   * @return JSON字符串或 fallback 字符串
   */
  private String safeToJson(Object value) {
    try {
      return JSONUtil.toJsonStr(value);
    } catch (Exception ex) {
      if (value instanceof Object[] values) {
        return Arrays.toString(values);
      }
      return String.valueOf(value);
    }
  }
}
