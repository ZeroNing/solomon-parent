package com.steven.solomon.clazz;

import cn.hutool.core.util.ObjectUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Class 反射工具类。
 */
public final class ClassUtils {

    private ClassUtils() {}

    /**
     * 更新指定字段上的注解属性值。
     *
     * <p>该方法会直接修改 JDK 代理对象中的 memberValues，只建议在启动期框架适配场景使用，
     * 不建议在普通业务流程中动态修改注解。</p>
     *
     * @param clazz                     目标类
     * @param fieldName                 字段名
     * @param annotationClazz           注解类型
     * @param annotationNameAndValueMap 注解属性名和新值
     */
    public static <T extends Annotation> void updateClassField(Class<?> clazz, String fieldName, Class<T> annotationClazz, Map<String, Object> annotationNameAndValueMap) throws Exception {
        updateClassField(clazz.getDeclaredField(fieldName), annotationClazz, annotationNameAndValueMap);
    }

    /**
     * 更新字段上的注解属性值。
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> void updateClassField(Field field, Class<T> annotationClazz, Map<String, Object> annotationNameAndValueMap) throws Exception {
        field.setAccessible(true);
        T excelProperty = field.getAnnotation(annotationClazz);
        if (ObjectUtil.isEmpty(excelProperty)) {
            return;
        }
        InvocationHandler invocatiOnHandler = Proxy.getInvocationHandler(excelProperty);
        Field memberValues = invocatiOnHandler.getClass().getDeclaredField("memberValues");
        // 通过反射获取 memberValues，该 Map 存放注解的全部属性值。
        memberValues.setAccessible(true);

        Map<String, Object> values = (Map<String, Object>) memberValues.get(invocatiOnHandler);
        values.putAll(annotationNameAndValueMap);
    }
}
