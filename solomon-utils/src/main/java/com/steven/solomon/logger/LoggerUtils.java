package com.steven.solomon.logger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
  /**
   * 默认日志记录类
   *
   * @param clazz
   * @return
   */
  public static final Logger logger(Class<?> clazz) {
    return LoggerFactory.getLogger(clazz);
  }

}
