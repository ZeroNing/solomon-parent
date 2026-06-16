package com.steven.solomon.utils.logger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类。
 */
public final class LoggerUtils {

  private LoggerUtils() {}

  /**
   * 根据类创建日志记录器。
   *
   * @param clazz 日志所属类
   * @return slf4j 日志记录器
   */
  public static Logger logger(Class<?> clazz) {
    return LoggerFactory.getLogger(clazz);
  }

}
