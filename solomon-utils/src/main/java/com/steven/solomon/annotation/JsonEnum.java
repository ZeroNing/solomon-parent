package com.steven.solomon.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.steven.solomon.json.EnumSerializer;
import com.steven.solomon.code.BaseMethodNameEnum;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 枚举序列化返回参数
 * @author huangweihua
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = EnumSerializer.class)
public @interface JsonEnum {

  Class<? extends Enum> enumClass();

  /**
   * 调用enumClass的Get方法，然后在返回字段后面添加后缀
   */
  String methodName() default BaseMethodNameEnum.DESCRIPTION;

  /**
   * 如果自定义字段返回,则可以增加此字段返回
   */
  String fieldName() default "";

  /**
   * 是否输出这个结果
   * @return
   */
  boolean ignore() default true;
}
