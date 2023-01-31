package com.steven.solomon.enums;


import com.steven.solomon.verification.ValidateUtils;
import java.util.Collection;

public class EnumUtils {

  /**
   * 判断枚举是否存在
   * @param enumClass 枚举类
   * @param object 枚举值
   * @return true 有枚举值 false 没有枚举值
   */
  public static boolean exist(Class<? extends Enum> enumClass, Object object) {
    if(ValidateUtils.isEmpty(object)){
      return false;
    }
    if (object instanceof Collection) {
      Collection<Object> collections = (Collection<Object>) object;
      for (Object o : collections) {
        Object enums = codeOf(enumClass, o);
        if (ValidateUtils.isEmpty(enums)) {
          return false;
        }
      }
      return true;
    } else {
      return ValidateUtils.isEmpty(codeOf(enumClass, object)) ? false : true;
    }
  }

  /**
   * 获取枚举类
   * @param enumClass 枚举对象类
   * @param value 枚举值
   * @return 返回枚举类
   */
  public static <E extends Enum<?> & BaseEnum> E codeOf(Class<? extends Enum> enumClass, Object value) {
    if(ValidateUtils.isEmpty(value)){
      return null;
    }
    E[] enumConstants = (E[]) enumClass.getEnumConstants();
    for (E e : enumConstants) {
      if (e.label().equals(String.valueOf(value))) {
        return e;
      }
    }
    return null;
  }
}
