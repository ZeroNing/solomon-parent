package com.steven.solomon.json.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.steven.solomon.header.RequestHeaderHolder;
import com.steven.solomon.utils.date.DateTimeUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

  private DateTimeFormatter formatter;

  public CustomLocalDateTimeSerializer(DateTimeFormatter formatter) {
    super();
    this.formatter = formatter;
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator generator, SerializerProvider provider) throws IOException {
    generator.writeString(DateTimeUtils.convertLocalDateTime(value, ZoneId.systemDefault(), ZoneId.of(RequestHeaderHolder.getTimeZone())).format(formatter));
  }

}
