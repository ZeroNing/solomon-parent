package com.steven.solomon.spring;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.verification.ValidateUtils;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;

/**
 * Spring 容器工具类。
 *
 * <p>该工具只用于少量无法直接注入 Bean 的静态入口，例如统一异常、枚举工具和消息初始化流程。
 * 业务代码优先使用构造器注入，避免隐藏依赖。</p>
 */
@AutoConfiguration
@Order(1)
public class SpringUtil implements ApplicationContextAware {

  private static volatile ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    setContext(applicationContext);
  }

  /**
   * 设置 Spring 上下文。
   *
   * <p>只允许首次赋值，避免运行期被其他上下文覆盖后出现不可预期的 Bean 解析结果。</p>
   */
  public static void setContext(ApplicationContext applicationContext) {
    if (ObjectUtil.isEmpty(context)) {
      context = applicationContext;
    }
  }

  public static ApplicationContext getApplicationContext() {
    return requireContext();
  }

  public static <T> T getBean(Class<T> beanClass) {
    return requireContext().getBean(beanClass);
  }

  public static <T> T getBean(String name, Class<T> beanClass) {
    return requireContext().getBean(name, beanClass);
  }

  /**
   * 根据注解类型查找 Bean。
   */
  public static Map<String, Object> getBeansWithAnnotation(
      Class<? extends Annotation> annotationType) {
    return requireContext().getBeansWithAnnotation(annotationType);
  }

  /**
   * 根据注解类型查找 Bean，并按容器返回顺序转为列表。
   *
   * <p>多个消息组件只关心 Bean 实例本身，统一在这里转换可以减少重复的
   * {@code new ArrayList<>(map.values())} 写法。</p>
   */
  public static List<Object> getBeanListWithAnnotation(
      Class<? extends Annotation> annotationType) {
    return new ArrayList<>(getBeansWithAnnotation(annotationType).values());
  }

  public static <T> Map<String, T> getBeansOfType(Class<T> type) {
    return requireContext().getBeansOfType(type);
  }

  /**
   * 获取指定类型的第一个 Bean；不存在时返回默认值。
   */
  public static <T> T getBeansOfType(Class<T> type, T defaultVal) {
    List<T> list = new ArrayList<>(getBeansOfType(type).values());
    return ObjectUtil.isEmpty(list) ? defaultVal : list.get(0);
  }

  /**
   * 按 {@link ResolvableType} 查找 Bean，适用于带泛型的接口。
   */
  @SuppressWarnings("unchecked")
  public static <T> T getBeansOfType(ResolvableType type, T defaultVal) {
    ConfigurableApplicationContext configurableContext =
        (ConfigurableApplicationContext) requireContext();
    DefaultListableBeanFactory beanFactory =
        (DefaultListableBeanFactory) configurableContext.getBeanFactory();

    String[] beanNames = beanFactory.getBeanNamesForType(type);
    Map<String, T> beans = new LinkedHashMap<>();
    for (String beanName : beanNames) {
      beans.put(beanName, (T) beanFactory.getBean(beanName));
    }
    List<T> list = new ArrayList<>(beans.values());
    return ObjectUtil.isEmpty(list) ? defaultVal : list.get(0);
  }

  /**
   * 解析 Spring 占位符表达式，例如 {@code ${server.port:8080}}。
   */
  public static String getElValue(String elKey, String defaultValue) {
    return ObjectUtil.defaultIfNull(getElValue(elKey), defaultValue);
  }

  /**
   * 解析 Spring 占位符表达式；非表达式会原样返回。
   */
  public static String getElValue(String elKey) {
    if (ObjectUtil.isNotEmpty(elKey) && ValidateUtils.isELExpression(elKey)) {
      return requireContext().getEnvironment().resolveRequiredPlaceholders(elKey);
    }
    return elKey;
  }

  /**
   * 获取服务 Bean 的泛型引用映射。
   */
  public static <T> Map<String, ParameterizedTypeReference<?>> getAllServicesWithGenerics(
      Class<T> clazz) {
    Map<String, ParameterizedTypeReference<?>> result = new LinkedHashMap<>();
    Map<String, T> beans = getBeansOfType(clazz);
    for (String beanName : beans.keySet()) {
      result.put(beanName, new ParameterizedTypeReference<T>() {});
    }
    return result;
  }

  private static ApplicationContext requireContext() {
    if (ObjectUtil.isEmpty(context)) {
      throw new IllegalStateException("Spring ApplicationContext 尚未初始化");
    }
    return context;
  }
}
