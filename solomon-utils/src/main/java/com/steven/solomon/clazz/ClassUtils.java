package com.steven.solomon.clazz;

import cn.hutool.core.util.ObjectUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

public class ClassUtils {

  public static <T extends Annotation> void updateClassField(Class clazz,String fieldName,String value,String annotationName,Class<T> annotationClazz) throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
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
    values.put(annotationName,new String[]{value});
  }
}
