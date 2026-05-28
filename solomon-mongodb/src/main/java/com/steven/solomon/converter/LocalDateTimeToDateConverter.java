package com.steven.solomon.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

/**
 * LocalDateTime 转 Date 转换器。
 *
 * <p>用于MongoDB读写时的类型自动转换，将 {@link LocalDateTime} 转为 {@link Date}。</p>
 */
public class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {

  /**
   * 将LocalDateTime转换为Date，使用系统默认时区。
   *
   * @param dateTime LocalDateTime对象
   * @return 对应的Date
   */
  @Override
  public Date convert(LocalDateTime dateTime) {
    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
