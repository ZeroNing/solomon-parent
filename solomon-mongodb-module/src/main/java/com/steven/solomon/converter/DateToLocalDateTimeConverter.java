package com.steven.solomon.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

/**
 * Date 转 LocalDateTime 转换器。
 *
 * <p>用于MongoDB读写时的类型自动转换，将 {@link Date} 转为 {@link LocalDateTime}。</p>
 */
public class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {

  /**
   * 将Date转换为LocalDateTime，使用系统默认时区。
   *
   * @param date Date对象
   * @return 对应的LocalDateTime
   */
  @Override
  public LocalDateTime convert(Date date) {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }
}
