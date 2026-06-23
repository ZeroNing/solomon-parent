package com.steven.solomon.exception;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.code.BaseExceptionCode;
import com.steven.solomon.context.RequestContextSnapshot;
import com.steven.solomon.exception.handler.AbstractExceptionHandler;
import com.steven.solomon.pojo.vo.BaseExceptionVO;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.Locale;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

/**
 * 统一异常处理工具。
 *
 * <p>异常处理器采用约定式命名：异常简单类名 + {@code Processor}。
 * 例如 {@code BaseException} 对应 Bean 名称 {@code BaseExceptionProcessor}。
 * 这样业务模块只要声明同名处理器，就可以覆盖默认异常翻译逻辑。</p>
 */
public class ExceptionUtil {

  private static final Logger logger = LoggerUtils.logger(ExceptionUtil.class);

  /**
   * 请求 ID 的线程上下文。
   *
   * <p>这里保留原有 public 字段以兼容旧代码；Web 请求结束时必须清理，避免线程池复用时串数据。</p>
   */
  public static final ThreadLocal<String> requestId = new ThreadLocal<>();

  private ExceptionUtil() {
  }

  public static Runnable wrapContext(Runnable task) {
    return RequestContextSnapshot.capture().wrap(task);
  }

  /**
   * 根据异常简单类名查找对应处理器。
   *
   * @param exceptionSimpleName 异常简单类名，例如 BaseException
   * @return 匹配的异常处理器，不存在时返回 null
   */
  public static AbstractExceptionHandler getExceptionHandler(String exceptionSimpleName) {
    if (ObjectUtil.isEmpty(AbstractExceptionHandler.exceptionHandlerMap)) {
      AbstractExceptionHandler.refreshExceptionHandlers(SpringUtil.getBeansOfType(
          AbstractExceptionHandler.class));
    }
    return AbstractExceptionHandler.exceptionHandlerMap.get(
        exceptionSimpleName + AbstractExceptionHandler.HANDLER_NAME);
  }

  /**
   * 把任意异常翻译为统一异常响应对象。
   *
   * <p>未知异常会被翻译为系统默认错误码，避免原始异常结构泄露给前端。</p>
   */
  public static BaseExceptionVO getBaseExceptionVO(String exceptionSimpleName, Throwable ex) {
    BaseExceptionVO baseExceptionVO = new BaseExceptionVO(
        BaseExceptionCode.BASE_EXCEPTION_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());

    AbstractExceptionHandler exceptionHandler = getExceptionHandler(exceptionSimpleName);
    if (ObjectUtil.isEmpty(exceptionHandler)) {
      logger.error("未找到异常处理器，使用默认系统异常响应: exception={}", exceptionSimpleName, ex);
      return baseExceptionVO;
    }
    return exceptionHandler.handleBaseException(ex);
  }

  /**
   * 获取可直接展示的异常消息。
   */
  public static String getMessage(String exceptionSimpleName, Throwable ex, Locale locale) {
    BaseExceptionVO baseExceptionVO = getBaseExceptionVO(exceptionSimpleName, ex);
    if (ObjectUtil.isNotEmpty(baseExceptionVO.getMessage())) {
      return baseExceptionVO.getMessage();
    }
    return I18nUtils.getErrorMessage(baseExceptionVO.getCode(), locale, baseExceptionVO.getArg());
  }
}
