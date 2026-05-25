package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;
import com.steven.solomon.verification.ValidateUtils;
import java.util.Collection;

/**
 * 枚举工具类。
 *
 * <p>项目内业务枚举统一实现 {@link BaseEnum}，这里集中提供按编码查找和存在性校验，
 * 避免各处重复遍历枚举常量。</p>
 */
public final class EnumUtils {

  private EnumUtils() {}

  /**
   * 判断一个值或一组值是否都存在于指定枚举中。
   *
   * @param enumClass 枚举类
   * @param object 单个枚举编码、枚举名称，或编码集合
   * @return true 表示全部存在；false 表示有任意值不存在
   */
  @SuppressWarnings("unchecked")
  public static boolean exist(Class<? extends Enum<?>> enumClass, Object object) {
    if (ValidateUtils.isEmpty(object)) {
      return false;
    }
    if (object instanceof Collection<?> collection) {
      for (Object value : collection) {
        if (ValidateUtils.isEmpty(codeOf(enumClass, value))) {
          return false;
        }
      }
      return true;
    }
    return ValidateUtils.isNotEmpty(codeOf(enumClass, object));
  }

  /**
   * 根据枚举编码或枚举名称查找枚举实例。
   *
   * <p>先匹配 {@link BaseEnum#label()}，再匹配枚举 {@code name()}，兼容接口入参传编码或枚举名。</p>
   *
   * @param enumClass 枚举类
   * @param value 枚举编码或枚举名称
   * @return 匹配的枚举；未匹配时返回 null
   */
  @SuppressWarnings("unchecked")
  public static <E extends Enum<?> & BaseEnum<?>> E codeOf(
      Class<? extends Enum<?>> enumClass, Object value) {
    if (ValidateUtils.isEmpty(value)) {
      return null;
    }
    String targetValue = String.valueOf(value);
    E[] enumConstants = (E[]) enumClass.getEnumConstants();
    for (E item : enumConstants) {
      if (item.label().equals(targetValue) || item.name().equals(targetValue)) {
        return item;
      }
    }
    return null;
  }
}
