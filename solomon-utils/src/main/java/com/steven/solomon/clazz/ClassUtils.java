package com.steven.solomon.clazz;

import cn.hutool.core.util.ObjectUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

public class ClassUtils {

  /**
   * 更新Class内注解的值
   * @param clazz class
   * @param fieldName 字段名
   * @param annotationClazz 注解类Class
   * @param annotationNameAndValueMap 注解内值参数名以及对应修改的值
   * @param <T>
   * @throws Exception
   */
  public static <T extends Annotation> void updateClassField(Class clazz,String fieldName,Class<T> annotationClazz, Map<String,Object> annotationNameAndValueMap) throws Exception {
    updateClassField(clazz.getDeclaredField(fieldName),annotationClazz,annotationNameAndValueMap);
  }

  public static <T extends Annotation> void updateClassField(Field field,Class<T> annotationClazz, Map<String,Object> annotationNameAndValueMap) throws Exception {
    field.setAccessible(true);
    T excelProperty =  field.getAnnotation(annotationClazz);
    if(ObjectUtil.isEmpty(excelProperty)){
      return;
    }
    InvocationHandler invocatiOnHandler = Proxy.getInvocationHandler(excelProperty);
    Field             memberValues      = invocatiOnHandler.getClass().getDeclaredField("memberValues");
    //通过反射获取memberValues 这个属性是Map类型 存放着所有的属性。
    memberValues.setAccessible(true);

    Map<String, Object> values = (Map<String, Object>) memberValues.get(invocatiOnHandler);
    for(Map.Entry<String, Object> entry: annotationNameAndValueMap.entrySet()){
      values.put(entry.getKey(),entry.getValue());
    }
  }
}
