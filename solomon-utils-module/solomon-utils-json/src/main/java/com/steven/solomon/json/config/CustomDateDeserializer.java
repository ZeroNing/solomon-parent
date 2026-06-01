package com.steven.solomon.json.config;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Date;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

  @Override
  public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JacksonException {
      return DateUtil.parse(jsonParser.getText(),"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss:SSS");
  }

}
