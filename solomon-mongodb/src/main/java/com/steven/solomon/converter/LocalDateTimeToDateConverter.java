package com.steven.solomon.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {

  @Override
  public Date convert(LocalDateTime dateTime) {
    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
