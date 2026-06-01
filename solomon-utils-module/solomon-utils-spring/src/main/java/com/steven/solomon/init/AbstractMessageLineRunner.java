package com.steven.solomon.init;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.TypeUtil;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;

/**
 * 消息消费者初始化模板。
 *
 * <p>子类可以显式传入要扫描的注解类型；未传入时继续按泛型推断，兼容已有模块。</p>
 *
 * @param <T> 消费者标记注解类型
 */
public abstract class AbstractMessageLineRunner<T extends Annotation> implements CommandLineRunner {

  protected final Logger logger = LoggerUtils.logger(getClass());

  private final Class<T> annotationType;

  protected AbstractMessageLineRunner() {
    this.annotationType = null;
  }

  protected AbstractMessageLineRunner(Class<T> annotationType) {
    this.annotationType = annotationType;
  }

  @Override
  public void run(String... args) throws Exception {
    Class<T> clazz = resolveAnnotationType();
    List<Object> clazzList = SpringUtil.getBeanListWithAnnotation(clazz);
    if (ObjectUtil.isEmpty(clazzList)) {
      logger.error("AbstractMessageLineRunner: 没有{}消费者", clazz.getSimpleName());
      return;
    }
    init(clazzList);
  }

  @SuppressWarnings("unchecked")
  private Class<T> resolveAnnotationType() {
    if (annotationType != null) {
      return annotationType;
    }
    Type type = TypeUtil.getTypeArgument(getClass(), 0);
    return ClassUtil.loadClass(type.getTypeName());
  }

  /**
   * 初始化消息消费者。
   *
   * @param clazzList 标注了目标注解的消费者 Bean 列表
   */
  public abstract void init(List<Object> clazzList) throws Exception;
}
