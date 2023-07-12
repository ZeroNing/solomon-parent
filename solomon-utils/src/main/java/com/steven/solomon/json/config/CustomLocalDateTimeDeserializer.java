package com.steven.solomon.json.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.steven.solomon.header.RequestHeaderHolder;
import com.steven.solomon.utils.date.DateTimeUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

  private DateTimeFormatter formatter;

  public CustomLocalDateTimeDeserializer(DateTimeFormatter formatter) {
    super();
    this.formatter = formatter;
  }

  @Override
  public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    return DateTimeUtils.convertLocalDateTime(LocalDateTime.parse(parser.getText(), formatter), ZoneId.systemDefault(),
        ZoneId.of(RequestHeaderHolder.getServerTimeZone()));
  }

}
