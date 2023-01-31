package com.steven.solomon.verification;

import cn.hutool.core.map.MapUtil;
import com.steven.solomon.exception.BaseException;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class EmptyUtils {

  /**
   * 判断是否为空
   *
   * @param obj
   * @return
   */
  public static final Boolean isEmpty(Object obj) {
    Boolean flag = false;

    if(obj instanceof Map && MapUtil.isEmpty((Map) obj)){
      return true;
    }

    if (obj instanceof String && StringUtils.isBlank((String) obj)) {
      return true;
    }

    if (obj instanceof Collection<?> && CollectionUtils.isEmpty((Collection<?>) obj)) {
      return true;
    }

    if (obj instanceof File && !((File) obj).exists()) {
      return true;
    }

    if (obj instanceof Number && obj == null) {
      return true;
    }

    if (obj instanceof Boolean && obj == null) {
      return true;
    }

    if(obj == null){
      return true;
    }
    return false;
  }

  /**
   * 判断是否为空
   *
   * @param obj       对象
   * @param errorCode 报错异常
   */
  public static <T> T isEmpty(T obj, String errorCode) throws BaseException {
    return isEmpty(obj, errorCode, (String[]) null);
  }

  /**
   * 判断是否为空
   *
   * @param obj       对象
   * @param errorCode 报错异常
   * @param args      可替换的信息
   */
  public static <T> T isEmpty(T obj, String errorCode, String... args) throws BaseException {
    if (isEmpty(obj)) {
      throw new BaseException(errorCode, args);
    }
    return obj;
  }

  /**
   * 判断对象是否非空
   *
   * @param obj 对象
   * @return {@code true}: 非空<br>
   * {@code false}: 空
   */
  public static boolean isNotEmpty(Object obj) {
    return !isEmpty(obj);
  }

  /**
   * 判断对象是否非空
   *
   * @param obj       对象
   * @param errorCode 报错异常
   */
  public static <T> T isNotEmpty(T obj, String errorCode) throws BaseException {
    return isNotEmpty(obj, errorCode, (String[]) null);
  }

  /**
   * 判断对象是否非空
   *
   * @param obj  对象
   * @param code 报错异常
   * @param args 可替换的信息
   */
  public static <T> T isNotEmpty(T obj, String code, String... args) throws BaseException {
    if (!isEmpty(obj)) {
      throw new BaseException(code, args);
    }
    return obj;
  }
}
