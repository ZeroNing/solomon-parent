package com.steven.solomon.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.steven.solomon.annotation.JsonEnum;
import com.steven.solomon.enums.EnumUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;

public class EnumSerializer extends JsonSerializer<String> implements ContextualSerializer {

  private Logger logger = LoggerUtils.logger(getClass());

  private Class<? extends Enum> enumClass;

  private String prefix;

  private String[] methodNames;

  private String fieldName;

  public EnumSerializer() {
    super();
  }

  public EnumSerializer(Class<? extends Enum> enumClass,String prefix,String[] methodNames,String fieldName) {
    this.enumClass = enumClass;
    this.prefix = prefix;
    this.methodNames = methodNames;
    this.fieldName = fieldName;
  }

  @Override
  public void serialize(String o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {

    jsonGenerator.writeString(o);
    try {
      Enum   enums  = EnumUtils.codeOf(enumClass,o);
      if(ValidateUtils.isEmpty(enums)){
        logger.info("EnumSerializer 转换枚举为空,值:{},类名:{}",o,enumClass.getName());
        return;
      }
      for (String methodName : methodNames) {
        String value    = (String) enumClass.getMethod(methodName).invoke(enums);
        if(ValidateUtils.isEmpty(fieldName)){
          jsonGenerator.writeStringField(new StringBuilder(prefix).append(methodName).toString(), value);
        } else {
          jsonGenerator.writeStringField(fieldName,value);
        }
      }
    } catch (Exception e) {
      logger.info("EnumSerializer 转换失败,值:{},枚举类为:{},调用方法名为:{}报错异常为 e:{}",o,enumClass.getName(),methodNames.toString(),e);
      return;
    }
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
      throws JsonMappingException {
    if (ValidateUtils.isEmpty(beanProperty)) {
      return serializerProvider.findNullValueSerializer(beanProperty);
    }

    if (!Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
      return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
    }

    JsonEnum enumLabel = beanProperty.getAnnotation(JsonEnum.class);
    if (ValidateUtils.isEmpty(enumLabel) || enumLabel.ignore()) {
      return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
    }
    return new EnumSerializer(enumLabel.enumClass(),beanProperty.getName(),enumLabel.methodNames(),enumLabel.fieldName());
  }
}