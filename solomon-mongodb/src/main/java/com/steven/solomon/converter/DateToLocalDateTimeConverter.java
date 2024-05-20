package com.steven.solomon.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {

  @Override
  public LocalDateTime convert(Date date) {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  }
}
