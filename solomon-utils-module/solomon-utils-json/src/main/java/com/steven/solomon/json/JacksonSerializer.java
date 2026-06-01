package com.steven.solomon.json;

import cn.hutool.core.util.ObjectUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.steven.solomon.annotation.EnumSerialize;
import com.steven.solomon.enums.EnumUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;

/**
 * 枚举字段扩展序列化器。
 *
 * <p>被 {@link EnumSerialize} 标记的 String 字段会先正常输出原值，再额外输出一个枚举展示字段，
 * 例如 {@code status=1} 同时输出 {@code statusName=启用}。</p>
 */
public class JacksonSerializer extends JsonSerializer<String> implements ContextualSerializer {

  private final Logger logger = LoggerUtils.logger(getClass());

  private Class<? extends Enum<?>> enumClass;

  private String prefix;

  private String methodName;

  private String fieldName;

  public JacksonSerializer() {
    super();
  }

  public JacksonSerializer(
      Class<? extends Enum<?>> enumClass, String prefix, String methodName, String fieldName) {
    this.enumClass = enumClass;
    this.prefix = prefix;
    this.methodName = methodName;
    this.fieldName = fieldName;
  }

  @Override
  public void serialize(String value, JsonGenerator generator, SerializerProvider provider)
      throws IOException {
    generator.writeString(value);
    writeEnumLabel(value, generator);
  }

  /**
   * 输出枚举展示字段。
   *
   * <p>转换失败只记录日志，不阻断主字段序列化，避免因为展示值缺失影响接口主体返回。</p>
   */
  private void writeEnumLabel(String value, JsonGenerator generator) throws IOException {
    try {
      Enum<?> enumValue = EnumUtils.codeOf(enumClass, value);
      if (ObjectUtil.isEmpty(enumValue)) {
        logger.error("EnumSerializer 转换枚举为空,值:{},类名:{}", value, enumClass.getName());
        return;
      }
      String label = (String) enumClass.getMethod(methodName).invoke(enumValue);
      generator.writeStringField(resolveOutputFieldName(), label);
    } catch (Throwable e) {
      logger.error(
          "EnumSerializer 转换失败,值:{},枚举类为:{},调用方法名为:{}报错异常为 e:",
          value,
          enumClass.getName(),
          methodName,
          e);
    }
  }

  /**
   * 优先使用注解指定字段名；未指定时沿用历史规则：字段名 + 展示方法名。
   */
  private String resolveOutputFieldName() {
    return ObjectUtil.isEmpty(fieldName) ? prefix + methodName : fieldName;
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
      throws JsonMappingException {
    if (ObjectUtil.isEmpty(property)) {
      return provider.findNullValueSerializer(property);
    }
    if (!Objects.equals(property.getType().getRawClass(), String.class)) {
      return provider.findValueSerializer(property.getType(), property);
    }

    EnumSerialize enumSerialize = property.getAnnotation(EnumSerialize.class);
    if (ObjectUtil.isEmpty(enumSerialize) || !enumSerialize.ignore()) {
      return provider.findValueSerializer(property.getType(), property);
    }
    return new JacksonSerializer(
        enumSerialize.enumClass(),
        property.getName(),
        enumSerialize.methodName(),
        enumSerialize.fieldName());
  }
}
