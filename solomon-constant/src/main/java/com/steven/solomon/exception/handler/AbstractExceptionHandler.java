package com.steven.solomon.exception.handler;

import com.steven.solomon.pojo.vo.BaseExceptionVO;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一异常处理器基类。
 *
 * <p>所有异常处理器都放入 {@link #exceptionHandlerMap}，key 为 Spring Bean 名称。
 * 业务侧可以声明同名 Bean 覆盖默认实现，因此默认处理器要使用稳定的 Bean 名称。</p>
 */
public abstract class AbstractExceptionHandler {

  /**
   * 异常处理器 Bean 名称后缀。
   */
  public static final String HANDLER_NAME = "Processor";

  /**
   * 异常处理器缓存。
   *
   * <p>使用 volatile 保证刷新缓存后其他线程立即可见；读取时不加锁，保持异常处理链路轻量。</p>
   */
  public static volatile Map<String, AbstractExceptionHandler> exceptionHandlerMap =
      new ConcurrentHashMap<>();

  /**
   * 刷新处理器缓存。
   *
   * @param handlers Spring 容器中所有异常处理器
   */
  public static void refreshExceptionHandlers(Map<String, AbstractExceptionHandler> handlers) {
    if (handlers == null || handlers.isEmpty()) {
      exceptionHandlerMap = Collections.emptyMap();
      return;
    }
    exceptionHandlerMap = new ConcurrentHashMap<>(handlers);
  }

  /**
   * 将具体异常转换为统一响应对象。
   *
   * @param ex 原始异常
   * @return 统一异常响应对象
   */
  public abstract BaseExceptionVO handleBaseException(Throwable ex);
}
