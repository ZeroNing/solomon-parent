package com.steven.solomon.spring;

import java.lang.annotation.Annotation;
import java.util.*;

import com.steven.solomon.verification.ValidateUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;

/**
 * Spring上下文工具类
 */
@Configuration
@Order(1)
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    public SpringUtil(ApplicationContext applicationContext) {
        SpringUtil.context = applicationContext;
    }

    /**
     * Spring在bean初始化后会判断是不是ApplicationContextAware的子类
     * 如果该类是,setApplicationContext()方法,会将容器中ApplicationContext作为参数传入进去
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtil.context == null) {
            SpringUtil.context = applicationContext;
        }
    }

    public static void setContext(ApplicationContext applicationContext) {
        if (SpringUtil.context == null) {
            SpringUtil.context = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * 通过Name返回指定的Bean
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static <T> T getBean(String name, Class<T> beanClass) {
        return context.getBean(name, beanClass);
    }

    /**
     * 根据注解找到使用注解的类
     *
     * @param annotationType 注解class
     */
    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        return context.getBeansWithAnnotation(annotationType);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return context.getBeansOfType(type);
    }

    public static <T> T getBeansOfType(Class<T> type,T defaultVal) {
        List<T> list =  new ArrayList<T>(context.getBeansOfType(type).values());
        return ValidateUtils.isEmpty(list) ? defaultVal : list.get(0);
    }

    public static <T> T getBeansOfType(ResolvableType type,T defaultVal) {
        DefaultListableBeanFactory beanFactory =
                (DefaultListableBeanFactory) ((ConfigurableApplicationContext) context).getBeanFactory();

        String[] beanNames = beanFactory.getBeanNamesForType(type);
        Map<String, T> beans = new LinkedHashMap<>();
        for (String beanName : beanNames) {
            beans.put(beanName, (T) beanFactory.getBean(beanName));
        }
        List<T> list =  new ArrayList<T>(beans.values());
        return ValidateUtils.isEmpty(list) ? defaultVal : list.get(0);
    }


    /**
     * 读取#{}和${}值
     */
    public static String getElValue(String elKey,String defaultValue){
        return ValidateUtils.getOrDefault(getElValue(elKey),defaultValue);
    }

    /**
     * 读取#{}和${}值
     */
    public static String getElValue(String elKey){
        if(ValidateUtils.isNotEmpty(elKey) && ValidateUtils.isELExpression(elKey)){
            return context.getEnvironment().resolveRequiredPlaceholders(elKey);
        }
        return elKey;
    }

    public static <T> Map<String, ParameterizedTypeReference<?>> getAllServicesWithGenerics(Class<T> clazz) {
        Map<String, ParameterizedTypeReference<?>> result = new HashMap<>();
        Map<String, T> beans = context.getBeansOfType(clazz);

        for (Map.Entry<String, T> entry : beans.entrySet()) {
            ParameterizedTypeReference<?> typeRef = new ParameterizedTypeReference<T>() {};
            result.put(entry.getKey(), typeRef);
        }
        return result;
    }
}
