package com.steven.solomon.verification;

import cn.hutool.core.util.ObjectUtil;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

public class ValidateUtils {

  private static final Pattern IS_NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

  private static Logger logger = LoggerUtils.logger(ValidateUtils.class);

  public String valueOf(Number target,String def){
    if(isEmpty(target)){
      return def;
    }
    return String.valueOf(target);
  }

  public Long valueOf(String target,Long def){
    if(isEmpty(target)){
      return def;
    }
    return Long.valueOf(target);
  }

  public Integer valueOf(String target,Integer def){
    if(isEmpty(target)){
      return def;
    }
    return Integer.valueOf(target);
  }

  public Double valueOf(String target,Double def){
    if(isEmpty(target)){
      return def;
    }
    return Double.valueOf(target);
  }

  public Short valueOf(String target,Short def){
    if(isEmpty(target)){
      return def;
    }
    return Short.valueOf(target);
  }

  public Float valueOf(String target,Float def){
    if(isEmpty(target)){
      return def;
    }
    return Float.valueOf(target);
  }

  /**
   * 获取默认值
   * @param obj 参数
   * @param def 默认值
   * @param <T>
   * @return
   */
  public static <T> T getOrDefault(T obj,T def){
    return isEmpty(obj) ? def : obj;
  }

  public static boolean equals(String contrast, String var) {
    return ObjectUtil.equals(contrast,var);
  }

  /**
   * 判断值相等报错
   */
  public static void equals(String contrast, String var, String errorCode) throws BaseException {
    check(equals(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值相等报错
   */
  public static void equals(String contrast, String var, String errorCode, Object... args) throws BaseException {
    check(equals(contrast, var),new BaseException(errorCode, args));
  }

  /**
   * 判断值不相等报错
   */
  public static boolean notEquals(String contrast, String var) {
    return !equals(contrast, var);
  }

  /**
   * 判断值不相等报错
   */
  public static void notEquals(String contrast, String var, String errorCode) throws BaseException {
    check(notEquals(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值不相等报错
   */
  public static void notEquals(String contrast, String var, String errorCode, Object... args) throws BaseException {
    check(notEquals(contrast, var),new BaseException(errorCode, args));
  }

  public static boolean equalsIgnoreCase(String contrast, String var) {
    boolean flag = false;
    if(isEmpty(contrast) || isEmpty(var)){
      return false;
    }
    if (contrast.equalsIgnoreCase(var)) {
      flag = true;
    }
    return flag;
  }

  /**
   * 判断值相等报错（忽略大小写）
   */
  public static void equalsIgnoreCase(String contrast, String var, String errorCode) throws BaseException {
    check(equalsIgnoreCase(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值相等报错（忽略大小写）
   */
  public static void equalsIgnoreCase(String contrast, String var, String errorCode, Object... args)
      throws BaseException {
    check(equalsIgnoreCase(contrast, var),new BaseException(errorCode, args));
  }

  public static boolean notEqualsIgnoreCase(String contrast, String var) {
    return !equalsIgnoreCase(contrast, var);
  }

  /**
   * 判断值不相等报错（忽略大小写）
   */
  public static void notEqualsIgnoreCase(String contrast, String var, String errorCode) throws BaseException {
    check(notEqualsIgnoreCase(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值不相等报错（忽略大小写）
   */
  public static void notEqualsIgnoreCase(String contrast, String var, String errorCode, Object... args) throws BaseException {
    check(notEqualsIgnoreCase(contrast, var),new BaseException(errorCode, args));
  }

  public static boolean equals(Number contrast, Number var) {
    return ObjectUtil.equals(contrast,var);
  }

  /**
   * 判断值相等报错
   */
  public static void equals(Number contrast, Number var, String errorCode) throws BaseException {
    check(equals(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值相等报错
   */
  public static void equals(Number contrast, Number var, String errorCode, Object... args) throws BaseException {
    check(equals(contrast, var),new BaseException(errorCode, args));
  }

  public static boolean notEquals(Number contrast, Number var) {
    return !equals(contrast, var);
  }

  /**
   * 判断值不相等报错
   */
  public static void notEquals(Number contrast, Number var, String errorCode) throws BaseException {
    check(equals(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值不相等报错
   */
  public static void notEquals(Number contrast, Number var, String errorCode, Object... args) throws BaseException {
    if (notEquals(contrast, var)) {
      throw new BaseException(errorCode, args);
    }
  }

  public static boolean equals(Boolean contrast, Boolean var) {
    return ObjectUtil.equals(contrast,var);
  }

  /**
   * 判断值相等报错
   */
  public static void equals(Boolean contrast, Boolean var, String errorCode) throws BaseException {
    check(equals(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值相等报错
   */
  public static void equals(Boolean contrast, Boolean var, String errorCode, Object... args) throws BaseException {
    check(equals(contrast, var),new BaseException(errorCode, args));
  }

  public static boolean notEquals(Boolean contrast, Boolean var) {
    return !equals(contrast, var);
  }

  /**
   * 判断值不相等报错
   */
  public static void notEquals(Boolean contrast, Boolean var, String errorCode) throws BaseException {
    check(notEquals(contrast, var),new BaseException(errorCode));
  }

  /**
   * 判断值不相等报错
   */
  public static void notEquals(Boolean contrast, Boolean var, String errorCode, Object... args) throws BaseException {
    check(notEquals(contrast, var),new BaseException(errorCode, args));
  }

  /**
   * 判断传入的数字类型的值是否等于0或者是否为空，如果等于0或者等于空都会返回一个true
   * @param number
   * @return
   */
  public static boolean isZero(Object number){
    boolean flag = false;
    if(isEmpty(number) || equals(String.valueOf(number),"0") || equals(String.valueOf(number),"0.0")){
      flag = true;
    }
    return flag;
  }

  /**
   * 判断是否为数字
   *
   * @param str
   * @return
   */
  public static boolean isNumber(String str) {
    return regular(IS_NUMBER_PATTERN,str);
  }

  private static void check(boolean flag,BaseException e) throws BaseException {
    if(flag){
      throw e;
    }
  }

  public static boolean regular(Pattern pattern,Object object){
    if (ValidateUtils.isEmpty(object)) {
      return false;
    }
    String bigStr;
    try {
      bigStr = object.toString();
    } catch (Throwable e) {
      logger.error("正则校验报错异常,传入的值为:{},异常为:{}",object,e);
      return false;
    }
    Matcher matcher = pattern.matcher(bigStr);
    return matcher.matches();
  }

  public static String camelName(String name) {
    StringBuilder result = new StringBuilder();
    // 快速检查
    if (name == null || name.isEmpty()) {
      // 没必要转换
      return "";
    } else if (!name.contains("_")) {
      // 不含下划线，仅将首字母小写
      return name.substring(0, 1).toLowerCase() + name.substring(1).toLowerCase();
    }
    // 用下划线将原始字符串分割
    String camels[] = name.split("_");
    for (String camel : camels) {
      // 跳过原始字符串中开头、结尾的下换线或双重下划线
      if (camel.isEmpty()) {
        continue;
      }
      // 处理真正的驼峰片段
      if (result.length() == 0) {
        // 第一个驼峰片段，全部字母都小写
        result.append(camel.toLowerCase());
      } else {
        // 其他的驼峰片段，首字母大写
        result.append(camel.substring(0, 1).toUpperCase());
        result.append(camel.substring(1).toLowerCase());
      }
    }
    return result.toString();
  }

  /**
   * 判断是否为空
   *
   * @param obj
   * @return
   */
  public static final Boolean isEmpty(Object obj) {
    if(ObjectUtil.isEmpty(obj)){
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
  public static <T> T isEmpty(T obj, String errorCode, Object... args) throws BaseException {
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
  public static <T> T isNotEmpty(T obj, String code, Object... args) throws BaseException {
    if (!isEmpty(obj)) {
      throw new BaseException(code, args);
    }
    return obj;
  }

  /**
   * 检查枚举是否不符合
   * @param value 枚举名称
   * @param clazz 枚举class
   * @return
   */
  public static boolean checkEnumValueIsEmpty(String value,Class clazz){
    Enum enums = Enum.valueOf(clazz,value);
    return isEmpty(enums);
  }

  /**
   * 检查枚举是否不符合
   * @param value 枚举名称
   * @param clazz 枚举class
   * @return
   */
  public static void checkEnumValueIsEmpty(String value,Class clazz,String errorCode,String[] args) throws BaseException {
    if(checkEnumValueIsEmpty(value,clazz)){
      throw new BaseException(errorCode, args);
    }
  }

  public static void checkEnumValueIsEmpty(String value,Class clazz,String errorCode) throws BaseException {
    if(checkEnumValueIsEmpty(value,clazz)){
      throw new BaseException(errorCode, (String) null);
    }
  }

  /**
   * 检查枚举是否符合
   * @param value 枚举名称
   * @param clazz 枚举class
   * @return
   */
  public static boolean checkEnumValueIsNotEmpty(String value,Class clazz){
    return !checkEnumValueIsEmpty(value,clazz);
  }

  public static void checkEnumValueIsNotEmpty(String value,Class clazz,String errorCode,String[] args) throws BaseException {
    if(checkEnumValueIsNotEmpty(value,clazz)){
      throw new BaseException(errorCode, args);
    }
  }

  public static void checkEnumValueIsNotEmpty(String value,Class clazz,String errorCode) throws BaseException {
    if(checkEnumValueIsNotEmpty(value,clazz)){
      throw new BaseException(errorCode, (String) null);
    }
  }

}
