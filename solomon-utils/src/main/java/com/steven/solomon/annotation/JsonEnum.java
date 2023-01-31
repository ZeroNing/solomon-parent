package com.steven.solomon.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.steven.solomon.json.EnumSerializer;
import com.steven.solomon.constant.code.BaseMethodNameEnum;
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

  String[] methodNames() default {BaseMethodNameEnum.DESCRIPTION};

  boolean ignore() default false;
}
