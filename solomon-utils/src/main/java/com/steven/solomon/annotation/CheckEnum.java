package com.steven.solomon.annotation;

import com.steven.solomon.annotation.impl.EnumValidator;
import com.steven.solomon.constant.code.BaseExceptionCode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 校验枚举参数是否为空
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface CheckEnum {

  /**
   * 异常报错编码
   */
  String message() default BaseExceptionCode.PARAMETER_EXCEPTION_CODE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /**
   * 枚举类
   */
  Class<? extends Enum<?>> enumClass();

  /**
   * 是否必传
   */
  boolean required() default true;
}
