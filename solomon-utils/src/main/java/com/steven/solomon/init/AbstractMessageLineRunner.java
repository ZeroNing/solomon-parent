package com.steven.solomon.init;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.TypeUtil;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 消息消费者初始化模板。
 *
 * <p>子类通过泛型声明要扫描的注解类型，本模板统一完成 Bean 查找和空列表校验，
 * 子类只需要关注具体中间件的订阅初始化。</p>
 */
public abstract class AbstractMessageLineRunner<T extends Annotation> implements CommandLineRunner {

    protected final Logger logger = LoggerUtils.logger(getClass());

    @Override
    public void run(String... args) throws Exception {
        // 根据子类声明的注解泛型查找消费者，避免每个消息组件重复扫描 Spring 容器。
        Class<T> clazz = ClassUtil.loadClass(TypeUtil.getTypeArgument(getClass(),0).getTypeName());
        List<Object> clazzList = SpringUtil.getBeanListWithAnnotation(clazz);
        if (ValidateUtils.isEmpty(clazzList)) {
            logger.error("AbstractMessageLineRunner:没有{}消费者",clazz.getSimpleName());
            return;
        }
        this.init(clazzList);
    }

    /**
     * 初始化消息消费者。
     *
     * @param clazzList 标注了目标注解的消费者 Bean 列表
     */
    public abstract void init(List<Object> clazzList) throws Exception;
}
