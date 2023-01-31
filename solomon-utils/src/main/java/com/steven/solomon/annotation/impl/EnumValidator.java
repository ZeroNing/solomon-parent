package com.steven.solomon.annotation.impl;

import com.steven.solomon.annotation.CheckEnum;
import com.steven.solomon.enums.EnumUtils;
import com.steven.solomon.verification.ValidateUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<CheckEnum, Object> {

  /**
   * 枚举class
   */
  private Class<? extends Enum<?>> enumClass;
  /**
   * 是否必传
   */
  private boolean required;

  @Override
  public void initialize(CheckEnum constraintAnnotation) {
    enumClass = constraintAnnotation.enumClass();
    required  = constraintAnnotation.required();
  }

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (!required && ValidateUtils.isEmpty(value)) {
      return true;
    }
    if (ValidateUtils.isEmpty(value) || ValidateUtils.isEmpty(enumClass)) {
      return false;
    }
    return EnumUtils.exist(enumClass, value);
  }
}
